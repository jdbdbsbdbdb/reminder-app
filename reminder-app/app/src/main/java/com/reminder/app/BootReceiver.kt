package com.reminder.app

import android.content.*
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
            // 开机后如果之前是开启状态，自动恢复服务
            if (prefs.getBoolean("enabled", false)) {
                val si = Intent(context, ReminderService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(si)
                else context.startService(si)
            }
        }
    }
}
