package com.ccmc.covid.travelpass

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_view_pass.*
import java.text.SimpleDateFormat
import java.util.*

class ViewPassActivity : AppCompatActivity() {

    private lateinit var userId: String
    val TAG : String = this.javaClass.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pass)
        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        showPass()
    }

    @SuppressLint("SetTextI18n")
    private fun showPass() {
        val essentialPass = SessionStorage.getEssentialPass(this)
        val emergencyPass = SessionStorage.getEmergencyPass(this)
        if (emergencyPass!=null)
        {
            if (isValidPass(emergencyPass.validityTimeStamp)) {
                if ((getCurrentTimeStamp()-emergencyPass.createdTimeStamp)<300)
                {
                    minWithInTextView.text = "Generated within 5 minutes"
                }else{
                    minWithInTextView.visibility = View.GONE
                }
                cardView.visibility = View.VISIBLE
                validityStatus.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.green))
                validityStatus.text = "Status : Valid"
                textPassName.text = "Name : ${emergencyPass.name} "
                phoneNumberPass.text = "Phone No. : ${emergencyPass.phoneNumber}"
                destinationPass.text = "Destination : ${emergencyPass.destination}"
                descriptionPass.text = "Description : ${emergencyPass.description}"
                addressPass.text = "Address : ${emergencyPass.address}"
                vehicleNumberPass.text = "Vehicle No. : ${emergencyPass.vehicleNumber}"
                validityPass.text = "Expiry Time : "+formatDate(emergencyPass.validityTimeStamp)
                Log.d(TAG,"Success")
            }else{
                cardView.visibility = View.VISIBLE
                validityStatus.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.red))
                validityStatus.text = "Status : Expired"
                textPassName.text = "Name : ${emergencyPass.name} "
                phoneNumberPass.text = "Phone No. : ${emergencyPass.phoneNumber}"
                destinationPass.text = "Destination : ${emergencyPass.destination}"
                descriptionPass.text = "Description : ${emergencyPass.description}"
                addressPass.text = "Address : ${emergencyPass.address}"
                vehicleNumberPass.text = "Vehicle No. : ${emergencyPass.vehicleNumber}"
                validityPass.text = "Expiry Time : "+formatDate(emergencyPass.validityTimeStamp)
                Log.d(TAG,"Pass Expired")
            }
        }
        if (essentialPass!=null)
        {
            if (isValidPass(essentialPass.validityTimeStamp)) {
                if ((getCurrentTimeStamp()-essentialPass.createdTimeStamp)<300)
                {
                    minWithInTextViewEssential.text = "Generated within 5 minutes"
                }else{
                    minWithInTextViewEssential.visibility = View.GONE
                }
                cardView1.visibility = View.VISIBLE
                validityStatus1.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.green))
                validityStatus1.text = "Status : Valid"
                textPassName1.text = "Name : ${essentialPass.name} "
                phoneNumberPass1.text = "Phone No. : ${essentialPass.phoneNumber}"
                destinationPass1.text = "Destination : ${essentialPass.destination}"
                descriptionPass1.text = "Description : ${essentialPass.description}"
                addressPass1.text = "Address : ${essentialPass.address}"
                vehicleNumberPass1.text = "Vehicle No. : ${essentialPass.vehicleNumber}"
                validityPass1.text = "Expiry Time : "+formatDate(essentialPass.validityTimeStamp)
                Log.d(TAG,"Success")
            }else{
                cardView1.visibility = View.VISIBLE
                validityStatus1.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.red))
                validityStatus1.text = "Status : Expired"
                textPassName1.text = "Name : ${essentialPass.name} "
                phoneNumberPass1.text = "Phone No. : ${essentialPass.phoneNumber}"
                destinationPass1.text = "Destination : ${essentialPass.destination}"
                descriptionPass1.text = "Description : ${essentialPass.description}"
                addressPass1.text = "Address : ${essentialPass.address}"
                vehicleNumberPass1.text = "Vehicle No. : ${essentialPass.vehicleNumber}"
                validityPass1.text = "Expiry Time : "+formatDate(essentialPass.validityTimeStamp)
                Log.d(TAG,"Pass Expired")
            }
        }
    }

    /* private fun loadPass(userId : String)
     {
         var collectionReference : CollectionReference = FirebaseFirestore.getInstance().collection("requests")
         val query : Query = collectionReference.whereEqualTo("userId",userId)
             .orderBy("createdTimeStamp",Query.Direction.DESCENDING)
             .limit(1)
         query.addSnapshotListener{ querySnapshot: QuerySnapshot?, e : FirebaseFirestoreException? ->
                     if (querySnapshot==null)
                     {

                         return@addSnapshotListener
                     }
                     if(querySnapshot.size()==0)
                     {
                         return@addSnapshotListener
                     }
                     var pass = querySnapshot.documents[0]?.toObject(PassModel::class.java)
                     if (pass != null) {
                         if (isValidPass(pass.validityTimeStamp)) {
                             validityStatus.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.green))
                             validityStatus.append(" : Valid")
                             textPassName.append(" : ${pass.name} ")
                             phoneNumberPass.append(" : ${pass.phoneNumber}")
                             destinationPass.append(" : ${pass.destination}")
                             descriptionPass.append(" : ${pass.description}")
                             addressPass.append(" : ${pass.address}")
                             vehicleNumberPass.append(" : ${pass.vehicleNumber}")
                             validityPass.append(" : "+formatDate(pass.validityTimeStamp))
                             Log.d(TAG,"Success")
                             return@addSnapshotListener
                         }else{
                             validityStatus.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.red))
                             validityStatus.append(" : Expired")
                             textPassName.append(" : ${pass.name} ")
                             phoneNumberPass.append(" : ${pass.phoneNumber}")
                             destinationPass.append(" : ${pass.destination}")
                             descriptionPass.append(" : ${pass.description}")
                             addressPass.append(" : ${pass.address}")
                             vehicleNumberPass.append(" : ${pass.vehicleNumber}")
                             validityPass.append(" : "+formatDate(pass.validityTimeStamp))
                             Log.d(TAG,"Pass Expired")
                             return@addSnapshotListener
                         }
                     }else{
                         val pass : PassModel? = SessionStorage.getEmergencyPass(this@ViewPassActivity)
                         if (pass!=null)
                         {
                             if (isValidPass(pass.validityTimeStamp)) {
                                 validityStatus.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.green))
                                 validityStatus.append(" : Valid")
                                 textPassName.append(" : ${pass.name} ")
                                 phoneNumberPass.append(" : ${pass.phoneNumber}")
                                 destinationPass.append(" : ${pass.destination}")
                                 descriptionPass.append(" : ${pass.description}")
                                 addressPass.append(" : ${pass.address}")
                                 vehicleNumberPass.append(" : ${pass.vehicleNumber}")
                                 validityPass.append(" : "+formatDate(pass.validityTimeStamp))
                                 Log.d(TAG,"Success")
                                 return@addSnapshotListener
                             }else{
                                 validityStatus.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.red))
                                 validityStatus.append(" : Expired")
                                 textPassName.append(" : ${pass.name} ")
                                 phoneNumberPass.append(" : ${pass.phoneNumber}")
                                 destinationPass.append(" : ${pass.destination}")
                                 descriptionPass.append(" : ${pass.description}")
                                 addressPass.append(" : ${pass.address}")
                                 vehicleNumberPass.append(" : ${pass.vehicleNumber}")
                                 validityPass.append(" : "+formatDate(pass.validityTimeStamp))
                                 Log.d(TAG,"Pass Expired")
                                 return@addSnapshotListener
                             }
                         }
                         else{
                             cardView.visibility = View.INVISIBLE
                             Toast.makeText(applicationContext,"Error Occured",Toast.LENGTH_SHORT).show()
                             finish()
                         }
                         return@addSnapshotListener
                     }
         }
     }
 */
    private fun isValidPass(validityTimeStamp : Long):Boolean{
        return (System.currentTimeMillis() / 1000L) <= validityTimeStamp
    }

    private fun getCurrentTimeStamp():Long{
        return (System.currentTimeMillis()/1000L)
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDate(a: Long): String? {
        val timeStamp = a
        val today = Date(timeStamp * 1000L)
        val formatter = SimpleDateFormat("dd-M-yyyy hh:mm a")
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(today)
    }
}
