package com.ccmc.covid.travelpass

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.agrawalsuneet.dotsloader.loaders.LazyLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_apply_pass_actiivity.*
import kotlinx.android.synthetic.main.alertdialog_layout.view.*
import java.text.SimpleDateFormat
import java.util.*


class ApplyPassActiivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var type: String
    var firebaseUser: FirebaseUser? = null
    lateinit var userModel:UserModel
    var remoteConfig: FirebaseRemoteConfig? =null
    lateinit var alertDialog :AlertDialog
    val TAG : String = this.javaClass.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply_pass_actiivity)
        remoteConfig= FirebaseRemoteConfig.getInstance()
        firebaseAuth=FirebaseAuth.getInstance()
        firebaseUser=firebaseAuth.currentUser
        type = intent.getStringExtra("type")!!
        val n = "$type Pass"
        typeTextView.text = n
        if (type == "Essential")
        {
            tiApplyPass.hint = "Description (Eg:)Groceries,Medicines"
        }else{
            tiApplyPass.hint = "Description (Eg:)Hospital"
        }
        descriptionEditText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (type== "Essential") {
                if (hasFocus) {
                    tiApplyPass.hint = "Description"
                } else {
                    if (descriptionEditText.length()>0){
                        tiApplyPass.hint = "Description"
                    }else {
                        tiApplyPass.hint = "Description (Eg:)Groceries,Medicines"
                    }
                }
            }else{
                if (hasFocus) {
                    tiApplyPass.hint = "Description"
                } else {
                    if (descriptionEditText.length()>0){
                        tiApplyPass.hint = "Description"
                    }else {
                        tiApplyPass.hint = "Description (Eg:)Hospital"
                    }
                }
            }
        }

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
        showDialog()
        var diff : Long = 0
        if (type.equals("Essential"))
            diff  = remoteConfig!!.getLong("essentialPassValiditySeconds")
        if (type.equals("Emergency"))
            diff = remoteConfig!!.getLong("emergencyPassValiditySeconds")
        val user = getUserInfo()
        val timeStamp = (System.currentTimeMillis() / 1000L)
        val endTimeStamp = ((System.currentTimeMillis() / 1000L)+diff)
        val passModel = PassModel(user.name,user.phoneNumber,user.fullAddress, type!!,description,timeStamp,endTimeStamp,user.vehicleNumber,user.userId,destination)
        val collectionReference: CollectionReference = FirebaseFirestore.getInstance().collection(type)
        collectionReference.document().set(passModel).addOnCompleteListener{
            if (it.isSuccessful) {
                    if (passModel.isEssential()) {
                        SessionStorage.saveEssentialPass(this, passModel)
                    }else{
                        SessionStorage.saveEmergencyPass(this,passModel)
                    }
                    dismissDialog()
                    Toast.makeText(applicationContext,"Pass Approved",Toast.LENGTH_LONG).show()
                    finish()
            }else{
                Toast.makeText(applicationContext,"Error occured",Toast.LENGTH_LONG).show()
                    dismissDialog()
                    finish()
            }
        }
    }

    private fun getUserInfo():UserModel
    {
        return SessionStorage.getUser(this)!!
    }

    fun formatDate(a: Long): String? {
        val timeStamp = a
        val today = Date(timeStamp * 1000L)
        val formatter = SimpleDateFormat("dd-M-yyyy hh:mm:ss a")
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(today)
    }

    private fun showDialog(){
        alertDialog= AlertDialog.Builder(this).create()
        val view: View = LayoutInflater.from(this).inflate(R.layout.alertdialog_layout,null,false)
        view.textViewAlertDialog.setText("Applying...")
        var lazyLoader = LazyLoader(this, 25, 10,
            ContextCompat.getColor(this, R.color.colorPrimary),
            ContextCompat.getColor(this, R.color.colorPrimary),
            ContextCompat.getColor(this, R.color.colorPrimaryDark))
            .apply {
                animDuration = 500
                firstDelayDuration = 100
                secondDelayDuration = 200
                interpolator = DecelerateInterpolator()
            }
        var layout = view.findViewById<LinearLayout>(R.id.linearAlertDialog)
        layout.addView(lazyLoader)
        lazyLoader.animate()
        alertDialog.setView(view)
        alertDialog.setCancelable(false)
        alertDialog.show()
        Log.d(TAG,"Dialog shown")
    }

    private fun  dismissDialog()
    {
        alertDialog.let {
            if (it.isShowing) {
                it.dismiss()
                Log.d(TAG,"Dismissed")
            }
        }
    }
}
