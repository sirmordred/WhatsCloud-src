package app.mordred.whatscloud.ad

import android.support.v7.app.AppCompatActivity
import app.mordred.whatscloud.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener

class AdManager(val baseActivity: AppCompatActivity): RewardedVideoAdListener {

    var mRewardedVideoAd: RewardedVideoAd

    init {
        MobileAds.initialize(baseActivity, BuildConfig.AdmobAppId)
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(baseActivity)
        mRewardedVideoAd.rewardedVideoAdListener = this
    }

    override fun onRewardedVideoAdClosed() {
        // empty
    }

    override fun onRewardedVideoAdLeftApplication() {
        // empty
    }

    override fun onRewardedVideoAdLoaded() {
        if (mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.show()
        }
    }

    override fun onRewardedVideoAdOpened() {
        // empty
    }

    override fun onRewardedVideoCompleted() {
        // empty
    }

    override fun onRewarded(p0: RewardItem?) {
        // empty
    }

    override fun onRewardedVideoStarted() {
        // empty
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        // empty
    }

    fun showRewardedVideoAd() {
        mRewardedVideoAd.loadAd(BuildConfig.AdmobUnitId,
            AdRequest.Builder().build())
    }
}