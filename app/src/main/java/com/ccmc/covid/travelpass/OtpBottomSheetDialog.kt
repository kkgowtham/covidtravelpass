package com.ccmc.covid.travelpass

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.otp_bottom_sheet_layout.view.*
import java.util.concurrent.TimeUnit

class OtpBottomSheetDialog : BottomSheetDialogFragment() {

    lateinit var firebaseAuth: FirebaseAuth
    private val TAG : String = this.javaClass.simpleName
    lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var storedVerificationId : String
    lateinit var a:Activity
    lateinit var c:Context
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view:View=LayoutInflater.from(context).inflate(R.layout.otp_bottom_sheet_layout,null,false)
        firebaseAuth=FirebaseAuth.getInstance()
        arguments?.getString("phoneno")?.let { sendOtp(it) }
        view.editTextOtp.setText("123456")
        view.verifyOTPButton.setOnClickListener { v ->
            val otp:String= view.editTextOtp.text.toString().trim()
            val credential = PhoneAuthProvider.getCredential(storedVerificationId,otp)
            signInWithPhoneAuthCredential(credential)
        }
        return view
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    // ...
                    this.dismiss()
                    if(user?.displayName.isNullOrEmpty())
                    {
                        val intent=Intent(context,UserDetailsActivity::class.java)
                        intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }else {
                        saveUser(user!!.uid)
                    }
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    private fun sendOtp(phoneNumber:String) {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
            }
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            activity!!, // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks


    }

    private fun saveUser(uid: String)
    {
         FirebaseFirestore.getInstance().collection("users")
            .document(uid).get().addOnSuccessListener {
                 var userModel : UserModel = it.toObject(UserModel::class.java)!!
                 a.let { it1 -> SessionStorage.saveUser(userModel, it1)
                     val intent = Intent(c, MainActivity::class.java)
                     intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                     c.startActivity(intent)
                 }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        c = context
        if (context is Activity)
        {
            a = context
        }
    }
}