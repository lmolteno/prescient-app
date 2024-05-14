package net.molteno.linus.prescient.sun.api.models

import com.google.gson.annotations.SerializedName

data class SolarRegion (
    val region: Int,
    val latitude: Int,
    val longitude: Int,
    val location: String,
    val observedDate: String, // iso 8601
    val carringtonLongitude: Int,
    val area: Int,
    val spotClass: String,
    val extent: Int,
    val numberSpots: Int,
    val magClass: String,
    val magString: String,
    val status: String,
    val cXrayEvents: Int,
    val mXrayEvents: Int,
    val xXrayEvents: Int,
    val protonEvents: Int?,
    val cFlareProbability: Int,
    val mFlareProbability: Int,
    val xFlareProbability: Int,
    val firstDate: String // iso 8601
)

/*
{
    "observed_date": "2024-05-13",
    "region": 3664,
    "latitude": -19,
    "longitude": -87,
    "location": "S19W87",
    "carrington_longitude": 348,
    "old_carrington_longitude": 349,
    "area": 1170,
    "spot_class": "Fkc",
    "extent": 24,
    "number_spots": 15,
    "mag_class": "BGD",
    "mag_string": null,
    "status": "f",
    "c_xray_events": 0,
    "m_xray_events": 5,
    "x_xray_events": 0,
    "proton_events": null,
    "s_flares": 6,
    "impulse_flares_1": 0,
    "impulse_flares_2": 0,
    "impulse_flares_3": 0,
    "impulse_flares_4": 0,
    "protons": 0,
    "c_flare_probability": 99,
    "m_flare_probability": 75,
    "x_flare_probability": 40,
    "proton_probability": 99,
    "first_date": "2024-05-01T16:42:56"
  },
 */
