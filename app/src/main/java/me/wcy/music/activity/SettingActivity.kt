package me.wcy.music.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.hwangjr.rxbus.RxBus
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.common.ext.toast
import me.wcy.music.R
import me.wcy.music.common.BaseMusicActivity
import me.wcy.music.common.DarkModeService
import me.wcy.music.const.Keys
import me.wcy.music.const.RxBusTags
import me.wcy.music.service.AudioPlayer2
import me.wcy.music.storage.preference.MusicPreferences
import me.wcy.music.utils.MusicUtils
import me.wcy.router.annotation.Route
import javax.inject.Inject

@Route("/settings")
@AndroidEntryPoint
class SettingActivity : BaseMusicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        val settingFragment = SettingFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, settingFragment)
            .commitAllowingStateLoss()
    }

    @AndroidEntryPoint
    class SettingFragment : PreferenceFragmentCompat() {
        private val darkMode: Preference by lazy {
            findPreference(getString(R.string.setting_key_dark_mode))!!
        }
        private val mSoundEffect: Preference by lazy {
            findPreference(getString(R.string.setting_key_sound_effect))!!
        }
        private val mFilterSize: Preference by lazy {
            findPreference(getString(R.string.setting_key_filter_size))!!
        }
        private val mFilterTime: Preference by lazy {
            findPreference(getString(R.string.setting_key_filter_time))!!
        }

        @Inject
        lateinit var player: AudioPlayer2

        @Inject
        lateinit var darkModeService: DarkModeService

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = Keys.PREFERENCE_NAME
            addPreferencesFromResource(R.xml.preference_setting)

            initDarkMode()
            initSoundEffect()
            initFilter()
        }

        private fun initDarkMode() {
            darkMode.summary = getSummary(
                MusicPreferences.darkMode,
                R.array.dark_mode_entries,
                R.array.dark_mode_values
            )
            darkMode.setOnPreferenceChangeListener { preference, newValue ->
                val value = newValue.toString()
                mFilterSize.summary = getSummary(
                    value,
                    R.array.dark_mode_entries,
                    R.array.dark_mode_values
                )
                val mode = DarkModeService.DarkMode.fromValue(value)
                darkModeService.setDarkMode(mode)
                true
            }
        }

        private fun initSoundEffect() {
            mSoundEffect.setOnPreferenceClickListener {
                startEqualizer()
                true
            }
        }

        private fun initFilter() {
            mFilterSize.summary = getSummary(
                MusicPreferences.filterSize,
                R.array.filter_size_entries,
                R.array.filter_size_entry_values
            )
            mFilterSize.setOnPreferenceChangeListener { preference, newValue ->
                val value = newValue.toString()
                mFilterSize.summary = getSummary(
                    value,
                    R.array.filter_size_entries,
                    R.array.filter_size_entry_values
                )
                RxBus.get().post(RxBusTags.SCAN_MUSIC, 1)
                true
            }

            mFilterTime.summary = getSummary(
                MusicPreferences.filterTime,
                R.array.filter_time_entries,
                R.array.filter_time_entry_values
            )
            mFilterTime.setOnPreferenceChangeListener { preference, newValue ->
                val value = newValue.toString()
                mFilterTime.summary = getSummary(
                    value,
                    R.array.filter_time_entries,
                    R.array.filter_time_entry_values
                )
                RxBus.get().post(RxBusTags.SCAN_MUSIC, 1)
                true
            }
        }

        private fun startEqualizer() {
            if (MusicUtils.isAudioControlPanelAvailable(requireContext())) {
                val intent = Intent()
                val packageName = requireContext().packageName
                intent.action = AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL
                intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                intent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    player.getAudioSessionId()
                )
                try {
                    startActivityForResult(intent, 1)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    toast(R.string.device_not_support)
                }
            } else {
                toast(R.string.device_not_support)
            }
        }

        private fun getSummary(value: String, entries: Int, values: Int): String {
            val entryArray = resources.getStringArray(entries)
            val valueArray = resources.getStringArray(values)
            val index = valueArray.indexOf(value).coerceAtLeast(0)
            return entryArray[index]
        }
    }
}