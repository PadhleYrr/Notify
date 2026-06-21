package com.notifyforwarder.app

import android.content.Context

object Prefs {
    private const val FILE = "notify_forwarder_prefs"
    private const val KEY_TOKEN = "bot_token"
    private const val KEY_CHAT_ID = "chat_id"
    private const val KEY_MONITOR_SMS = "monitor_sms"
    private const val KEY_MONITOR_GMAIL = "monitor_gmail"
    private const val KEY_EXTRA_PACKAGES = "extra_packages"

    // Common SMS app package names across OEMs - any of these being toggled "on"
    // means we forward notifications from whichever one is actually installed.
    val SMS_PACKAGES = listOf(
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms",
        "com.android.messaging"
    )
    const val GMAIL_PACKAGE = "com.google.android.gm"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getToken(ctx: Context): String = prefs(ctx).getString(KEY_TOKEN, "") ?: ""
    fun setToken(ctx: Context, value: String) = prefs(ctx).edit().putString(KEY_TOKEN, value).apply()

    fun getChatId(ctx: Context): String = prefs(ctx).getString(KEY_CHAT_ID, "") ?: ""
    fun setChatId(ctx: Context, value: String) = prefs(ctx).edit().putString(KEY_CHAT_ID, value).apply()

    fun getMonitorSms(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_MONITOR_SMS, true)
    fun setMonitorSms(ctx: Context, value: Boolean) = prefs(ctx).edit().putBoolean(KEY_MONITOR_SMS, value).apply()

    fun getMonitorGmail(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_MONITOR_GMAIL, true)
    fun setMonitorGmail(ctx: Context, value: Boolean) = prefs(ctx).edit().putBoolean(KEY_MONITOR_GMAIL, value).apply()

    // Comma-separated extra package names the user wants forwarded too.
    fun getExtraPackages(ctx: Context): String = prefs(ctx).getString(KEY_EXTRA_PACKAGES, "") ?: ""
    fun setExtraPackages(ctx: Context, value: String) = prefs(ctx).edit().putString(KEY_EXTRA_PACKAGES, value).apply()

    fun monitoredPackages(ctx: Context): Set<String> {
        val set = mutableSetOf<String>()
        if (getMonitorSms(ctx)) set.addAll(SMS_PACKAGES)
        if (getMonitorGmail(ctx)) set.add(GMAIL_PACKAGE)
        getExtraPackages(ctx).split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { set.add(it) }
        return set
    }
}
