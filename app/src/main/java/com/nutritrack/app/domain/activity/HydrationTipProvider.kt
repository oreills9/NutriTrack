package com.nutritrack.app.domain.activity

import com.nutritrack.app.data.local.entity.ActivityType
import com.nutritrack.app.data.local.entity.Intensity

object HydrationTipProvider {

    fun tipFor(activityType: ActivityType, intensity: Intensity): String {
        val context = when (activityType) {
            ActivityType.TENNIS -> "Tennis involves repeated bursts of sprinting and quick direction changes"
            ActivityType.PADEL -> "Padel keeps you moving constantly through short, sharp rallies"
            ActivityType.WALKING -> "Walking is low-impact, but fluid loss still adds up over a longer session"
            ActivityType.INDOOR_CYCLING -> "Indoor cycling in a warm studio increases sweat loss quickly"
        }
        val advice = when (intensity) {
            Intensity.LIGHT -> "sip water regularly and do some light stretching afterwards."
            Intensity.MODERATE -> "drink around 500ml of water over the next hour and cool down for a few minutes."
            Intensity.INTENSE ->
                "rehydrate with 750ml+ of water or an electrolyte drink and prioritize stretching or foam rolling."
        }
        return "$context — $advice"
    }
}
