package cn.inksay.xiaowine.hook.app

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import cn.inksay.xiaowine.hook.BaseHook
import cn.inksay.xiaowine.utils.ActivityUtils
import cn.inksay.xiaowine.utils.LogUtils
import cn.inksay.xiaowine.utils.Utils
import cn.inksay.xiaowine.utils.Utils.isNew
import cn.inksay.xiaowine.utils.Utils.isNotNull
import cn.inksay.xiaowine.utils.Utils.isNull
import cn.inksay.xiaowine.utils.XposedOwnSP.config
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import java.util.*

@SuppressLint("StaticFieldLeak")
object SystemUI : BaseHook() {

    @SuppressLint("StaticFieldLeak")
    var textView: TextView? = null
    private var context1: Context? = null
    private var context2: Context? = null
    private var context3: Context? = null
    private var viewGroup1: ViewGroup? = null
    private var viewGroup2: ViewGroup? = null
    private var viewGroup3: ViewGroup? = null
    private var updateTextHandler: Handler? = null
    private var waitUpdate = false
    private var sayText = ""
    private val inkSayReceiver by lazy { InkSayReceiver() }

    @SuppressLint("SetTextI18n") override fun init() {
        updateTextHandler = Handler(Looper.getMainLooper()) {
            LogUtils.i("刷新文字")
            textView?.text = sayText
            true
        }
        if (!Utils.isMiui) {
            LogUtils.i("本设备非MIUI")
            LogUtils.i("本模块仅支持MIUI")
        }
        if (!config.getSwitch()) {
            LogUtils.i("总开关未打开")
            return
        }

        findMethod("com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
            viewGroup1 = methodHookParam.thisObject as ViewGroup
            context1 = viewGroup1!!.context
            viewGroup1!!.setBackgroundColor(Color.BLACK)
            init(context1!!,viewGroup1!!)
        }
     //   if (isNew()) {
            findMethod("com.android.systemui.qs.MiuiNotificationHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
                viewGroup2 = methodHookParam.thisObject as ViewGroup
                context2= viewGroup2!!.context
                viewGroup2!!.setBackgroundColor(Color.BLACK)
                init(context2!!,viewGroup2!!)
            }
      //  } else {
            findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
                viewGroup3 = methodHookParam.thisObject as ViewGroup
                context3 = viewGroup3!!.context
                viewGroup3!!.setBackgroundColor(Color.BLACK)
                init(context3!!,viewGroup3!!)
            }
       // }

        findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "updateLayout" }.hookAfter {
            if (textView.isNotNull()) {
                textView!!.visibility = if (context1!!.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) View.GONE else View.VISIBLE
            }
        }
        UpdateTask().cancel()
        if (config.getUpdateInterval() != 0) {
            LogUtils.i(config.getUpdateInterval() * 1000)
            Timer().scheduleAtFixedRate(UpdateTask(), 0, (config.getUpdateInterval() * 1000).toLong())
            println("其他任务")
        }
    }

//    fun initView(context: Context, viewGroup: ViewGroup) {
//        if (!(Utils.getDate().toInt() >= 1647014400 && !Utils.getIncremental().endsWith("DEV") && !Utils.getIncremental().endsWith("XM"))) {
//            val bigTimeId = context.resources.getIdentifier("big_time", "id", context.packageName)
//            val bigTime: TextView = viewGroup.findViewById(bigTimeId)
//            val dateTimeId = context.resources.getIdentifier("date_time", "id", context.packageName)
//            val dateTime: TextView = viewGroup.findViewById(dateTimeId)
//            viewGroup.removeView(bigTime)
//            viewGroup.removeView(dateTime)
//            init(context, viewGroup)
//            viewGroup.addView(bigTime)
//            viewGroup.addView(dateTime)
//        } else {
//            init(context, viewGroup)
//        }
//    }

    private fun init(context: Context, viewGroup: ViewGroup) {
        textView = TextView(context).apply {
            text = sayText
            //setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            textSize = 15f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, Utils.dp2px(context, 15f), 0, 0) }
        }
        viewGroup.addView(textView)
        runCatching { context.unregisterReceiver(inkSayReceiver) }
        context.registerReceiver(inkSayReceiver, IntentFilter().apply { addAction("InkSay_Server") })
    }

    fun getDateUpdate() {
        if (waitUpdate) {
            return
        } else {
            waitUpdate = true
        }
        Thread {
            textView.isNull {
                LogUtils.i("textView未初始化，跳过获取")
                return@isNull
            }
            LogUtils.i("获取一言")
            val request = ActivityUtils.getHttp("https://v1.hitokoto.cn/?encode=text")
            sayText = request ?: "大海中的麻子：无处可寻"
            updateTextHandler!!.sendMessage(updateTextHandler!!.obtainMessage())
            LogUtils.i(if (request.isNull()) "获取失败" else "发送刷新:${sayText}")
            waitUpdate = false
        }.start()
    }

    //任务广播
    class InkSayReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            LogUtils.i("收到广播，开始刷新")
            getDateUpdate()
        }
    }

    internal class UpdateTask : TimerTask() {
        override fun run() {
            getDateUpdate()
        }
    }

}