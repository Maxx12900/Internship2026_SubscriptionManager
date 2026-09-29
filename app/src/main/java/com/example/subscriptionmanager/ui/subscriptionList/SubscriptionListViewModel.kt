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
    // Actual vals that keep the query settings
    private val _selectedCategories = MutableStateFlow<Set<Category>>(emptySet())
    val selectedCategories: StateFlow<Set<Category>> = _selectedCategories
    private val _sortingOption = MutableStateFlow<SortBy>(SortBy.NAME)
    val sortingOption: StateFlow<SortBy> = _sortingOption
    private val _isAscending = MutableStateFlow<Boolean>(true)
    val isAscending: StateFlow<Boolean> = _isAscending

    // vals that are used in filter&sort dialog, applied only on "Done"
    private val _draftCategories = MutableStateFlow<Set<Category>>(emptySet())
    val draftCategories = _draftCategories
    private val _draftSort= MutableStateFlow<SortBy>(SortBy.NAME)
    val draftSort = _draftSort
    private val _draftAsc = MutableStateFlow<Boolean>(true)
    val draftAsc = _draftAsc


    fun seedDialog() {
        _draftCategories.value = _selectedCategories.value
        _draftSort.value = _sortingOption.value
        _draftAsc.value = _isAscending.value
    }

    fun toggleCategory(category: Category) {
        _draftCategories.update { current ->
            if (category in current) current - category else current + category
        }
    }

    fun setSortingOption(value: SortBy) {
        _draftSort.value = value
    }

    fun setAscending(value: Boolean) {
        _draftAsc.value = value
    }

    fun resetDialog() {
        _draftCategories.value = emptySet()
        _draftSort.value = SortBy.NAME
        _draftAsc.value = true
    }

    fun applyDialog() {
        _selectedCategories.value = _draftCategories.value
        _sortingOption.value = _draftSort.value
        _isAscending.value = _draftAsc.value
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val subscriptions: StateFlow<List<Subscription>> =
         combine(_selectedCategories, _sortingOption, _isAscending) { c, s, asc -> Triple(c, s, asc) }
             .flatMapLatest { (categories, sort, ascending) ->
                 subscriptionRepository.getSubscriptions(categories, sort, ascending)
                     .map{ entities -> entities.map { it.toSubscription() } }
             }
             .stateIn(
                 scope = viewModelScope,
                 started = SharingStarted.WhileSubscribed(5000),
                 initialValue = emptyList()
             )

}