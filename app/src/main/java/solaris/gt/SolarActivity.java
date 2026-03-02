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

import com.google.android.material.appbar.MaterialToolbar;

public class SolarActivity extends AppCompatActivity {

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
