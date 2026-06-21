# Notify Forwarder

Forwards SMS and Gmail notifications from your old phone to a Telegram chat,
by reading the notifications it already shows (no SMS resending, no email
password stored on device).

## How it works

The app registers as a **Notification Listener**. When a notification arrives
from your Messages app or Gmail, it grabs the sender + text and posts it to
your Telegram bot via the Bot API. Group summary / "X new messages" stacking
notifications are skipped so you only get real messages.

## 1. Get your Telegram chat ID

You said you already have a bot + token from BotFather. You still need your
**chat ID** (not the bot's username):

1. Open Telegram, search for your bot, and send it any message (e.g. `/start`).
2. In a browser, visit:
   `https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates`
3. Look for `"chat":{"id":123456789,...}` in the response — that number is
   your chat ID.

## 2. Build the APK (no Android Studio needed)

This repo includes `.github/workflows/build.yml`. Push this project to a
GitHub repo, then:

1. Go to the **Actions** tab → select **Build APK** → **Run workflow**
   (or just push to `main`, it triggers automatically).
2. When it finishes, open the run and download the
   **notify-forwarder-debug-apk** artifact (a zip containing `app-debug.apk`).
3. Transfer the APK to the old phone (email it to yourself, Google Drive,
   USB cable — whatever's easiest) and install it. You'll need to allow
   "install from unknown sources" the first time since it's not from a store yet.

## 3. Set up on the old phone

1. Open the app, paste in your **Bot Token** and **Chat ID**, tap **Save Settings**.
2. Tap **Grant Notification Access** → find "Notify Forwarder" in the list →
   turn it on.
3. Tap **Send Test Message** to confirm it reaches Telegram.
4. Leave "Forward SMS notifications" and "Forward Gmail notifications" checked
   (on by default). If your phone uses a different SMS app than Google
   Messages/Samsung Messages, add its package name under "Extra package names".
5. **Important on Samsung/Xiaomi/Huawei phones:** go to Settings → Battery →
   find the app → disable battery optimization / allow background activity,
   or the OEM may kill the listener service after a while.

## 4. Publishing to the Play Store (optional)

A few things you'll need before submitting:

- A **Play Console developer account** ($25 one-time, from Google).
- A **privacy policy URL** — required because the app uses notification
  access. State plainly that notification content is sent to a Telegram bot
  the user controls, nothing is stored on Anthropic/Google servers, and
  nothing is sold or shared. A free GitHub Pages page works fine for hosting it.
- A **signed release build** instead of the debug APK — Play Console will
  walk you through generating a keystore and uploading an `.aab` (the
  workflow currently builds a debug APK; ask me to add a signed
  `bundleRelease` job once you're ready for that step, since it needs a
  keystore secret).
- In the Store listing description, mention upfront that the app's purpose
  is "forwarding selected notifications to a Telegram bot you choose" —
  Google's reviewers check that the listed purpose matches what sensitive
  permissions are used for.

## Project structure

```
app/src/main/java/com/notifyforwarder/app/
  MainActivity.kt                 - settings screen
  NotificationForwarderService.kt - the listener that reads + forwards notifications
  TelegramSender.kt               - Telegram Bot API HTTP call
  Prefs.kt                        - stores token/chat id/settings on device
```
