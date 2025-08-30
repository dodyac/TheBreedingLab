package com.tocletoque.thebreedinglab.model

enum class Reputation(
    val displayName: String,
    val maximumDog: Int
) {
    NoviceBreeder(
        "Novice Breeder",
        11
    ),
    AmateurBreeder(
        "Amateur Breeder",
        15
    ),
    ExperiencedBreeder(
        "Experienced Breeder",
        19
    ),
    ProfessionalBreeder(
        "Professional Breeder",
        21
    ),
    ExpertBreeder(
        "Expert Breeder",
        23
    ),
    MasterBreeder(
        "Master Breeder",
        27
    ),
    EliteBreeder(
        "Elite Breeder",
        31
    ),
    LegendaryBreeder(
        "Legendary Breeder",
        35
    )
}