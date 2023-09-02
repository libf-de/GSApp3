package de.xorg.gsapp.data.sources.remote

import de.xorg.gsapp.data.model.Additive
import de.xorg.gsapp.data.model.FoodOffer
import de.xorg.gsapp.data.model.Subject
import de.xorg.gsapp.data.model.SubstitutionSet
import de.xorg.gsapp.data.model.Teacher

interface RemoteDataSource {
    suspend fun loadSubstitutionPlan(): Result<SubstitutionSet>
    suspend fun loadSubjects(): Result<List<Subject>>
    suspend fun loadTeachers(): Result<List<Teacher>>

    suspend fun loadFoodPlan(): Result<List<FoodOffer>>
    suspend fun loadAdditives(): Result<List<Additive>>
}