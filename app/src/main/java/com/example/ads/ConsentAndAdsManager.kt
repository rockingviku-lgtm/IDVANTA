package com.example.ads

import android.app.Activity
import android.content.Context
import com.example.BuildConfig
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AdsConsentState(
    val canRequestAds: Boolean = false,
    val isPrivacyOptionsRequired: Boolean = false,
    val isMobileAdsInitialized: Boolean = false,
    val isTestAdConfiguration: Boolean = BuildConfig.IS_TEST_AD_CONFIG,
    val lastConsentStatusMessage: String? = null
)

@Singleton
class ConsentAndAdsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(AdsConsentState())
    val state: StateFlow<AdsConsentState> = _state.asStateFlow()

    private val adsInitialized = AtomicBoolean(false)

    private fun getConsentInformation(): ConsentInformation {
        return UserMessagingPlatform.getConsentInformation(context)
    }

    fun requestConsentOnLaunch(activity: Activity) {
        val consentInfo = runCatching { getConsentInformation() }.getOrElse { err ->
            _state.update {
                it.copy(
                    canRequestAds = false,
                    isPrivacyOptionsRequired = false,
                    lastConsentStatusMessage = err.message
                )
            }
            return
        }

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    syncConsentState(consentInfo, formError?.message)
                    if (consentInfo.canRequestAds()) {
                        initializeMobileAdsIfNeeded()
                    }
                }
            },
            { requestError ->
                // On offline / error, honor previously cached consent status without crashing
                syncConsentState(consentInfo, requestError.message)
                if (consentInfo.canRequestAds()) {
                    initializeMobileAdsIfNeeded()
                }
            }
        )

        if (consentInfo.canRequestAds()) {
            syncConsentState(consentInfo, null)
            initializeMobileAdsIfNeeded()
        }
    }

    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (String?) -> Unit = {}) {
        val consentInfo = runCatching { getConsentInformation() }.getOrNull() ?: return
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            syncConsentState(consentInfo, formError?.message)
            if (consentInfo.canRequestAds()) {
                initializeMobileAdsIfNeeded()
            }
            onDismissed(formError?.message)
        }
    }

    private fun syncConsentState(consentInfo: ConsentInformation, message: String?) {
        val privacyRequired =
            consentInfo.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        _state.update {
            it.copy(
                canRequestAds = consentInfo.canRequestAds(),
                isPrivacyOptionsRequired = privacyRequired,
                lastConsentStatusMessage = message
            )
        }
    }

    private fun initializeMobileAdsIfNeeded() {
        if (BuildConfig.ADMOB_BANNER_AD_UNIT_ID.isBlank()) return
        if (adsInitialized.compareAndSet(false, true)) {
            runCatching {
                MobileAds.initialize(context) {
                    _state.update { it.copy(isMobileAdsInitialized = true) }
                }
            }.onFailure {
                adsInitialized.set(false)
            }
        }
    }
}
