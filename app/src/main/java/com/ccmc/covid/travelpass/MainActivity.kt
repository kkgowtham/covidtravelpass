package com.ccmc.covid.travelpass

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.agrawalsuneet.dotsloader.loaders.LazyLoader
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

     lateinit var firebaseAuth:FirebaseAuth
     var firebaseUser: FirebaseUser? = null
    var TAG:String = this.javaClass.simpleName
    var remoteConfig: FirebaseRemoteConfig? =null
    lateinit var alertDialog:AlertDialog
    val list = ArrayList<PassModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseUser= firebaseAuth.currentUser
        remoteConfig= FirebaseRemoteConfig.getInstance()
        setRemoteConfig()
        saveUserData()
        emergencyButton.setOnClickListener {
            if (canApplyEmergencyPass()) {
                showDialog()
                checkForPass("Emergency")
            }else{
                showSnackBar(viewPassButton,"You already have a valid pass","Emergency")
            }
        }
        essentialsButton.setOnClickListener{
            if (canApplyEssentialPass()) {
                showDialog()
                checkForPass("Essential")
            }else{
                showSnackBar(viewPassButton,"You already have a valid pass","Essential")
            }
        }
        viewPassButton.setOnClickListener {
            if(SessionStorage.getEmergencyPass(this@MainActivity)==null&&SessionStorage.getEssentialPass(this@MainActivity)==null)
            {
                showSnackBar(viewPassButton,"No Pass Found","")
            }else {
                startActivity(Intent(this, ViewPassActivity::class.java))
            }
        }
    }

    private fun saveUserData() {
        if (SessionStorage.getUser(this)==null)
        {
            if(firebaseUser!=null) {
                FirebaseFirestore.getInstance().collection("users").document(firebaseUser?.uid!!)
                    .get().addOnSuccessListener {
                    val userModel = it.toObject(UserModel::class.java)
                        Log.d(TAG,userModel.toString())
                    userModel?.let {
                        SessionStorage.saveUser(userModel, this)
                        Log.d(TAG,SessionStorage.getUser(this).toString())
                    }
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if (firebaseUser==null)
        {
            startActivity(Intent(this,PhoneVerificationActivity::class.java))
        }else{
            if (firebaseUser!!.displayName.isNullOrEmpty())
            {
                val intent= Intent(this,UserDetailsActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }


    private fun checkForPass(reasonType:String){
        //val userId : String = firebaseUser?.uid.toString()
        list.clear()
        FirebaseFirestore.getInstance().collection(reasonType).whereEqualTo("vehicleNumber",SessionStorage.getUser(this)?.vehicleNumber)
            .orderBy("createdTimeStamp",Query.Direction.DESCENDING)
            .limit(1).get().addOnSuccessListener {
                Log.d(TAG,"Success")
                if (it!=null && it.size()>0) {
                    for (a in it) {
                        list.add(a.toObject(PassModel::class.java))
                    }
                    updateAddressList(reasonType)
                }
                else{
                    updateAddressList(reasonType)
                }
            }.addOnFailureListener{
                Log.d(TAG,it.message.toString())
                updateAddressList(reasonType)
            }

    }

    private fun updateAddressList(reasonType: String) {
        val phoneNumber:String = firebaseUser?.phoneNumber.toString()
        val collectionReference:CollectionReference= FirebaseFirestore.getInstance().collection(reasonType)
        val query : Query = collectionReference.whereEqualTo("address",
            SessionStorage.getUser(this)?.fullAddress
        )
            .orderBy("createdTimeStamp",Query.Direction.DESCENDING)
            .limit(1)
        query.get().addOnCompleteListener{
            if (it.isSuccessful)
            {
                val value = it.result
                    if (value != null&&value.size()>=0)
                    {
                        for (a in value) {
                            list.add(a.toObject(PassModel::class.java))
                        }
                        checkValidityForPass(reasonType)
                    }else {
                        checkValidityForPass(reasonType)
                    }
                }else{
                    dismissDialog()
                    Toast.makeText(applicationContext,"Error occured",Toast.LENGTH_LONG).show()
                }
            }

    }

    private fun setRemoteConfig()
    {
        remoteConfig = Firebase.remoteConfig
       remoteConfig!!.setDefaultsAsync(R.xml.remote_config)
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 2000
        }
        remoteConfig!!.setConfigSettingsAsync(configSettings)
        remoteConfig!!.fetchAndActivate().addOnSuccessListener {
            Log.d(TAG,"Updated Data Successfully")
            Log.d(TAG, remoteConfig!!.getLong("days").toString())
            Log.d(TAG, remoteConfig!!.getLong("limit").toString())
        }
    }

    private fun showDialog(){
        alertDialog=AlertDialog.Builder(this).create()
        val view:View = LayoutInflater.from(this).inflate(R.layout.alertdialog_layout,null,false)
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

    private fun showSnackBar(root: View?, snackTitle: String?,reasonType: String) {
        val snackbar = Snackbar.make(root!!, snackTitle!!, Snackbar.LENGTH_LONG)
        snackbar.setAction("OK") {
            reloadPass(reasonType)
            snackbar.dismiss()
        }
        snackbar.show()
        val view = snackbar.view
        val txtv = view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        txtv.gravity = Gravity.CENTER_HORIZONTAL
    }

    private fun reloadPass(reasonType: String) {
        if (reasonType.trim().isNotEmpty()) {
            val query: Query = FirebaseFirestore.getInstance().collection(reasonType)
                .whereEqualTo("userId", firebaseUser?.uid)
                .orderBy("createdTimeStamp", Query.Direction.DESCENDING)
                .limit(1)
            query.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val a = it.result?.documents?.size ?: return@addOnCompleteListener
                    if (a == 0) {
                        return@addOnCompleteListener
                    }
                    val passModel = it.result!!.documents[0].toObject(PassModel::class.java)
                    if (passModel != null && passModel.isEmergency()) {
                        SessionStorage.saveEmergencyPass(this@MainActivity, passModel)
                    }
                    if (passModel != null && passModel.isEssential()) {
                        SessionStorage.saveEssentialPass(this@MainActivity, passModel)
                    }
                }
            }
        }
    }

    private fun goToApplyPassActivity(reasonType: String)
    {
        val intent = Intent(this, ApplyPassActiivity::class.java)
        intent.putExtra("type", reasonType)
        startActivity(intent)
    }

    private fun checkValidityForPass(reasonType: String) {
        var p1 : PassModel = PassModel()
        var p2 : PassModel
        if (list.size==1) {
            p1 = list[0]
        }
        if (list.size==2)
        {
            p2 = list[1]
            if (p1.validityTimeStamp<=p2.validityTimeStamp)
            {
                list.remove(p1)
            }else{
                list.remove(p2)
            }
        }
        if (list.size==0)
        {
            dismissDialog()
            goToApplyPassActivity(reasonType)
        }else {
                var passModel = list[0]
                val key :String = if (passModel.isEssential()) "essentialNextPassAfter" else "emergencyNextPassAfter"
                val daysInSeconds: Long? = remoteConfig?.getLong(key)
                Log.d(TAG, daysInSeconds.toString())
                val diff: Long =
                    (System.currentTimeMillis() / 1000L) - (passModel.createdTimeStamp)
                Log.d(
                    TAG,
                    (System.currentTimeMillis() / 1000L).toString() + "\n" + passModel.createdTimeStamp.toString() + "\n" + diff.toString()
                )
                if (diff > daysInSeconds!!) {
                    Log.d(TAG, "1")
                    dismissDialog()
                    val intent = Intent(this, ApplyPassActiivity::class.java)
                    intent.putExtra("type", reasonType)
                    startActivity(intent)
                } else {
                    if (passModel.userId == firebaseUser?.uid) {
                        Log.d(TAG, "You cannot apply for pass")
                        showSnackBar(viewPassButton, "You cannot apply for pass again within 24 hours", reasonType)
                        dismissDialog()
                    } else {
                        Log.d(TAG, "Someone from same address applied for pass")
                            showSnackBar(
                                viewPassButton,
                                "Someone from same address or vehicle no. applied for pass",
                                reasonType
                            )
                    }
                    dismissDialog()
                }
            }
    }

    private fun canApplyEssentialPass():Boolean
    {
        val passModel: PassModel = SessionStorage.getEssentialPass(this@MainActivity) ?: return true
        if (passModel.isValidPass()) {
            return false
        }
        return true
    }

    private fun canApplyEmergencyPass():Boolean
    {
        val passModel = SessionStorage.getEmergencyPass(this@MainActivity) ?: return true
        if (passModel.isValidPass()) {
            return false
        }
        return true
    }

}
