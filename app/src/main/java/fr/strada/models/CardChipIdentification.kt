package fr.strada.models
import com.google.gson.annotations.SerializedName
class CardChipIdentification{
	@SerializedName("icSerialNumber") var icSerialNumber : String
	@SerializedName("icManufacturingReferences") var icManufacturingReferences : String

	constructor(icSerialNumber: String, icManufacturingReferences: String) {
		this.icSerialNumber = icSerialNumber
		this.icManufacturingReferences = icManufacturingReferences
	}

	override fun toString(): String {
		return "CardChipIdentification(icSerialNumber='$icSerialNumber', icManufacturingReferences='$icManufacturingReferences')"
	}


}