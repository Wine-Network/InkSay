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

package cn.inksay.xiaowine.config

import android.content.SharedPreferences
import de.robv.android.xposed.XSharedPreferences
import cn.inksay.xiaowine.utils.ConfigUtils

class Config {
    private var config: ConfigUtils

    constructor(xSharedPreferences: XSharedPreferences?) {
        config = ConfigUtils(xSharedPreferences)
    }

    constructor(sharedPreferences: SharedPreferences) {
        config = ConfigUtils(sharedPreferences)
    }

    fun update() {
        config.update()
    }

    fun clear() {
        config.clearConfig()
    }

    fun getDebug(): Boolean {
        return config.optBoolean("Debug", false)
    }

    fun setDebug(b: Boolean) {
        return config.put("Debug", b)
    }

    fun getSwitch(): Boolean {
        return config.optBoolean("Switch", false)
    }

    fun setSwitch(b: Boolean) {
        return config.put("Switch", b)
    }

    fun getUpdateInterval(): Int {
        return config.optInt("UpdateInterval", 60)
    }

    fun setUpdateInterval(i: Int) {
        return config.put("UpdateInterval", i)
    }
    fun getAutoHide(): Boolean {
        return config.optBoolean("AutoHide", true)
    }
}
