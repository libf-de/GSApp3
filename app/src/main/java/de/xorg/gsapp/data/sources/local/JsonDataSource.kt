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

package de.xorg.gsapp.data.sources.local

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.xorg.gsapp.data.cache.CacheManager
import de.xorg.gsapp.data.model.Additive
import de.xorg.gsapp.data.model.FoodOffer
import de.xorg.gsapp.data.model.Subject
import de.xorg.gsapp.data.model.SubstitutionSet
import de.xorg.gsapp.data.model.Teacher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException


class JsonDataSource(private val cacheManager: CacheManager) : LocalDataSource {
    private var gson = Gson()
    private var substitutionsFile = File(cacheManager.getCacheDirectory(), "substitutions.json")
    private var subjectsFile = File(cacheManager.getCacheDirectory(), "subjects.json")
    private var teachersFile = File(cacheManager.getCacheDirectory(), "teachers.json")
    private var foodplanFile = File(cacheManager.getCacheDirectory(), "foodplan.json")
    private var additivesFile = File(cacheManager.getCacheDirectory(), "additives.json")

    override suspend fun loadSubstitutionPlan(): Result<SubstitutionSet> {
        return if(substitutionsFile.exists()) {
            try {
                Result.success(withContext(Dispatchers.IO) {
                    gson.fromJson(
                        FileReader(substitutionsFile),
                        object : TypeToken<SubstitutionSet>() {}.type
                    )
                })
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("substitutions-cache does not exist!"));
        }
    }

    override suspend fun storeSubstitutionPlan(value: SubstitutionSet) {
        withContext(Dispatchers.IO) {
            FileWriter(substitutionsFile).use { writer -> gson.toJson(value, writer) }
        }
        /*withContext(Dispatchers.IO) {
            val gsonStr = gson.toJson(value)
            Log.d("GSApp", gsonStr)
            gson.toJson(value, FileWriter(substitutionsFile))
        }*/
    }

    override suspend fun loadSubjects(): Result<List<Subject>> {
        return if(subjectsFile.exists()) {
            try {
                Result.success(withContext(Dispatchers.IO) {
                    gson.fromJson(
                        FileReader(subjectsFile),
                        object : TypeToken<List<Subject>>() {}.type
                    )
                })
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("subjects-cache does not exist!"));
        }
    }

    override suspend fun storeSubjects(value: List<Subject>) {
        withContext(Dispatchers.IO) {
            FileWriter(subjectsFile).use { writer -> gson.toJson(value, writer) }
        }
    }

    override suspend fun loadTeachers(): Result<List<Teacher>> {
        return if(teachersFile.exists()) {
            try {
                Result.success(withContext(Dispatchers.IO) {
                    gson.fromJson(
                        FileReader(teachersFile),
                        object : TypeToken<List<Teacher>>() {}.type
                    )
                })
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("teacher-cache does not exist!"));
        }
    }

    override suspend fun storeTeachers(value: List<Teacher>) {
        withContext(Dispatchers.IO) {
            FileWriter(teachersFile).use { writer -> gson.toJson(value, writer) }
        }
    }

    override suspend fun loadFoodPlan(): Result<List<FoodOffer>> {
        return if(foodplanFile.exists()) {
            try {
                Result.success(withContext(Dispatchers.IO) {
                    gson.fromJson(
                        FileReader(foodplanFile),
                        object : TypeToken<List<FoodOffer>>() {}.type
                    )
                })
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("foodplan-cache does not exist!"));
        }
    }

    override suspend fun storeFoodPlan(value: List<FoodOffer>) {
        withContext(Dispatchers.IO) {
            FileWriter(foodplanFile).use { writer -> gson.toJson(value, writer) }
        }
    }

    override suspend fun loadAdditives(): Result<List<Additive>> {
        return if(additivesFile.exists()) {
            try {
                Result.success(withContext(Dispatchers.IO) {
                    gson.fromJson(
                        FileReader(additivesFile),
                        object : TypeToken<List<Additive>>() {}.type
                    )
                })
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("substitutions-cache does not exist!"));
        }
    }

    override suspend fun storeAdditives(value: List<Additive>) {
        withContext(Dispatchers.IO) {
            FileWriter(additivesFile).use { writer -> gson.toJson(value, writer) }
        }
    }
}