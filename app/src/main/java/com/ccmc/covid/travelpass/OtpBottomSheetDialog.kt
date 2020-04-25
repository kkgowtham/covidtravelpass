package com.ccmc.covid.travelpass

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_phone_verification.view.*
import kotlinx.android.synthetic.main.otp_bottom_sheet_layout.*
import kotlinx.android.synthetic.main.otp_bottom_sheet_layout.view.*
import java.util.concurrent.TimeUnit
import kotlin.math.log

class OtpBottomSheetDialog : BottomSheetDialogFragment() {

    lateinit var firebaseAuth: FirebaseAuth
    private val TAG : String = this.javaClass.simpleName
    lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var storedVerificationId : String
    lateinit var a:Activity
    lateinit var c:Context
     lateinit var  v : View
     var smsCode :String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         var view:View=LayoutInflater.from(context).inflate(R.layout.otp_bottom_sheet_layout,null,false)
        v=view
        firebaseAuth=FirebaseAuth.getInstance()
        var number = arguments?.getString("phoneno")
        number?.let {
            view.numberDialog.text = number
            sendOtp(it)
        }
        view.verifyOTPButton.setOnClickListener { v ->
            if (::storedVerificationId.isInitialized) {
                if (!smsCode.isNullOrEmpty())
                {
                    val credential = PhoneAuthProvider.getCredential(storedVerificationId, smsCode.toString())
                    signInWithPhoneAuthCredential(credential)
                }else {
                    val otp: String = view.editTextOtp.text.toString().trim()
                    Log.d(TAG, otp)
                    val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
                    signInWithPhoneAuthCredential(credential)
                }
            }
        }
        view.changeNumberTextView.setOnClickListener {
            this.dismiss()
        }
        view.otpResendTextView.setOnClickListener {
            arguments?.getString("phoneno")?.let {
                    it1 -> resendOtp(it1)
            Toast.makeText(c,"Resent OTP",Toast.LENGTH_SHORT).show()
            }

        }
        return view
    }

    private fun resendOtp(phoneNumber: String) {
        if (::resendToken.isInitialized) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                a,
                callbacks,
                resendToken
            )
        }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    // ...
                    if(user?.displayName.isNullOrEmpty())
                    {
                        this.dismiss()
                        val intent=Intent(context,UserDetailsActivity::class.java)
                        intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        intent.putExtra("reload",true)
                        startActivity(intent)
                    }else {
                        reloadPass("Essential")
                        reloadPass("Emergency")
                        saveUser(user!!.uid)
                    }
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Log.d(TAG, (task.exception as FirebaseAuthInvalidCredentialsException).message.toString())
                    }
                }
            }
    }

    private fun sendOtp(phoneNumber:String) {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                smsCode = credential.smsCode
                v.editTextOtp.setText(credential.smsCode)
                v.verifyOTPButton.performClick()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                Log.d(TAG,e.message.toString())
               // showSnackBar(v.rootView,e.message)
                Toast.makeText(context,"Error Occurred",Toast.LENGTH_LONG).show()
                dismiss()
                //Toast.makeText(requireContext(),e.message,Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                verifyOTPButton.isEnabled = true
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
            Log.d(TAG,"Yes")
        }
    }

    private fun showSnackBar(root: View?, snackTitle: String?) {
        val snackbar = Snackbar.make(root!!, snackTitle!!, Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("OK") {
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
                .whereEqualTo("userId", firebaseAuth.currentUser?.uid)
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
                        SessionStorage.saveEmergencyPass(this.a, passModel)
                    }
                    if (passModel != null && passModel.isEssential()) {
                        SessionStorage.saveEssentialPass(this.a, passModel)
                    }
                    Log.d(TAG,passModel.toString())
                }
            }
        }
    }
}