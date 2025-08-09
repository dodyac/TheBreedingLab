package com.tocletoque.thebreedinglab.common

import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val rand = Random()

/**
 * Calculate epigenetic trait inheritance based on parents with environmental influence.
 *
 * @param parent1Trait first parent's trait value (must exist in traitValues)
 * @param parent2Trait second parent's trait value (must exist in traitValues)
 * @param traitValues list of possible trait values in ascending order
 * @param heritability how much the trait is influenced by parents (0.0..1.0)
 * @return inherited trait value from traitValues
 */
fun calculateEpigeneticTrait(
    parent1Trait: String,
    parent2Trait: String,
    traitValues: List<String>,
    heritability: Double = 0.7
): String {
    val parent1Index = traitValues.indexOf(parent1Trait).takeIf { it >= 0 }
        ?: throw IllegalArgumentException("parent1Trait not found in traitValues")
    val parent2Index = traitValues.indexOf(parent2Trait).takeIf { it >= 0 }
        ?: throw IllegalArgumentException("parent2Trait not found in traitValues")

    val parentalAverage = (parent1Index + parent2Index) / 2.0

    // Gaussian environmental effect (mean 0, sd 0.5)
    val environmentalEffect = rand.nextGaussian() * 0.5

    // Combine parental and environmental effects
    var finalIndex = heritability * parentalAverage + (1.0 - heritability) * environmentalEffect

    // Add some uniform random variation between -0.3 and 0.3
    finalIndex += (rand.nextDouble() * 0.6) - 0.3

    // Clamp to valid range and round
    val clamped = max(0.0, min((traitValues.size - 1).toDouble(), finalIndex))
    val rounded = clamped.roundToInt()

    return traitValues[rounded]
}

/**
 * Calculate if offspring has dilute coat pattern based on parents and base coat color.
 * Returns true if dilute is present.
 */
fun calculateDilutePattern(parent1HasDilute: Boolean, parent2HasDilute: Boolean, coatColor: String): Boolean {
    var chance = when {
        parent1HasDilute && parent2HasDilute -> 0.8
        parent1HasDilute || parent2HasDilute -> 0.5
        else -> 0.1
    }

    // Coat-color-specific modifiers
    if ("Black" in coatColor) {
        chance *= 1.2
    } else if ("Brown" in coatColor) {
        chance *= 1.1
    }

    return rand.nextDouble() < chance
}

/**
 * Calculate fertility rate based on temperament, wellness score, and age.
 *
 * @param temperament "shy", "reactive", or anything else assumed "friendly"
 * @param wellnessScore numeric wellness score (e.g. 0..100)
 * @param age in years
 * @return fertility rate clamped between 0.75 and 0.98
 */
fun calculateFertilityRate(temperament: String, wellnessScore: Double, age: Int): Double {
    val baseFertility = 0.93

    val fertilityModifier = when (temperament.lowercase()) {
        "shy" -> -0.05
        "reactive" -> -0.03
        else -> 0.02 // friendly or default
    }

    val wellnessModifier = (wellnessScore - 85.0) * 0.001

    val ageModifier = when {
        age < 2 -> -0.08
        age > 8 -> -0.05
        else -> 0.01
    }

    val finalFertility = baseFertility + fertilityModifier + wellnessModifier + ageModifier
    return max(0.75, min(0.98, finalFertility))
}

/**
 * Calculate puppy survivability rate based on maternal temperament, wellness, and sociability.
 *
 * @param temperament "friendly", "reactive", or others ("shy" expected)
 * @param wellnessScore numeric wellness score (e.g. 0..100)
 * @param sociability "high", "medium", or "low"
 * @return survivability rate clamped between 0.80 and 0.98
 */
fun calculateSurvivabilityRate(temperament: String, wellnessScore: Double, sociability: String): Double {
    val baseSurvival = 0.93

    val tempModifier = when (temperament.lowercase()) {
        "friendly" -> 0.03
        "reactive" -> -0.02
        else -> -0.01 // shy or default
    }

    val wellnessModifier = (wellnessScore - 85.0) * 0.0015

    val socialModifier = when (sociability.lowercase()) {
        "high" -> 0.02
        "low" -> -0.02
        else -> 0.0 // medium/default
    }

    val finalSurvival = baseSurvival + tempModifier + wellnessModifier + socialModifier
    return max(0.80, min(0.98, finalSurvival))
}