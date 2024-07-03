package cn.milkycandy.skyautolighting

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    companion object {
        var instance: MyAccessibilityService? = null
    }

    var targetNodes = mutableListOf<AccessibilityNodeInfo>()
    private val handler = Handler(Looper.getMainLooper())
    var isRealTimeDisplayEnabled = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        // 启动悬浮窗服务
//        startService(Intent(this, FloatingWindowService::class.java))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    fun startRealTimeTextDisplay() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isRealTimeDisplayEnabled) {
                    logAllNodes()
                    handler.postDelayed(this, 50) // Update 20 times per second
                }
            }
        }, 50)
    }

    fun logAllNodes() {
        targetNodes.clear()
        val rootNode = rootInActiveWindow ?: return
        findTargetNodes(rootNode)
        Log.d("AccessibilityService", "Found ${targetNodes.size} target nodes.")

        // 更新悬浮窗上的 TextView
        val detectedTexts = targetNodes.joinToString(separator = "\n") { it.text.toString() }
        FloatingWindowService.instance?.textViewInfo?.text = detectedTexts
    }

    private fun findTargetNodes(node: AccessibilityNodeInfo) {
        if (node.className == "android.widget.TextView" && node.text != null) {
            val text = node.text.toString()
            if (text != "挚友" && text != "好友") {
                targetNodes.add(node)
                Log.d(
                    "AccessibilityService",
                    "Class: ${node.className}, Text: ${node.text}, Clickable: ${node.isClickable}"
                )
            }
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { childNode ->
                findTargetNodes(childNode)
            }
        }
    }

    fun performClick(x: Int, y: Int): Boolean {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }
        val gestureDescription = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 40))
            .build()
        return dispatchGesture(gestureDescription, null, null)
    }

    fun clickNode(node: AccessibilityNodeInfo) {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val centerX = (bounds.left + bounds.right) / 2
        val centerY = (bounds.top + bounds.bottom) / 2
        performClick(centerX, centerY)
    }

    fun clickNodeLeftEdge(node: AccessibilityNodeInfo) {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val centerX = bounds.left
        val centerY = (bounds.top + bounds.bottom) / 2
        performClick(centerX, centerY)
    }

    fun clickNodeRightEdge(node: AccessibilityNodeInfo) {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val centerX = bounds.right
        val centerY = (bounds.top + bounds.bottom) / 2
        performClick(centerX, centerY)
    }

    fun checkIfOnlyText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val textViewCounts = mutableMapOf<String, Int>()
        countTextViews(rootNode, textViewCounts)
        return textViewCounts[text] == 1 && textViewCounts.size == 1
    }

    private fun countTextViews(node: AccessibilityNodeInfo, counts: MutableMap<String, Int>) {
        if (node.className == "android.widget.TextView" && node.text != null) {
            val text = node.text.toString()
            if (!text.contains("心火")) {
                counts[text] = counts.getOrDefault(text, 0) + 1
            }
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { childNode ->
                countTextViews(childNode, counts)
            }
        }
    }

    fun allTextViewsContainText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        return checkAllTextViews(rootNode, text)
    }

    private fun checkAllTextViews(node: AccessibilityNodeInfo, text: String): Boolean {
        if (node.className == "android.widget.TextView" && node.text != null) {
            return node.text.toString().contains(text)
        }
        return false
    }

    fun hasExactlyTwoTextViews(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val textViewCount = countTextViews(rootNode)
        return textViewCount == 2
    }

    private fun countTextViews(node: AccessibilityNodeInfo): Int {
        var count = 0

        if (node.className == "android.widget.TextView") {
            count++
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { childNode ->
                count += countTextViews(childNode)
            }
        }

        return count
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        instance = null
    }
}
