package cn.inksay.xiaowine.hook.app


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import cn.inksay.xiaowine.hook.BaseHook
import cn.inksay.xiaowine.utils.ActivityUtils
import cn.inksay.xiaowine.utils.LogUtils
import cn.inksay.xiaowine.utils.Utils
import cn.inksay.xiaowine.utils.Utils.isNull
import cn.inksay.xiaowine.utils.XposedOwnSP
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import java.util.*

@SuppressLint("StaticFieldLeak")
object NewSystemUI : BaseHook() {

    private var sayText = ""
    private var context: Context? = null
    private var ViewGroupList: List<ViewGroup> = listOf()
    private var TextViewList: List<TextView> = listOf()
    private var isRegister: Boolean = false
    private lateinit var updateTextHandler: Handler
    private val inkSayReceiver by lazy { SystemUI.InkSayReceiver() }

    override fun init() {
        updateTextHandler = Handler(Looper.getMainLooper()) {
            LogUtils.i("刷新文字")
            TextViewList.forEach { view ->
                run { view.text = sayText }
            }
            true
        }
        if (!Utils.isMiui) {
            LogUtils.i("本设备非MIUI")
            LogUtils.i("本模块仅支持MIUI")
        }
        if (!XposedOwnSP.config.getSwitch()) {
            LogUtils.i("总开关未打开")
            return
        }

        findMethod("com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
            val qsViewGroup = methodHookParam.thisObject as ViewGroup
            context = qsViewGroup.context
            ViewGroupList.plus(qsViewGroup)
            registerBroadcast()
        }
        findMethod("com.android.systemui.qs.MiuiNotificationHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
            val notificationViewGroup = methodHookParam.thisObject as ViewGroup
            context = notificationViewGroup.context
            ViewGroupList.plus(notificationViewGroup)
            registerBroadcast()
        }
        findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
            val miuiQSViewGroup = methodHookParam.thisObject as ViewGroup
            context = miuiQSViewGroup.context
            ViewGroupList.plus(miuiQSViewGroup)
            registerBroadcast()
        }


        findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "updateLayout" }.hookAfter {

        }
        if (isInit){
            initView()
            UpdateTask().cancel()
            if (XposedOwnSP.config.getUpdateInterval() != 0) {
                LogUtils.i(XposedOwnSP.config.getUpdateInterval() * 1000)
                Timer().scheduleAtFixedRate(SystemUI.UpdateTask(), 0, (XposedOwnSP.config.getUpdateInterval() * 1000).toLong())
                LogUtils.i("开始任务")
            }
        }
    }

    private fun registerBroadcast() {
        if (!isRegister) {
            runCatching { context?.unregisterReceiver(inkSayReceiver) }
            context?.registerReceiver(inkSayReceiver, IntentFilter().apply { addAction("InkSay_Server") })
            isRegister = true
        }

    }

    private fun initView() {
        ViewGroupList.forEach { view ->
            run {
                val textView = TextView(context).apply {
                    text = sayText
                    //setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    textSize = 15f
                    setTextColor(Color.WHITE)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, Utils.dp2px(context, 20f), 0, 0) }
                }
                TextViewList.plus(textView)
                view.addView(textView)
            }
        }
    }

    fun getDateUpdate() {
        Thread {
            if (TextViewList.isEmpty()) {
                LogUtils.i("textView未初始化，跳过获取")
                return@Thread
            }
            LogUtils.i("获取一言")
            val request = ActivityUtils.getHttp("https://v1.hitokoto.cn/?encode=text")
            sayText = request ?: "大海中的麻子：无处可寻"
            updateTextHandler.sendMessage(updateTextHandler.obtainMessage())
            LogUtils.i(if (request.isNull()) "获取失败" else "发送刷新:${sayText}")
        }.start()
    }


    internal class UpdateTask : TimerTask() {
        override fun run() {
            getDateUpdate()
        }
    }

    class InkSayReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            LogUtils.i("收到广播，开始刷新")
            SystemUI.getDateUpdate()
        }
    }

}