package com.notifyforwarder.app

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import java.util.concurrent.Executors

class NotificationForwarderService : NotificationListenerService() {

    private val executor = Executors.newSingleThreadExecutor()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        try {
            handle(sbn)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling notification", e)
        }
    }

    private fun handle(sbn: StatusBarNotification) {
        val pkg = sbn.packageName

        // Never forward our own notifications.
        if (pkg == packageName) return

        val monitored = Prefs.monitoredPackages(applicationContext)
        if (pkg !in monitored) return

        val notification = sbn.notification

        // Skip group summary notifications (the "3 new messages" stacking ones)
        // and ongoing/foreground-service notifications - they're not real messages.
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return
        if (notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0) return

        val (title, body) = extractContent(notification)
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(body)) return

        val appLabel = labelFor(pkg)
        val message = buildString {
            append(appLabel)
            append("\n")
            if (!title.isNullOrBlank()) append("From: $title\n")
            if (!body.isNullOrBlank()) append(body)
        }.trim()

        val token = Prefs.getToken(applicationContext)
        val chatId = Prefs.getChatId(applicationContext)

        executor.execute {
            TelegramSender.sendMessage(token, chatId, message)
        }
    }

    private fun extractContent(notification: Notification): Pair<String?, String?> {
        val extras = notification.extras ?: return Pair(null, null)

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()

        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val summary = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()

        val body = when {
            !textLines.isNullOrEmpty() -> textLines.joinToString("\n") { it.toString() }
            !bigText.isNullOrBlank() -> bigText
            !text.isNullOrBlank() -> text
            !summary.isNullOrBlank() -> summary
            else -> null
        }
        return Pair(title, body)
    }

    private fun labelFor(pkg: String): String = when {
        pkg in Prefs.SMS_PACKAGES -> "\uD83D\uDCAC SMS"
        pkg == Prefs.GMAIL_PACKAGE -> "\uD83D\uDCE7 Gmail"
        else -> "\uD83D\uDD14 $pkg"
    }

    companion object {
        private const val TAG = "NotifyForwarderSvc"
    }
}
