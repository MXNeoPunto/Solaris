package solaris.gt;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import com.google.android.material.appbar.MaterialToolbar;

public class SolarActivity extends AppCompatActivity {

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
        results.append("Resultados Calculadora Solar:\n");

        TextView tvPanels = findViewById(R.id.tvPanelsResult);
        if (tvPanels != null && !tvPanels.getText().toString().isEmpty()) results.append("Paneles: ").append(tvPanels.getText().toString()).append("\n");

        TextView tvBat = findViewById(R.id.tvBatResult);
        if (tvBat != null && !tvBat.getText().toString().isEmpty()) results.append("Baterías: ").append(tvBat.getText().toString()).append("\n");

        TextView tvInv = findViewById(R.id.tvInvResult);
        if (tvInv != null && !tvInv.getText().toString().isEmpty()) results.append("Inversor: ").append(tvInv.getText().toString()).append("\n");

        TextView tvRoi = findViewById(R.id.tvRoiResult);
        if (tvRoi != null && !tvRoi.getText().toString().isEmpty()) results.append("ROI: ").append(tvRoi.getText().toString()).append("\n");

        TextView tvTilt = findViewById(R.id.tvTiltResult);
        if (tvTilt != null && !tvTilt.getText().toString().isEmpty()) results.append("Inclinación: ").append(tvTilt.getText().toString()).append("\n");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, results.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_solar);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.solar_coordinator), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        setupTool1Panels();
        setupTool2Batteries();
        setupTool3Inverter();
        setupTool5ROI();
        setupTool6Tilt();
    }

    private void setupTool1Panels() {
        EditText etCons = findViewById(R.id.etSolarCons);
        EditText etPanelW = findViewById(R.id.etSolarPanelW);
        EditText etHSP = findViewById(R.id.etSolarHSP);
        TextView tvResult = findViewById(R.id.tvPanelsResult);
        findViewById(R.id.btnCalcPanels).setOnClickListener(v -> {
            try {
                double cons = Double.parseDouble(etCons.getText().toString());
                double panelW = Double.parseDouble(etPanelW.getText().toString());
                double hsp = Double.parseDouble(etHSP.getText().toString());
                double wDay = cons * 1000;
                // Efficiency factor ~0.8
                double dailyGenPerPanel = panelW * hsp * 0.8;
                double panels = Math.ceil(wDay / dailyGenPerPanel);
                tvResult.setText(String.format("Paneles necesarios: %.0f", panels));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool2Batteries() {
        EditText etCons = findViewById(R.id.etBatCons);
        EditText etDays = findViewById(R.id.etBatDays);
        EditText etVolts = findViewById(R.id.etBatVolts);
        TextView tvResult = findViewById(R.id.tvBatResult);
        findViewById(R.id.btnCalcBat).setOnClickListener(v -> {
            try {
                double cons = Double.parseDouble(etCons.getText().toString());
                double days = Double.parseDouble(etDays.getText().toString());
                double volts = Double.parseDouble(etVolts.getText().toString());
                // Profundidad de descarga 50%
                double capAh = (cons * days) / (volts * 0.5);
                tvResult.setText(String.format("Capacidad necesaria: %.0f Ah", capAh));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool3Inverter() {
        EditText etLoad = findViewById(R.id.etInvLoad);
        TextView tvResult = findViewById(R.id.tvInvResult);
        findViewById(R.id.btnCalcInv).setOnClickListener(v -> {
            try {
                double load = Double.parseDouble(etLoad.getText().toString());
                // Inverter size + 25% safety margin
                double invSize = load * 1.25;
                tvResult.setText(String.format("Tamaño sugerido: %.0f W", invSize));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool5ROI() {
        EditText etCost = findViewById(R.id.etRoiCost);
        EditText etSav = findViewById(R.id.etRoiSav);
        TextView tvResult = findViewById(R.id.tvRoiResult);
        findViewById(R.id.btnCalcRoi).setOnClickListener(v -> {
            try {
                double cost = Double.parseDouble(etCost.getText().toString());
                double sav = Double.parseDouble(etSav.getText().toString());
                double savYear = sav * 12;
                double roi = cost / savYear;
                tvResult.setText(String.format("Retorno en: %.1f Años", roi));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool6Tilt() {
        EditText etLat = findViewById(R.id.etTiltLat);
        TextView tvResult = findViewById(R.id.tvTiltResult);
        findViewById(R.id.btnCalcTilt).setOnClickListener(v -> {
            try {
                double lat = Double.parseDouble(etLat.getText().toString());
                // Simple rule of thumb: Tilt roughly equals latitude for annual perf
                tvResult.setText(String.format("Ángulo sugerido: %.0f° (apunte al ecuador)", Math.abs(lat)));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }
}
