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

        EditText etDailyConsumption = findViewById(R.id.etDailyConsumption);
        EditText etPanelWattage = findViewById(R.id.etPanelWattage);
        EditText etSunHours = findViewById(R.id.etSunHours);
        Button btnCalculateSolar = findViewById(R.id.btnCalculateSolar);
        TextView tvSolarResult = findViewById(R.id.tvSolarResult);

        btnCalculateSolar.setOnClickListener(v -> {
            String dailyConsumptionStr = etDailyConsumption.getText().toString();
            String panelWattageStr = etPanelWattage.getText().toString();
            String sunHoursStr = etSunHours.getText().toString();

            if (dailyConsumptionStr.isEmpty() || panelWattageStr.isEmpty() || sunHoursStr.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double consumptionKwh = Double.parseDouble(dailyConsumptionStr);
                double panelWattage = Double.parseDouble(panelWattageStr);
                double sunHours = Double.parseDouble(sunHoursStr);

                // Consumption in Wh per day
                double consumptionWh = consumptionKwh * 1000;

                // Account for system losses (typical efficiency is ~80%)
                double requiredDailyProductionWh = consumptionWh / 0.8;

                // How much one panel produces per day
                double panelDailyProductionWh = panelWattage * sunHours;

                // Panels needed
                double panelsNeeded = requiredDailyProductionWh / panelDailyProductionWh;

                int roundedPanels = (int) Math.ceil(panelsNeeded);

                tvSolarResult.setText(String.format("Paneles estimados: %d", roundedPanels));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valores inválidos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}