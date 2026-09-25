package com.example.subscriptionmanager.data.entities

// Category.order are the flags representing
enum class Category(val value:Long) {
    NONE(0L),
    STREAMING(1L shl 0),
    PRODUCTIVITY(1L shl 1),
    GAMES(1L shl 2),
    FOOD(1L shl 3)
}

infix fun Category.or(other: Category): Long = this.value or other.value

fun Set<Category>.toBitmask(): Long = fold(0L) { bitmask, category -> bitmask or category.value}