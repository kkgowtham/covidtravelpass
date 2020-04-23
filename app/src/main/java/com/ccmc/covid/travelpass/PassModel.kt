package com.ccmc.covid.travelpass

import java.text.SimpleDateFormat
import java.util.*

data class PassModel(
    var name:String="",
    var phoneNumber:String="",
    var address : String="",
    val type: String="",
    val description : String="",
    val createdTimeStamp: Long=0,
    val validityTimeStamp: Long=0,
    val vehicleNumber: String="",
    val userId: String="",
    val destination: String=""
){

    override fun toString(): String {
        return "Pass:\n"+ "Name:'$name'\n Phone Number:'$phoneNumber'\n Address:'$address'\n Type:'$type'\n Description:'$description'\n Created Time:${formatDate(createdTimeStamp)}\n Validity Time=${formatDate(validityTimeStamp)}\nVehicle Number:'$vehicleNumber', User Id:'$userId', Destination:'$destination'"
    }

    fun formatDate(a: Long): String? {
        val timeStamp = a
        val today = Date(timeStamp * 1000L)
        val formatter = SimpleDateFormat("dd-M-yyyy hh:mm:ss a")
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(today)
    }

}