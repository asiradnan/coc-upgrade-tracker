package com.asiradnan.cocupgradetracker.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.asiradnan.cocupgradetracker.MainActivity
import com.asiradnan.cocupgradetracker.R
import com.asiradnan.cocupgradetracker.data.UpgradeCategory
import com.asiradnan.cocupgradetracker.data.UpgradeItem
import com.asiradnan.cocupgradetracker.data.VillageData

object UpgradeNotificationScheduler {

    const val CHANNEL_ID = "coc_upgrade_completions"
    private const val CHANNEL_NAME = "Upgrade Completions"
    private const val CHANNEL_DESC = "Notifications when a Builder or Laboratory becomes free"

    private const val PREFS_NAME = "coc_upgrade_notification_prefs"
    private const val KEY_SCHEDULED_CODES = "scheduled_request_codes"

    const val ACTION_UPGRADE_COMPLETE = "com.asiradnan.cocupgradetracker.ACTION_UPGRADE_COMPLETE"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_VILLAGE_TAG = "extra_village_tag"
    const val EXTRA_VILLAGE_NAME = "extra_village_name"
    const val EXTRA_SLOT_TITLE = "extra_slot_title"
    const val EXTRA_UPGRADE_NAME = "extra_upgrade_name"
    const val EXTRA_LEVEL_TEXT = "extra_level_text"

    fun ensureNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Cancels all previously scheduled upgrade alarms and schedules fresh alarms for every
     * future Builder, Laboratory, Builder Base Builder, and Star Laboratory completion.
     */
    fun syncVillages(context: Context, villages: List<VillageData>) {
        val appContext = context.applicationContext
        ensureNotificationChannel(appContext)

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Cancel all previously registered alarms
        val oldCodes = prefs.getStringSet(KEY_SCHEDULED_CODES, emptySet()).orEmpty()
        for (codeStr in oldCodes) {
            val reqCode = codeStr.toIntOrNull() ?: continue
            val cancelIntent = Intent(appContext, UpgradeNotificationReceiver::class.java).apply {
                action = ACTION_UPGRADE_COMPLETE
            }
            val pending = PendingIntent.getBroadcast(
                appContext,
                reqCode,
                cancelIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) {
                alarmManager.cancel(pending)
                pending.cancel()
            }
        }

        // 2. Schedule alarms for all future upgrades across all villages
        val nowEpochSeconds = System.currentTimeMillis() / 1000L
        val newCodes = mutableSetOf<String>()

        villages.forEachIndexed { villageIdx, village ->
            val baseEpoch = village.effectiveBaseEpochSeconds
            if (baseEpoch <= 0L) return@forEachIndexed

            val trackedUpgrades =
                village.builderUpgrades +
                    village.labUpgrades +
                    village.builderBaseUpgrades +
                    village.starLabUpgrades

            for (upgrade in trackedUpgrades) {
                val finishEpochSeconds = upgrade.finishEpochSeconds(baseEpoch)
                if (finishEpochSeconds <= nowEpochSeconds) continue

                val triggerAtMillis = finishEpochSeconds * 1000L
                val slotTitle = resolveSlotFreeTitle(village, upgrade)
                val reqCode = computeRequestCode(villageIdx, village.tag, upgrade.id, finishEpochSeconds)

                val alarmIntent = Intent(appContext, UpgradeNotificationReceiver::class.java).apply {
                    action = ACTION_UPGRADE_COMPLETE
                    putExtra(EXTRA_NOTIFICATION_ID, reqCode)
                    putExtra(EXTRA_VILLAGE_TAG, village.tag)
                    putExtra(EXTRA_VILLAGE_NAME, village.username)
                    putExtra(EXTRA_SLOT_TITLE, slotTitle)
                    putExtra(EXTRA_UPGRADE_NAME, upgrade.name)
                    putExtra(EXTRA_LEVEL_TEXT, upgrade.levelTransitionText)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    reqCode,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                scheduleExactOrAllowWhileIdle(alarmManager, triggerAtMillis, pendingIntent)
                newCodes.add(reqCode.toString())
            }
        }

        prefs.edit().putStringSet(KEY_SCHEDULED_CODES, newCodes).apply()
    }

    private fun scheduleExactOrAllowWhileIdle(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun resolveSlotFreeTitle(village: VillageData, upgrade: UpgradeItem): String {
        return when (upgrade.category) {
            UpgradeCategory.BUILDING,
            UpgradeCategory.TRAP,
            UpgradeCategory.HERO -> {
                if (upgrade.isGoblinHelper) "Goblin Builder Free!" else "Builder Free!"
            }

            UpgradeCategory.TROOP,
            UpgradeCategory.SPELL,
            UpgradeCategory.SIEGE -> {
                when {
                    upgrade.isGoblinHelper -> "Goblin Researcher Free!"
                    village.isLabMaxed -> "Laboratory Maxed!"
                    else -> "Laboratory Free!"
                }
            }

            UpgradeCategory.BUILDER_BASE_BUILDING -> "Builder Base Builder Free!"

            UpgradeCategory.BUILDER_BASE_RESEARCH -> {
                if (village.isStarLabMaxed) "Star Laboratory Maxed!" else "Star Laboratory Free!"
            }

            UpgradeCategory.PET -> "Pet House Free!"
        }
    }

    private fun computeRequestCode(
        villageIdx: Int,
        villageTag: String,
        upgradeId: String,
        finishEpochSeconds: Long
    ): Int {
        val key = "$villageIdx|$villageTag|$upgradeId|$finishEpochSeconds"
        return (key.hashCode() and 0x7FFFFFFF).coerceAtLeast(1000)
    }

    fun showUpgradeCompletedNotification(
        context: Context,
        notificationId: Int,
        villageTag: String,
        villageName: String,
        slotTitle: String,
        upgradeName: String,
        levelText: String
    ) {
        val appContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureNotificationChannel(appContext)

        val tapIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_VILLAGE_TAG, villageTag)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            appContext,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "$villageName • $slotTitle"
        val body = if (levelText.isNotBlank()) {
            "$upgradeName ($levelText) upgrade is complete."
        } else {
            "$upgradeName upgrade is complete."
        }

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        NotificationManagerCompat.from(appContext).notify(notificationId, notification)
    }
}
