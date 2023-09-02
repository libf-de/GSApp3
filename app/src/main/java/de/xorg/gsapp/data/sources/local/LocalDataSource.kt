package de.xorg.gsapp.data.sources.local

import de.xorg.gsapp.data.model.FoodOffer
import de.xorg.gsapp.data.model.Subject
import de.xorg.gsapp.data.model.Substitution
import de.xorg.gsapp.data.model.Teacher
import de.xorg.gsapp.data.model.Additive
import de.xorg.gsapp.data.model.SubstitutionSet

interface LocalDataSource {
    suspend fun loadSubstitutionPlan(): Result<SubstitutionSet>
    suspend fun storeSubstitutionPlan(value: SubstitutionSet)
    suspend fun loadSubjects(): Result<List<Subject>>
    suspend fun storeSubjects(value: List<Subject>)
    suspend fun loadTeachers(): Result<List<Teacher>>
    suspend fun storeTeachers(value: List<Subject>)

    suspend fun loadFoodPlan(): Result<List<FoodOffer>>
    suspend fun storeFoodPlan(value: List<FoodOffer>)
    suspend fun loadAdditives(): Result<List<Additive>>
    suspend fun storeAdditives(value: List<Additive>)
}