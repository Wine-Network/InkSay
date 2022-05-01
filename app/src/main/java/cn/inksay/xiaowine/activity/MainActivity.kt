package cn.inksay.xiaowine.activity



import android.content.Intent
import android.os.Bundle
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.dialog.MIUIDialog
import cn.inksay.xiaowine.R
import cn.inksay.xiaowine.utils.ActivityOwnSP
import cn.inksay.xiaowine.utils.Utils
import kotlin.system.exitProcess

class MainActivity : MIUIActivity() {
    private val activity = this
    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityOwnSP.activity = activity
        if (!checkLSPosed()) isLoad = false
        super.onCreate(savedInstanceState)
    }

    private fun checkLSPosed(): Boolean {
        return try {
            Utils.getSP(activity, "InkSay_Config")?.let { setSP(it) }
            true
        } catch (e: Throwable) {
            MIUIDialog(this) {
                setTitle(R.string.Tips)
                setMessage(R.string.NotSupport)
                setRButton(R.string.ReStart) {
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    exitProcess(0)
                }
                setCancelable(false)
            }.show()
            false
        }
    }
}