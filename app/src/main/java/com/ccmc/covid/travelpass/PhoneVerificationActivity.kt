package com.ccmc.covid.travelpass

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_phone_verification.*


class PhoneVerificationActivity : AppCompatActivity() {

    private val TAG : String = this.javaClass.simpleName
    private var canRegister : Boolean= false
    lateinit var  remoteDeviceId: String
    lateinit var remotePhoneno: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)
        phoneNumberEditText.setText("8883502600")
        val android_id: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Log.d(TAG,android_id)
        checkDeviceId(android_id)
        sendOtpButton.setOnClickListener { v ->
            if (phoneNumberEditText.text.trim().startsWith("+91"))
            {
                phoneNumberEditText.setText(phoneNumberEditText.text.substring(3).trim())
            }
                val phoneNo = phoneNumberEditText.text.toString().trim()
                if (phoneNo.length == 10) {
                    var fullPhoneNumber = "+91" + phoneNo
                    if (!canRegister)
                    {
                        if (fullPhoneNumber != remotePhoneno)
                        {
                            Toast.makeText(this,"This device already registered with Another Number",Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }
                    }
                    showBottomSheetFragment(fullPhoneNumber)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Invalid Phone No",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun checkDeviceId(androidId: String){
        FirebaseFirestore.getInstance().collection("users").whereEqualTo("deviceId",androidId)
            .get().addOnCompleteListener{
                if (it.isSuccessful)
                {
                    val querySnapshot = it.result
                    if (querySnapshot==null||querySnapshot.isEmpty||querySnapshot.size()==0)
                    {
                        canRegister = true
                        return@addOnCompleteListener
                    }
                    val list = querySnapshot.documents
                    val userModel:UserModel = list[0].toObject(UserModel::class.java)!!
                    remoteDeviceId = userModel.deviceId
                    remotePhoneno = userModel.phoneNumber
                }else{
                    canRegister = true
                }
            }
    }

    private fun showBottomSheetFragment( phoneno:String){
        val otpBottomSheetDialog = OtpBottomSheetDialog()
        otpBottomSheetDialog.isCancelable = false
        var bundle =  Bundle()
        bundle.putString("phoneno",phoneno)
        otpBottomSheetDialog.arguments = bundle
        otpBottomSheetDialog.show(supportFragmentManager,"Dialog")
    }

}