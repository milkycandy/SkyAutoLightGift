package cn.milkycandy.skyautolighting

import android.accessibilityservice.AccessibilityService
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var editTextX: EditText
    private lateinit var editTextY: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextX = findViewById(R.id.editTextX)
        editTextY = findViewById(R.id.editTextY)
        findViewById<Button>(R.id.buttonSave).setOnClickListener {
            saveCoordinates()
        }

        loadCoordinates()

        findViewById<Button>(R.id.start_button).setOnClickListener {
            // Check if coordinates are saved
            if (loadCoordinates()) {
                // 检查无障碍服务和悬浮窗权限是否启用
                if (isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
                    if (checkOverlayPermission()) {
                        val intent = Intent(this, FloatingWindowService::class.java)
                        startService(intent)
                        Toast.makeText(this, "悬浮窗已启动", Toast.LENGTH_SHORT).show()
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.require_floating_permission))
                            .setMessage("请启用悬浮窗权限，以便本工具正常工作。")
                            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                                Toast.makeText(
                                    this,
                                    "请找到光遇自动送火，并允许在其他应用的上层显示",
                                    Toast.LENGTH_LONG
                                ).show()
                                requestOverlayPermission()
                            }
                            .setNegativeButton("取消", null)
                            .show()
                    }
                } else {
                    showAccessibilityServiceDialog()
                }
            } else {
                Toast.makeText(this, getString(R.string.enter_coordinates_first), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 保存送火键坐标
    private fun saveCoordinates() {
        val xCoordinate = editTextX.text.toString()
        val yCoordinate = editTextY.text.toString()

        if (xCoordinate.isNotEmpty() && yCoordinate.isNotEmpty()) {
            val sharedPref = getSharedPreferences("SkySharedPreferences", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("xCoordinate", xCoordinate)
            editor.putString("yCoordinate", yCoordinate)
            editor.apply()
            Toast.makeText(this, getString(R.string.coordinate_saved), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "X和Y坐标都要输入", Toast.LENGTH_SHORT).show()
        }
    }

    // 加载送火键坐标
    private fun loadCoordinates(): Boolean {
        val sharedPref = getSharedPreferences("SkySharedPreferences", Context.MODE_PRIVATE)
        val xCoordinate = sharedPref.getString("xCoordinate", "")
        val yCoordinate = sharedPref.getString("yCoordinate", "")

        if (!xCoordinate.isNullOrEmpty() && !yCoordinate.isNullOrEmpty()) {
            editTextX.setText(xCoordinate)
            editTextY.setText(yCoordinate)
            return true
        } else {
            return false
        }
    }

    // 检查无障碍服务是否启用
    private fun isAccessibilityServiceEnabled(
        context: Context,
        service: Class<out AccessibilityService>
    ): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
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
            .setTitle(getString(R.string.require_accessibility_service))
            .setMessage("请在设置中启用无障碍服务，以便本工具正常工作。")
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                Toast.makeText(
                    this,
                    "请在“已下载的应用”中找到光遇自动送火，并打开无障碍开关",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivityForResult(intent, REQUEST_ACCESSIBILITY_PERMISSION)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 检查悬浮窗权限
    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    // 请求悬浮窗权限
    private fun requestOverlayPermission() {
        val intent =
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
    }

    @Deprecated("Deprecated in Java")
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
//        if (requestCode == REQUEST_ACCESSIBILITY_PERMISSION) {
//            if (isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
//                val intent = Intent(this, FloatingWindowService::class.java)
//                startService(intent)
//                Toast.makeText(this, "悬浮窗已启动", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "无障碍服务未开启", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1
        private const val REQUEST_ACCESSIBILITY_PERMISSION = 2
    }
}
