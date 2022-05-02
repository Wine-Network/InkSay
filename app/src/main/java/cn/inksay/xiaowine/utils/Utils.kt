package cn.inksay.xiaowine.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import cn.inksay.xiaowine.BuildConfig
import de.robv.android.xposed.XSharedPreferences
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

object Utils {
    val isMiui: Boolean = isPresent("android.provider.MiuiSettings")

    fun dp2px(context: Context, dpValue: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.resources.displayMetrics).toInt()


    fun getDate(): String {
       return returnShell("getprop ro.build.date.utc")
    }
    fun getIncremental(): String {
       return returnShell("getprop ro.build.version.incremental")
    }
    private fun returnShell(command: String): String {
        try {
            val bufferedReader = BufferedReader(InputStreamReader(Runtime.getRuntime().exec(command).inputStream), 1024)
            val buffer: String = bufferedReader.readLine()
            bufferedReader.close()
            return buffer
        } catch (ignored: IOException) {
        }
        return ""
    }
    // 判断class是否存在
    private fun isPresent(name: String): Boolean {
        return try {
            Objects.requireNonNull(Thread.currentThread().contextClassLoader).loadClass(name)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    fun Any?.isNull(callback: () -> Unit) {
        if (this == null) callback()
    }

    fun Any?.isNotNull(callback: () -> Unit) {
        if (this != null) callback()
    }

    fun Any?.isNull() = this == null

    fun Any?.isNotNull() = this != null

    @SuppressLint("WorldReadableFiles") @Suppress("DEPRECATION")
    fun getSP(context: Context, key: String?): SharedPreferences? {
        return context.createDeviceProtectedStorageContext().getSharedPreferences(key, Context.MODE_WORLD_READABLE)
    }

    fun getPref(key: String?): XSharedPreferences? {
        val pref = XSharedPreferences(BuildConfig.APPLICATION_ID, key)
        return if (pref.file.canRead()) pref else null
    }

}