package net.molteno.linus.prescient.utils

fun lerp(x: Double, y: Double, t: Double?): Double? = if (t == null) null else x * (1-t) + y*t