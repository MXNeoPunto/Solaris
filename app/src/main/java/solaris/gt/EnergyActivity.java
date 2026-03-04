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

public class EnergyActivity extends AppCompatActivity {



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
        results.append("Resultados Calculadora Energía:\n");

        TextView tvOhm = findViewById(R.id.tvOhmResult);
        if (tvOhm != null && !tvOhm.getText().toString().isEmpty()) results.append("Ley de Ohm: ").append(tvOhm.getText().toString()).append("\n");

        TextView tvPower = findViewById(R.id.tvPowerResult);
        if (tvPower != null && !tvPower.getText().toString().isEmpty()) results.append("Potencia: ").append(tvPower.getText().toString()).append("\n");

        TextView tvDrop = findViewById(R.id.tvDropResult);
        if (tvDrop != null && !tvDrop.getText().toString().isEmpty()) results.append("Caída Tensión: ").append(tvDrop.getText().toString()).append("\n");

        TextView tvCable = findViewById(R.id.tvCableResult);
        if (tvCable != null && !tvCable.getText().toString().isEmpty()) results.append("Cable: ").append(tvCable.getText().toString()).append("\n");

        TextView tvCap = findViewById(R.id.tvCapResult);
        if (tvCap != null && !tvCap.getText().toString().isEmpty()) results.append("Capacitor: ").append(tvCap.getText().toString()).append("\n");

        TextView tvCost = findViewById(R.id.tvCostResult);
        if (tvCost != null && !tvCost.getText().toString().isEmpty()) results.append("Costo: ").append(tvCost.getText().toString()).append("\n");

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

        setupTool1Ohm();
        setupTool2Power();
        setupTool3Drop();
        setupTool4Cable();
        setupTool5Capacitor();
        setupTool6Cost();
    }

    private void setupTool1Ohm() {
        EditText etVolts = findViewById(R.id.etOhmVoltage);
        EditText etAmps = findViewById(R.id.etOhmCurrent);
        TextView tvResult = findViewById(R.id.tvOhmResult);
        findViewById(R.id.btnCalcOhm).setOnClickListener(v -> {
            try {
                double volts = Double.parseDouble(etVolts.getText().toString());
                double amps = Double.parseDouble(etAmps.getText().toString());
                tvResult.setText(String.format("Resistencia: %.2f Ω", (volts / amps)));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool2Power() {
        EditText etVolts = findViewById(R.id.etPowerVoltage);
        EditText etAmps = findViewById(R.id.etPowerCurrent);
        TextView tvResult = findViewById(R.id.tvPowerResult);
        findViewById(R.id.btnCalcPower).setOnClickListener(v -> {
            try {
                double volts = Double.parseDouble(etVolts.getText().toString());
                double amps = Double.parseDouble(etAmps.getText().toString());
                tvResult.setText(String.format("Potencia: %.2f W", (volts * amps)));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool3Drop() {
        EditText etLen = findViewById(R.id.etDropLength);
        EditText etAmps = findViewById(R.id.etDropCurrent);
        EditText etArea = findViewById(R.id.etDropArea);
        TextView tvResult = findViewById(R.id.tvDropResult);
        findViewById(R.id.btnCalcDrop).setOnClickListener(v -> {
            try {
                double len = Double.parseDouble(etLen.getText().toString());
                double amps = Double.parseDouble(etAmps.getText().toString());
                double area = Double.parseDouble(etArea.getText().toString());
                // Rho for copper is ~0.0172 ohm mm2 / m
                double drop = (2 * len * 0.0172 * amps) / area;
                tvResult.setText(String.format("Caída: %.2f V", drop));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool4Cable() {
        EditText etAmps = findViewById(R.id.etCableCurrent);
        TextView tvResult = findViewById(R.id.tvCableResult);
        findViewById(R.id.btnCalcCable).setOnClickListener(v -> {
            try {
                double amps = Double.parseDouble(etAmps.getText().toString());
                String awg = "";
                if (amps <= 15) awg = "AWG 14";
                else if (amps <= 20) awg = "AWG 12";
                else if (amps <= 30) awg = "AWG 10";
                else if (amps <= 40) awg = "AWG 8";
                else if (amps <= 55) awg = "AWG 6";
                else if (amps <= 70) awg = "AWG 4";
                else if (amps <= 95) awg = "AWG 2";
                else awg = "Mayor a AWG 2 / Cable especial";
                tvResult.setText("Calibre sugerido: " + awg);
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool5Capacitor() {
        EditText etCap = findViewById(R.id.etCapCapacitance);
        EditText etVolts = findViewById(R.id.etCapVoltage);
        TextView tvResult = findViewById(R.id.tvCapResult);
        findViewById(R.id.btnCalcCap).setOnClickListener(v -> {
            try {
                double capMicro = Double.parseDouble(etCap.getText().toString());
                double volts = Double.parseDouble(etVolts.getText().toString());
                double capFarad = capMicro / 1000000.0;
                double energy = 0.5 * capFarad * volts * volts;
                tvResult.setText(String.format("Energía: %.4f J", energy));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    private void setupTool6Cost() {
        EditText etPower = findViewById(R.id.etCostPower);
        EditText etTime = findViewById(R.id.etCostTime);
        EditText etRate = findViewById(R.id.etCostRate);
        TextView tvResult = findViewById(R.id.tvCostResult);
        findViewById(R.id.btnCalcCost).setOnClickListener(v -> {
            try {
                double powerW = Double.parseDouble(etPower.getText().toString());
                double hours = Double.parseDouble(etTime.getText().toString());
                double rate = Double.parseDouble(etRate.getText().toString());

                double kwhPerDay = (powerW / 1000.0) * hours;
                double costPerMonth = kwhPerDay * 30 * rate;
                tvResult.setText(String.format("Costo aprox 30 días: $%.2f", costPerMonth));
            } catch (Exception e) {
                tvResult.setText("Valores inválidos");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}