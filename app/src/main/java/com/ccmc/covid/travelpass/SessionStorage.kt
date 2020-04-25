package com.ccmc.covid.travelpass

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson


object SessionStorage{

    const val PREF_NAME = "Covid"

    val TAG : String = this.javaClass.simpleName
    fun saveUser(userModel: UserModel,activity: Activity)
    {
        var mPrefs: SharedPreferences = activity.getSharedPreferences(PREF_NAME,MODE_PRIVATE)
        val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
       // Log.d(TAG,userModel.toString())
        val gson = Gson()
        val json = gson.toJson(userModel).toString()
       // Log.d(TAG,json)
        prefsEditor.putString("user", json)
        prefsEditor.apply()
        //Log.d(TAG,prefsEditor.commit().toString())
    }

    fun getUser(activity: Activity): UserModel? {
        var mPrefs: SharedPreferences = activity.getSharedPreferences(PREF_NAME,MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs.getString("user", null)
       // Log.d(TAG,json)
        if (json.isNullOrEmpty())
        {
            return null
        }
        val user: UserModel = gson.fromJson<UserModel>(json, UserModel::class.java)
        return user
    }

    fun saveEmergencyPass(activity: Activity,passModel: PassModel)
    {
        var mPrefs: SharedPreferences = activity.getSharedPreferences(PREF_NAME,MODE_PRIVATE)
        val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
        Log.d(TAG,passModel.toString())
        val gson = Gson()
        val json = gson.toJson(passModel).toString()
        //Log.d(TAG,json)
        prefsEditor.putString("emergencyPass", json)
        prefsEditor.apply()
        //Log.d(TAG,prefsEditor.commit().toString())
    }

    fun getEmergencyPass(activity: Activity): PassModel?
    {
        var mPrefs: SharedPreferences = activity.getSharedPreferences(PREF_NAME,MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs.getString("emergencyPass", null)
        //Log.d(TAG,json)
        if (json.isNullOrEmpty())
        {
            return null
        }
        val pass: PassModel = gson.fromJson<PassModel>(json, PassModel::class.java)
        return pass
    }

    fun saveEssentialPass(activity: Activity,passModel: PassModel)
    {
        var mPrefs: SharedPreferences = activity.getSharedPreferences(PREF_NAME,MODE_PRIVATE)
        val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
        Log.d(TAG,passModel.toString())
        val gson = Gson()
        val json = gson.toJson(passModel).toString()
        //Log.d(TAG,json)
        prefsEditor.putString("essentialPass", json)
        prefsEditor.apply()
        //Log.d(TAG,prefsEditor.commit().toString())
    }

    fun getEssentialPass(activity: Activity): PassModel?
    {
        var mPrefs: SharedPreferences = activity.getSharedPreferences(PREF_NAME,MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs.getString("essentialPass", null)
        //Log.d(TAG,json)
        if (json.isNullOrEmpty())
        {
            return null
        }
        val pass: PassModel = gson.fromJson<PassModel>(json, PassModel::class.java)
        return pass
    }
}