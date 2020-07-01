package fr.strada.models
import com.google.gson.annotations.SerializedName
data class CardPlaceDailyWorkPeriod (

	@SerializedName("placePointerNewestRecord") val placePointerNewestRecord : Int,
	@SerializedName("placeRecords") val placeRecords : List<PlaceRecords>
)