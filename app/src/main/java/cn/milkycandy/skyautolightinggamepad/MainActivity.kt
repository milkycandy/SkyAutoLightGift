package cn.milkycandy.skyautolightinggamepad

import android.accessibilityservice.AccessibilityService
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 启动悬浮窗按钮
        findViewById<Button>(R.id.start_button).setOnClickListener {
            // 检查无障碍服务和悬浮窗权限是否启用
            if (isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
                if (checkOverlayPermission()) {
                    val intent = Intent(this, FloatingWindowService::class.java)
                    startService(intent)
                    Toast.makeText(this, "悬浮窗已启动", Toast.LENGTH_SHORT).show()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("需要悬浮窗权限")
                        .setMessage("请启用悬浮窗权限，以便本工具正常工作。")
                        .setPositiveButton("去设置") { _, _ ->
                            Toast.makeText(this, "请找到光遇自动送火，并允许在其他应用的上层显示", Toast.LENGTH_LONG).show()
                            requestOverlayPermission()
                        }
                        .setNegativeButton("取消", null)
                        .show()
                }
            } else {
                showAccessibilityServiceDialog()
            }
        }
    }

    // 检查无障碍服务是否启用
    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals("${context.packageName}/${service.name}", ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    // 显示无障碍服务授权对话框
    private fun showAccessibilityServiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要无障碍权限")
            .setMessage("请在设置中启用无障碍服务，以便本工具正常工作。")
            .setPositiveButton("去设置") { _, _ ->
                Toast.makeText(this, "请在“已下载的应用”中找到光遇自动送火，并打开无障碍开关", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 检查悬浮窗权限
    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    // 请求悬浮窗权限
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (checkOverlayPermission()) {
                val intent = Intent(this, FloatingWindowService::class.java)
                startService(intent)
                Toast.makeText(this, "悬浮窗已启动", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "悬浮窗权限未授予", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1
    }
}
