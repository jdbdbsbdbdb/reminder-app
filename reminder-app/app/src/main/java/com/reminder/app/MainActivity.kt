package com.reminder.app

import android.app.*
import android.content.*
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.reminder.app.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        createNotificationChannel()
        requestNotificationPermission()

        // 恢复保存的状态
        binding.mainSwitch.isChecked = prefs.getBoolean("enabled", false)
        binding.autoStartSwitch.isChecked = prefs.getBoolean("auto_start", false)
        binding.autoStopSwitch.isChecked = prefs.getBoolean("auto_stop", false)
        binding.startTimePicker.hour = prefs.getInt("start_hour", 8)
        binding.startTimePicker.minute = prefs.getInt("start_minute", 0)
        binding.stopTimePicker.hour = prefs.getInt("stop_hour", 22)
        binding.stopTimePicker.minute = prefs.getInt("stop_minute", 0)

        updateStatusText()

        // 主开关
        binding.mainSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("enabled", checked).apply()
            if (checked) startReminderService() else stopReminderService()
            updateStatusText()
        }

        // 定时自动开启开关
        binding.autoStartSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("auto_start", checked).apply()
            if (checked) scheduleAutoStart(binding.startTimePicker.hour, binding.startTimePicker.minute)
            else cancelAutoStart()
            showToast(if (checked) "已设置自动开启时间" else "已取消自动开启")
        }

        // 定时自动关闭开关
        binding.autoStopSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("auto_stop", checked).apply()
            if (checked) scheduleAutoStop(binding.stopTimePicker.hour, binding.stopTimePicker.minute)
            else cancelAutoStop()
            showToast(if (checked) "已设置自动关闭时间" else "已取消自动关闭")
        }

        // 开始时间选择
        binding.startTimePicker.setOnTimeChangedListener { _, h, m ->
            prefs.edit().putInt("start_hour", h).putInt("start_minute", m).apply()
            if (binding.autoStartSwitch.isChecked) scheduleAutoStart(h, m)
        }

        // 结束时间选择
        binding.stopTimePicker.setOnTimeChangedListener { _, h, m ->
            prefs.edit().putInt("stop_hour", h).putInt("stop_minute", m).apply()
            if (binding.autoStopSwitch.isChecked) scheduleAutoStop(h, m)
        }
    }

    private fun updateStatusText() {
        val isEnabled = prefs.getBoolean("enabled", false)
        binding.statusText.text = if (isEnabled) "✅ 提醒已开启 — 每20分钟提醒一次，25秒后再次提醒"
        else "⭕ 提醒已关闭"
        binding.statusText.setTextColor(
            if (isEnabled) getColor(android.R.color.holo_green_dark)
            else getColor(android.R.color.darker_gray)
        )
    }

    private fun startReminderService() {
        val intent = Intent(this, ReminderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent)
        else startService(intent)
        showToast("提醒已开启")
    }

    private fun stopReminderService() {
        stopService(Intent(this, ReminderService::class.java))
        showToast("提醒已关闭")
    }

    private fun scheduleAutoStart(hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }
        val pi = PendingIntent.getBroadcast(
            this, 100,
            Intent(this, AlarmReceiver::class.java).apply { action = "ACTION_AUTO_START" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
    }

    private fun cancelAutoStart() {
        val pi = PendingIntent.getBroadcast(
            this, 100,
            Intent(this, AlarmReceiver::class.java).apply { action = "ACTION_AUTO_START" },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let { (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(it) }
    }

    private fun scheduleAutoStop(hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }
        val pi = PendingIntent.getBroadcast(
            this, 101,
            Intent(this, AlarmReceiver::class.java).apply { action = "ACTION_AUTO_STOP" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
    }

    private fun cancelAutoStop() {
        val pi = PendingIntent.getBroadcast(
            this, 101,
            Intent(this, AlarmReceiver::class.java).apply { action = "ACTION_AUTO_STOP" },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let { (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(it) }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "提醒通知", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "定时提醒推送通知"
                enableVibration(true)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID, "后台服务", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "提醒服务运行状态" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(serviceChannel)
        }
    }

    private fun showToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val SERVICE_CHANNEL_ID = "service_channel"
    }
}
