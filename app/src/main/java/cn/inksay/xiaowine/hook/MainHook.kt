package cn.inksay.xiaowine.hook

import cn.inksay.xiaowine.hook.app.NewSystemUI
import cn.inksay.xiaowine.utils.LogUtils
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.Log.logexIfThrow
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage


private const val PACKAGE_NAME_HOOKED = "com.android.systemui"
private const val TAG = "InkSay"

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit /* Optional */ {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        Log.e(lpparam.packageName)
        if (lpparam.packageName == PACKAGE_NAME_HOOKED) { // Init EzXHelper
            EzXHelperInit.initHandleLoadPackage(lpparam)
            EzXHelperInit.setLogTag(TAG)
            EzXHelperInit.setToastTag(TAG)
            initHooks(NewSystemUI)
        }
    }

    // Optional
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    private fun initHooks(vararg hook: BaseHook) {
        hook.forEach {
            runCatching {
                if (it.isInit) return@forEach
                it.init()
                it.isInit = true
                LogUtils.i("Hook 初始化: ${it.javaClass.simpleName}")
            }.logexIfThrow("Hook 初始化失败: ${it.javaClass.simpleName}")
        }
    }
}