package fr.strada.models
import com.google.gson.annotations.SerializedName
data class CurrentUsage (

	@SerializedName("cardCurrentUse") val cardCurrentUse : CardCurrentUse
)