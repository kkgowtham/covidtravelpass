package com.ccmc.covid.travelpass

import android.app.admin.SecurityLog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.SpinnerAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.agrawalsuneet.dotsloader.loaders.LazyLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_details.*
import kotlinx.android.synthetic.main.alert_dialog_cusotm_layout.view.*

class UserDetailsActivity : AppCompatActivity() {

    var fireBaseUser : FirebaseUser? = null
    val TAG : String = this.javaClass.simpleName
    lateinit var alertDialog:AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        fireBaseUser = FirebaseAuth.getInstance().currentUser!!
        showDialog()
        val taluks = resources.getStringArray(R.array.taluk)
        val adapter : SpinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,taluks)
        talukSpinner.adapter = adapter
        talukSpinner.setSelection(0)
        val cityAdapter : SpinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item, arrayOf("Coimbatore"))
        citySpinner.adapter = cityAdapter
        citySpinner.setSelection(0)
        submitButton.setOnClickListener{
            val  name = nameEditText.text.toString()
            val  addressLineText :String = addressLine.text.toString()
            val  talukSpinnerText :String = talukSpinner.selectedItem.toString()
            val   citySpinnerText : String = citySpinner.selectedItem.toString()
            val vehicleRegNo: String = vehicleRegEditText.text.toString()
            if (name.isNullOrEmpty())
            {
                nameEditText.error = "Name Cannot Be Empty"
                return@setOnClickListener
            }
            if (addressLineText.isNullOrEmpty())
            {

                addressLine.error = "Address Cannot be Empty"
                return@setOnClickListener
            }
            if (vehicleRegNo.isNullOrEmpty())
            {
                vehicleRegEditText.error = "Vehicle No Cannot be Empty"
                return@setOnClickListener
            }
            val fullAddress:String = addressLineText + "," +talukSpinnerText+","+citySpinnerText
            val android_id: String = Settings.Secure.getString(contentResolver,Settings.Secure.ANDROID_ID)
            val userModel = UserModel(name,addressLineText,talukSpinnerText,citySpinnerText,vehicleRegNo,fireBaseUser?.phoneNumber.toString(),fullAddress.trim(),fireBaseUser!!.uid,android_id)
            SessionStorage.saveUser(userModel,this)
            FirebaseFirestore.getInstance().collection("users")
                .document(fireBaseUser!!.uid).set(userModel).addOnCompleteListener{
                   if(it.isSuccessful)
                   {
                       val up = UserProfileChangeRequest.Builder()
                           .setDisplayName(userModel.name).build()
                       fireBaseUser?.updateProfile(up)?.addOnCompleteListener{
                           val intent = Intent(this,MainActivity::class.java)
                           intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                           startActivity(intent)
                       }

                   }else{
                       Log.d(TAG,it.exception?.message.toString())
                   }
               }
        }
    }

    private fun showDialog(){
        alertDialog= AlertDialog.Builder(this).create()
        val view: View = LayoutInflater.from(this).inflate(R.layout.alert_dialog_cusotm_layout,null,false)
        alertDialog.setView(view)
        view.gotItButton.setOnClickListener {
            alertDialog.dismiss()
        }
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
