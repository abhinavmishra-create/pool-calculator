package com.example.poolcalculator

import java.util.Locale

enum class LengthUnit(val label: String) { FEET("Feet"), METER("Meter") }
enum class AreaUnit(val label: String) { SQ_FEET("Sq.Ft"), SQ_METER("Sq.M") }
enum class RateMode { SINGLE, SPLIT }

const val FT_PER_M = 3.280839895        // 1 metre  = 3.2808 ft
const val SQFT_PER_SQM = 10.763910417   // 1 sq.m   = 10.7639 sq.ft

fun String.toD(): Double = trim().replace(",", "").toDoubleOrNull() ?: 0.0

/** plain, for text fields */
fun fmt(v: Double, dec: Int = 2): String =
    if (v.isNaN() || v.isInfinite()) "0" else String.format(Locale.US, "%.${dec}f", v)

/** with thousand separators, for display */
fun money(v: Double, dec: Int = 2): String =
    if (v.isNaN() || v.isInfinite()) "0" else String.format(Locale.US, "%,.${dec}f", v)
