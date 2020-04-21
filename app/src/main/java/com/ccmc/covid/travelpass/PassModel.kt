package com.ccmc.covid.travelpass

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
)