package solaris.gt;

import android.content.Intent;
import android.media.MediaPlayer;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import java.util.Locale;


import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.AdError;

public class MainActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd;
    private BillingHelper billingHelper;
    private boolean isAdsRemoved = false;
    private int interstitialClickCount = 0;


    private int headerClickCount = 0;
    private long lastHeaderClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme mode before setting the content view
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int savedMode = prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);

        // Handle the splash screen transition.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_coordinator), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        com.google.android.material.materialswitch.MaterialSwitch themeSwitch = findViewById(R.id.themeSwitch);
        TextView themeText = findViewById(R.id.themeText);

        int themeMode = prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        boolean isNightMode;
        if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            isNightMode = true;
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            isNightMode = false;
        } else {
            int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            isNightMode = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }

        themeSwitch.setChecked(isNightMode);
        themeText.setText(isNightMode ? "Oscuro" : "Claro");

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newMode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("theme", newMode);
            editor.apply();

            AppCompatDelegate.setDefaultNightMode(newMode);
            themeText.setText(isChecked ? "Oscuro" : "Claro");
        });

        findViewById(R.id.btnOptions).setOnClickListener(v -> {  showOptionsDialog(); });



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

        MobileAds.initialize(this, initializationStatus -> {});

        // Load Banner Ad
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        // Load Interstitial Ad if not removed
        if (!isAdsRemoved) {
            loadInterstitialAd();
        }

        setupSecretTrick();


