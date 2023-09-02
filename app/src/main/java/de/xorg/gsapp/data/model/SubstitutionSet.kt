package de.xorg.gsapp.data.model

data class SubstitutionSet(
    val date: String,
    val notes: String,
    val substitutions: List<Substitution>
)
