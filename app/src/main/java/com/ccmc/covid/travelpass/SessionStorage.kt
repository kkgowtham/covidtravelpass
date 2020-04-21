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
        Log.d(TAG,userModel.toString())
        val gson = Gson()
        val json = gson.toJson(userModel).toString()
        Log.d(TAG,json)
        prefsEditor.putString("user", json)
        Log.d(TAG,prefsEditor.commit().toString())
    }

    fun getUser(activity: Activity):UserModel{
        var mPrefs: SharedPreferences = activity.getSharedPreferences(PREF_NAME,MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs.getString("user", null)
        Log.d(TAG,json)
        val user: UserModel = gson.fromJson<UserModel>(json, UserModel::class.java)
        return user
    }
}