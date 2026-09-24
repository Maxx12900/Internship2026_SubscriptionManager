package com.example.subscriptionmanager.data

import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.model.Subscription

fun SubscriptionEntity.toSubscription(): Subscription = Subscription(
    name = name,
    packageName = packageName,
    price = price,
    status = status
)