package fr.strada.models
import com.google.gson.annotations.SerializedName
class Identification {
	@SerializedName("cardIdentification") var cardIdentification : CardIdentification
	@SerializedName("driverCardHolderIdentification") var driverCardHolderIdentification : DriverCardHolderIdentification

	
	constructor(
		cardIdentification: CardIdentification,
		driverCardHolderIdentification: DriverCardHolderIdentification
	) {
		this.cardIdentification = cardIdentification
		this.driverCardHolderIdentification = driverCardHolderIdentification
	}
}