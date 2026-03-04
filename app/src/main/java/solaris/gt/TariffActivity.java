package solaris.gt;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class TariffActivity extends AppCompatActivity {
    private SoundHelper soundHelper;


    private EditText etPrice;
    private EditText etCons;
    private Spinner spCurrency;
    private Spinner spTariffType;
    private TextView tvTotal;
    private TextView tvSavings;
    private TextView tvROI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tariff);
        soundHelper = new SoundHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tariff_coordinator), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        etPrice = findViewById(R.id.etTariffPrice);
        etCons = findViewById(R.id.etTariffCons);
        spCurrency = findViewById(R.id.spCurrency);
        spTariffType = findViewById(R.id.spTariffType);
        tvTotal = findViewById(R.id.tvTariffTotal);
        tvSavings = findViewById(R.id.tvTariffSavings);
        tvROI = findViewById(R.id.tvTariffROI);

        setupSpinners();

        findViewById(R.id.btnCalcTariff).setOnClickListener(v -> { if (soundHelper != null) soundHelper.playClick(); calculate(); });
    }

    private void setupSpinners() {
        String[] currencies = {"Quetzal (Q)", "Peso Mexicano ($)", "Dólar USD ($)", "Euro (€)"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCurrency.setAdapter(currencyAdapter);

        String[] tariffTypes = {"Residencial Básica", "Residencial Alto Consumo", "Comercial"};
        ArrayAdapter<String> tariffAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tariffTypes);
        tariffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTariffType.setAdapter(tariffAdapter);
    }

    private void calculate() {
        try {
            double price = Double.parseDouble(etPrice.getText().toString());
            double cons = Double.parseDouble(etCons.getText().toString());

            String selectedCurrencyStr = spCurrency.getSelectedItem().toString();
            String currencySymbol = "$";
            if (selectedCurrencyStr.contains("Q")) currencySymbol = "Q";
            if (selectedCurrencyStr.contains("€")) currencySymbol = "€";

            int tariffTypeIndex = spTariffType.getSelectedItemPosition();

            // Total estimado
            double total = price * cons;

            // Ahorro con panel solar
            // Suponiendo un ahorro conservador de 80% para un sistema dimensionado al consumo
            double savingFactor = 0.8;
            if (tariffTypeIndex == 1) { // Alto consumo
                savingFactor = 0.9;
            } else if (tariffTypeIndex == 2) { // Comercial
                savingFactor = 0.85;
            }
            double ahorro = total * savingFactor;

            // Retorno de inversión (ROI)
            // Estimación muy general: el sistema cuesta aprox (consumo * factor_precio) meses de ahorro
            // Se calcula que un sistema retorna en promedio entre 3 y 5 años (36-60 meses)
            double defaultSystemCost = ahorro * 48; // Costo por defecto equivalente a 4 años de ahorro
            double roiMeses = defaultSystemCost / ahorro;
            double roiAnos = roiMeses / 12;

            tvTotal.setText(String.format("Total estimado: %s%.2f / mes", currencySymbol, total));
            tvSavings.setText(String.format("Ahorro con panel solar: %s%.2f / mes", currencySymbol, ahorro));
            tvROI.setText(String.format("Retorno de inversión estimado: %.1f Años", roiAnos));

        } catch (Exception e) {
            Toast.makeText(this, "Por favor ingrese valores válidos", Toast.LENGTH_SHORT).show();
            tvTotal.setText("Total estimado: ");
            tvSavings.setText("Ahorro con panel solar: ");
            tvROI.setText("Retorno de inversión: ");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem aboutItem = menu.findItem(R.id.action_about);
        if (aboutItem != null) {
            aboutItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareResults();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareResults() {
        StringBuilder results = new StringBuilder();
        results.append("Resultados Calculadora Tarifas:\n");

        if (tvTotal != null && !tvTotal.getText().toString().equals("Total estimado: "))
            results.append(tvTotal.getText().toString()).append("\n");

        if (tvSavings != null && !tvSavings.getText().toString().equals("Ahorro con panel solar: "))
            results.append(tvSavings.getText().toString()).append("\n");

        if (tvROI != null && !tvROI.getText().toString().equals("Retorno de inversión: "))
            results.append(tvROI.getText().toString()).append("\n");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, results.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
}