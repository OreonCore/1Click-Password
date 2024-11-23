package com.oneclick.password

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SecureClipboardService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val password = intent?.getStringExtra("PASSWORD") ?: return START_NOT_STICKY
        copyToClipboard(password)
        return START_NOT_STICKY
    }

    private fun copyToClipboard(password: String) {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Generated Password", password)
            clipboard.setPrimaryClip(clip)
            Log.d("SecureClipboardService", "Password copied securely.")
        } catch (e: Exception) {
            Log.e("SecureClipboardService", "Failed to copy to clipboard: ${e.message}")
        }

    }

    override fun onBind(intent: Intent?): IBinder? = null

}
