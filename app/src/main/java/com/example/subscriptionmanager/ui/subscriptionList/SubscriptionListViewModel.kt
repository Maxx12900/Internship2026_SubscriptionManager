package com.example.subscriptionmanager.ui.subscriptionList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.data.model.Subscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SubscriptionListViewModel : ViewModel() {

    private val _subscriptions = MutableStateFlow(
        // TODO: change to db entries
        listOf(
            Subscription("Netflix", price = "15.99", packageName = "com.netflix.mediaclient", category = "Entertainment"),
            Subscription("Spotify", price = "9.99",  packageName = "com.spotify.music"),
            Subscription("YouTube", price = "7.99",  packageName = "com.google.android.youtube")
        )
    )

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())

    fun toggleCategory(category: String) {
        _selectedCategories.value = if (category in _selectedCategories.value) {
            _selectedCategories.value - category
        } else {
            _selectedCategories.value + category
        }
    }

    val subscriptions: StateFlow<List<Subscription>> =
        combine(_subscriptions, _selectedCategories) { subs, categories ->
            if (categories.isEmpty()) subs
            else subs.filter { it.category in categories }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCategories: StateFlow<Set<String>> = _selectedCategories
}