package com.nutritrack.app.domain.units

private const val CM_PER_INCH = 2.54
private const val INCHES_PER_FOOT = 12
private const val KG_PER_LB = 0.45359237

object UnitConverter {

    fun feetInchesToCm(feet: Int, inches: Double): Double = (feet * INCHES_PER_FOOT + inches) * CM_PER_INCH

    fun cmToFeetInches(cm: Double): Pair<Int, Double> {
        val totalInches = cm / CM_PER_INCH
        val feet = (totalInches / INCHES_PER_FOOT).toInt()
        val inches = totalInches - feet * INCHES_PER_FOOT
        return feet to inches
    }

    fun lbsToKg(lbs: Double): Double = lbs * KG_PER_LB

    fun kgToLbs(kg: Double): Double = kg / KG_PER_LB
}
