package com.example.subscriptionmanager.data.entities

// Category.order are the flags representing
data class Category(val order:Long) {
    val none: Long= 0 // So that there are none added by mistake and given 0 as a value
    val streaming: Long = 1L
    val productivity: Long = 1L shl 2
    val games: Long = 1L shl 3
    val food: Long = 1L shl 4
    val all: Long = 1L shl 64
}