package com.example.cityflowbkk.features.route

import android.content.Context

class ArrivalAlertSettingsRepository(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean {
        return preferences.getBoolean(KEY_ENABLED, DEFAULT_ENABLED)
    }

    fun thresholdMeters(): Int {
        return preferences.getInt(KEY_THRESHOLD_METERS, DEFAULT_THRESHOLD_METERS)
            .coerceIn(MIN_THRESHOLD_METERS, MAX_THRESHOLD_METERS)
    }

    fun setEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun setThresholdMeters(thresholdMeters: Int) {
        preferences.edit()
            .putInt(
                KEY_THRESHOLD_METERS,
                thresholdMeters.coerceIn(MIN_THRESHOLD_METERS, MAX_THRESHOLD_METERS),
            )
            .apply()
    }

    companion object {
        const val DEFAULT_ENABLED = true
        const val DEFAULT_THRESHOLD_METERS = 300
        const val MIN_THRESHOLD_METERS = 100
        const val MAX_THRESHOLD_METERS = 1000

        private const val PREFERENCES_NAME = "arrival_alert_settings"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_THRESHOLD_METERS = "threshold_meters"
    }
}
