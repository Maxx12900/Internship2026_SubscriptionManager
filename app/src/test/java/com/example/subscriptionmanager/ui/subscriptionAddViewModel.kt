package com.example.subscriptionmanager.ui.subscriptionAdd

import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SubscriptionAddViewModelTest {

    @Test
    fun formState_defaultValuesAreInitializedCorrectly() {
        val defaultState = SubscriptionFormState()

        assertEquals("", defaultState.name)
        assertEquals(Category.NONE, defaultState.category)
        assertEquals("", defaultState.price)
        assertEquals(BillingPeriod.MONTHLY, defaultState.billingPeriod)
        assertNull(defaultState.error)
    }

    @Test
    fun priceParsing_cleanPriceStripsDollarAndCommaCorrectly() {
        val rawPrice = "$14,99"
        val cleanPrice = rawPrice.trim().replace("$", "").replace(",", ".")
        val parsedDouble = cleanPrice.toDoubleOrNull()

        assertNotNull(parsedDouble)
        assertEquals(14.99, parsedDouble!!, 0.01)
    }

    @Test
    fun updateName_updatesFormStateNameAndClearsError() {
        val formState = SubscriptionFormState(name = "", error = "Enter a subscription name")
        val updatedState = formState.copy(name = "Spotify Premium", error = null)

        assertEquals("Spotify Premium", updatedState.name)
        assertNull(updatedState.error)
    }
}