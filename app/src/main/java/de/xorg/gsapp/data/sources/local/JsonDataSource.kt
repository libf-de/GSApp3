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

import de.xorg.gsapp.data.cache.CacheManager
import de.xorg.gsapp.data.model.Additive
import de.xorg.gsapp.data.model.FoodOffer
import de.xorg.gsapp.data.model.Subject
import de.xorg.gsapp.data.model.SubstitutionSet
import de.xorg.gsapp.data.model.Teacher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset


class JsonDataSource(cacheManager: CacheManager) : LocalDataSource {
    private var substitutionsFile = File(cacheManager.getCacheDirectory(), "substitutions.json")
    private var subjectsFile = File(cacheManager.getCacheDirectory(), "subjects.json")
    private var teachersFile = File(cacheManager.getCacheDirectory(), "teachers.json")
    private var foodplanFile = File(cacheManager.getCacheDirectory(), "foodplan.json")
    private var additivesFile = File(cacheManager.getCacheDirectory(), "additives.json")

    override suspend fun loadSubstitutionPlan(): Result<SubstitutionSet> {
        return if(substitutionsFile.exists()) {
            try {
                Result.success(
                    Json.decodeFromString(withContext(Dispatchers.IO) {
                        substitutionsFile.readText(Charset.forName("UTF-8"))
                    })
                )
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("substitutions-cache does not exist!"));
        }
    }

    override suspend fun storeSubstitutionPlan(value: SubstitutionSet) {
        val jsonString = Json.encodeToString(value)
        withContext(Dispatchers.IO) {
            substitutionsFile.writeText(jsonString, Charset.forName("UTF-8"))
        }
    }

    override suspend fun loadSubjects(): Result<List<Subject>> {
        return if(subjectsFile.exists()) {
            try {
                Result.success(
                    Json.decodeFromString(withContext(Dispatchers.IO) {
                        subjectsFile.readText(Charset.forName("UTF-8"))
                    })
                )
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("subjects-cache does not exist!"));
        }
    }

    override suspend fun storeSubjects(value: List<Subject>) {
        val jsonString = Json.encodeToString(value)
        withContext(Dispatchers.IO) {
            subjectsFile.writeText(jsonString, Charset.forName("UTF-8"))
        }
    }

    override suspend fun loadTeachers(): Result<List<Teacher>> {
        return if(teachersFile.exists()) {
            try {
                Result.success(
                    Json.decodeFromString(withContext(Dispatchers.IO) {
                        teachersFile.readText(Charset.forName("UTF-8"))
                    })
                )
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("teacher-cache does not exist!"));
        }
    }

    override suspend fun storeTeachers(value: List<Teacher>) {
        val jsonString = Json.encodeToString(value)
        withContext(Dispatchers.IO) {
            teachersFile.writeText(jsonString, Charset.forName("UTF-8"))
        }
    }

    override suspend fun loadFoodPlan(): Result<List<FoodOffer>> {
        return if(foodplanFile.exists()) {
            try {
                Result.success(
                    Json.decodeFromString(withContext(Dispatchers.IO) {
                        foodplanFile.readText(Charset.forName("UTF-8"))
                    })
                )
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("foodplan-cache does not exist!"));
        }
    }

    override suspend fun storeFoodPlan(value: List<FoodOffer>) {
        val jsonString = Json.encodeToString(value)
        withContext(Dispatchers.IO) {
            foodplanFile.writeText(jsonString, Charset.forName("UTF-8"))
        }
    }

    override suspend fun loadAdditives(): Result<List<Additive>> {
        return if(additivesFile.exists()) {
            try {
                Result.success(
                    Json.decodeFromString(withContext(Dispatchers.IO) {
                        additivesFile.readText(Charset.forName("UTF-8"))
                    })
                )
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("additives-cache does not exist!"));
        }
    }

    override suspend fun storeAdditives(value: List<Additive>) {
        val jsonString = Json.encodeToString(value)
        withContext(Dispatchers.IO) {
            additivesFile.writeText(jsonString, Charset.forName("UTF-8"))
        }
    }
}