package solaris.gt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.provider.MediaStore;
import java.io.OutputStream;

public class SubInvoiceActivity extends AppCompatActivity {

    private Spinner spCurrency;
    private EditText etTotalAmount;
    private EditText etTotalKwh;
    private EditText etNumTenants;
    private EditText etDueDate;
    private EditText etAddress;
    private EditText etNotes;
    private LinearLayout llTenantsContainer;
    private MaterialButton btnGenerateSliders;

    private SharedPreferences prefs;
    private static final String PREF_CURRENCY_POS = "sub_inv_currency_pos";
    private static final String PREF_ADDRESS = "sub_inv_address";

    private double totalAmount = 0.0;
    private double totalKwh = 0.0;
    private int numTenants = 0;
    private String currencySymbol = "$";

    private List<TenantData> tenantsDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sub_invoice);

        prefs = getSharedPreferences("SolarisPrefs", Context.MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sub_invoice_coordinator), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        spCurrency = findViewById(R.id.spCurrency);
        etTotalAmount = findViewById(R.id.etTotalAmount);
        etTotalKwh = findViewById(R.id.etTotalKwh);
        etNumTenants = findViewById(R.id.etNumTenants);
        etDueDate = findViewById(R.id.etDueDate);
        etAddress = findViewById(R.id.etAddress);
        etNotes = findViewById(R.id.etNotes);
        llTenantsContainer = findViewById(R.id.llTenantsContainer);
        btnGenerateSliders = findViewById(R.id.btnGenerateSliders);

        setupSpinners();
        loadPreferences();

        btnGenerateSliders.setOnClickListener(v -> generateTenants());
    }

    private void setupSpinners() {
        String[] currencies = {
            "Quetzal (Q)", "Dólar USD ($)", "Peso Mexicano ($)", "Real Brasileño (R$)",
            "Peso Argentino ($)", "Peso Colombiano ($)", "Peso Chileno ($)", "Sol Peruano (S/)", "Euro (€)", "Shekel (₪)"
        };
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCurrency.setAdapter(currencyAdapter);
    }

    private void loadPreferences() {
        int savedPos = prefs.getInt(PREF_CURRENCY_POS, 0);
        if (savedPos >= 0 && savedPos < spCurrency.getCount()) {
            spCurrency.setSelection(savedPos);
        }
        String savedAddress = prefs.getString(PREF_ADDRESS, "");
        if (!savedAddress.isEmpty()) {
            etAddress.setText(savedAddress);
        }
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_CURRENCY_POS, spCurrency.getSelectedItemPosition());
        editor.putString(PREF_ADDRESS, etAddress.getText().toString());
        editor.apply();
    }

    private void generateTenants() {
        savePreferences();

        String amountStr = etTotalAmount.getText().toString();
        String tenantsStr = etNumTenants.getText().toString();
        String kwhStr = etTotalKwh.getText().toString();

        if (amountStr.isEmpty() || tenantsStr.isEmpty()) {
            Toast.makeText(this, "Debe ingresar el monto total y la cantidad de inquilinos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            totalAmount = Double.parseDouble(amountStr);
            numTenants = Integer.parseInt(tenantsStr);
            totalKwh = kwhStr.isEmpty() ? 0 : Double.parseDouble(kwhStr);

            if (numTenants <= 0) {
                Toast.makeText(this, "Debe haber al menos 1 inquilino", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedCurrencyStr = spCurrency.getSelectedItem().toString();
            currencySymbol = "$";
            if (selectedCurrencyStr.contains("Q")) currencySymbol = "Q";
            if (selectedCurrencyStr.contains("€")) currencySymbol = "€";
            if (selectedCurrencyStr.contains("R$")) currencySymbol = "R$";
            if (selectedCurrencyStr.contains("S/")) currencySymbol = "S/";
            if (selectedCurrencyStr.contains("₪")) currencySymbol = "₪";

            llTenantsContainer.removeAllViews();
            tenantsDataList.clear();

            double basePercentage = 100.0 / numTenants;

            for (int i = 0; i < numTenants; i++) {
                TenantData tenant = new TenantData(i, basePercentage);
                tenantsDataList.add(tenant);

                View tenantView = LayoutInflater.from(this).inflate(R.layout.item_tenant_slider, llTenantsContainer, false);
                tenant.view = tenantView;

                TextView tvName = tenantView.findViewById(R.id.tvTenantNameLabel);
                tvName.setText("Inquilino / Habitación " + (i + 1));

                Slider slider = tenantView.findViewById(R.id.sliderTenant);
                slider.setValue((float) basePercentage);

                updateTenantUI(tenant);

                slider.addOnChangeListener((slider1, value, fromUser) -> {
                    if (fromUser) {
                        handleSliderChange(tenant, value);
                    }
                });

                MaterialButton btnCreate = tenantView.findViewById(R.id.btnCreateInvoice);
                btnCreate.setOnClickListener(v -> showGenerateDialog(tenant));

                llTenantsContainer.addView(tenantView);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error en los datos ingresados", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isUpdatingSliders = false;

    private void handleSliderChange(TenantData changedTenant, float newValue) {
        if (isUpdatingSliders) return;

        double oldValue = changedTenant.percentage;
        changedTenant.percentage = newValue;

        double diff = newValue - oldValue;
        int remainingTenants = numTenants - 1;

        if (remainingTenants > 0) {
            isUpdatingSliders = true;

            // Distribute the difference equally among the other tenants
            double adjustmentPerTenant = diff / remainingTenants;
            double sumRemaining = 0.0;

            for (TenantData tenant : tenantsDataList) {
                if (tenant != changedTenant) {
                    double adjustedValue = tenant.percentage - adjustmentPerTenant;

                    // Constrain between 0 and 100
                    if (adjustedValue < 0) {
                        adjustedValue = 0;
                    } else if (adjustedValue > 100) {
                        adjustedValue = 100;
                    }

                    tenant.percentage = adjustedValue;
                    sumRemaining += adjustedValue;
                }
            }

            // Due to rounding or bounds limits, the sum might not be exactly 100
            // Re-normalize remaining tenants if needed
            double expectedRemainingSum = 100.0 - newValue;
            if (sumRemaining > 0 && Math.abs(sumRemaining - expectedRemainingSum) > 0.1) {
                double scaleFactor = expectedRemainingSum / sumRemaining;
                for (TenantData tenant : tenantsDataList) {
                    if (tenant != changedTenant) {
                        tenant.percentage *= scaleFactor;
                    }
                }
            }

            // Update UI for all tenants
            for (TenantData tenant : tenantsDataList) {
                updateTenantUI(tenant);
            }

            isUpdatingSliders = false;
        } else {
            // Only one tenant, keep at 100%
            changedTenant.percentage = 100.0;
            updateTenantUI(changedTenant);
        }
    }

    private void updateTenantUI(TenantData tenant) {
        if (tenant.view == null) return;

        Slider slider = tenant.view.findViewById(R.id.sliderTenant);
        if (slider.getValue() != (float) tenant.percentage) {
            slider.setValue((float) tenant.percentage);
        }

        TextView tvPercent = tenant.view.findViewById(R.id.tvTenantPercent);
        tvPercent.setText(String.format(Locale.getDefault(), "%.1f%%", tenant.percentage));

        tenant.currentAmount = totalAmount * (tenant.percentage / 100.0);
        tenant.currentKwh = totalKwh * (tenant.percentage / 100.0);

        TextView tvAmount = tenant.view.findViewById(R.id.tvTenantAmount);
        tvAmount.setText(String.format(Locale.getDefault(), "%s%.2f", currencySymbol, tenant.currentAmount));

        TextView tvKwh = tenant.view.findViewById(R.id.tvTenantKwh);
        if (totalKwh > 0) {
            tvKwh.setVisibility(View.VISIBLE);
            tvKwh.setText(String.format(Locale.getDefault(), "%.1f kWh", tenant.currentKwh));
        } else {
            tvKwh.setVisibility(View.GONE);
        }
    }

    private void showGenerateDialog(TenantData tenant) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_generate_invoice, null);
        dialog.setContentView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etDialogName);
        EditText etTax = dialogView.findViewById(R.id.etDialogTax);
        EditText etInterest = dialogView.findViewById(R.id.etDialogInterest);

        MaterialButton btnPdf = dialogView.findViewById(R.id.btnSavePdf);
        MaterialButton btnJpg = dialogView.findViewById(R.id.btnSaveJpg);
        MaterialButton btnWhatsApp = dialogView.findViewById(R.id.btnShareWhatsApp);

        btnPdf.setOnClickListener(v -> {
            dialog.dismiss();
            generateInvoice(tenant, etName.getText().toString(), etTax.getText().toString(), etInterest.getText().toString(), "PDF");
        });

        btnJpg.setOnClickListener(v -> {
            dialog.dismiss();
            generateInvoice(tenant, etName.getText().toString(), etTax.getText().toString(), etInterest.getText().toString(), "JPG");
        });

        btnWhatsApp.setOnClickListener(v -> {
            dialog.dismiss();
            generateInvoice(tenant, etName.getText().toString(), etTax.getText().toString(), etInterest.getText().toString(), "WHATSAPP");
        });

        dialog.show();
    }

    private void generateInvoice(TenantData tenant, String name, String taxStr, String interestStr, String action) {
        if (name == null || name.trim().isEmpty()) {
            name = "Consumidor Final";
        }

        double tax = 0.0;
        if (!taxStr.trim().isEmpty()) {
            try {
                if (taxStr.contains("%")) {
                    double percentage = Double.parseDouble(taxStr.replace("%", "").trim());
                    tax = tenant.currentAmount * (percentage / 100.0);
                } else {
                    tax = Double.parseDouble(taxStr.trim());
                }
            } catch (NumberFormatException ignored) {}
        }

        double interest = 0.0;
        if (!interestStr.trim().isEmpty()) {
            try { interest = Double.parseDouble(interestStr); } catch (NumberFormatException ignored) {}
        }

        View invoiceView = LayoutInflater.from(this).inflate(R.layout.layout_invoice_receipt, null);

        TextView tvInvDate = invoiceView.findViewById(R.id.tvInvDate);
        TextView tvInvDueDate = invoiceView.findViewById(R.id.tvInvDueDate);
        TextView tvInvClientName = invoiceView.findViewById(R.id.tvInvClientName);
        TextView tvInvAddress = invoiceView.findViewById(R.id.tvInvAddress);
        TextView tvInvKwh = invoiceView.findViewById(R.id.tvInvKwh);
        TextView tvInvSubtotal = invoiceView.findViewById(R.id.tvInvSubtotal);
        TextView tvInvTax = invoiceView.findViewById(R.id.tvInvTax);
        TextView tvInvInterest = invoiceView.findViewById(R.id.tvInvInterest);
        TextView tvInvTotal = invoiceView.findViewById(R.id.tvInvTotal);
        TextView tvInvNotes = invoiceView.findViewById(R.id.tvInvNotes);
        LinearLayout llInvTax = invoiceView.findViewById(R.id.llInvTax);
        LinearLayout llInvInterest = invoiceView.findViewById(R.id.llInvInterest);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvInvDate.setText(sdf.format(new Date()));

        String dueDate = etDueDate.getText().toString();
        tvInvDueDate.setText(dueDate.isEmpty() ? "N/A" : dueDate);

        tvInvClientName.setText(name);

        String address = etAddress.getText().toString();
        tvInvAddress.setText(address.isEmpty() ? "N/A" : address);

        if (tenant.currentKwh > 0) {
            tvInvKwh.setText(String.format(Locale.getDefault(), "%.1f", tenant.currentKwh));
        } else {
            tvInvKwh.setText("N/A");
        }

        tvInvSubtotal.setText(String.format(Locale.getDefault(), "%s%.2f", currencySymbol, tenant.currentAmount));

        if (tax > 0) {
            tvInvTax.setText(String.format(Locale.getDefault(), "%s%.2f", currencySymbol, tax));
            llInvTax.setVisibility(View.VISIBLE);
        } else {
            llInvTax.setVisibility(View.GONE);
        }

        if (interest > 0) {
            tvInvInterest.setText(String.format(Locale.getDefault(), "%s%.2f", currencySymbol, interest));
            llInvInterest.setVisibility(View.VISIBLE);
        } else {
            llInvInterest.setVisibility(View.GONE);
        }

        double finalTotal = tenant.currentAmount + tax + interest;
        tvInvTotal.setText(String.format(Locale.getDefault(), "%s%.2f", currencySymbol, finalTotal));

        String notes = etNotes.getText().toString();
        if (!notes.isEmpty()) {
            tvInvNotes.setText(notes);
            tvInvNotes.setVisibility(View.VISIBLE);
        } else {
            tvInvNotes.setVisibility(View.GONE);
        }

        // Measure and layout the view
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        invoiceView.measure(widthMeasureSpec, heightMeasureSpec);
        int width = invoiceView.getMeasuredWidth();
        int height = invoiceView.getMeasuredHeight();
        invoiceView.layout(0, 0, width, height);

        // Render to Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        invoiceView.draw(canvas);

        if (action.equals("JPG")) {
            saveAndShareImage(bitmap, false);
        } else if (action.equals("WHATSAPP")) {
            saveAndShareImage(bitmap, true);
        } else if (action.equals("PDF")) {
            savePdf(bitmap, width, height);
        }
    }

    private void saveAndShareImage(Bitmap bitmap, boolean shareWhatsApp) {
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File imageFile = new File(cachePath, "invoice_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream stream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();

            if (shareWhatsApp) {
                Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", imageFile);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setPackage("com.whatsapp");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "WhatsApp no está instalado.", Toast.LENGTH_SHORT).show();
                    // Fallback to normal share
                    intent.setPackage(null);
                    Intent shareIntent = Intent.createChooser(intent, "Compartir factura");
                    startActivity(shareIntent);
                }
            } else {
                // Save to public Pictures directory using MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "invoice_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Solaris");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        Toast.makeText(this, "Imagen guardada en Galería (Pictures/Solaris).", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Error creando archivo en galería.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error guardando la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePdf(Bitmap bitmap, int width, int height) {
        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            canvas.drawBitmap(bitmap, 0, 0, null);
            document.finishPage(page);

            File cachePath = new File(getCacheDir(), "pdfs");
            cachePath.mkdirs();
            File pdfFile = new File(cachePath, "invoice_" + System.currentTimeMillis() + ".pdf");
            FileOutputStream stream = new FileOutputStream(pdfFile);
            document.writeTo(stream);
            document.close();
            stream.close();

            Toast.makeText(this, "PDF guardado en caché (Temporal).", Toast.LENGTH_LONG).show();

            // Allow user to view/share it
            Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Abrir PDF"));

        } catch (Exception e) {
            Toast.makeText(this, "Error guardando el PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private class TenantData {
        int index;
        double percentage;
        double currentAmount;
        double currentKwh;
        View view;

        TenantData(int index, double percentage) {
            this.index = index;
            this.percentage = percentage;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true; // No menu for this activity
    }
}
