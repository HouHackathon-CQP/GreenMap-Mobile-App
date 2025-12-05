/*
 * Copyright 2025 HouHackathon-CQP
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.houhackathon.greenmap_app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

data class UserInfo(
    val email: String?,
    val token: String?,
    val fullName: String?,
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val keyEmail = stringPreferencesKey("email")
    private val keyToken = stringPreferencesKey("token")
    private val keyFullName = stringPreferencesKey("full_name")

    val userInfoFlow: Flow<UserInfo> = context.dataStore.data.map { prefs ->
        UserInfo(
            email = prefs[keyEmail],
            token = prefs[keyToken],
            fullName = prefs[keyFullName],
        )
    }

    suspend fun saveUser(email: String?, token: String?, fullName: String?) {
        context.dataStore.edit { prefs ->
            if (email != null) prefs[keyEmail] = email else prefs.remove(keyEmail)
            if (token != null) prefs[keyToken] = token else prefs.remove(keyToken)
            if (fullName != null) prefs[keyFullName] = fullName else prefs.remove(keyFullName)
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}
