package com.ccmc.covid.travelpass

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_apply_pass_actiivity.*
import java.text.SimpleDateFormat
import java.util.*


class ApplyPassActiivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var type: String
    var firebaseUser: FirebaseUser? = null
    lateinit var userModel:UserModel
    var remoteConfig: FirebaseRemoteConfig? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply_pass_actiivity)
        remoteConfig= FirebaseRemoteConfig.getInstance()
        firebaseAuth=FirebaseAuth.getInstance()
        firebaseUser=firebaseAuth.currentUser
        type = intent.getStringExtra("type")!!
        typeTextView.text = type
        applyPassButton.setOnClickListener{
            if (descriptionEditText.text.toString().trim().isEmpty())
            {
                descriptionEditText.error = "Description Can Not Be Empty"
                return@setOnClickListener
            }
            if (destinationEditText.text.toString().trim().isEmpty())
            {
                destinationEditText.error = "Description Can Not Be Empty"
                return@setOnClickListener
            }
            val description= descriptionEditText.text.toString()
            val destination = destinationEditText.text.toString()
            applyPass(type,description,destination)
        }
    }

    private fun applyPass(
        type: String?,
        description: String,
        destination: String
    )
    {
        val diff : Long = remoteConfig!!.getLong("limit")
        val user = getUserInfo()
        val timeStamp = (System.currentTimeMillis() / 1000L)
        val endTimeStamp = ((System.currentTimeMillis() / 1000L)+diff)
        val passModel = PassModel(user.name,user.phoneNumber,user.fullAddress, type!!,description,timeStamp,endTimeStamp,user.vehicleNumber,user.userId,destination)
        val collectionReference: CollectionReference = FirebaseFirestore.getInstance().collection("requests")
        collectionReference.document().set(passModel).addOnCompleteListener{
            if (it.isSuccessful) {
                    Toast.makeText(applicationContext,"Pass Approved",Toast.LENGTH_LONG).show()
                    finish()
            }else{
                Toast.makeText(applicationContext,"Pass Failed",Toast.LENGTH_LONG).show()
                    finish()
            }
        }
    }

    private fun getUserInfo():UserModel
    {
        return SessionStorage.getUser(this)
    }

    fun formatDate(a: String): String? {
        val timeStamp = a.toLong()
        val today = Date(timeStamp * 1000L)
        val formatter = SimpleDateFormat("dd-M-yyyy hh:mm:ss a")
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(today)
    }
}
