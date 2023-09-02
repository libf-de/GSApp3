package de.xorg.gsapp.data.sources.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.xorg.gsapp.data.model.Additive
import de.xorg.gsapp.data.model.FoodOffer
import de.xorg.gsapp.data.model.Subject
import de.xorg.gsapp.data.model.Substitution
import de.xorg.gsapp.data.model.Teacher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter


class JsonDataSource(private var context: Context) : LocalDataSource {
    private var gson = Gson()
    private var substitutionsFile = File(context.applicationContext.cacheDir, "substitutions.json")
    private var subjectsFile = File(context.applicationContext.cacheDir, "subjects.json")
    private var teachersFile = File(context.applicationContext.cacheDir, "teachers.json")
    private var foodplanFile = File(context.applicationContext.cacheDir, "foodplan.json")
    private var additivesFile = File(context.applicationContext.cacheDir, "additives.json")

    override suspend fun loadSubstitutionPlan(): Result<List<Substitution>> {
        return if(substitutionsFile.exists()) {
            try {
                Result.success(withContext(Dispatchers.IO) {
                    gson.fromJson(
                        FileReader(substitutionsFile),
                        object : TypeToken<List<Substitution>>() {}.type
                    )
                })
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        } else {
            Result.failure(FileNotFoundException("substitutions-cache does not exist!"));
        }
    }

    override suspend fun storeSubstitutionPlan(value: List<Substitution>) {
        return withContext(Dispatchers.IO) {
            gson.toJson(value, FileWriter(substitutionsFile))
        }
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
        return withContext(Dispatchers.IO) {
            gson.toJson(value, FileWriter(subjectsFile))
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

    override suspend fun storeTeachers(value: List<Subject>) {
        return withContext(Dispatchers.IO) {
            gson.toJson(value, FileWriter(teachersFile))
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
        return withContext(Dispatchers.IO) {
            gson.toJson(value, FileWriter(foodplanFile))
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
        return withContext(Dispatchers.IO) {
            gson.toJson(value, FileWriter(additivesFile))
        }
    }
}