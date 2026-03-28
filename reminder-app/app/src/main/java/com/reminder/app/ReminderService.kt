package com.reminder.app

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat

class ReminderService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var notifId = 1000

    // 20分钟 = 1,200,000ms
    private val INTERVAL_MS = 20 * 60 * 1000L
    // 25秒 = 25,000ms
    private val SECOND_DELAY_MS = 25 * 1000L

    private val firstReminderRunnable = object : Runnable {
        override fun run() {
            sendReminderNotification(
                title = "⏰ 提醒时间到",
                text = "休息一下，活动活动身体！"
            )
            // 25秒后发送第二次提醒
            handler.postDelayed(secondReminderRunnable, SECOND_DELAY_MS)
            // 20分钟后再次循环
            handler.postDelayed(this, INTERVAL_MS)
        }
    }

    private val secondReminderRunnable = Runnable {
        sendReminderNotification(
            title = "🔔 再次提醒",
            text = "别忘了刚才的提醒哦！"
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(999, buildForegroundNotification())
        // 立即开始第一次，之后每20分钟循环
        handler.post(firstReminderRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(firstReminderRunnable)
        handler.removeCallbacks(secondReminderRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun sendReminderNotification(title: String, text: String) {
        val notif = NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()
        getSystemService(NotificationManager::class.java).notify(notifId++, notif)
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, MainActivity.SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("提醒助手运行中")
            .setContentText("每20分钟提醒一次，25秒后再次提醒")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
