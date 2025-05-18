/*
 * Copyright (c) 2025. All rights reserved.
 *
 * This file is a part of the QbyChat project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.cubewhy.qbychat.domain.model

import org.cubewhy.qbychat.common.v1.Platform as PbPlatform

data class ClientMetadata(
    val name: String,
    val version: String,
    val platform: Platform,
) {

    enum class Platform {
        WINDOWS, LINUX, MACOS, ANDROID, IOS, BROWSER, UNKNOWN;

        companion object {
            fun fromProtobuf(proto: PbPlatform): ClientMetadata.Platform {
                return when (proto) {
                    PbPlatform.ANDROID -> ANDROID
                    PbPlatform.IOS -> IOS
                    PbPlatform.WINDOWS -> WINDOWS
                    PbPlatform.LINUX -> LINUX
                    PbPlatform.OSX -> MACOS
                    PbPlatform.BROWSER -> BROWSER

                    else -> UNKNOWN
                }
            }
        }
    }
}