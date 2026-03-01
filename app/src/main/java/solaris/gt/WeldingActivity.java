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

        EditText etMetalThickness = findViewById(R.id.etMetalThickness);
        Button btnCalculateWelding = findViewById(R.id.btnCalculateWelding);
        TextView tvWeldingResult = findViewById(R.id.tvWeldingResult);

        btnCalculateWelding.setOnClickListener(v -> {
            String thicknessStr = etMetalThickness.getText().toString();

            if (thicknessStr.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese el grosor", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double thickness = Double.parseDouble(thicknessStr);

                String recommendation = "";

                if (thickness < 1.0) {
                    recommendation = "Grosor muy delgado. Se recomienda soldadura TIG o MIG. Amperaje: 20-40A. Electrodo: 1/16\"";
                } else if (thickness <= 2.0) {
                    recommendation = "Amperaje recomendado: 40-70A.\nElectrodo: E6013 de 3/32\"";
                } else if (thickness <= 3.0) {
                    recommendation = "Amperaje recomendado: 70-90A.\nElectrodo: E6013 de 1/8\"";
                } else if (thickness <= 5.0) {
                    recommendation = "Amperaje recomendado: 90-120A.\nElectrodo: E6013 o E7018 de 1/8\"";
                } else if (thickness <= 8.0) {
                    recommendation = "Amperaje recomendado: 120-150A.\nElectrodo: E7018 de 5/32\"";
                } else {
                    recommendation = "Amperaje recomendado: 150A o más.\nElectrodo: E7018 de 5/32\" o 3/16\". Considere pasadas múltiples.";
                }

                tvWeldingResult.setText(recommendation);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
            }
        });
    }
}