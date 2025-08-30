package com.tocletoque.thebreedinglab.model

import com.tocletoque.thebreedinglab.R
import com.tocletoque.thebreedinglab.common.Constant
import com.tocletoque.thebreedinglab.common.calculateFertilityRate
import com.tocletoque.thebreedinglab.common.calculateSurvivabilityRate
import java.time.LocalDate
import java.time.Period
import java.util.UUID
import kotlin.math.max

data class Dog(
    var name: String,
    val sex: Sex,
    val birthday: LocalDate,
    val B: String,
    val E: String,
    val D: String,
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
    val hasDilute: Boolean = false,
    val mother: String? = null,
    val father: String? = null,
    val id: String = UUID.randomUUID().toString(),
    val unlockedAt: Reputation = Reputation.NoviceBreeder
) {
    val age: Int = Period.between(birthday, LocalDate.now()).years

//    val baseCoatColor : String = when {
//        E == "ee" -> "Yellow coat"
//        B == "bb" -> "Brown coat"
//        else -> "Black coat"
//    }

    fun getBaseCoatColor(): String {
        val base = when {
            E == "ee" -> "Yellow"
            B == "bb" -> "Brown"
            else -> "Black"
        }

        // Apply dilution only if dd
        return when {
            D == "dd" && base == "Yellow" -> "Champagne"
            D == "dd" && base == "Brown" -> "Lilac"
            D == "dd" && base == "Black" -> "Blue"
            else -> base
        } + " coat"
    }

    val image = when(getBaseCoatColor()) {
        "Yellow coat" -> R.drawable.yellow
        "Brown coat" -> R.drawable.brown
        "Black coat" -> R.drawable.black
        "Champagne coat" -> R.drawable.champagne
        "Lilac coat" -> R.drawable.lilac
        "Blue coat" -> R.drawable.blue
        else -> R.drawable.yellow
    }

    val coatColor: String = if (hasDilute) {
        "Dilute ${getBaseCoatColor()}"
    } else {
        getBaseCoatColor()
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

    var wellnessScore: Int = calculateEnhancedWellnessScore()

    private fun getHealthStatus(genotype: String): String = when {
        genotype =="NN" -> "Clear"
        "N" in genotype -> "Carrier"
        else -> "Affected"
    }

    var fertilityRate = calculateFertilityRate(
        temperament = temperament,
        wellnessScore = wellnessScore.toDouble(),
        age = age
    )
    var survivabilityRate = calculateSurvivabilityRate(
        temperament = temperament,
        wellnessScore = wellnessScore.toDouble(),
        sociability = sociability
    )

    // Seasonal and discovery state
    var seasonsOld: Int = if (mother != null || father != null) 0 else 8
    var traitsDiscovered: Boolean = if (mother != null || father != null) false else true

    fun advanceSeason() {
        seasonsOld += 1
        if (!traitsDiscovered && seasonsOld >= 2) {
            traitsDiscovered = true
        }

        // Recompute age by birthday is optional; we keep yearly progression by real date
        // Refresh stats for new season (seasonal modifiers may change)
        wellnessScore = calculateEnhancedWellnessScore()
        fertilityRate = calculateFertilityRate(
            temperament = temperament,
            wellnessScore = wellnessScore.toDouble(),
            age = age
        )
        survivabilityRate = calculateSurvivabilityRate(
            temperament = temperament,
            wellnessScore = wellnessScore.toDouble(),
            sociability = sociability
        )
    }

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

        var enhancedWellness = baseWellness + tempBonus + trainBonus + socialBonus

        // Seasonal wellness adjustment
        val seasonalDelta = SEASONAL_WELLNESS_DELTA[Constant.gameTime.seasonName] ?: 0
        enhancedWellness += seasonalDelta

        return maxOf(0, minOf(100, enhancedWellness))
    }

    fun getCurrentFertilityRate(gameTime: GameTime): Double {
        val seasonal = SEASONAL_FERTILITY_DELTA[gameTime.seasonName] ?: 0.0
        val adjusted = fertilityRate + seasonal
        return adjusted.coerceIn(0.75, 0.98)
    }

    fun getFertilityCategory(): String {
//        return when {
//            fertilityRate >= 0.95 -> "high"
//            fertilityRate >= 0.90 -> "medium"
//            else -> "low"
//        }

        val currentFertilityRate = getCurrentFertilityRate(Constant.gameTime)

        return when {
            currentFertilityRate >= 0.95 -> "high"
            currentFertilityRate >= 0.90 -> "medium"
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
            appendLine("Season Age: $seasonsOld | Traits Discovered: ${if (traitsDiscovered) "Yes" else "No"}")
            appendLine()
            appendLine("=== HEALTH STATUS ===")
            appendLine("PRA: ${healthStatuses["pra"]}")
            appendLine("EIC: ${healthStatuses["eic"]}")
            appendLine("HNPK: ${healthStatuses["hnpk"]}")
            appendLine("CNM: ${healthStatuses["cnm"]}")
            appendLine("SD2: ${healthStatuses["sd2"]}")
            appendLine("Wellness Score: $wellnessScore")
            appendLine()
            appendLine("=== EPIGENETIC TRAITS ===")
            val temperament = if (traitsDiscovered) {
                temperament.uppercaseFirst()
            } else {
                "Unknown"
            }
            val trainability = if (traitsDiscovered) {
                trainability.uppercaseFirst()
            } else {
                "Unknown"
            }
            val sociability = if (traitsDiscovered) {
                sociability.uppercaseFirst()
            } else {
                "Unknown"
            }
            appendLine("Temperament: $temperament")
            appendLine("Trainability: $trainability")
            appendLine("Sociability: $sociability")
            appendLine("Fertility: ${getFertilityCategory().uppercaseFirst()} (${fertilityRate.toPercentageString()})")
            appendLine("Survivability: ${getSurvivabilityCategory().uppercaseFirst()} (${survivabilityRate.toPercentageString()})")
            appendLine()
            appendLine("=== PEDIGREE ===")

            val mother = mother ?: "UNKNOWN"
            val dad = father ?: "UNKNOWN"
            val children = getChildren(name).takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "None"
            val fullSiblings = getFullSiblings(this@Dog).takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "None"
            val halfSiblings = getHalfSiblings(this@Dog).takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "None"
            appendLine("Parents: Mother: $mother | Father: $dad")
            appendLine("Children: $children")
            appendLine("Full Siblings: $fullSiblings")
            appendLine("Half Siblings: $halfSiblings")
        }
    }

    fun getDetailWithoutName(): String {
        return buildString {
            appendLine("Sex: $sex")
            appendLine("Age: $age")
            appendLine("Coat: $coatColor")
            appendLine("Tail: $tailLength")
            appendLine("Season Age: $seasonsOld | Traits Discovered: ${if (traitsDiscovered) "Yes" else "No"}")
            appendLine()
            appendLine("=== HEALTH STATUS ===")
            appendLine("PRA: ${healthStatuses["pra"]}")
            appendLine("EIC: ${healthStatuses["eic"]}")
            appendLine("HNPK: ${healthStatuses["hnpk"]}")
            appendLine("CNM: ${healthStatuses["cnm"]}")
            appendLine("SD2: ${healthStatuses["sd2"]}")
            appendLine("Wellness Score: $wellnessScore")
            appendLine()
            appendLine("=== EPIGENETIC TRAITS ===")
            val temperament = if (traitsDiscovered) {
                temperament.uppercaseFirst()
            } else {
                "Unknown"
            }
            val trainability = if (traitsDiscovered) {
                trainability.uppercaseFirst()
            } else {
                "Unknown"
            }
            val sociability = if (traitsDiscovered) {
                sociability.uppercaseFirst()
            } else {
                "Unknown"
            }
            appendLine("Temperament: $temperament")
            appendLine("Trainability: $trainability")
            appendLine("Sociability: $sociability")
            appendLine("Fertility: ${getFertilityCategory().uppercaseFirst()} (${fertilityRate.toPercentageString()})")
            appendLine("Survivability: ${getSurvivabilityCategory().uppercaseFirst()} (${survivabilityRate.toPercentageString()})")
            appendLine()
            appendLine("=== PEDIGREE ===")

            val mother = mother ?: "UNKNOWN"
            val dad = father ?: "UNKNOWN"
            val children = getChildren(name).takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "None"
            val fullSiblings = getFullSiblings(this@Dog).takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "None"
            val halfSiblings = getHalfSiblings(this@Dog).takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "None"
            appendLine("Parents: Mother: $mother | Father: $dad")
            appendLine("Children: $children")
            appendLine("Full Siblings: $fullSiblings")
            appendLine("Half Siblings: $halfSiblings")
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
        // Seasonal price multiplier
        price *= SEASONAL_PRICE_MULTIPLIER[Constant.gameTime.seasonName] ?: 1.0

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