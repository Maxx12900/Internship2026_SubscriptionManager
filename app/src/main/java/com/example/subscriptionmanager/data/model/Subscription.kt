package com.example.subscriptionmanager.data.model

data class Subscription(
    val name : String           = "default name",
    val packageName : String?   = null,
    val category : String     = "default category",
    val price : String          = "default price",
    val isActive : Boolean      = true
)