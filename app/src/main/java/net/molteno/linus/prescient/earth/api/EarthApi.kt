package net.molteno.linus.prescient.earth.api

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EarthApi @Inject constructor(private val db: EarthDb) {
    fun getCoastlines() = db.getCoastlines()
}