/*
 * StatusBarLyric
 * Copyright (C) 2021-2022 fkj@fkj233.cn
 * https://github.com/577fkj/StatusBarLyric
 *
 * This software is free opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by 577fkj.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/577fkj/StatusBarLyric/blob/main/LICENSE>.
 */

package cn.inksay.xiaowine.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import app.xiaowine.xtoast.XToast
import cn.fkj233.ui.dialog.MIUIDialog
import cn.inksay.xiaowine.BuildConfig
import cn.inksay.xiaowine.R
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL

object ActivityUtils {
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    fun voidShell(command: String, isSu: Boolean) {
        try {
            if (isSu) {
                val p = Runtime.getRuntime().exec("su")
                val outputStream = p.outputStream
                val dataOutputStream = DataOutputStream(outputStream)
                dataOutputStream.writeBytes(command)
                dataOutputStream.flush()
                dataOutputStream.close()
                outputStream.close()
            } else {
                Runtime.getRuntime().exec(command)
            }
        } catch (ignored: Throwable) {
        }
    }

    //清除配置
    fun cleanConfig(activity: Activity) {
        ActivityOwnSP.ownSPConfig.clear()
        showToast(activity, activity.getString(R.string.ResetSuccess))
        activity.finishActivity(0)
    }

    @Suppress("DEPRECATION") fun showToast(context: Context, message: String) {
        try {
            handler.post {
                XToast.makeText(context, ">墨•言：${message}", toastIcon = context.resources.getDrawable(R.mipmap.ic_launcher_round)).show()
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    //检查更新
    fun checkUpdate(activity: Activity) {
        val handler = Handler(Looper.getMainLooper()) { message: Message ->
            val data: String = message.data.getString("value") as String
            try {
                val jsonObject = JSONObject(data)
                if (jsonObject.getString("tag_name").split("v").toTypedArray()[1].toInt() > BuildConfig.VERSION_CODE) {
                    MIUIDialog(activity) {
                        setTitle(String.format("%s [%s]", activity.getString(R.string.NewVer), jsonObject.getString("name")))
                        setMessage(jsonObject.getString("body").replace("#", ""))
                        setRButton(R.string.Update) {
                            try {
                                val uri: Uri = Uri.parse(jsonObject.getJSONArray("assets").getJSONObject(0).getString("browser_download_url"))
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                activity.startActivity(intent)
                            } catch (e: JSONException) {
                                showToast(activity, activity.getString(R.string.GetNewVerError) + e)
                            }
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                    }.show()
                } else {
                   showToast(activity, activity.getString(R.string.NoVerUpdate))
                }
            } catch (ignored: JSONException) {
                showToast(activity, activity.getString(R.string.CheckUpdateError))
            }

            true
        }
        Thread {
            val value = getHttp("https://api.github.com/repos/577fkj/StatusBarLyric/releases/latest")
            if (value != null) {
                handler.obtainMessage().let {
                    it.data = Bundle().apply {
                        putString("value", value)
                    }
                    handler.sendMessage(it)
                }
            } else {
                showToast(activity, activity.getString(R.string.CheckUpdateFailed))
            }
        }.start()
    }

    fun openUrl(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun getHttp(Url: String): String? {
        try {
            val connection = URL(Url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            return reader.readLine()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}