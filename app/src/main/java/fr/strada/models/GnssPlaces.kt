package fr.strada.models
import com.google.gson.annotations.SerializedName
data class GnssPlaces (

	@SerializedName("gnssContinuousDriving") val gnssContinuousDriving : GnssContinuousDriving
)