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

package org.cubewhy.qbychat.util.protobuf

import org.cubewhy.qbychat.websocket.user.v1.RegisterAccountResponse

object RegisterAccountResponsesV1 {
    fun usernameExists(): RegisterAccountResponse = build(RegisterAccountResponse.Status.USERNAME_EXISTS)

    fun badUsername(): RegisterAccountResponse = build(RegisterAccountResponse.Status.BAD_USERNAME)

    fun success(accountId: String): RegisterAccountResponse = build(RegisterAccountResponse.Status.SUCCESS, accountId)

    private fun build(status: RegisterAccountResponse.Status, accountId: String? = null): RegisterAccountResponse =
        RegisterAccountResponse.newBuilder().apply {
            this.status = status
            accountId?.let { this.accountId = it }
        }.build()
}
