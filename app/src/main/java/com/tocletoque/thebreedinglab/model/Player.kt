package com.tocletoque.thebreedinglab.model

data class Player(
    var money: Int = 2000,
    var reputation: Int = 0,
    var totalPuppiesBred: Int = 0,
    var totalPuppiesSold: Int = 0
) {
    val reputationMilestones = mapOf(
        0 to Reputation.NoviceBreeder,
        300 to Reputation.AmateurBreeder,
        600 to Reputation.ExperiencedBreeder,
        1200 to Reputation.ProfessionalBreeder,
        2400 to Reputation.ExpertBreeder,
        4800 to Reputation.MasterBreeder,
        9600 to Reputation.EliteBreeder,
        19200 to Reputation.LegendaryBreeder
    )

    fun getReputationTitle(): String {
        return reputationMilestones.entries
            .filter { reputation >= it.key }
            .maxByOrNull { it.key }?.value?.displayName ?: Reputation.NoviceBreeder.displayName
    }

    fun getMaximumPuppies(): Int {
        return reputationMilestones.entries
            .filter { reputation >= it.key }
            .maxByOrNull { it.key }?.value?.maximumDog ?: Reputation.NoviceBreeder.maximumDog
    }

    fun getNextReputationMilestone(): Pair<String, Int>? {
        val sortedMilestones = reputationMilestones.entries.sortedBy { it.key }
        val next = sortedMilestones.firstOrNull { reputation < it.key }
        return next?.let { it.value.displayName to (it.key - reputation) }
    }

    private fun canAfford(amount: Int): Boolean {
        return money >= amount
    }

    fun spend(amount: Int): Boolean = if (canAfford(amount)) {
        money -= amount
        true
    } else {
        false
    }

    fun spendCheck(amount: Int): Boolean = if (canAfford(amount)) {
        true
    } else {
        false
    }

    fun calculateBreedingReputation(puppies: List<Dog>): Int {
        val baseRep = 5

        val totalRep = if (puppies.isNotEmpty()) {
            val avgWellness = puppies.sumOf { it.wellnessScore } / puppies.size.toDouble()
            val wellnessBonus = (avgWellness / 10).toInt()

            val premiumCount = puppies.count { it.wellnessScore >= 90 }
            val highQualityCount = puppies.count { it.wellnessScore >= 85 }

            val premiumBonus = premiumCount * 3
            val qualityBonus = highQualityCount * 1.5

            baseRep + wellnessBonus + premiumBonus + qualityBonus
        } else {
            baseRep.toDouble()
        }

        return maxOf(totalRep.toInt(), 1)
    }

    fun addReputation(amount: Int): Pair<Boolean, String> {
        val oldTitle = getReputationTitle()
        reputation += amount
        val newTitle = getReputationTitle()

        return Pair(oldTitle != newTitle, newTitle)
    }

    fun earn(amount: Int) {
        money += amount
    }

    fun calculateSaleReputation(puppy: Dog, price: Int): Int {
        val baseRep = 1

        val qualityRep = when {
            puppy.wellnessScore >= 90 -> 3
            puppy.wellnessScore >= 85 -> 2
            puppy.wellnessScore >= 70 -> 1
            else -> 0
        }

        val priceBonus = maxOf(0, (price - 500) / 200)

        return baseRep + qualityRep + priceBonus
    }
}