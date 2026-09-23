package com.example.subscriptionmanager.data.entities

// Category.order are the flags representing
enum class Category(val value:Long) {
    NONE(0), // So that there are none added by mistake and given 0 as a value
    STREAMING(1L),
    PRODUCTIVITY(1L shl 2),
    GAMES(1L shl 3),
    FOOD(1L shl 4),
    ALL(1L shl 64)
}

infix fun Category.or(other: Category): Long = this.value or other.value