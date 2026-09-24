package com.example.subscriptionmanager.ui.subscriptionList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.SortBy
import com.example.subscriptionmanager.data.model.Subscription
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import com.example.subscriptionmanager.data.toSubscription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class SubscriptionListViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _selectedCategories = MutableStateFlow<Set<Category>>(emptySet())
    val selectedCategories: StateFlow<Set<Category>> = _selectedCategories
    private val _sortingOption = MutableStateFlow<SortBy>(SortBy.NAME)
    val sortingOption: StateFlow<SortBy> = _sortingOption
    private val _isAscending = MutableStateFlow<Boolean>(true)
    val isAscending: StateFlow<Boolean> = _isAscending

    fun toggleCategory(category: Category) {
        _selectedCategories.update { current ->
            if (category in current) current - category else current + category
        }
    }

    // TODO: move filtering to the db, viewmodel just imports the data
    @OptIn(ExperimentalCoroutinesApi::class)
    val subscriptions: StateFlow<List<Subscription>> =
         combine(_selectedCategories, _sortingOption) { categories, sort -> categories to sort }
             .flatMapLatest { (categories, sort) ->
                 subscriptionRepository.getSubscriptions(categories, sort)
                     .map {entities -> entities.map {it.toSubscription() }}
             }
             .stateIn(
                 scope = viewModelScope,
                 started = SharingStarted.WhileSubscribed(5000),
                 initialValue = emptyList()
             )

}