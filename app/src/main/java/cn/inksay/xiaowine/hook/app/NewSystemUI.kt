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
import cn.inksay.xiaowine.utils.Utils.isNotNull
import cn.inksay.xiaowine.utils.Utils.isNull
import cn.inksay.xiaowine.utils.XposedOwnSP.config
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import de.robv.android.xposed.XC_MethodHook
import java.util.*


@SuppressLint("StaticFieldLeak")
object NewSystemUI : BaseHook() {
    private val isTextViewMaps = HashMap<String, Boolean>().apply {
        put("qsViewGroup", false)
        put("notificationViewGroup", false)
        put("miuiQSViewGroup", false)
    }
    private val TextViewMaps = HashMap<String, TextView?>().apply {
        put("qsViewGroup", null)
        put("notificationViewGroup", null)
        put("miuiQSViewGroup", null)
    }
    private val ViewGroupMaps = HashMap<String, ViewGroup?>().apply {
        put("qsViewGroup", null)
        put("notificationViewGroup", null)
        put("miuiQSViewGroup", null)
    }
    private var sayText = "123"
    private var context: Context? = null
    private var ViewGroupList: ArrayList<ViewGroup> = arrayListOf()
    private var TextViewList: ArrayList<TextView> = arrayListOf()
    private var isRegister: Boolean = false
    private lateinit var updateTextHandler: Handler
    private val inkSayReceiver by lazy { InkSayReceiver() }

    override fun init() {
//        if (!isInit) {
        updateTextHandler = Handler(Looper.getMainLooper()) {
            LogUtils.i("刷新文字")
            //XToast.makeText(context!!, "墨•言：刷新文字").show()
            TextViewMaps.forEach { (_, view) ->
                run { view?.text = sayText }
            }
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
            if (isTextViewMaps["qsViewGroup"] == false) {
                val qsViewGroup = methodHookParam.thisObject as ViewGroup
                context = qsViewGroup.context
                ViewGroupMaps["qsViewGroup"] = qsViewGroup
                initView()
                registerBroadcast()
            }


        }
        findMethod("com.android.systemui.qs.MiuiNotificationHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
            if (isTextViewMaps["notificationViewGroup"] == false) {
                val notificationViewGroup = methodHookParam.thisObject as ViewGroup
                context = notificationViewGroup.context
                ViewGroupMaps["notificationViewGroup"] = notificationViewGroup
                initView()
                registerBroadcast()
            }
        }
        findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "onFinishInflate" }.hookAfter { methodHookParam ->
            if (isTextViewMaps["miuiQSViewGroup"] == false) {
                val miuiQSViewGroup = methodHookParam.thisObject as ViewGroup
                context = miuiQSViewGroup.context
                ViewGroupMaps["miuiQSViewGroup"] = miuiQSViewGroup
                initView()
                registerBroadcast()
            }
        }

        catchNoClass {
            findMethod("com.android.systemui.qs.MiuiNotificationHeaderView") { name == "updateLayout" }.hookAfter {
                LogUtils.i("1")
                autoHide(it)
            }
        }
        catchNoClass {
            findMethod("com.android.systemui.qs.MiuiQSHeaderView") { name == "updateLayout" }.hookAfter {
                autoHide(it)
            }
        }
        catchNoClass {
            findMethod("com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView") { name == "updateLayout" }.hookAfter {
                LogUtils.i("3")
                autoHide(it)
            }
        }

        UpdateTask().cancel()
        LogUtils.i(config.getUpdateInterval() * 1000)
        if (config.getUpdateInterval() != 0) {
            Timer().scheduleAtFixedRate(UpdateTask(), 0, (config.getUpdateInterval() * 1000).toLong())
            LogUtils.i("开始任务")
        }
//        }
    }

    private fun autoHide(methodHookParam: XC_MethodHook.MethodHookParam) {
        val viewGroup = methodHookParam.thisObject as ViewGroup
        val context = viewGroup.context
        LogUtils.i("mOrientation")
        TextViewMaps.forEach { (_, view) ->
            run {
                LogUtils.i("VISIBLE")
                LogUtils.i(view?.visibility)
                view?.visibility = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) View.GONE else View.VISIBLE
                LogUtils.i(view?.visibility)

            }
        }
    }


    private fun initView() {
        ViewGroupMaps.forEach { (name, view) ->
            run {
                view.isNotNull {
                    if (isTextViewMaps[name] == false) {
                        LogUtils.i("初始化$name")
                        TextViewMaps[name].isNull {
                            val textView = TextView(context).apply {
                                text = sayText
                                //setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                                textSize = 15f
                                setTextColor(Color.WHITE)
                                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, Utils.dp2px(context, 20f), 0, 0) }
                            }
                            view!!.addView(textView)
                            isTextViewMaps[name] = true
                            TextViewMaps[name] = textView
                        }
                    }
                }
            }
        }
    }

    fun getDateUpdate() {
        Thread {
            //if (TextViewList.isNotEmpty()) {
                LogUtils.i("获取一言")
                val request = ActivityUtils.getHttp("https://v1.hitokoto.cn/?encode=text")
                sayText = request ?: "大海中的麻子：无处可寻"
                updateTextHandler.sendMessage(updateTextHandler.obtainMessage())
                LogUtils.i(if (request.isNull()) "获取失败" else "发送刷新:${sayText}")
            //} else {
              //  LogUtils.i("textView未初始化，跳过获取")
            //}
        }.start()
    }

    private fun catchNoClass(callback: () -> Unit) {
        try {
            callback()
        } catch (e: NoSuchMethodException) {
            LogUtils.i("${e.message} 未找到class：${callback.javaClass.name}")
        }
    }

    private fun registerBroadcast() {
        if (!isRegister) {
            //getDateUpdate()
            runCatching { context?.unregisterReceiver(inkSayReceiver) }
            context?.registerReceiver(inkSayReceiver, IntentFilter().apply { addAction("InkSay_Server") })
            isRegister = true
        }

    }

    internal class UpdateTask : TimerTask() {
        override fun run() {
            getDateUpdate()
        }
    }

    class InkSayReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            LogUtils.i("收到广播，开始刷新")
            getDateUpdate()
        }
    }

}