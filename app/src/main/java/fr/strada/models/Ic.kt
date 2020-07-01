package fr.strada.models
import com.google.gson.annotations.SerializedName
class Ic {
	@SerializedName("cardChipIdentification") val cardChipIdentification : CardChipIdentification

	constructor(cardChipIdentification: CardChipIdentification) {
		this.cardChipIdentification = cardChipIdentification
	}

	override fun toString(): String {
		return "Ic(cardChipIdentification=${cardChipIdentification.toString()})"
	}


}
