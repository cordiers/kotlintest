
package fr.strada.models
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

open class LectureHistory: RealmObject , Comparable<LectureHistory>{

	@SerializedName("emailUser") var emailUser : String=""
	 @SerializedName("file") var file : String=""
	 @SerializedName("cardNumber") var cardNumber : String=""
	 @PrimaryKey
	 @SerializedName("time") var time : String =""
	 constructor()
	 constructor(file: String, cardNumber: String, time: String,emailUser:String) : super() {
		this.file = file
		this.cardNumber = cardNumber
		this.time = time
		this.emailUser = emailUser
	 }

	override fun compareTo(other: LectureHistory): Int {
		var luangageFormatEnglais = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
		var lectureHistoryTime = Date()
		var otherLectureHistoryTime = Date()
		try {
			lectureHistoryTime =  luangageFormatEnglais.parse(this.time)
			otherLectureHistoryTime =  luangageFormatEnglais.parse(other.time)
		}catch (ex:Exception)
		{

		}
		if(lectureHistoryTime.after(otherLectureHistoryTime))
		{
			return -1
		}else
		{
			return 1
		}
	}
}