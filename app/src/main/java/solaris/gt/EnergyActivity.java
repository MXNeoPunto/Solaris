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

public class EnergyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_energy);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.energy_coordinator), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        EditText etVoltage = findViewById(R.id.etVoltage);
        EditText etCurrent = findViewById(R.id.etCurrent);
        Button btnCalculateEnergy = findViewById(R.id.btnCalculateEnergy);
        TextView tvEnergyResult = findViewById(R.id.tvEnergyResult);

        btnCalculateEnergy.setOnClickListener(v -> {
            String voltageStr = etVoltage.getText().toString();
            String currentStr = etCurrent.getText().toString();

            if (voltageStr.isEmpty() || currentStr.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese voltaje y corriente", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double voltage = Double.parseDouble(voltageStr);
                double current = Double.parseDouble(currentStr);
                double power = voltage * current;
                tvEnergyResult.setText(String.format("Potencia (W): %.2f Watts", power));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valores inválidos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}