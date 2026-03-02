import re

with open("app/src/main/java/solaris/gt/MainActivity.java", "r") as f:
    content = f.read()

# Add imports
imports = """import android.view.View;
import android.widget.Toast;
"""
content = re.sub(r'(import android.content.Intent;)', r'\1\n' + imports, content)

# Add variable
var_decl = """    private InterstitialAd mInterstitialAd;
    private BillingHelper billingHelper;
    private boolean isAdsRemoved = false;"""
content = re.sub(r'    private InterstitialAd mInterstitialAd;', var_decl, content)


# Setup billing helper
setup_billing = """
        // Initialize Billing Helper
        billingHelper = new BillingHelper(this, isPurchased -> {
            isAdsRemoved = isPurchased;
            runOnUiThread(() -> {
                if (isAdsRemoved) {
                    AdView adView = findViewById(R.id.adView);
                    if (adView != null) {
                        adView.setVisibility(View.GONE);
                    }
                }
            });
        });
        billingHelper.startConnection();

        // Initialize Mobile Ads SDK
"""
content = re.sub(r'        // Initialize Mobile Ads SDK', setup_billing, content)


# Modify interstitial load condition
interstitial_load = """
        // Load Interstitial Ad if not removed
        if (!isAdsRemoved) {
            loadInterstitialAd();
        }
"""
content = re.sub(r'        // Load Interstitial Ad\n        loadInterstitialAd\(\);', interstitial_load, content)

# Modify interstitial show condition
interstitial_show = """
        if (item.getItemId() == R.id.action_about) {
            if (mInterstitialAd != null && !isAdsRemoved) {
"""
content = re.sub(r'        if \(item.getItemId\(\) == R.id.action_about\) \{\n            if \(mInterstitialAd != null\) \{', interstitial_show, content)

onDestroy = """
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingHelper != null) {
            billingHelper.endConnection();
        }
    }
}"""
content = re.sub(r'}\n?$', onDestroy, content)


with open("app/src/main/java/solaris/gt/MainActivity.java", "w") as f:
    f.write(content)
