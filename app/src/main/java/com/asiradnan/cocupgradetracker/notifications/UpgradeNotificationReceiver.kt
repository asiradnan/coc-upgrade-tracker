package com.asiradnan.cocupgradetracker.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.asiradnan.cocupgradetracker.data.repository.VillageRepository
import com.asiradnan.cocupgradetracker.widget.TrackerWidgetProvider

class UpgradeNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        when (action) {
            UpgradeNotificationScheduler.ACTION_UPGRADE_COMPLETE -> {
                val notificationId = intent.getIntExtra(
                    UpgradeNotificationScheduler.EXTRA_NOTIFICATION_ID,
                    1001
                )
                val villageTag = intent.getStringExtra(
                    UpgradeNotificationScheduler.EXTRA_VILLAGE_TAG
                ).orEmpty()
                val villageName = intent.getStringExtra(
                    UpgradeNotificationScheduler.EXTRA_VILLAGE_NAME
                ).orEmpty().ifBlank { "Village" }
                val slotTitle = intent.getStringExtra(
                    UpgradeNotificationScheduler.EXTRA_SLOT_TITLE
                ).orEmpty().ifBlank { "Upgrade Complete!" }
                val upgradeName = intent.getStringExtra(
                    UpgradeNotificationScheduler.EXTRA_UPGRADE_NAME
                ).orEmpty().ifBlank { "Upgrade" }
                val levelText = intent.getStringExtra(
                    UpgradeNotificationScheduler.EXTRA_LEVEL_TEXT
                ).orEmpty()

                UpgradeNotificationScheduler.showUpgradeCompletedNotification(
                    context = context,
                    notificationId = notificationId,
                    villageTag = villageTag,
                    villageName = villageName,
                    slotTitle = slotTitle,
                    upgradeName = upgradeName,
                    levelText = levelText
                )
                TrackerWidgetProvider.updateAllWidgets(context.applicationContext)
            }

            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Reloading villages automatically re-syncs all pending upgrade alarms
                VillageRepository(context.applicationContext).loadVillages()
            }
        }
    }
}
