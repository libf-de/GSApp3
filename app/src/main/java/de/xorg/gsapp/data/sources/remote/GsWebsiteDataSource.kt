/*
 * GSApp3 (https://github.com/libf-de/GSApp3)
 * Copyright (C) 2023 Fabian Schillig
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.xorg.gsapp.data.sources.remote

import android.util.Log
import de.xorg.gsapp.data.exceptions.HolidayException
import de.xorg.gsapp.data.exceptions.NoEntriesException
import de.xorg.gsapp.data.model.Additive
import de.xorg.gsapp.data.model.Food
import de.xorg.gsapp.data.model.FoodOffer
import de.xorg.gsapp.data.model.FoodOfferSet
import de.xorg.gsapp.data.model.Subject
import de.xorg.gsapp.data.model.Substitution
import de.xorg.gsapp.data.model.SubstitutionSet
import de.xorg.gsapp.data.model.Teacher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


class GsWebsiteDataSource : RemoteDataSource {

    private val TAG = "GsWebsiteDataSource"

    @Throws(ArrayIndexOutOfBoundsException::class)
    private fun parseResponse(result: String): Result<SubstitutionSet> {
        //this.isFiltered = !this.Filter.isEmpty();
        val substitutions = ArrayList<Substitution>()

        val doc: Document = Jsoup.parse(result)
        val dateElement: Element = doc.select("td[class*=vpUeberschr]").first()!!
        val noteElement: Element = doc.select("td[class=vpTextLinks]").first()!!
        var noteStr: String
        try {
            noteStr = if (noteElement.hasText())
                noteElement.text().replace("Hinweis:", "").trim()
            else ""
        } catch (e: java.lang.Exception) {
            noteStr = ""
            e.printStackTrace()
        }
        val dateText: String = try {
            if (dateElement.hasText()) dateElement.text()
            else "(kein Datum)"
        } catch (e: java.lang.Exception) {
            "(Kein Datum)"
        }
        if (dateText == "Beschilderung beachten!") return Result.failure(HolidayException())

        var substElements: Elements = doc.select("tr[id=Svertretungen], tr[id=Svertretungen] ~ tr")
        if (substElements.size < 1) {
            Log.w(TAG, "ParseResponse: No entries found, trying fallback method...")

            try {
                val parent: Element = doc.select("td[class*=vpTextZentriert]").first()!!.parent()!!
                substElements = doc.select((parent.cssSelector() + ", " + parent.cssSelector()) + " ~ tr")
            } catch (npe: NullPointerException) {
                Log.e(TAG, "ParseResponse: Fallback failed, html is shown below: ")
                Log.e(TAG, result)
                return fallbackLoad(result)
                //return Result.failure(NullPointerException("Response could not be parsed (NPE): " + npe.message));
            }
        }
        Log.d(TAG, "vpEnts size is " + substElements.size.toString())
        val rowIterator: Iterator<*>
        var curRowChilds: Elements
        var data: Array<String>
        var isNew: Boolean
        var colNum: Int
        var colIterator: Iterator<*>

        rowIterator = substElements.iterator()
        while (rowIterator.hasNext()) {
            curRowChilds = (rowIterator.next() as Element).children()
            data = arrayOf("", "", "", "", "", "", "")
            isNew = false
            colNum = 0
            colIterator = curRowChilds.iterator()
            while (colIterator.hasNext() && colNum < 7) {
                data[colNum] = (colIterator.next() as Element).text()
                colNum++
            }
            if (curRowChilds.html().contains("<strong>")) {
                isNew = true
            }

            substitutions.add(Substitution(
                klass = data[0],
                lessonNr = data[1],
                origSubject = data[2],
                substName = data[3],
                substRoom = data[4],
                substSubject = data[5],
                notes = data[6],
                isNew = isNew
            ))
        }

        return Result.success(SubstitutionSet(dateText, noteStr, substitutions))
    }

    private fun fallbackLoad(result: String): Result<SubstitutionSet> {
        if (result == "E") return Result.failure(Exception("Result is E"))
        val substitutions = ArrayList<Substitution>()
        var dateStr = ""
        var noteStr = ""

        try {
            dateStr = result
                    .split("<td colspan=\"7\" class=\"rundeEckenOben vpUeberschr\">".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1]
                        .split("</td>".toRegex())
                        .dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0]
                        .replace("        ", "")
            noteStr = "[!] " + result
                .split("<tr id=\"Shinweis\">".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
                    .split("</tr>".toRegex())
                    .dropLastWhile { it.isEmpty() }
                .toTypedArray()[0]
                    .replace("Hinweis: <br />", "")
                    .replace("<br />", "· ")
                .replace("<.*?>".toRegex(), "")
                .replace("&uuml;", "ü")
                .replace("&Uuml;", "Ü")
                .replace("&auml;", "ä")
                .replace("&Auml;", "Ä")
                .replace("&ouml;", "ö")
                .replace("&Ouml;", "Ö")
                .replace("&szlig;", "ß")
                .replace("[\r\n]+".toRegex(), "")
                .trim { it <= ' ' }
        } catch (_: java.lang.Exception) {
        }

        val newC: Array<String>
        try {
             newC = clearUp(result
                        .split("<td class=\"vpTextZentriert\">".toRegex(), limit = 2)
                        .toTypedArray()[1]
                            .split("\n".toRegex())
                            .dropLastWhile { it.isEmpty() }
                        .toTypedArray())
                .split("\n")
                .toTypedArray()
            var counter = 1
            var klasse = ""
            var stunde = ""
            var orgfach = ""
            var vertret = ""
            var raum = ""
            var verfach = ""
            var str: String
            for (cnt in newC) {
                when (counter) {
                    1 -> {
                        klasse = cnt
                        counter++
                    }
                    2 -> {
                        stunde = cnt
                        counter++
                    }
                    3 -> {
                        orgfach = cnt
                        counter++
                    }
                    4 -> {
                        vertret = cnt
                        counter++
                    }
                    5 -> {
                        raum = cnt
                        counter++
                    }
                    6 -> {
                        verfach = cnt
                        counter++
                    }
                    7 -> {
                        str = cnt
                        counter = 1
                        substitutions.add(
                            Substitution(
                                klass = klasse,
                                lessonNr = stunde,
                                origSubject = orgfach,
                                substName = vertret,
                                substRoom = raum,
                                substSubject = verfach,
                                notes = str,
                                isNew = false
                            )
                        )
                        klasse = ""
                        stunde = ""
                        orgfach = ""
                        vertret = ""
                        raum = ""
                        verfach = ""
                    }
                }
            }
        } catch(ex: Exception) {
            return Result.failure(ex)
        }
        return  if(substitutions.isEmpty()) Result.failure(NoEntriesException())
                else Result.success(SubstitutionSet(
                    date = dateStr,
                    notes = noteStr,
                    substitutions = substitutions
                ))
    }

    private fun clearUp(input: Array<String>): String {
        val me = StringBuilder()
        for (ln2 in input) {
            var ln = ln2
            ln = ln.replace("<.*?>".toRegex(), "")
            ln = ln.replace("&uuml;", "ü")
                .replace("&Uuml;", "Ü")
                .replace("&auml;", "ä")
                .replace("&Auml;", "Ä")
                .replace("&ouml;", "ö")
                .replace("&Ouml;", "Ö")
                .replace("&szlig;", "ß")
            ln = ln.replace("                        ", "")
            ln = ln.trim { it <= ' ' }
            ln = ln.replace("	", "")

            if (ln != "      " &&
                ln != "var hoehe = parent.document.getElementById('inhframe').style.height;" &&
                ln != "setFrameHeight();" &&
                ln != "var pageTracker = _gat._getTracker(\"UA-5496889-1\");" &&
                ln != "pageTracker._trackPageview();" &&
                ln != "    " &&
                ln != "	" &&
                ln != "  " &&
                !ln.startsWith("var") &&
                !ln.startsWith("document.write") &&
                ln != "")

                if (ln.matches(".*\\w.*".toRegex()) || ln.contains("##"))
                    me.append(ln).append("\n")
        }
        return me.toString()
    }

    override suspend fun loadSubstitutionPlan(): Result<SubstitutionSet> {
        val b: OkHttpClient.Builder = OkHttpClient.Builder()
        b.readTimeout(20, TimeUnit.SECONDS)
        b.connectTimeout(20, TimeUnit.SECONDS)
        val client: OkHttpClient = b.build()
        System.setProperty("http.keepAlive", "false")
        val request: Request = Request.Builder()
            .url("https://www.gymnasium-sonneberg.de/Informationen/vp.php5")
            .build()
        client.newCall(request).execute().use { response ->
            if(!response.isSuccessful) return Result.failure(IOException("Unexpected code $response"))

            try {
                return parseResponse(response.body!!.string());
            }catch(ex: Exception){
                try {
                    return fallbackLoad(response.body!!.string())
                }catch(ex2: Exception) {
                    return Result.failure(ex);
                }
            }
        }
    }

    private fun loadTeachersPage(pageNr: Int?, parseMore: Boolean): Result<List<Teacher>> {
        val b: OkHttpClient.Builder = OkHttpClient.Builder()
        b.readTimeout(20, TimeUnit.SECONDS)
        b.connectTimeout(20, TimeUnit.SECONDS)
        val client: OkHttpClient = b.build()
        System.setProperty("http.keepAlive", "false")
        val request: Request = Request.Builder()
            .url("https://www.gymnasium-sonneberg.de/Kontakt/Sprech/ausgeben.php5?seite=" +
                    (pageNr ?: 1).toString() )
            .build()
        client.newCall(request).execute().use { response ->
            if(!response.isSuccessful) return Result.failure(IOException("Unexpected code $response"))

            val teachers = ArrayList<Teacher>()
            val doc: Document

            try {
                doc = Jsoup.parse(response.body!!.string())
                val tableElements: Elements = doc.select(
                    "table[class=\"eAusgeben\"] > tbody > tr:not(:first-child,:last-child)")

                for (tableRow: Element in tableElements) {
                    val teacherData = tableRow.selectFirst("td[class=\"eEintragGrau\"]")!!
                    if(!teacherData.html().contains("<br>")) continue
                    val teacherName = teacherData.html().split("<br>")[0]
                    val teacherShort = teacherData.html().substringAfter("Kürzel:").trim()
                    teachers.add(
                        Teacher(
                            shortName = teacherShort,
                            longName = teacherName)
                    )
                }
            } catch(ex: Exception) {
                return Result.failure(ex)
            }

            if(parseMore) {
                try {
                    val lastPage = doc.selectFirst(
                        "table[class=\"eAusgeben\"] > tbody > tr:last-child > td > a:nth-last-child(2)"
                    )!!
                        .attr("href").substringAfter("seite=").toInt()

                    for (page: Int in 2..lastPage) {
                        teachers.addAll(
                            loadTeachersPage(page, false).getOrElse { emptyList() }
                        )
                    }
                } catch (ex: Exception) {
                    return Result.failure(ex) //TODO: "fail" silently!
                }
            }

            return Result.success(teachers)
        }
    }

    override suspend fun loadSubjects(): Result<List<Subject>> {
        TODO("Not yet implemented")
    }

    override suspend fun loadTeachers(): Result<List<Teacher>> {
        return loadTeachersPage(1, true)
    }

    override suspend fun loadFoodPlan(): Result<FoodOfferSet> {
        val foods = mutableMapOf<String, MutableList<Food>>()
        try {
            val b: OkHttpClient.Builder = OkHttpClient.Builder()
            b.readTimeout(20, TimeUnit.SECONDS)
            b.connectTimeout(20, TimeUnit.SECONDS)
            val client: OkHttpClient = b.build()
            System.setProperty("http.keepAlive", "false")
            val request: Request = Request.Builder()
                .url("https://www.gymnasium-sonneberg.de/Informationen/vp.php5")
                .build()
            client.newCall(request).execute().use { response ->
                if(!response.isSuccessful) return Result.failure(IOException("Unexpected code $response"))

                try {
                    val doc = Jsoup.parse(response.body!!.string())
                    doc.select("sup").remove()
                    val currentDates = doc.select("button#time-selector-dropdown")
                    val kw = currentDates.text().split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0].replace("[\\D]".toRegex(), "").toInt()
                    if (kw < 0 || kw > 53) {
                        Log.w(TAG, "Parsed KW \"$kw\" is probably invalid")
                    }
                    Log.d(TAG, "NewEP::KW=$kw")
                    val dateFrom = currentDates.text()
                        .split("\\|\\|".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1]
                        .split("-".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0]
                        .trim { it <= ' ' } +
                            currentDates.text().split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()[1].split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray().last()

                    val dateTill = currentDates.text()
                        .split("\\|\\|".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1]
                        .split("-".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1]
                        .trim { it <= ' ' }

                    val tableElements = doc.select("table#menu-table_KW")

                    for (mealNr in arrayOf(1,2,3,7)) {
                        for(meal in tableElements.select("td[mealId=0$mealNr]")) {
                            val mealDate = meal.attr("day")
                            val mealName = meal.selectFirst("span[id=mealtext]")!!.text()
                            val mealAdditives = meal.selectFirst("sub")!!
                                .text()
                                .replace(" ", "")
                                .split(",")
                                .toList()
                            if(!foods.containsKey(mealDate)) foods[mealDate] = mutableListOf()
                            foods[mealDate]!!.add(
                                Food(
                                    num = mealNr,
                                    name = mealName,
                                    additives = mealAdditives
                                ))
                        }
                    }

                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    return Result.success(FoodOfferSet(
                        fromDate=sdf.parse(dateFrom)!!,
                        tillDate=sdf.parse(dateTill)!!,
                        foodOfferings=foods.mapKeys { sdf.parse(it.key)!! }
                    ) )
                } catch(ex: Exception) {
                    return Result.failure(ex);
                }
            }



        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Konnte Serverantwort nicht verarbeiten: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override suspend fun loadAdditives(): Result<List<Additive>> {
        TODO("Not yet implemented")
    }

}