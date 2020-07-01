
package fr.strada.models
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.RealmObject

open class ActivityChangeInfo: RealmObject {
	 @SerializedName("lecteur") var lecteur : String=""
	 @SerializedName("etatConduite") var etatConduite : String=""
	 @SerializedName("etatCarte") var etatCarte : String=""
	 @SerializedName("activite") var activite : String=""
	 @SerializedName("time") var time : String =""
	 constructor()
 }