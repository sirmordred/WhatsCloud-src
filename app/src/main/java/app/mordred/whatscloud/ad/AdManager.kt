package app.mordred.whatscloud.ad

import android.support.v7.app.AppCompatActivity
import app.mordred.whatscloud.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds

class AdManager(baseActivity: AppCompatActivity) {

    private var mInterstitialAd: InterstitialAd

    init {
        MobileAds.initialize(baseActivity, BuildConfig.AdmobAppId)
        mInterstitialAd = InterstitialAd(baseActivity)
        mInterstitialAd.adUnitId = BuildConfig.AdmobUnitId
        mInterstitialAd.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                }
            }
        }
    }

    fun showInterstitialAd() {
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }
}