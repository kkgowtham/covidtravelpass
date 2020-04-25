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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PassModel

        if (name != other.name) return false
        if (phoneNumber != other.phoneNumber) return false
        if (address != other.address) return false
        if (type != other.type) return false
        if (description != other.description) return false
        if (createdTimeStamp != other.createdTimeStamp) return false
        if (validityTimeStamp != other.validityTimeStamp) return false
        if (vehicleNumber != other.vehicleNumber) return false
        if (userId != other.userId) return false
        if (destination != other.destination) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + createdTimeStamp.hashCode()
        result = 31 * result + validityTimeStamp.hashCode()
        result = 31 * result + vehicleNumber.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + destination.hashCode()
        return result
    }

    public fun isValidPass():Boolean{
        return (System.currentTimeMillis() / 1000L) <= validityTimeStamp
    }

    public fun isEssential():Boolean
    {
        return type == "Essential"
    }

    public fun isEmergency():Boolean
    {
        return type == "Emergency"
    }


}