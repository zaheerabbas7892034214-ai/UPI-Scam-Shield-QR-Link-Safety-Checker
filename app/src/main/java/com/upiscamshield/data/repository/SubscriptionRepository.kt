package com.upiscamshield.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.upiscamshield.data.model.SubscriptionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "subscription_prefs")

class SubscriptionRepository(private val context: Context) {
    
    private object PreferencesKeys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val FREE_SCANS_REMAINING = intPreferencesKey("free_scans_remaining")
        val SUBSCRIPTION_END_DATE = longPreferencesKey("subscription_end_date")
    }
    
    val subscriptionState: Flow<SubscriptionState> = context.dataStore.data.map { prefs ->
        SubscriptionState(
            isPremium = prefs[PreferencesKeys.IS_PREMIUM] ?: false,
            freeScansRemaining = prefs[PreferencesKeys.FREE_SCANS_REMAINING] ?: 3,
            subscriptionEndDate = prefs[PreferencesKeys.SUBSCRIPTION_END_DATE]
        )
    }
    
    suspend fun setSubscriptionActive(active: Boolean, endDate: Long? = null) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_PREMIUM] = active
            if (endDate != null) {
                prefs[PreferencesKeys.SUBSCRIPTION_END_DATE] = endDate
            }
        }
    }
    
    suspend fun decrementFreeScans() {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.FREE_SCANS_REMAINING] ?: 3
            prefs[PreferencesKeys.FREE_SCANS_REMAINING] = maxOf(0, current - 1)
        }
    }
    
    suspend fun resetFreeScans() {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.FREE_SCANS_REMAINING] = 3
        }
    }
}
