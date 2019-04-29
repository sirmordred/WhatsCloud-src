package app.mordred.whatscloud.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.widget.*
import app.mordred.whatscloud.BuildConfig
import app.mordred.whatscloud.R
import app.mordred.whatscloud.adapter.StopWordListAdapter
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.github.aakira.expandablelayout.ExpandableRelativeLayout
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {
    var sharedPref: SharedPreferences? = null
    var stopWordEdx: EditText? = null
    var stpWrdListAdapter: StopWordListAdapter? = null
    var defWordCountInWd = 30
    var tvWordCount: TextView? = null

    var proExpView: ExpandableRelativeLayout? = null
    var wordNumLl: LinearLayout? = null
    var addStopWordButton: Button? = null

    var isBillingLibReady = false
    var isAppPro = false
    var bp: BillingProcessor? = null
    val PRODUCT_ID = BuildConfig.ProductId
    val LICENSE_KEY = BuildConfig.LicenceId
    val MERCHANT_ID = BuildConfig.MerchantId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(BillingProcessor.isIabServiceAvailable(this)) {
            bp = BillingProcessor(this, LICENSE_KEY, MERCHANT_ID, this)
        }

        val proIconImageView = findViewById<ImageView>(R.id.proIconImgView)
        proIconImageView.setColorFilter(ContextCompat.getColor(applicationContext, R.color.colorMaterialBlue),
            PorterDuff.Mode.SRC_IN)
        proExpView = findViewById(R.id.expInAppBillingLayout)
        val upgrToProBtn = findViewById<LinearLayout>(R.id.upgrProBtnLayout)
        upgrToProBtn.setOnClickListener {
            upgradeToProSettings()
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        defWordCountInWd = sharedPref?.getInt("defWordCntInWd", 30)!!

        tvWordCount = findViewById(R.id.tvWordNumVal)
        tvWordCount?.text = defWordCountInWd.toString()

        wordNumLl = findViewById(R.id.llWordNum)
        wordNumLl?.setOnClickListener {
            if (isAppPro) {
                showWordCountInputDialog()
            } else {
                Toast.makeText(applicationContext, getString(R.string.warn_upgrade_pro),
                    Toast.LENGTH_LONG).show()
            }
        }

        rv_stopword_list.layoutManager = LinearLayoutManager(this)
        rv_stopword_list.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
        stpWrdListAdapter = StopWordListAdapter(this)
        rv_stopword_list.adapter = stpWrdListAdapter

        stopWordEdx = findViewById(R.id.edxStopWord)

        addStopWordButton = findViewById<Button>(R.id.addStopWordBtn)
        addStopWordButton?.setOnClickListener {
            if (isAppPro) {
                val userEnteredStopWord = stopWordEdx?.text?.toString()?.trimStart()?.trimEnd()
                if (userEnteredStopWord != null && userEnteredStopWord.isNotEmpty() && userEnteredStopWord.isNotBlank()) {
                    stpWrdListAdapter?.addElementToList(userEnteredStopWord)
                }
                stopWordEdx?.text?.clear()
            } else {
                Toast.makeText(applicationContext, getString(R.string.warn_upgrade_pro),
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showWordCountInputDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.numofword_dialog_title))
        val dialogLayout = inflater.inflate(R.layout.input_dialog, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.edxInputDialog)
        editText?.setText(defWordCountInWd.toString())
        builder.setView(dialogLayout)
        builder.setPositiveButton(getString(R.string.dialog_ok_label)) { dialogInterface, _ ->
            val userEnteredNum = editText.text.toString().toInt()
            if (userEnteredNum in 1..499 && userEnteredNum != defWordCountInWd) {
                defWordCountInWd = userEnteredNum
                sharedPref?.edit()?.putInt("defWordCntInWd", defWordCountInWd)?.apply()
                tvWordCount?.text = userEnteredNum.toString()
            }
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(getString(R.string.dialog_cancel_label)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        builder.show()
    }

    /* Triggered when use click menu item. */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Get menu item id.
        when (item.itemId) {
            android.R.id.home-> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateProSettingsStatus() {
        if (bp?.isPurchased(PRODUCT_ID)!!) {
            isAppPro = true
            proExpView?.collapse()
        }
    }

    override fun onBillingInitialized() {
        isBillingLibReady = true
        updateProSettingsStatus()
    }

    override fun onPurchaseHistoryRestored() {
        updateProSettingsStatus()
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        updateProSettingsStatus()
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        // empty
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp?.handleActivityResult(requestCode, resultCode, data)!!)
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        updateProSettingsStatus()
    }

    override fun onDestroy() {
        bp?.release()
        super.onDestroy()
    }

    private fun upgradeToProSettings() {
        if (isBillingLibReady) {
            if (!bp?.isPurchased(PRODUCT_ID)!!) {
                bp?.purchase(this,PRODUCT_ID)
            } else {
                Toast.makeText(applicationContext, "You are already pro user",
                    Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(applicationContext, "Billing lib is not ready yet",
                Toast.LENGTH_LONG).show()
        }
    }
}