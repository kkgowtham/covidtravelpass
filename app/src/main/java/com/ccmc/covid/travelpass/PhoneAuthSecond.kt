package com.ccmc.covid.travelpass

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneAuthSecond{

    val TAG : String = this.javaClass.simpleName
    lateinit var authsecond:FirebaseAuth
    lateinit var secondary:FirebaseApp
    lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var storedVerificationId : String
    lateinit var applicationContext: Context
    lateinit var activity: Activity
    constructor(applicationContext:Context,activity: Activity)
    {
        this.applicationContext=applicationContext
        this.activity=activity
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        authsecond.signInWithCredential(credential).addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                Log.d(TAG,"User Created")
            }else{
                Log.d(TAG,task.exception?.message!!)
            }
        }
    }

    private fun sendOtp(phoneNumber: String) {
        var firebaseOptions : FirebaseOptions = FirebaseOptions.Builder()
            .setProjectId("eticketing-3cf5b")
            .setApplicationId("1:377297789439:android:0f0e1aa60b4689df")
            .setApiKey("AIzaSyBLVSDo420GNyVO0abM05_0pgGhOpdtL1s")
            .build()
        FirebaseApp.initializeApp(applicationContext,firebaseOptions,"second")
        secondary = FirebaseApp.getInstance("second")
        authsecond  = FirebaseAuth.getInstance(secondary)
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
            activity, // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks
    }

}