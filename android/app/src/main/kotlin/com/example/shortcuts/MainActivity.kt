package com.example.shortcuts

import android.app.PendingIntent
import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val kCHANNEL = "com.example/native"
    private var screenNames = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        if (intent.hasExtra("screenToOpen"))
            screenNames += intent.extras?.getString("screenToOpen")!!


        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger, kCHANNEL
        ).setMethodCallHandler { call, result ->
            if (call.method == "sendNativeData") {
                if (!call.hasArgument("name") || !call.hasArgument("imagePath")) {
                    result.error(
                        "MissingArgumentError",
                        "You must provide name and imagePath parameters.",
                        null
                    )
                }
                val name: String = call.argument<String>("name")!!
                val imagePath: String = call.argument<String>("imagePath")!!
                val data = sendNativeData(name, imagePath)
                result.success(data)
            } else if (call.method == "getNativeData") {
                Log.d("Screen Names Data", "Screen Names While result.success: $screenNames")
                result.success(screenNames)
            } else {
                result.notImplemented()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNativeData(name: String, imagePath: String): String {
        if (Build.VERSION.SDK_INT >= 25) Shortcuts.setUp(context, name, imagePath)
        shortcutPin(context, name, 1)
        return ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shortcutPin(context: Context, shortcutId: String, requestCode: Int) {

        val shortcutManager = getSystemService(ShortcutManager::class.java)

        if (shortcutManager!!.isRequestPinShortcutSupported) {
            val pinShortcutInfo = ShortcutInfo.Builder(context, shortcutId).build()

            val pinnedShortcutCallbackIntent =
                shortcutManager.createShortcutResultIntent(pinShortcutInfo)

            val successCallback = PendingIntent.getBroadcast(
                context, requestCode, pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE
            )

            shortcutManager.requestPinShortcut(
                pinShortcutInfo, successCallback.intentSender
            )
        }
    }
}