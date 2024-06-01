package cn.milkycandy.skyautolighting

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.lang.ref.WeakReference

@Suppress("DEPRECATION")
class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var detectButton: Button
    lateinit var textViewInfo: TextView
    private val handler = Handler()

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
                    getString(R.string.accessibility_service_not_available), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startCountdownAndClick(service: MyAccessibilityService) {
        val detectedTexts =
            service.targetNodes.joinToString(separator = "\n") { it.text.toString() }
        textViewInfo.text = getString(R.string.send_fire_after_3s, detectedTexts)
        handler.postDelayed({
            textViewInfo.text = getString(R.string.send_fire_after_2s, detectedTexts)
            handler.postDelayed({
                textViewInfo.text = getString(R.string.send_fire_after_1s, detectedTexts)
                handler.postDelayed({
                    clickAndCheck(service, 0)
                }, 1000)
            }, 1000)
        }, 1000)
    }

    private fun clickAndCheck(service: MyAccessibilityService, index: Int) {
        if (index >= service.targetNodes.size) {
            textViewInfo.text = getString(R.string.current_page_completed)
            detectButton.visibility = View.VISIBLE
            return
        }

        val node = service.targetNodes[index]
        textViewInfo.text = getString(R.string.sending_fire_to, node.text)
        Log.d("Lighting", "点击 ${node.text}")
        enterMenu(service, index, node, 1)
    }

    // 这一段嵌套看起来相当诡异，不过能用，先不管了（也不知道怎么解决）
    private fun enterMenu(
        service: MyAccessibilityService,
        index: Int,
        node: AccessibilityNodeInfo,
        tryTimes: Int
    ) {
        service.clickNode(node)
        handler.postDelayed({
            if (service.checkIfOnlyText(node.text.toString())) {
                Log.d("Lighting", "菜单进入成功，正在送火")
                textViewInfo.text = "菜单进入成功，正在送火"
                sendLight(service, node, index)
            } else {
                Log.d("Lighting", "菜单进入失败，尝试点击左侧")
                service.clickNodeLeftEdge(node)
                handler.postDelayed({
                    if (service.checkIfOnlyText(node.text.toString())) {
                        Log.d("Lighting", "菜单进入成功，正在送火")
                        textViewInfo.text =
                            getString(R.string.menu_entered_successfully_sending_fire)
                        sendLight(service, node, index)
                    } else {
                        Log.d("Lighting", "菜单进入失败，尝试点击右侧")
                        service.clickNodeRightEdge(node)
                        handler.postDelayed({
                            if (service.checkIfOnlyText(node.text.toString())) {
                                Log.d("Lighting", "菜单进入成功，正在送火")
                                textViewInfo.text =
                                    getString(R.string.menu_entered_successfully_sending_fire)
                                sendLight(service, node, index)
                            } else {
                                if (tryTimes < 3) {
                                    val newTryTimes = tryTimes + 1
                                    Log.d("Lighting", "进入失败，即将尝试第 $newTryTimes 次")
                                    handler.postDelayed({
                                        enterMenu(service, index, node, newTryTimes)
                                    }, 1000)
                                } else {
                                    textViewInfo.text =
                                        getString(R.string.failed_to_enter_menu, node.text)
                                    clickAndCheck(service, index + 1)
                                }
                            }
                        }, 1000)
                    }
                }, 1000)
            }
        }, 1000)
    }

    private fun sendLight(
        service: MyAccessibilityService,
        nodeInfo: AccessibilityNodeInfo,
        index: Int
    ) {
        textViewInfo.text = getString(R.string.clicking_send_fire_button)
        handler.postDelayed({
            Log.d("Lighting", "尝试点击送火按钮")
            clickSendLightButton(service)
            handler.postDelayed({
                if (service.allTextViewsContainText(nodeInfo.text.toString())) {
                    Log.d("Lighting", "仍在菜单中，再次点击以退出菜单")
                    textViewInfo.text = getString(R.string.still_in_menu)
                    clickSendLightButton(service)
                }
                handler.postDelayed({
                    Log.d("Lighting", "为下一位好友送火")
                    textViewInfo.text = "为下一位好友送火"
                    clickAndCheck(service, index + 1)
                }, 1200)
            }, 1500)
        }, 2000)
    }

    private fun clickSendLightButton(service: MyAccessibilityService) {
        val sharedPref = getSharedPreferences("SkySharedPreferences", Context.MODE_PRIVATE)
        val xCoordinate = sharedPref.getString("xCoordinate", "")
        val yCoordinate = sharedPref.getString("yCoordinate", "")
        if (xCoordinate != null && yCoordinate != null) {
            service.performClick(xCoordinate.toInt(), yCoordinate.toInt())
        } else {
            Log.d("Lighting", "坐标值为空！")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        handler.removeCallbacksAndMessages(null)
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
