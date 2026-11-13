
package com.jarvis.assistant.data

import android.content.Context

class SettingsStore(context: Context) {

    private val preferences = context.getSharedPreferences(
        "jarvis_settings",
        Context.MODE_PRIVATE
    )

    var backendUrl: String
        get() = preferences.getString("backend_url", "") ?: ""
        set(value) {
            preferences.edit()
                .putString("backend_url", value)
                .apply()
        }

    var voiceReplies: Boolean
        get() = preferences.getBoolean("voice_replies", true)
        set(value) {
            preferences.edit()
                .putBoolean("voice_replies", value)
                .apply()
        }
}
