package app.mordred.whatscloud.billing

import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import app.mordred.whatscloud.BuildConfig
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails

class BillingManager (val baseActivity: AppCompatActivity): BillingProcessor.IBillingHandler{

    var bp: BillingProcessor? = null
    var isBillingLibReady = false
    val PRODUCT_ID = BuildConfig.ProductId
    val LICENSE_KEY = BuildConfig.LicenceId
    val MERCHANT_ID = BuildConfig.MerchantId

    companion object {
        var isPremiumApp = false
    }

    init {
        if(BillingProcessor.isIabServiceAvailable(baseActivity)) {
            bp = BillingProcessor(baseActivity, LICENSE_KEY, MERCHANT_ID, this)
        }
    }

    fun upgradeToPro() {
        if (isBillingLibReady) {
            if (!bp?.isPurchased(PRODUCT_ID)!!) {
                bp?.purchase(baseActivity,PRODUCT_ID)
            } else {
                Toast.makeText(baseActivity, "You are already pro user",
                    Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(baseActivity, "Billing lib is not ready yet",
                Toast.LENGTH_LONG).show()
        }
    }

    fun undoUpgradeToPro() {
        if (isBillingLibReady) {
            if (bp?.isPurchased(PRODUCT_ID)!!) {
                bp?.consumePurchase(PRODUCT_ID)
            } else {
                Toast.makeText(baseActivity, "You are not pro user yet",
                    Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(baseActivity, "Billing lib is not ready yet",
                Toast.LENGTH_LONG).show()
        }
    }

    fun updateProductStatus() {
        isPremiumApp = bp?.isPurchased(PRODUCT_ID)!!
    }

    fun destroy() {
        bp?.release()
    }

    override fun onBillingInitialized() {
        isBillingLibReady = true
        updateProductStatus()
    }

    override fun onPurchaseHistoryRestored() {
        updateProductStatus()
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        updateProductStatus()
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Toast.makeText(baseActivity, "Error: Check your Google Play settings",
            Toast.LENGTH_LONG).show()
    }

}