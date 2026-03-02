import re

with open("app/src/main/java/solaris/gt/MainActivity.java", "r") as f:
    content = f.read()

# Replace setup toolbar with setup dynamic island
setup_toolbar = """
        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_coordinator), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        findViewById(R.id.btnOptions).setOnClickListener(v -> showOptionsDialog());
"""
content = re.sub(r'        // Set up the toolbar.*?WindowInsetsCompat\.CONSUMED;\n        }\);', setup_toolbar, content, flags=re.DOTALL)


# Add new imports
new_imports = """
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
"""

content = re.sub(r'(import android.os.Bundle;)', r'\1\n' + new_imports, content)

# Remove old menu methods
content = re.sub(r'    @Override\n    public boolean onCreateOptionsMenu\(Menu menu\) \{.*?return super.onOptionsItemSelected\(item\);\n    }', '', content, flags=re.DOTALL)


# Add showOptionsDialog method
show_options_method = """
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

        View btnAbout = dialog.findViewById(R.id.btnAbout);
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                dialog.dismiss();
                if (mInterstitialAd != null && !isAdsRemoved) {
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            mInterstitialAd = null;
                            loadInterstitialAd();
                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        }
                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            mInterstitialAd = null;
                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        }
                    });
                    mInterstitialAd.show(MainActivity.this);
                } else {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                }
            });
        }

        dialog.show();
    }
"""

content = re.sub(r'(    @Override\n    protected void onDestroy\(\))', show_options_method + r'\n\1', content)

with open("app/src/main/java/solaris/gt/MainActivity.java", "w") as f:
    f.write(content)
