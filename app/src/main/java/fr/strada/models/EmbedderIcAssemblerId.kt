package fr.strada.models
import com.google.gson.annotations.SerializedName
data class EmbedderIcAssemblerId (

	@SerializedName("countryCode") var countryCode : String,
	@SerializedName("moduleEmbedder") var moduleEmbedder : String,
	@SerializedName("manufacturerInformation") var manufacturerInformation : Int
) {

}