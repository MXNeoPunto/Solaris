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

public class WeldingActivity extends AppCompatActivity {
    private SoundHelper soundHelper;


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
        results.append("Resultados Calculadora Soldadura:\n");

        TextView tvAmp = findViewById(R.id.tvWeldAmpResult);
        if (tvAmp != null && !tvAmp.getText().toString().isEmpty()) results.append("Amperaje: ").append(tvAmp.getText().toString()).append("\n");

        TextView tvDuty = findViewById(R.id.tvDutyResult);
        if (tvDuty != null && !tvDuty.getText().toString().isEmpty()) results.append("Ciclo Trabajo: ").append(tvDuty.getText().toString()).append("\n");

        TextView tvGas = findViewById(R.id.tvGasResult);
        if (tvGas != null && !tvGas.getText().toString().isEmpty()) results.append("Flujo Gas: ").append(tvGas.getText().toString()).append("\n");

        TextView tvHeat = findViewById(R.id.tvHeatResult);
        if (tvHeat != null && !tvHeat.getText().toString().isEmpty()) results.append("Entrada Calor: ").append(tvHeat.getText().toString()).append("\n");

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
        setContentView(R.layout.activity_welding);
        soundHelper = new SoundHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welding_coordinator), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        setupTool1Amperage();
        setupTool3DutyCycle();
        setupTool4GasFlow();
        setupTool5HeatInput();
    }

    private void setupTool1Amperage() {
        EditText etThick = findViewById(R.id.etWeldThick);
        TextView tvResult = findViewById(R.id.tvWeldAmpResult);
        findViewById(R.id.btnCalcWeldAmp).setOnClickListener(v -> { if (soundHelper != null) soundHelper.playClick();
            try {
                double thick = Double.parseDouble(etThick.getText().toString());
                // Regla general: 40 amperios por cada mm (1 amperio por cada 0.001 pulg)
                double minAmp = thick * 35;
                double maxAmp = thick * 45;
                tvResult.setText(String.format("Sugerido: %.0f - %.0f A", minAmp, maxAmp));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool3DutyCycle() {
        EditText etNomA = findViewById(R.id.etDutyNomAmp);
        EditText etNomPct = findViewById(R.id.etDutyNomPct);
        EditText etDesA = findViewById(R.id.etDutyDesAmp);
        TextView tvResult = findViewById(R.id.tvDutyResult);
        findViewById(R.id.btnCalcDuty).setOnClickListener(v -> { if (soundHelper != null) soundHelper.playClick();
            try {
                double nomA = Double.parseDouble(etNomA.getText().toString());
                double nomPct = Double.parseDouble(etNomPct.getText().toString()) / 100.0;
                double desA = Double.parseDouble(etDesA.getText().toString());
                // Formula: Desired Duty Cycle = (Rated Amps^2 / Desired Amps^2) * Rated Duty Cycle
                double desPct = ((nomA * nomA) / (desA * desA)) * nomPct;
                if(desPct > 1.0) desPct = 1.0;
                tvResult.setText(String.format("Ciclo estimado: %.1f%%", desPct * 100));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool4GasFlow() {
        EditText etCup = findViewById(R.id.etGasCup);
        TextView tvResult = findViewById(R.id.tvGasResult);
        findViewById(R.id.btnCalcGas).setOnClickListener(v -> { if (soundHelper != null) soundHelper.playClick();
            try {
                double cup = Double.parseDouble(etCup.getText().toString());
                // Regla empirica: Flujo (L/min) ~ Diametro copa (mm)
                double minF = cup * 0.8;
                double maxF = cup * 1.2;
                tvResult.setText(String.format("Sugerido: %.1f - %.1f L/min", minF, maxF));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool5HeatInput() {
        EditText etV = findViewById(R.id.etHeatV);
        EditText etA = findViewById(R.id.etHeatA);
        EditText etSpeed = findViewById(R.id.etHeatSpeed);
        TextView tvResult = findViewById(R.id.tvHeatResult);
        findViewById(R.id.btnCalcHeat).setOnClickListener(v -> { if (soundHelper != null) soundHelper.playClick();
            try {
                double vlt = Double.parseDouble(etV.getText().toString());
                double amp = Double.parseDouble(etA.getText().toString());
                double speed = Double.parseDouble(etSpeed.getText().toString());
                // HI = (V * A * 60) / (Speed * 1000) -> kJ/mm
                // simplified for per mm directly using Joules
                double hi = (vlt * amp * 60) / (speed * 1000);
                tvResult.setText(String.format("Entrada Calor: %.2f kJ/mm", hi));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundHelper != null) {
            soundHelper.release();
        }
    }
}