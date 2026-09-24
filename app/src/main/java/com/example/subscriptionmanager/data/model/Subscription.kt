package com.example.subscriptionmanager.data.model

data class Subscription(
    val name : String           = "default name",
    val packageName : String?   = null,
    val price : Double          = 0.00,
    val status : Boolean        = true
)