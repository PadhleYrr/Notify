package com.notifyforwarder.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var editToken: EditText
    private lateinit var editChatId: EditText
    private lateinit var checkSms: CheckBox
    private lateinit var checkGmail: CheckBox
    private lateinit var editExtraPackages: EditText

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        editToken = findViewById(R.id.editToken)
        editChatId = findViewById(R.id.editChatId)
        checkSms = findViewById(R.id.checkSms)
        checkGmail = findViewById(R.id.checkGmail)
        editExtraPackages = findViewById(R.id.editExtraPackages)

        editToken.setText(Prefs.getToken(this))
        editChatId.setText(Prefs.getChatId(this))
        checkSms.isChecked = Prefs.getMonitorSms(this)
        checkGmail.isChecked = Prefs.getMonitorGmail(this)
        editExtraPackages.setText(Prefs.getExtraPackages(this))

        findViewById<Button>(R.id.btnGrantAccess).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveSettings()
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnTest).setOnClickListener {
            saveSettings()
            sendTestMessage()
        }
    }

    override fun onResume() {
        super.onResume()
        statusText.text = if (isNotificationAccessGranted()) {
            "✅ Notification access granted"
        } else {
            "⚠️ Notification access NOT granted yet - tap the button below"
        }
    }

    private fun saveSettings() {
        Prefs.setToken(this, editToken.text.toString().trim())
        Prefs.setChatId(this, editChatId.text.toString().trim())
        Prefs.setMonitorSms(this, checkSms.isChecked)
        Prefs.setMonitorGmail(this, checkGmail.isChecked)
        Prefs.setExtraPackages(this, editExtraPackages.text.toString().trim())
    }

    private fun sendTestMessage() {
        val token = Prefs.getToken(this)
        val chatId = Prefs.getChatId(this)
        if (token.isBlank() || chatId.isBlank()) {
            Toast.makeText(this, "Enter your bot token and chat ID first", Toast.LENGTH_SHORT).show()
            return
        }
        executor.execute {
            val ok = TelegramSender.sendMessage(token, chatId, "✅ Test message from Notify Forwarder")
            runOnUiThread {
                Toast.makeText(
                    this,
                    if (ok) "Test message sent! Check Telegram." else "Failed to send - check token/chat ID",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun isNotificationAccessGranted(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabled != null && enabled.contains(packageName)
    }
}
