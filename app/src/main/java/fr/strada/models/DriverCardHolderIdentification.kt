package fr.strada.models
import com.google.gson.annotations.SerializedName
data class DriverCardHolderIdentification (

	@SerializedName("cardHolderName") var cardHolderName : CardHolderName,
	@SerializedName("cardHolderBirthDate") var cardHolderBirthDate : String?,
	@SerializedName("cardHolderPreferredLanguage") var cardHolderPreferredLanguage : String?
)