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
import java.util.concurrent.atomic.AtomicBoolean


class MainActivity : AppCompatActivity() {

     lateinit var firebaseAuth:FirebaseAuth
     var firebaseUser: FirebaseUser? = null
    var TAG:String = this.javaClass.simpleName
    var remoteConfig: FirebaseRemoteConfig? =null
    lateinit var alertDialog:AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseUser= firebaseAuth.currentUser
        remoteConfig= FirebaseRemoteConfig.getInstance()
        setRemoteConfig()
        saveUserData()
        emergencyButton.setOnClickListener {
           showDialog()
            checkForPass("Emergency")
        }
        essentialsButton.setOnClickListener{
            showDialog()
            checkForPass("Essential")
        }
        viewPassButton.setOnClickListener {
            if(SessionStorage.getPass(this@MainActivity)==null)
            {
                showSnackBar(viewPassButton,"No Pass Found")
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
                    var userModel = it.toObject(UserModel::class.java)
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
        val phoneNumber:String = firebaseUser?.phoneNumber.toString()
        val collectionReference:CollectionReference= FirebaseFirestore.getInstance().collection("requests")
        val query : Query = collectionReference.whereEqualTo("address",
            SessionStorage.getUser(this)?.fullAddress
        )
            .orderBy("createdTimeStamp",Query.Direction.DESCENDING)
            .limit(1)
        val list = ArrayList<PassModel>()
        val succeeded = AtomicBoolean()
        query.get().addOnCompleteListener{
            if (it.isSuccessful)
            {
             val value = it.result

                if (value != null) {
                    if (value.size()==0)
                    {
                        dismissDialog()
                        val intent = Intent(this, ApplyPassActiivity::class.java)
                        intent.putExtra("type", reasonType)
                        startActivity(intent)
                    }else {
                        for (a in value) {
                            val passModel: PassModel = a.toObject(PassModel::class.java)
                            list.add(passModel)
                            val daysInSeconds: Long? = remoteConfig?.getLong("days")
                            Log.d(TAG, daysInSeconds.toString())
                            val diff: Long =
                                (System.currentTimeMillis() / 1000L) - (passModel.createdTimeStamp)
                            Log.d(
                                TAG,
                                (System.currentTimeMillis() / 1000L).toString() + "\n" + passModel.createdTimeStamp.toString() + "\n" + diff.toString()
                            )
                            if (diff > daysInSeconds!!) {
                                succeeded.set(true)
                                Log.d(TAG, "1")
                                Log.d(TAG, phoneNumber)
                                dismissDialog()
                                val intent = Intent(this, ApplyPassActiivity::class.java)
                                intent.putExtra("type", reasonType)
                                startActivity(intent)
                            } else {
                                if (passModel.userId == firebaseUser?.uid) {
                                    Log.d(TAG, "You cannot apply for pass")
                                    showSnackBar(viewPassButton,"You cannot apply for pass again")
                                    dismissDialog()
                                } else {
                                    Log.d(TAG, "Someone from same address applied for pass")
                                    showSnackBar(viewPassButton,"Someone from same address applied for pass")
                                }
                                dismissDialog()
                                succeeded.set(false)
                            }
                        }
                    }
                }
            }else{
                Log.d(TAG,it.exception?.message!!)
                Log.d(TAG,"3")
                Toast.makeText(applicationContext,"Error Occured",Toast.LENGTH_LONG).show()
                succeeded.set(false)
                dismissDialog()
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
            ContextCompat.getColor(this, R.color.blue),
            ContextCompat.getColor(this, R.color.blue),
            ContextCompat.getColor(this, R.color.blue))
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

    private fun showSnackBar(root: View?, snackTitle: String?) {
        val snackbar = Snackbar.make(root!!, snackTitle!!, Snackbar.LENGTH_LONG)
        snackbar.setAction("OK") {
            reloadPass()
            snackbar.dismiss()
        }
        snackbar.show()
        val view = snackbar.view
        val txtv = view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        txtv.gravity = Gravity.CENTER_HORIZONTAL
    }

    private fun reloadPass() {
        val query : Query = FirebaseFirestore.getInstance().collection("requests").whereEqualTo("userId",firebaseUser?.uid)
            .orderBy("createdTimeStamp",Query.Direction.DESCENDING)
            .limit(1)
        query.get().addOnCompleteListener{
            if(it.isSuccessful) {
                val a = it.result?.documents?.size ?: return@addOnCompleteListener
                if (a==0)
                {
                    return@addOnCompleteListener
                }
                val passModel = it.result!!.documents[0].toObject(PassModel::class.java)
                if(passModel!=null)
                {
                    SessionStorage.savePass(this@MainActivity,passModel)
                }
            }
        }
    }


}
