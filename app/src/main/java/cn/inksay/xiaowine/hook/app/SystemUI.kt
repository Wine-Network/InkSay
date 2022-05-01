package cn.inksay.xiaowine.hook.app

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import cn.inksay.xiaowine.utils.XposedOwnSP.config
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import java.util.*

@SuppressLint("StaticFieldLeak")
object SystemUI : BaseHook() {

    @SuppressLint("StaticFieldLeak") var textView: TextView? = null
    private var context: Context? = null
    private var updateText: Handler = Handler(Looper.getMainLooper()) { message: Message ->
        LogUtils.i("更新文字：${message.data.getString("value")!!}")
        textView?.text = message.data.getString("value") ?: "大海中的麻子：无处可寻"
        true
    }
    private val inkSayReceiver by lazy { InkSayReceiver() }

    @SuppressLint("SetTextI18n") override fun init() {

        LogUtils.i("初始化")
        if (!Utils.isMiui) {
            LogUtils.i("本设备非MIUI")
            LogUtils.i("本模块仅支持MIUI")
        }
        findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
            LogUtils.i("初始化View")
            val viewGroup = methodHookParam.thisObject as ViewGroup
            context = viewGroup.context
//            viewGroup.setBackgroundColor(Color.RED)

//            val dateTime: TextView = viewGroup.findViewById(context!!.resources.getIdentifier("date_time", "id", context!!.packageName))

            textView = TextView(context).apply {
                text = "test" //setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                textSize = 15f
                paint.isFakeBoldText = true
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, Utils.dp2px(context, 15f), 0, 0) }
                //                layoutParams = dateTime.layoutParams
//                try {
//                    val file = File(context.filesDir.path + "/font")
//                    if (file.exists() && file.isFile && file.canRead()) {
//                        typeface = Typeface.createFromFile(context.filesDir.path + "/font")
//                        LogUtils.i("加载个性化字体成功")
//                    }
//                } catch (e: Throwable) {
//                    LogUtils.i("设置字体失败：${e.message}")
//                }
            }
            viewGroup.addView(textView)
            runCatching { context!!.unregisterReceiver(inkSayReceiver) }
            context!!.registerReceiver(inkSayReceiver, IntentFilter().apply { addAction("InkSay_Server") })
        }

        findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "updateLayout" }.hookAfter {
            LogUtils.i("")
            getDateUpdate()
        }
        startTimer(60 * 20 * 1000, getUpdateDateTimer())

    }

    fun getDateUpdate() {
        Thread {
            if (textView == null) {
                LogUtils.i("textView未初始化，跳过获取")
                return@Thread
            }
            LogUtils.i("获取一言")
            val request = ActivityUtils.getHttp("https://v1.hitokoto.cn/?encode=text")
            if (request == null) {
                LogUtils.i("获取失败")
                context.isNotNull {
                    if (config.getIsToast()) Utils.showToast(context!!, "获取失败")
                }
            } else {
                LogUtils.i("发送更新")
                updateText.sendMessage(updateText.obtainMessage().also {
                    it.data = Bundle().apply { putString("value", request) }
                })
            }
        }.start()
    }

    class InkSayReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (config.getIsToast()) Utils.showToast(context, "开始更新")
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