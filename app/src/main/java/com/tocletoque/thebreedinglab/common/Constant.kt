package com.tocletoque.thebreedinglab.common

import com.tocletoque.thebreedinglab.model.Dog
import com.tocletoque.thebreedinglab.model.GameTime
import com.tocletoque.thebreedinglab.model.Sex
import java.time.LocalDate

object Constant {

    val gameTime = GameTime()

    val dogList = listOf(
        Dog(
            "Daisy",
            Sex.Female,
            LocalDate.of(2024, 6, 5),
            "Bb",
            "ee",
            "Tt",
            "Nm",
            "NN",
            "Nm",
            "NN",
            "Nm",
            1050,
            "friendly",
            "high",
            "medium",
            false
        ),
        Dog(
            "Samson",
            Sex.Male,
            LocalDate.of(2023, 1, 18),
            "BB",
            "Ee",
            "tt",
            "NN",
            "NN",
            "Nm",
            "NN",
            "NN",
            1600,
            "reactive",
            "medium",
            "high",
            false
        ),
        Dog(
            "Luna",
            Sex.Female,
            LocalDate.of(2022, 10, 1),
            "bb",
            "EE",
            "Tt",
            "NN",
            "NN",
            "NN",
            "NN",
            "NN",
            1750,
            "friendly",
            "high",
            "high",
            true
        ),
        Dog(
            "Ollie",
            Sex.Male,
            LocalDate.of(2019, 9, 9),
            "Bb",
            "Ee",
            "TT",
            "Nm",
            "Nm",
            "Nm",
            "NN",
            "NN",
            1000,
            "shy",
            "low",
            "low",
            false
        ),
        Dog(
            "Bea",
            Sex.Female,
            LocalDate.of(2023, 7, 24),
            "Bb",
            "EE",
            "Tt",
            "NN",
            "NN",
            "NN",
            "NN",
            "Nm",
            1350,
            "friendly",
            "medium",
            "medium",
            false
        ),
        Dog(
            "Prince",
            Sex.Male,
            LocalDate.of(2017, 9, 12),
            "bb",
            "ee",
            "tt",
            "mm",
            "mm",
            "mm",
            "mm",
            "mm",
            900,
            "reactive",
            "low",
            "low",
            true
        ),
    )

    const val FOOD_COST_PER_DOG = 50
    const val PUPPY_TRAINING_COST_PER_PUPPY = 40
    const val GROOMING_COST_PER_DOG = 30

}