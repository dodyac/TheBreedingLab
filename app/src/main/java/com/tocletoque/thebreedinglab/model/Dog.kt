package com.tocletoque.thebreedinglab.model

import java.time.LocalDate
import java.time.Period
import kotlin.math.max

data class Dog(
    val name: String,
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
    val price: Int
) {
    val age: Int = Period.between(birthday, LocalDate.now()).years

    val coatColor: String = when {
        E == "ee" -> "Yellow coat"
        B == "bb" -> "Brown coat"
        else -> "Black coat"
    }

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

    val wellnessScore: Int = calculateWellnessScore(age, sex, pra, eic, hnpk, cnm, sd2)

    private fun getHealthStatus(genotype: String): String = when {
        genotype =="NN" -> "Clear"
        "N" in genotype -> "Carrier"
        else -> "Affected"
    }

    fun calculateWellnessScore(age: Int, sex: Sex, vararg genotypes: String): Int {
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
            appendLine("Wellness Score: $wellnessScore")
            appendLine("PRA: ${healthStatuses["pra"]}")
            appendLine("EIC: ${healthStatuses["eic"]}")
            appendLine("HNPK: ${healthStatuses["hnpk"]}")
            appendLine("CNM: ${healthStatuses["cnm"]}")
            appendLine("SD2: ${healthStatuses["sd2"]}")
        }
    }
}