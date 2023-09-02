package de.xorg.gsapp.data.model

data class Substitution(
    val klass: String,
    val lessonNr: String,
    val origSubject: String,
    val substName: String,
    val substRoom: String,
    val substSubject: String,
    val notes: String,
    val isNew: Boolean
)
