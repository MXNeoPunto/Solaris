package solaris.gt;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.Collections;
import java.util.List;

public class BillingHelper {
    private static final String TAG = "BillingHelper";
    public static final String REMOVE_ADS_PRODUCT_ID = "remove_ads_199";

    private BillingClient billingClient;
    private final Activity activity;
    private final PurchasesUpdatedListener purchasesUpdatedListener;
    private final OnPurchaseListener onPurchaseListener;

    public interface OnPurchaseListener {
        void onPurchaseStatus(boolean isPurchased);
    }

    public BillingHelper(Activity activity, OnPurchaseListener listener) {
        this.activity = activity;
        this.onPurchaseListener = listener;

        purchasesUpdatedListener = (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                Log.d(TAG, "User canceled the purchase flow.");
            } else {
                Log.e(TAG, "Purchase failed: " + billingResult.getDebugMessage());
            }
        };

        billingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
    }

    public void startConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Setup Finished.");
                    queryPurchases();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "Billing Service Disconnected. Reconnecting...");
                // Note: It's strongly recommended that you implement your own retry logic
                // startConnection();
            }
        });
    }

    public void initiatePurchaseFlow() {
        if (!billingClient.isReady()) {
            Log.e(TAG, "BillingClient is not ready.");
            return;
        }

        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                        Collections.singletonList(
                                QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(REMOVE_ADS_PRODUCT_ID)
                                        .setProductType(BillingClient.ProductType.INAPP)
                                        .build()
                        )
                )
                .build();

        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                (billingResult, productDetailsList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null && !productDetailsList.isEmpty()) {
                        ProductDetails productDetails = productDetailsList.get(0);
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(
                                        Collections.singletonList(
                                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                                        .setProductDetails(productDetails)
                                                        .build()
                                        )
                                )
                                .build();
                        billingClient.launchBillingFlow(activity, billingFlowParams);
                    } else {
                        Log.e(TAG, "Failed to retrieve product details.");
                    }
                }
        );
    }

    private void queryPurchases() {
        if (!billingClient.isReady()) {
            return;
        }

        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        billingClient.queryPurchasesAsync(params, (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                boolean isPurchased = false;
                for (Purchase purchase : list) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        if (purchase.getProducts().contains(REMOVE_ADS_PRODUCT_ID)) {
                            isPurchased = true;
                            if (!purchase.isAcknowledged()) {
                                acknowledgePurchase(purchase);
                            }
                        }
                    }
                }
                if (onPurchaseListener != null) {
                    onPurchaseListener.onPurchaseStatus(isPurchased);
                }
            }
        });
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (purchase.getProducts().contains(REMOVE_ADS_PRODUCT_ID)) {
                if (!purchase.isAcknowledged()) {
                    acknowledgePurchase(purchase);
                }
                if (onPurchaseListener != null) {
                    onPurchaseListener.onPurchaseStatus(true);
                }
            }
        }
    }

    private void acknowledgePurchase(Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged successfully.");
            }
        });
    }

    public void endConnection() {
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}
