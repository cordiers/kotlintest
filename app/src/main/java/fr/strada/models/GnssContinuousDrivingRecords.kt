package fr.strada.models
import com.google.gson.annotations.SerializedName
class GnssContinuousDrivingRecords(){
	@SerializedName("timestamp") var timestamp : String = ""
	@SerializedName("gnssPlaceRecord") var gnssPlaceRecord : GnssPlaceRecord = GnssPlaceRecord()
}