// Setup Button Listeners
        findViewById(R.id.btnEnergy).setOnClickListener(v -> {  showInterstitialWithIntent(new Intent(MainActivity.this, EnergyActivity.class)); });
        findViewById(R.id.btnSolar).setOnClickListener(v -> {  showInterstitialWithIntent(new Intent(MainActivity.this, SolarActivity.class)); });
        findViewById(R.id.btnWelding).setOnClickListener(v -> {  showInterstitialWithIntent(new Intent(MainActivity.this, WeldingActivity.class)); });
        findViewById(R.id.btnCables).setOnClickListener(v -> {  showInterstitialWithIntent(new Intent(MainActivity.this, CablesActivity.class)); });
        findViewById(R.id.btnTariff).setOnClickListener(v -> {  showInterstitialWithIntent(new Intent(MainActivity.this, TariffActivity.class)); });
    }

    private void setupSecretTrick() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setOnClickListener(v -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastHeaderClickTime > 1000) {
                    headerClickCount = 0;
                }
                lastHeaderClickTime = currentTime;
                headerClickCount++;

                if (headerClickCount == 5) {
                    headerClickCount = 0;
                    activateSecretTrick();
                }
            });
        }
    }

    private void activateSecretTrick() {
        SharedPreferences prefs = getSharedPreferences("SecretTrick", MODE_PRIVATE);
        long lastTrickTime = prefs.getLong("last_used", 0);
        long currentTime = System.currentTimeMillis();

        // 1 day = 86400000 ms
        if (currentTime - lastTrickTime >= 86400000) {
            prefs.edit()
                .putLong("last_used", currentTime)
                .putLong("expiration", currentTime + 600000) // 10 minutes
                .apply();

            Toast.makeText(this, getString(R.string.secret_trick_activated), Toast.LENGTH_LONG).show();
            checkAndApplySecretTrick();
        } else {
            Toast.makeText(this, getString(R.string.secret_trick_limit), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndApplySecretTrick() {
        if (isAdsRemoved) return; // Ya es premium, no necesita el truco

        SharedPreferences prefs = getSharedPreferences("SecretTrick", MODE_PRIVATE);
        long expiration = prefs.getLong("expiration", 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime < expiration) {
            isAdsRemoved = true;
            runOnUiThread(() -> {
                AdView adView = findViewById(R.id.adView);
                if (adView != null) {
                    adView.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndApplySecretTrick();
    }



    private void showInterstitialWithIntent(Intent intent) {
        if (isAdsRemoved) {
            startActivity(intent);
            return;
        }

        interstitialClickCount++;

        if (interstitialClickCount >= 3 && mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                @Override
                public void onAdDismissedFullScreenContent() {
                    mInterstitialAd = null;
                    interstitialClickCount = 0; // Reset counter
                    loadInterstitialAd(); // Preload next ad
                    startActivity(intent);
                }
                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    mInterstitialAd = null;
                    interstitialClickCount = 0;
                    startActivity(intent);
                }
            });
            mInterstitialAd.show(MainActivity.this);
        } else {
            startActivity(intent);
        }
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    mInterstitialAd = interstitialAd;
                    Log.i("AdMob", "onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.d("AdMob", loadAdError.toString());
                    mInterstitialAd = null;
                }
            });
    }




    private void showOptionsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_options);

        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        RadioGroup rgTheme = dialog.findViewById(R.id.rgTheme);
        RadioButton rbThemeLight = dialog.findViewById(R.id.rbThemeLight);
        RadioButton rbThemeDark = dialog.findViewById(R.id.rbThemeDark);
        RadioButton rbThemeSystem = dialog.findViewById(R.id.rbThemeSystem);

        if (rgTheme != null) {
            if (themeMode == AppCompatDelegate.MODE_NIGHT_NO && rbThemeLight != null) {
                rbThemeLight.setChecked(true);
            } else if (themeMode == AppCompatDelegate.MODE_NIGHT_YES && rbThemeDark != null) {
                rbThemeDark.setChecked(true);
            } else if (rbThemeSystem != null) {
                rbThemeSystem.setChecked(true);
            }

            rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
                int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                if (checkedId == R.id.rbThemeLight) {
                    mode = AppCompatDelegate.MODE_NIGHT_NO;
                } else if (checkedId == R.id.rbThemeDark) {
                    mode = AppCompatDelegate.MODE_NIGHT_YES;
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("theme", mode);
                editor.apply();

                AppCompatDelegate.setDefaultNightMode(mode);
                dialog.dismiss();
            });
        }


        // Language configuration
        RadioGroup rgLanguage = dialog.findViewById(R.id.rgLanguage);
        RadioButton rbLangSystem = dialog.findViewById(R.id.rbLangSystem);
        RadioButton rbLangEs = dialog.findViewById(R.id.rbLangEs);
        RadioButton rbLangEn = dialog.findViewById(R.id.rbLangEn);
        RadioButton rbLangPt = dialog.findViewById(R.id.rbLangPt);

        if (rgLanguage != null) {
            LocaleListCompat currentLocaleList = AppCompatDelegate.getApplicationLocales();
            if (currentLocaleList.isEmpty()) {
                if (rbLangSystem != null) rbLangSystem.setChecked(true);
            } else {
                String lang = currentLocaleList.get(0).getLanguage();
                if (lang.equals("es") && rbLangEs != null) {
                    rbLangEs.setChecked(true);
                } else if (lang.equals("en") && rbLangEn != null) {
                    rbLangEn.setChecked(true);
                } else if (lang.equals("pt") && rbLangPt != null) {
                    rbLangPt.setChecked(true);
                } else {
                    if (rbLangSystem != null) rbLangSystem.setChecked(true);
                }
            }

            rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
                LocaleListCompat locales = LocaleListCompat.getEmptyLocaleList();
                if (checkedId == R.id.rbLangEs) {
                    locales = LocaleListCompat.forLanguageTags("es");
                } else if (checkedId == R.id.rbLangEn) {
                    locales = LocaleListCompat.forLanguageTags("en");
                } else if (checkedId == R.id.rbLangPt) {
                    locales = LocaleListCompat.forLanguageTags("pt");
                }

                AppCompatDelegate.setApplicationLocales(locales);
                dialog.dismiss();
            });
        }

        View btnRemoveAds = dialog.findViewById(R.id.btnRemoveAds);

        if (btnRemoveAds != null) {
            if (isAdsRemoved) {
                btnRemoveAds.setVisibility(View.GONE);
            } else {
                btnRemoveAds.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (billingHelper != null) {
                        billingHelper.initiatePurchaseFlow();
                    }
                });
            }
        }

        View btnRateApp = dialog.findViewById(R.id.btnRateApp);
        if (btnRateApp != null) {
            SharedPreferences ratePrefs = getSharedPreferences("RatePrefs", MODE_PRIVATE);
            boolean dontShowAgain = ratePrefs.getBoolean("dont_show_again", false);
            if (dontShowAgain) {
                btnRateApp.setVisibility(View.GONE);
            } else {
                btnRateApp.setOnClickListener(v -> {
                    dialog.dismiss();
                    showRateDialog();
                });
            }
        }

        View btnAbout = dialog.findViewById(R.id.btnAbout);
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                dialog.dismiss();
                showInterstitialWithIntent(new Intent(MainActivity.this, AboutActivity.class));
            });
        }

        dialog.show();
    }

    private void showRateDialog() {
        SharedPreferences ratePrefs = getSharedPreferences("RatePrefs", MODE_PRIVATE);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.rate_title))
                .setMessage(getString(R.string.rate_message))
                .setPositiveButton(getString(R.string.rate_positive), (dialogInterface, i) -> {
                    ratePrefs.edit().putBoolean("dont_show_again", true).apply();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=" + getPackageName())));
                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                })
                .setNeutralButton(getString(R.string.rate_neutral), null)
                .setNegativeButton(getString(R.string.rate_negative), (dialogInterface, i) -> {
                    ratePrefs.edit().putBoolean("dont_show_again", true).apply();
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingHelper != null) {
            billingHelper.endConnection();
        }
    }
}