package fr.strada.models
import com.google.gson.annotations.SerializedName
class GnssContinuousDriving {
	@SerializedName("gnssCDPointerNewestRecord") var gnssCDPointerNewestRecord : Int = 0
	@SerializedName("gnssContinuousDrivingRecords") var gnssContinuousDrivingRecords : List<GnssContinuousDrivingRecords> = emptyList()
}