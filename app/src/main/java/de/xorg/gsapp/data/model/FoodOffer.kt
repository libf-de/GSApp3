package de.xorg.gsapp.data.model

import java.util.Date

data class FoodOffer(
    val date: Date,
    val foods: List<Food>
)