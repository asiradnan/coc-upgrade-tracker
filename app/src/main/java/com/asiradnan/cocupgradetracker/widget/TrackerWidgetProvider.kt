package com.asiradnan.cocupgradetracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.asiradnan.cocupgradetracker.MainActivity
import com.asiradnan.cocupgradetracker.R
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import com.asiradnan.cocupgradetracker.data.VillageData
import com.asiradnan.cocupgradetracker.data.repository.VillageRepository
import com.asiradnan.cocupgradetracker.notifications.UpgradeNotificationScheduler
import com.asiradnan.cocupgradetracker.ui.components.getTownHallDrawableRes

class TrackerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_NEXT_VILLAGE) {
            val repo = VillageRepository(context.applicationContext)
            val snapshot = repo.loadVillages()
            if (snapshot.villages.size > 1) {
                val nextIndex = (snapshot.selectedIndex + 1) % snapshot.villages.size
                // saveVillages also triggers updateAllWidgets
                repo.saveVillages(snapshot.villages, nextIndex)
            } else {
                updateAllWidgets(context)
            }
        }
    }

    companion object {
        const val ACTION_NEXT_VILLAGE = "com.asiradnan.cocupgradetracker.ACTION_WIDGET_NEXT_VILLAGE"

        private val COLOR_GREEN_BRIGHT = Color.parseColor("#84E038")
        private val COLOR_RED_ALERT = Color.parseColor("#FF6B5E")
        private val COLOR_GOLD_BRIGHT = Color.parseColor("#FFC933")
        private val COLOR_LABEL_WARM = Color.parseColor("#E0D3C3")

        fun updateAllWidgets(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext) ?: return
            val component = ComponentName(appContext, TrackerWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids != null && ids.isNotEmpty()) {
                updateWidgets(appContext, manager, ids)
            }
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val appContext = context.applicationContext
            val snapshot = VillageRepository(appContext).loadVillages(syncExternal = false)
            val villages = snapshot.villages
            val selectedIndex = snapshot.selectedIndex.coerceIn(
                0,
                (villages.size - 1).coerceAtLeast(0)
            )
            val village = villages.getOrNull(selectedIndex)
            val nowEpochSeconds = System.currentTimeMillis() / 1000L

            for (widgetId in appWidgetIds) {
                val views = buildRemoteViews(
                    context = appContext,
                    widgetId = widgetId,
                    village = village,
                    totalVillages = villages.size,
                    nowEpochSeconds = nowEpochSeconds
                )
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }

        private fun buildRemoteViews(
            context: Context,
            widgetId: Int,
            village: VillageData?,
            totalVillages: Int,
            nowEpochSeconds: Long
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_village_next_free)

            if (village == null) {
                views.setImageViewResource(R.id.widget_th_image, R.drawable.th_1)
                views.setTextViewText(R.id.widget_village_name, "No village yet")
                views.setTextViewText(R.id.widget_village_subtitle, "Tap to add your village")
                views.setTextViewText(R.id.widget_status_label, "Status")
                views.setTextColor(R.id.widget_status_label, COLOR_LABEL_WARM)
                views.setTextViewText(R.id.widget_status_value, "Add")
                views.setTextColor(R.id.widget_status_value, COLOR_GOLD_BRIGHT)
                views.setViewVisibility(R.id.widget_next_village_btn, View.GONE)

                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openPending = PendingIntent.getActivity(
                    context,
                    widgetId,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openPending)
                return views
            }

            val baseEpoch = village.effectiveBaseEpochSeconds
            val freeDurationSeconds = village.getFreeDurationSeconds(nowEpochSeconds)
            val allActiveUpgrades = (
                village.builderUpgrades +
                    village.labUpgrades +
                    village.builderBaseUpgrades +
                    village.starLabUpgrades
                ).filter { it.remainingSeconds(baseEpoch, nowEpochSeconds) > 0L }
            val earliest = allActiveUpgrades.minByOrNull { it.finishEpochSeconds(baseEpoch) }
            val isUninitialized = village.pastedAtEpochSeconds == 0L && village.rawJson.isBlank()

            views.setImageViewResource(
                R.id.widget_th_image,
                getTownHallDrawableRes(village.townHallLevel)
            )
            views.setTextViewText(R.id.widget_village_name, village.username)

            if (isUninitialized) {
                views.setTextViewText(R.id.widget_village_subtitle, "Tap to paste JSON")
                views.setTextViewText(R.id.widget_status_label, "Setup")
                views.setTextColor(R.id.widget_status_label, COLOR_LABEL_WARM)
                views.setTextViewText(R.id.widget_status_value, "Paste")
                views.setTextColor(R.id.widget_status_value, COLOR_GOLD_BRIGHT)
            } else if (freeDurationSeconds != null) {
                val freeSlots = village.getFreeSlotDescriptions(nowEpochSeconds)
                val slotSummary = freeSlots.firstOrNull()
                val subtitle = if (!slotSummary.isNullOrBlank()) {
                    "TH ${village.townHallLevel} • $slotSummary"
                } else {
                    "TH ${village.townHallLevel}"
                }
                views.setTextViewText(R.id.widget_village_subtitle, subtitle)

                val isFresh = freeDurationSeconds < 60L
                val (statusTitle, statusValue) = if (isFresh) {
                    "Free" to "now"
                } else {
                    "Idle for" to TimeFormatters.formatCompactDuration(freeDurationSeconds)
                }
                views.setTextViewText(R.id.widget_status_label, statusTitle)
                views.setTextColor(R.id.widget_status_label, COLOR_RED_ALERT)
                views.setTextViewText(R.id.widget_status_value, statusValue)
                views.setTextColor(R.id.widget_status_value, COLOR_RED_ALERT)
            } else if (earliest != null) {
                val finishEpoch = earliest.finishEpochSeconds(baseEpoch)
                val formattedClock = TimeFormatters.formatShortClockTime(finishEpoch, nowEpochSeconds)
                views.setTextViewText(
                    R.id.widget_village_subtitle,
                    "TH ${village.townHallLevel} • ${earliest.name}"
                )
                views.setTextViewText(R.id.widget_status_label, "Earliest free")
                views.setTextColor(R.id.widget_status_label, COLOR_LABEL_WARM)
                views.setTextViewText(R.id.widget_status_value, formattedClock)
                views.setTextColor(R.id.widget_status_value, COLOR_GREEN_BRIGHT)
            } else {
                views.setTextViewText(R.id.widget_village_subtitle, "TH ${village.townHallLevel}")
                views.setTextViewText(R.id.widget_status_label, "Status")
                views.setTextColor(R.id.widget_status_label, COLOR_LABEL_WARM)
                views.setTextViewText(R.id.widget_status_value, "Maxed")
                views.setTextColor(R.id.widget_status_value, COLOR_GOLD_BRIGHT)
            }

            // Open selected village detail screen when card is tapped
            val openVillageIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(UpgradeNotificationScheduler.EXTRA_VILLAGE_TAG, village.tag)
            }
            val openPending = PendingIntent.getActivity(
                context,
                widgetId,
                openVillageIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openPending)

            // Show cycle button if user tracks multiple villages
            if (totalVillages > 1) {
                views.setViewVisibility(R.id.widget_next_village_btn, View.VISIBLE)
                val nextIntent = Intent(context, TrackerWidgetProvider::class.java).apply {
                    action = ACTION_NEXT_VILLAGE
                }
                val nextPending = PendingIntent.getBroadcast(
                    context,
                    widgetId + 10_000,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_next_village_btn, nextPending)
            } else {
                views.setViewVisibility(R.id.widget_next_village_btn, View.GONE)
            }

            return views
        }
    }
}
