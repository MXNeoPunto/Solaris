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

public class WeldingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welding);

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
        findViewById(R.id.btnCalcWeldAmp).setOnClickListener(v -> {
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
        findViewById(R.id.btnCalcDuty).setOnClickListener(v -> {
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
        findViewById(R.id.btnCalcGas).setOnClickListener(v -> {
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
        findViewById(R.id.btnCalcHeat).setOnClickListener(v -> {
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
}
