package com.oneclick.password

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.service.quicksettings.TileService
import java.util.Locale

private const val PREFS_NAME = "password_prefs"
private const val KEY_PASSWORD_LENGTH = "password_length"

class TileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()

        val context = getLocalizedContext()

        qsTile?.apply {
            icon = Icon.createWithResource(this@TileService, R.drawable.ic_generate)
            label = context.getString(R.string.qs_generate)
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()

        val context = getLocalizedContext()

        // Зчитуємо довжину з налаштувань
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val length = prefs.getInt(KEY_PASSWORD_LENGTH, 24)

        // Генеруємо пароль при натисканні
        val password = PasswordGenerator.generatePassword(length)

        // Запускаємо сервіс для копіювання пароля
        val intent = Intent(this, SecureClipboardService::class.java).apply {
            putExtra("PASSWORD", password)
        }
        startService(intent)

        // Показуємо коротке повідомлення в плитці
        qsTile?.apply {
            label = context.getString(R.string.qs_copied)
            updateTile()
        }
    }

    // ⬇️ Повертає Context з мовою, яка збережена в SharedPreferences
    private fun getLocalizedContext(): Context {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "uk") ?: "uk"
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        return createConfigurationContext(config)
    }
}
