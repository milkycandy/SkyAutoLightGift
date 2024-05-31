package cn.milkycandy.skyautolightinggamepad

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

class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var detectButton: Button
    lateinit var detectedTextView: TextView
    private val handler = Handler()

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null)

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

        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = 0
        params.y = 100

        windowManager.addView(floatingView, params)

        detectButton = floatingView.findViewById(R.id.detect_button)
        detectedTextView = floatingView.findViewById(R.id.detected_text)
        instance = this

        detectButton.setOnClickListener {
            val accessibilityService = MyAccessibilityService.instance
            if (accessibilityService != null) {
                accessibilityService.logAllNodes()
                startCountdownAndClick(accessibilityService)
                detectButton.visibility = View.GONE
            } else {
                Toast.makeText(this, "无障碍服务不可用", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCountdownAndClick(service: MyAccessibilityService) {
        val detectedTexts = service.targetNodes.joinToString(separator = "\n") { it.text.toString() }
        detectedTextView.text = "3秒后开始送火\n$detectedTexts"
        handler.postDelayed({
            detectedTextView.text = "2秒后开始送火\n$detectedTexts"
            handler.postDelayed({
                detectedTextView.text = "1秒后开始送火\n$detectedTexts"
                handler.postDelayed({
                    clickAndCheck(service, 0)
                }, 1000)
            }, 1000)
        }, 1000)
    }

    private fun clickAndCheck(service: MyAccessibilityService, index: Int) {
        if (index >= service.targetNodes.size) {
            detectedTextView.text = "本页已完成"
            detectButton.visibility = View.VISIBLE

                return
        }

        val node = service.targetNodes[index]
        detectedTextView.text = "正在为 ${node.text} 送火"
        Log.d("Lighting", "点击 ${node.text}")
        enterMenu(service, index, node, 1)
    }

    private fun enterMenu(service: MyAccessibilityService, index: Int, node: AccessibilityNodeInfo, tryTimes: Int) {
        service.clickNode(node)
        handler.postDelayed({
            if (service.checkIfOnlyText(node.text.toString())) {
                // 送火
                Log.d("Lighting", "菜单进入成功，正在送火")
                detectedTextView.text = "正在为 ${node.text} 送火\n菜单进入成功，正在送火"
                sendLight(service, node, index)
            } else {
                Log.d("Lighting", "菜单进入失败，尝试点击左侧")
                service.clickNodeLeftEdge(node)
                handler.postDelayed({
                    if (service.checkIfOnlyText(node.text.toString())) {
                        // 送火
                        Log.d("Lighting", "菜单进入成功，正在送火")
                        detectedTextView.text = "正在为 ${node.text} 送火\n菜单进入成功，正在送火"
                        sendLight(service, node, index)
                    } else {
                        Log.d("Lighting", "菜单进入失败，尝试点击右侧")
                        service.clickNodeRightEdge(node)
                        handler.postDelayed({
                            if (service.checkIfOnlyText(node.text.toString())) {
                                // 送火
                                Log.d("Lighting", "菜单进入成功，正在送火")
                                detectedTextView.text = "正在为 ${node.text} 送火\n菜单进入成功，正在送火"
                                sendLight(service, node, index)
                            } else {
                                if (tryTimes < 3) {
                                    val newTryTimes = tryTimes + 1;
                                    Log.d("Lighting", "进入失败，即将尝试第 $newTryTimes 次")
                                    handler.postDelayed({
                                        enterMenu(service, index, node, newTryTimes)
                                    }, 1000)
                                } else {
                                    detectedTextView.text = "进入菜单失败，跳过 ${node.text} "
                                    clickAndCheck(service, index + 1)
                                }
                            }
                        }, 1000)
                    }
                }, 1000)
            }
        }, 1000)
    }

    private fun sendLight(service: MyAccessibilityService, nodeInfo: AccessibilityNodeInfo, index: Int) {
        detectedTextView.text = "正在为 ${nodeInfo.text} 送火\n尝试点击送火按钮"
        handler.postDelayed({
            Log.d("Lighting", "尝试点击送火按钮")

            service.clickFixedPosition()
            handler.postDelayed({
                if (service.allTextViewsContainText(nodeInfo.text.toString())) {
                    Log.d("Lighting", "仍在菜单中，再次点击以退出菜单")
                    detectedTextView.text = "仍在菜单中，正在尝试退出菜单"
                    service.clickFixedPosition()
                }
                handler.postDelayed({
                    Log.d("Lighting", "为下一位好友送火")
                    detectedTextView.text = "为下一位好友送火"
                    clickAndCheck(service, index + 1)
                }, 1200)
            }, 1500)
        }, 2000)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        handler.removeCallbacksAndMessages(null)
        instance = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        var instance: FloatingWindowService? = null
    }

    init {
        instance = this
    }
}
