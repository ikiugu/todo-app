package com.task.noteapp.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private const val TAG = "PreferencesManager"

enum class SortPriority { BY_TITLE, BY_CREATED_DATE, BY_UPDATED_DATE }

data class FilterPreferences(val sortPriority: SortPriority, val hideCompleted: Boolean)

class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.createDataStore("user_preferences")

    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortPriority.valueOf(
                preferences[PreferencesKeys.SORT_PRIORITY] ?: SortPriority.BY_TITLE.name
            )
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: true
            FilterPreferences(sortOrder, hideCompleted)
        }

    suspend fun updateSortPriority(sortPriority: SortPriority) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_PRIORITY] = sortPriority.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
        }
    }

    private object PreferencesKeys {
        val SORT_PRIORITY = preferencesKey<String>("sort_priority")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }
}