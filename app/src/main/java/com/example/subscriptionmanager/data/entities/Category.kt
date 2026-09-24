package com.example.subscriptionmanager.data.entities

// Category.order are the flags representing
enum class Category(val value:Long) {
    STREAMING(1L),
    PRODUCTIVITY(1L shl 2),
    GAMES(1L shl 3),
    FOOD(1L shl 4),
}

infix fun Category.or(other: Category): Long = this.value or other.value

fun Set<Category>.toBitmask(): Long = fold(0) { bitmask, category -> bitmask + category.value}