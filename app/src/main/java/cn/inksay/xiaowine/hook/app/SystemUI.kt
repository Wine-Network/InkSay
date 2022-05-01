package cn.inksay.xiaowine.hook.app

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import cn.inksay.xiaowine.hook.BaseHook
import cn.inksay.xiaowine.utils.Utils.dp2px
import cn.inksay.xiaowine.utils.Utils.getHttp
import cn.inksay.xiaowine.utils.Utils.isMiui
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import java.io.File
import java.util.*


@SuppressLint("StaticFieldLeak")
object SystemUI : BaseHook() {

    @SuppressLint("StaticFieldLeak") var textView: TextView? = null
    private var updateText: Handler = Handler(Looper.getMainLooper()) { message: Message ->
        Log.i("更新文字：${message.data.getString("value")!!}")
        textView?.text = message.data.getString("value") ?: "网络跟随嫦娥飞去！"
        true
    }

    @SuppressLint("SetTextI18n") override fun init() {
        Log.i("初始化")
        if (!isMiui) {
            Log.i("本设备非MIUI")
            Log.i("本模块仅支持MIUI")
        }
        findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
            Log.i("初始化View")
            val viewGroup = methodHookParam.thisObject as ViewGroup
            val context = viewGroup.context
//            viewGroup.setBackgroundColor(Color.RED)


            val dateTime: TextView = viewGroup.findViewById(context.resources.getIdentifier("date_time", "id", context.packageName))

            textView = TextView(context).apply {
                text = "test" //setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                textSize = dateTime.textSize - 7
                paint.isFakeBoldText = true
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, dp2px(context, 10f), 0, 0) }
                //                layoutParams = dateTime.layoutParams
                try {
                    val file = File(context.filesDir.path + "/font")
                    if (file.exists() && file.isFile && file.canRead()) {
                        typeface = Typeface.createFromFile(context.filesDir.path + "/font")
                        Log.i("加载个性化字体成功")
                    }
                } catch (e: Throwable) {
                    Log.e("设置字体失败：${e.message}")
                }


            }
            viewGroup.addView(textView)
            getText()

        }

        findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "updateLayout" }.hookAfter {
            Log.i("") //
        }
        startTimer(60 * 20 * 1000, getUpdateDateTimer())

    }

    fun getText() {
        Thread {
            if (textView == null) {
                Log.i("textView未初始化，跳过获取")
                return@Thread
            }
            Log.i("获取一言")
            val request = getHttp("https://v1.hitokoto.cn/?encode=text")
            Log.i("request：${request} ")
            if (request != null) {
                Log.i("发送更新")
                updateText.sendMessage(updateText.obtainMessage().also {   // update lyric
                    it.data = Bundle().apply { putString("value", request) }
                })
            }
        }.start()
    }

    private var updateDateTimer: TimerTask? = null

    private fun startTimer(period: Long, timerTask: TimerTask) {
        val timerQueue: ArrayList<TimerTask> = arrayListOf()
        timerQueue.forEach { task -> if (task == timerTask) return }
        timerQueue.add(timerTask)
        val timer = Timer()
        timer.schedule(timerTask, 0, period)
        Log.i("开启 ${timerTask::class} 循环")
    }

    private fun getUpdateDateTimer(): TimerTask {
        if (updateDateTimer == null) {
            updateDateTimer = object : TimerTask() {
                override fun run() {
                    getText()
                }
            }
        }
        return updateDateTimer as TimerTask
    }
}