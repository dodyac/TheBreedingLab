package com.tocletoque.thebreedinglab.model

import com.tocletoque.thebreedinglab.common.calculateFertilityRate
import com.tocletoque.thebreedinglab.common.calculateSurvivabilityRate
import java.time.LocalDate
import java.time.Period
import kotlin.math.max

data class Dog(
    var name: String,
    val sex: Sex,
    val birthday: LocalDate,
    val B: String,
    val E: String,
    val tail: String,
    val pra: String = "NN",
    val eic: String = "NN",
    val hnpk: String = "NN",
    val cnm: String = "NN",
    val sd2: String = "NN",
    val price: Int = 0,
    val temperament: String = "friendly",
    val trainability: String = "medium",
    val sociability: String = "medium",
    val hasDilute: Boolean = false
) {
    val age: Int = Period.between(birthday, LocalDate.now()).years

    val baseCoatColor : String = when {
        E == "ee" -> "Yellow coat"
        B == "bb" -> "Brown coat"
        else -> "Black coat"
    }

    val coatColor: String = if (hasDilute) {
        "Dilute $baseCoatColor"
    } else {
        baseCoatColor
    }

    val isDiluteYellowCoat = coatColor == "Dilute Yellow coat"
    val isYellowCoat = coatColor == "Yellow coat"

    val tailLength: String = if ("T" in tail) {
        "Long tail"
    } else {
        "Short tail"
    }

    val healthStatuses: Map<String, String> = mapOf(
        "pra" to getHealthStatus(pra),
        "eic" to getHealthStatus(eic),
        "hnpk" to getHealthStatus(hnpk),
        "cnm" to getHealthStatus(cnm),
        "sd2" to getHealthStatus(sd2)
    )

    val wellnessScore: Int = calculateEnhancedWellnessScore()

    private fun getHealthStatus(genotype: String): String = when {
        genotype =="NN" -> "Clear"
        "N" in genotype -> "Carrier"
        else -> "Affected"
    }

    val fertilityRate = calculateFertilityRate(
        temperament = temperament,
        wellnessScore = wellnessScore.toDouble(),
        age = age
    )
    val survivabilityRate = calculateSurvivabilityRate(
        temperament = temperament,
        wellnessScore = wellnessScore.toDouble(),
        sociability = sociability
    )

    fun calculateEnhancedWellnessScore(): Int {
        // base wellness from genetics and age
        val baseWellness = calculateWellnessScore(pra, eic, hnpk, cnm, sd2)

        // Temperament affects stress and overall wellness
        val tempBonus = when (temperament.lowercase()) {
            "friendly" -> 3
            "reactive" -> -2
            else -> -1 // shy or default
        }

        // Trainability affects mental stimulation and wellness
        val trainBonus = when (trainability.lowercase()) {
            "high" -> 2
            "low" -> -1
            else -> 0 // medium/default
        }

        // Sociability affects mental health
        val socialBonus = when (sociability.lowercase()) {
            "high" -> 2
            "low" -> -1
            else -> 0
        }

        val enhancedWellness = baseWellness + tempBonus + trainBonus + socialBonus
        return maxOf(0, minOf(100, enhancedWellness))
    }

    fun getFertilityCategory(): String {
        return when {
            fertilityRate >= 0.95 -> "high"
            fertilityRate >= 0.90 -> "medium"
            else -> "low"
        }
    }

    fun getSurvivabilityCategory(): String {
        return when {
            survivabilityRate >= 0.95 -> "high"
            survivabilityRate >= 0.90 -> "medium"
            else -> "low"
        }
    }

    fun calculateWellnessScore(vararg genotypes: String): Int {
        val agePenalty = if (sex == Sex.Female) {
            when {
                age < 5 -> age * 3
                age < 8 -> 5 * 3 + (age - 5) * 5
                else -> 5 * 3 + 3 * 5 + (age - 8) * 8
            }
        } else {
            age * 2
        }

        val baseScore = max(0, 100 - agePenalty)

        val penalties = genotypes.sumOf { genotype ->
            when (genotype) {
                "mm" -> 10
                "Nm" -> 2
                else -> 0
            }.toInt()
        }

        val wellness = baseScore - penalties
        return max(0, wellness)
    }

    fun getDetail(): String {
        return buildString {
            appendLine("Name: $name")
            appendLine("Sex: $sex")
            appendLine("Age: $age")
            appendLine("Coat: $coatColor")
            appendLine("Tail: $tailLength")
            appendLine()
            appendLine("PRA: ${healthStatuses["pra"]}")
            appendLine("EIC: ${healthStatuses["eic"]}")
            appendLine("HNPK: ${healthStatuses["hnpk"]}")
            appendLine("CNM: ${healthStatuses["cnm"]}")
            appendLine("SD2: ${healthStatuses["sd2"]}")
            appendLine()
            appendLine("Temperament: ${temperament.uppercaseFirst()}")
            appendLine("Trainability: ${trainability.uppercaseFirst()}")
            appendLine("Sociability: ${sociability.uppercaseFirst()}")
            appendLine("Fertility: ${getFertilityCategory().uppercaseFirst()} (${fertilityRate.toPercentageString()})")
            appendLine("Survivability: ${getSurvivabilityCategory().uppercaseFirst()} (${survivabilityRate.toPercentageString()})")
            appendLine()
            appendLine("Wellness Score: $wellnessScore")
        }
    }

    fun getDetailWithoutName(): String {
        return buildString {
            appendLine("Sex: $sex")
            appendLine("Age: $age")
            appendLine("Coat: $coatColor")
            appendLine("Tail: $tailLength")
            appendLine()
            appendLine("PRA: ${healthStatuses["pra"]}")
            appendLine("EIC: ${healthStatuses["eic"]}")
            appendLine("HNPK: ${healthStatuses["hnpk"]}")
            appendLine("CNM: ${healthStatuses["cnm"]}")
            appendLine("SD2: ${healthStatuses["sd2"]}")
            appendLine()
            appendLine("Temperament: ${temperament.uppercaseFirst()}")
            appendLine("Trainability: ${trainability.uppercaseFirst()}")
            appendLine("Sociability: ${sociability.uppercaseFirst()}")
            appendLine("Fertility: ${getFertilityCategory().uppercaseFirst()} (${fertilityRate.toPercentageString()})")
            appendLine("Survivability: ${getSurvivabilityCategory().uppercaseFirst()} (${survivabilityRate.toPercentageString()})")
            appendLine()
            appendLine("Wellness Score: $wellnessScore")
        }
    }

    fun calculatePuppyPrice(): Int {
        val basePrice = wellnessScore * 5

        var price = if (sex == Sex.Female) {
            basePrice * 1.2
        } else {
            basePrice * 1.0
        }

        price *= when {
            wellnessScore >= 96 -> 1.3
            wellnessScore >= 90 -> 1.2
            wellnessScore >= 80 -> 1.1
            else -> 1.0
        }

        return maxOf(price.toInt(), 200)
    }
}

fun String.uppercaseFirst(): String =
    if (this.isNotEmpty()) this[0].uppercase() + this.substring(1) else this

fun Double.toPercentageString(): String {
    val percent = this * 100
    val formatted = "%.2f".format(percent).removeSuffix(".00")
    return "$formatted%"
}