package com.reminder.app

import android.content.*
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        when (intent.action) {
            "ACTION_AUTO_START" -> {
                prefs.edit().putBoolean("enabled", true).apply()
                startService(context)
            }
            "ACTION_AUTO_STOP" -> {
                prefs.edit().putBoolean("enabled", false).apply()
                context.stopService(Intent(context, ReminderService::class.java))
            }
        }
    }

    private fun startService(context: Context) {
        val si = Intent(context, ReminderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(si)
        else context.startService(si)
    }
}
