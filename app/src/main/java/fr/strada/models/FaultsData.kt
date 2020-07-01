package fr.strada.models
import com.google.gson.annotations.SerializedName
data class FaultsData (

	@SerializedName("cardFaultData") val cardFaultData : CardFaultData
)