package cn.inksay.xiaowine.activity


import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.view.SwitchV
import cn.fkj233.ui.activity.view.TextSummaryV
import cn.fkj233.ui.activity.view.TextV
import cn.fkj233.ui.dialog.MIUIDialog
import cn.inksay.xiaowine.BuildConfig
import cn.inksay.xiaowine.R
import cn.inksay.xiaowine.utils.ActivityOwnSP
import cn.inksay.xiaowine.utils.ActivityUtils
import cn.inksay.xiaowine.utils.Utils
import kotlin.system.exitProcess

class SettingActivity : MIUIActivity() {
    private val activity = this
    private var updateConfig = false

    init {
        setAllCallBacks {
            updateConfig = true
        }
        initView {
            registerMain(getString(R.string.App_name), false) {
                TextWithSwitch(TextV(resId = R.string.Switch), SwitchV("Switch", true))
                TextSummaryArrow(TextSummaryV(textId = R.string.update, onClickListener = {
                    activity.sendBroadcast(Intent().apply { action = "InkSay_Server" })
                }))
                Text(resId = R.string.UpdateInterval, onClickListener = {
                    MIUIDialog(activity) {
                        setTitle(R.string.UpdateInterval)
                        setMessage(R.string.UpdateIntervalTips)
                        setEditText(ActivityOwnSP.ownSPConfig.getUpdateInterval().toString(), "60")
                        setRButton(R.string.Ok) {
                            if (getEditText().isNotEmpty()) {
                                try {
                                    val value = getEditText().toInt()
                                    if (value in (1..1440)) {
                                        ActivityOwnSP.ownSPConfig.setUpdateInterval(value)
                                        updateConfig = true
                                        dismiss()
                                        return@setRButton
                                    }
                                } catch (_: Throwable) {
                                }
                            }
                            ActivityUtils.showToast(activity, getString(R.string.InputError))
                            ActivityOwnSP.ownSPConfig.setUpdateInterval(60)
                            updateConfig = true
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                    }.show()
                })
                SeekBarWithText("UpdateInterval", 1, 1440, defaultProgress = 60)
                Line()
//                TextWithSwitch(TextV(resId = R.string.IsToast), SwitchV("IsToast", true))
                TextWithSwitch(TextV(resId = R.string.HideDeskIcon), SwitchV("HLauncherIcon", customOnCheckedChangeListener = {
                    packageManager.setComponentEnabledSetting(ComponentName(activity, "${BuildConfig.APPLICATION_ID}.launcher"), if (it) {
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    } else {
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    }, PackageManager.DONT_KILL_APP)
                }))
                TextWithSwitch(TextV(resId = R.string.DebugMode), SwitchV("Debug"))

                TextSummaryArrow(TextSummaryV(textId = R.string.ResetModule, onClickListener = {
                    MIUIDialog(activity) {
                        setTitle(R.string.ResetModuleDialog)
                        setMessage(R.string.ResetModuleDialogTips)
                        setRButton(R.string.Ok) {
                            ActivityUtils.cleanConfig(activity)
                            updateConfig = true
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                    }.show()
                }))
                TextSummaryArrow(TextSummaryV(textId = R.string.ReStartSystemUI, onClickListener = {
                    MIUIDialog(activity) {
                        setTitle(R.string.RestartUI)
                        setMessage(R.string.RestartUITips)
                        setRButton(R.string.Ok) {
                            ActivityUtils.voidShell("pkill -f com.android.systemui", true)
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                    }.show()
                }))
                Line()
                TitleText("Module Version")
                Text("${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})-${BuildConfig.BUILD_TYPE}")
                TextV()
            }

            registerMenu(getString(R.string.Menu)) {}
        }
    }

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