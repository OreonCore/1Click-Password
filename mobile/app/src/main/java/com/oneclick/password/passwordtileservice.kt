package com.oneclick.password

import android.content.Intent
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class PasswordTileService : TileService() {

    override fun onTileAdded() {
        qsTile.state = Tile.STATE_ACTIVE
        qsTile.label = "Generate Password"
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_generate)
        updateTile()
    }

    override fun onClick() {
        val password = PasswordGenerator.generatePassword()

        // Запуск SecureClipboardService для копіювання пароля
        val intent = Intent(this, SecureClipboardService::class.java)
        intent.putExtra("PASSWORD", password)
        startForegroundService(intent)  // Використовуємо startForegroundService()

        // Змінити статус плитки
        qsTile.state = Tile.STATE_INACTIVE
        updateTile()
    }

    private fun updateTile() {
        qsTile.updateTile()
    }
}



