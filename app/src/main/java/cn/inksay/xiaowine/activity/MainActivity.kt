package cn.inksay.xiaowine.activity


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import cn.inksay.xiaowine.utils.ActivityOwnSP
import cn.inksay.xiaowine.utils.LogUtils
import cn.inksay.xiaowine.utils.Utils
import java.lang.reflect.Array.set
import kotlin.system.exitProcess

class MainActivity : Activity() {
    private val activity = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityOwnSP.activity = activity
        checkLSPosed()
    }

    private fun checkLSPosed(): Boolean {
        return try {
            Utils.getSP(activity, "LikSay_Config")?.let { }
            true
        } catch (e: Throwable) {
            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            LogUtils.e(e.message.toString())
            AlertDialog.Builder(activity).apply {
                setMessage("未激活")
                setPositiveButton("重启") { _, _ ->
                    run {
                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        exitProcess(0)
                    }
                }
                create()
                show()
            }
            false
        }
    }
}