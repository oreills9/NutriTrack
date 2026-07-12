package com.nutritrack.app.domain.activity

import com.nutritrack.app.data.local.entity.ActivityType
import com.nutritrack.app.data.local.entity.Intensity

object ActivityMetDefaults {

    // Compendium-of-Physical-Activities-style reference METs. Padel isn't catalogued there;
    // 6.0 approximates its doubles-tennis-like intensity.
    fun baseMet(activityType: ActivityType): Double = when (activityType) {
        ActivityType.TENNIS -> 7.0
        ActivityType.PADEL -> 6.0
        ActivityType.WALKING -> 3.5
        ActivityType.INDOOR_CYCLING -> 7.0
    }
}

// Moderate is the unadjusted baseline; Light/Intense shift it +-10%.
fun Intensity.metMultiplier(): Double = when (this) {
    Intensity.LIGHT -> 0.9
    Intensity.MODERATE -> 1.0
    Intensity.INTENSE -> 1.1
}
