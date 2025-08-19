package com.tocletoque.thebreedinglab.model

// Game time and seasonal system
class GameTime {
    private val seasons = listOf("Winter", "Spring", "Summer", "Fall")
    var seasonIndex: Int = 0  // 0-based across years
    var year: Int = 1

    val seasonName: String
        get() = seasons[seasonIndex % 4]

    fun advance() {
        seasonIndex++
        if (seasonIndex % 4 == 0) {
            year++
        }
    }
}


// Seasonal modifiers
val SEASONAL_WELLNESS_DELTA = mapOf(
    "Winter" to -2,
    "Spring" to 0,
    "Summer" to 0,
    "Fall" to -1
)

val SEASONAL_FERTILITY_DELTA = mapOf(
    "Winter" to -0.03,
    "Spring" to 0.03,
    "Summer" to 0.0,
    "Fall" to 0.0
)

val SEASONAL_PRICE_MULTIPLIER = mapOf(
    "Winter" to 0.90,
    "Spring" to 1.10,
    "Summer" to 1.00,
    "Fall" to 1.00
)