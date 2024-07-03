package cn.milkycandy.skyautolighting

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.lang.ref.WeakReference

class FloatingWindowService : Service() {
    private val TAG = "FloatingWindowService"
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var detectButton: Button
    private lateinit var pressXButton: Button
    private lateinit var pressBButton: Button
    lateinit var textViewInfo: TextView
    private val client = OkHttpClient()
    private val ESP32_IP = "http://192.168.43.228"

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null, false)

        val layoutFlag = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        windowManager.addView(floatingView, params)

        detectButton = floatingView.findViewById(R.id.detect_button)
        textViewInfo = floatingView.findViewById(R.id.detected_text)
        pressXButton = floatingView.findViewById(R.id.pressX_button)
        pressBButton = floatingView.findViewById(R.id.pressB_button)
        serviceReference = WeakReference(this)

        detectButton.setOnClickListener {
            val accessibilityService = MyAccessibilityService.instance
            if (accessibilityService != null) {
                accessibilityService.logAllNodes()
                startCountdownAndClick(accessibilityService)
                detectButton.visibility = View.GONE
            } else {
                Toast.makeText(
                    this,
                    "无障碍服务不可用",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        pressXButton.setOnClickListener {
            sendCommand("pressXtwice")
        }

        pressBButton.setOnClickListener {
            sendCommand("pressB")
        }

    }

    private fun startCountdownAndClick(service: MyAccessibilityService) {
        val detectedTexts =
            service.targetNodes.joinToString(separator = "\n") { it.text.toString() }
        val mainScope = CoroutineScope(Dispatchers.Main + Job())

        mainScope.launch {
            textViewInfo.text = "将在3秒后送火:\n$detectedTexts"
            delay(1000)
            textViewInfo.text = "将在2秒后送火:\n$detectedTexts"
            delay(1000)
            textViewInfo.text = "将在1秒后送火:\n$detectedTexts"
            delay(1000)
            clickAndCheck(service, 0)
        }
    }

    private fun clickAndCheck(service: MyAccessibilityService, index: Int) {
        if (index >= service.targetNodes.size) {
            textViewInfo.text = "当前页面完成"
            detectButton.visibility = View.VISIBLE
            return
        }

        val node = service.targetNodes[index]
        textViewInfo.text = "正在送火给: ${node.text}"
        Log.d(TAG, "点击 ${node.text}")
        enterMenu(service, index, node, 1)
    }

    private fun enterMenu(
        service: MyAccessibilityService,
        index: Int,
        node: AccessibilityNodeInfo,
        tryTimes: Int
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            // 由于好友textview可能在下方、左方或右方，所以都点一遍
            service.clickNode(node)
            delay(40)
            service.clickNodeLeftEdge(node)
            delay(40)
            service.clickNodeRightEdge(node)
            Log.d(TAG, "完成快速点击，稍后检查是否在菜单中")
            delay(1000)
            if (service.checkIfOnlyText(node.text.toString())) {
                Log.d(TAG, "进入菜单成功")
                Log.d(TAG, "正在发送X")
                sendCommand("sendLightAndBack")
                delay(1500)
                Log.d(TAG, "完成，进入下一次")
                clickAndCheck(service, index + 1)
            } else {
                if (tryTimes < 3) {
                    Log.d(TAG, "进入菜单失败，尝试再次进入")
                    enterMenu(service, index, node, tryTimes + 1)
                } else {
                    Log.d(TAG, "尝试三次失败，跳过该好友")
                    textViewInfo.text = "进入菜单失败，跳过该好友"
                    delay(1000)
                    clickAndCheck(service, index + 1)
                }
            }
        }
    }


    private fun sendCommand(command: String) {
        val url = "$ESP32_IP/$command"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "Error sending command", e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.string()?.let {
                    Log.d(TAG, "Response: $it")
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        serviceReference?.clear()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private var serviceReference: WeakReference<FloatingWindowService>? = null

        val instance: FloatingWindowService?
            get() = serviceReference?.get()
    }

    init {
        serviceReference = WeakReference(this)
    }
}
