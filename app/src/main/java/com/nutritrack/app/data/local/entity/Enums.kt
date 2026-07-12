package com.nutritrack.app.data.local.entity

enum class MealSlot {
    MORNING,
    AFTERNOON,
    EVENING,
    ACTIVITY_SNACK,
}

enum class DataSource {
    OPEN_FOOD_FACTS,
    NUTRITIONIX,
    MANUAL,
}

enum class QuantityUnit {
    GRAMS,
    MILLILITERS,
}

enum class ActivityType {
    TENNIS,
    PADEL,
    WALKING,
    INDOOR_CYCLING,
}

enum class Intensity {
    LIGHT,
    MODERATE,
    INTENSE,
}

enum class TimeOfDay {
    MORNING,
    EVENING,
}

enum class BiologicalSex {
    MALE,
    FEMALE,
    OTHER,
}

enum class ActivityLevel {
    SEDENTARY,
    LIGHTLY_ACTIVE,
    MODERATELY_ACTIVE,
    VERY_ACTIVE,
    EXTRA_ACTIVE,
}

enum class UnitSystem {
    METRIC,
    IMPERIAL,
}
