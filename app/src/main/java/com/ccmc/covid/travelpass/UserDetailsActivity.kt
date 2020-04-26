package com.ccmc.covid.travelpass

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    lateinit var loadingDialog : AlertDialog
    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        fireBaseUser = FirebaseAuth.getInstance().currentUser!!
        showDialog()
        vehicleRegEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
            {
                ti_layout.hint = "Vehicle Registration No"
            }else{
                if (vehicleRegEditText.length()>0)
                {
                    ti_layout.hint = "Vehicle Registration No"
                }else {
                    ti_layout.hint = "Vehicle Registration No(Eg:TN01AA0001)"
                }
            }
        }
        addressLine.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
            {
                ti_layout_address.hint = "Full address"
            }else{
                if (addressLine.length()>0)
                {
                    ti_layout_address.hint = "Full address"
                }else{
                    ti_layout_address.hint = "Full Address as per License"
                }
            }
        }
/*        val taluks = resources.getStringArray(R.array.taluk)
        val adapter : SpinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,taluks)
        talukSpinner.adapter = adapter
        talukSpinner.setSelection(0)
        val cityAdapter : SpinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item, arrayOf("Coimbatore"))
        citySpinner.adapter = cityAdapter
        citySpinner.setSelection(0)*/
        vehicleRegEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        submitButton.setOnClickListener{
            val  name = nameEditText.text.toString()
            val  addressLineText :String = addressLine.text.toString().toUpperCase()
/*            val  talukSpinnerText :String = talukSpinner.selectedItem.toString()
            val   citySpinnerText : String = citySpinner.selectedItem.toString()*/
            val vehicleRegNo: String = vehicleRegEditText.text.toString().toUpperCase()
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
            if (vehicleRegNo.length!=10)
            {
                vehicleRegEditText.error = "Please enter in correct format"
                return@setOnClickListener
            }
            if(vehicleRegNo.contains(" "))
            {
                vehicleRegEditText.error = "Please enter without spaces"
                return@setOnClickListener
            }

            val fullAddress:String = addressLineText
            val android_id: String = Settings.Secure.getString(contentResolver,Settings.Secure.ANDROID_ID)
            val userModel = UserModel(name,addressLineText,vehicleRegNo,fireBaseUser?.phoneNumber.toString(),fullAddress.trim(),fireBaseUser!!.uid,android_id)
            SessionStorage.saveUser(userModel,this)
            showLoadingDialog()
            FirebaseFirestore.getInstance().collection("users")
                .document(fireBaseUser!!.uid).set(userModel).addOnCompleteListener{
                   if(it.isSuccessful)
                   {
                       val up = UserProfileChangeRequest.Builder()
                           .setDisplayName(userModel.name).build()
                       fireBaseUser?.updateProfile(up)?.addOnCompleteListener{
                           dismissLoadingDialog()
                           val intent = Intent(this,MainActivity::class.java)
                           intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                           startActivity(intent)
                       }

                   }else{
                       Toast.makeText(applicationContext,"Error Occured",Toast.LENGTH_LONG).show()
                       dismissLoadingDialog()
                       Log.d(TAG,it.exception?.message.toString())
                   }
               }
        }
    }

    private fun showDialog(){
        alertDialog= AlertDialog.Builder(this).create()
        val view: View = LayoutInflater.from(this).inflate(R.layout.alert_dialog_cusotm_layout,null,false)
        alertDialog.setView(view)
        view.checkBoxAgree.setOnCheckedChangeListener{ compoundButton: CompoundButton, b: Boolean ->
            view.gotItButton.isEnabled = b
        }
        view.gotItButton.setOnClickListener {
           dismissDialog()
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

    private fun showLoadingDialog(){
        loadingDialog=AlertDialog.Builder(this).create()
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
        loadingDialog.setView(view)
        loadingDialog.setCancelable(false)
        loadingDialog.show()
        Log.d(TAG,"Dialog shown")
    }

    private fun  dismissLoadingDialog()
    {
        loadingDialog.let {
            if (it.isShowing) {
                it.dismiss()
                Log.d(TAG,"Dismissed")
            }
        }
    }


}
