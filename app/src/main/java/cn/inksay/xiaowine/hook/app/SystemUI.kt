package cn.inksay.xiaowine.hook.app

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import cn.inksay.xiaowine.hook.BaseHook
import cn.inksay.xiaowine.utils.ActivityUtils
import cn.inksay.xiaowine.utils.LogUtils
import cn.inksay.xiaowine.utils.Utils
import cn.inksay.xiaowine.utils.Utils.isNotNull
import cn.inksay.xiaowine.utils.Utils.isNull
import cn.inksay.xiaowine.utils.XposedOwnSP.config
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import java.util.*

@SuppressLint("StaticFieldLeak")
object SystemUI : BaseHook() {

    @SuppressLint("StaticFieldLeak") var textView: TextView? = null
    private var context: Context? = null
    private var updateTextHandler: Handler? = null
    private var waitUpate = false

    @SuppressLint("SetTextI18n") override fun init() {
        val inkSayReceiver by lazy { InkSayReceiver() }
        val updateMargins = Handler(Looper.getMainLooper()) { message ->
            LogUtils.i("${message.obj} ${textView.isNull()}")
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(0, message.obj as Int, 0, 0)
            textView!!.layoutParams = layoutParams

            true
        }
        updateTextHandler = Handler(Looper.getMainLooper()) { message: Message ->
            LogUtils.i("更新文字：${message.data.getString("value")!!}")
            textView?.text = message.data.getString("value") ?: "大海中的麻子：无处可寻"
            true
        }
        LogUtils.i("初始化")
        if (!Utils.isMiui) {
            LogUtils.i("本设备非MIUI")
            LogUtils.i("本模块仅支持MIUI")
        }
        if (!config.getSwitch()) {
            LogUtils.i("总开关未打开")
            return
        }
        findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
            LogUtils.i("初始化View")
            val viewGroup = methodHookParam.thisObject as ViewGroup
            context = viewGroup.context
            textView = TextView(context).apply {
                text = "test" //setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                textSize = 15f
                setTextColor(Color.WHITE)
//                paint.isFakeBoldText = true
//                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, Utils.dp2px(context, 15f), 0, 0) }

            }
            viewGroup.addView(textView)
            runCatching { context!!.unregisterReceiver(inkSayReceiver) }
            context!!.registerReceiver(inkSayReceiver, IntentFilter().apply { addAction("InkSay_Server") })
        }

        findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "updateLayout" }.hookAfter {
            LogUtils.i("")
            getDateUpdate()
            context.isNotNull {
                LogUtils.i(context!!.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                updateMargins.sendMessage(updateMargins.obtainMessage().also {
                    it.obj = if (context!!.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 0 else Utils.dp2px(context!!, 15f)
                })

            }
        }
        if (config.getUpdateInterval() != 0) {
            startTimer(((config.getUpdateInterval() * 1000).toLong()), getUpdateDateTimer())
        }
    }

    fun getDateUpdate() {
        if (!waitUpate) {
            return
        } else {
            waitUpate = true
        }
        Thread {
            textView.isNull {
                LogUtils.i("textView未初始化，跳过获取")
                return@isNull
            }
            LogUtils.i("获取一言")
            val request = ActivityUtils.getHttp("https://v1.hitokoto.cn/?encode=text")
            if (request.isNull()) {
                LogUtils.i("获取失败")
            } else {
                LogUtils.i("发送更新")
                updateTextHandler!!.sendMessage(updateTextHandler!!.obtainMessage().also {
                    it.data = Bundle().apply { putString("value", request) }
                })
            }
        }.start()
    }

    class InkSayReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            getDateUpdate()
        }
    }


    private var updateDateTimer: TimerTask? = null

    private fun startTimer(period: Long, timerTask: TimerTask) {
        val timerQueue: ArrayList<TimerTask> = arrayListOf()
        timerQueue.forEach { task -> if (task == timerTask) return }
        timerQueue.add(timerTask)
        val timer = Timer()
        timer.schedule(timerTask, 0, period)
        LogUtils.i("开启 ${timerTask::class} 循环")
    }

    private fun getUpdateDateTimer(): TimerTask {
        if (updateDateTimer == null) {
            updateDateTimer = object : TimerTask() {
                override fun run() {
                    getDateUpdate()
                }
            }
        }
        return updateDateTimer as TimerTask
    }
}