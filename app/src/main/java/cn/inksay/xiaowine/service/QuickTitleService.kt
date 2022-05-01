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

package cn.inksay.xiaowine.service

import android.content.Intent
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import cn.inksay.xiaowine.R
import cn.inksay.xiaowine.config.Config
import cn.inksay.xiaowine.utils.Utils

class QuickTitleService : TileService() {
    private lateinit var tile: Tile

    override fun onClick() {
        super.onClick()
//        val config: Config? = Utils.getSP(baseContext, "InkSay_Config")?.let { Config(it) }
//        config?.setLyricService(!config.getLyricService())
//        config?.let { set(it) }
        application.sendBroadcast(Intent().apply { action = "InkSay_Server" })
    }

//    fun set(config: Config) {
//        tile.icon = Icon.createWithResource(this, R.mipmap.ic_launcher_round)
//        tile.label = getString(R.string.QuickTitle)
//        tile.contentDescription = getString(R.string.QuickTitle)
//        tile.state = if (config.getLyricService()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
//        tile.updateTile()
//        application.sendBroadcast(Intent().apply {
//            action = "InkSay_Server"
//            putExtra("type")
//        })
//    }

//    override fun onStartListening() {
//        super.onStartListening()
//        val config: Config? = Utils.getSP(baseContext, "InkSay_Config")?.let { Config(it) }
//        tile = qsTile
//        config?.let { set(it) }
//    }
}

