package com.ccmc.covid.travelpass

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
        loadPass(userId)
    }

    private fun loadPass(userId : String)
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
                        val pass : PassModel? = SessionStorage.getPass(this@ViewPassActivity)
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

    private fun isValidPass(validityTimeStamp : Long):Boolean{
        return (System.currentTimeMillis() / 1000L) <= validityTimeStamp
    }

    fun formatDate(a: Long): String? {
        val timeStamp = a
        val today = Date(timeStamp * 1000L)
        val formatter = SimpleDateFormat("dd-M-yyyy hh:mm a")
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(today)
    }
}
