package me.wcy.music.account.service

import android.app.Application
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.wcy.common.ext.toUnMutable
import me.wcy.common.model.CommonResult
import me.wcy.music.account.AccountApi
import me.wcy.music.account.AccountPreference
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.ext.accessEntryPoint
import me.wcy.music.net.NetUtils.toJsonBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by wangchenyan.top on 2023/8/25.
 */
@Singleton
class UserService @Inject constructor() {
    private val _profile = MutableStateFlow(AccountPreference.profile)
    val profile = _profile.toUnMutable()

    fun getCookie(): String {
        return AccountPreference.cookie
    }

    fun isLogin(): Boolean {
        return profile.value != null
    }

    suspend fun login(cookie: String): CommonResult<ProfileData> {
        AccountPreference.cookie = cookie
        val res = kotlin.runCatching {
            AccountApi.get().getLoginStatus(body = emptyMap<String, String>().toJsonBody())
        }
        return if (res.isSuccess) {
            val loginStatusData = res.getOrThrow()
            val status = loginStatusData.data.account.status
            if (status == 0
                && loginStatusData.data.profile != null
            ) {
                val profileData = loginStatusData.data.profile
                _profile.value = profileData
                AccountPreference.profile = profileData
                CommonResult.success(profileData)
            } else {
                AccountPreference.cookie = ""
                CommonResult.fail(status, msg = "login fail")
            }
        } else {
            AccountPreference.cookie = ""
            CommonResult.fail(msg = res.exceptionOrNull()?.message)
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            AccountPreference.clear()
        }
        _profile.value = null
    }

    companion object {
        fun Application.userService(): UserService {
            return accessEntryPoint<UserServiceEntryPoint>().userService()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UserServiceEntryPoint {
        fun userService(): UserService
    }
}