package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etWeight;
    private EditText etRate;
    private Spinner spinnerTicketClass;
    private Button btnCalculate;
    private TextView tvExcessWeight;
    private TextView tvCharge;
    private TextView tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        etWeight = findViewById(R.id.etWeight);
        etRate = findViewById(R.id.etRate);
        spinnerTicketClass = findViewById(R.id.spinnerTicketClass);
        btnCalculate = findViewById(R.id.btnCalculate);
        tvExcessWeight = findViewById(R.id.tvExcessWeight);
        tvCharge = findViewById(R.id.tvCharge);
        tvTotal = findViewById(R.id.tvTotal);

        // Autofill excess rate when ticket class selection changes
        spinnerTicketClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedClass = parent.getItemAtPosition(position).toString();
                if (selectedClass.equalsIgnoreCase("Economy")) {
                    etRate.setText("15");
                } else if (selectedClass.equalsIgnoreCase("Business")) {
                    etRate.setText("12");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weightStr = etWeight.getText().toString().trim();
                String rateStr = etRate.getText().toString().trim();

                // Good Design Practice: Inline validation with setError
                if (weightStr.isEmpty()) {
                    etWeight.setError("Please enter luggage weight");
                    etWeight.requestFocus();
                    return;
                }

                if (rateStr.isEmpty()) {
                    etRate.setError("Please enter excess rate");
                    etRate.requestFocus();
                    return;
                }

                try {
                    // Fetch input values
                    double weight = Double.parseDouble(weightStr);
                    double rate = Double.parseDouble(rateStr);

                    // Check ticket type
                    String selectedClass = spinnerTicketClass.getSelectedItem().toString();

                    // Set allowance limit (Economy: 20kg, Business: 40kg)
                    double allowance = selectedClass.equalsIgnoreCase("Economy") ? 20.0 : 40.0;

                    // Count excess weight
                    double excessWeight = weight - allowance;
                    if (excessWeight < 0) {
                        excessWeight = 0.0;
                    }

                    // Calculate charge
                    double charge = excessWeight * rate;
                    double total = charge + (charge * 0.08);

                    // Display results
                    tvExcessWeight.setText(String.format(Locale.getDefault(), "Excess weight: %.1f kg", excessWeight));
                    tvCharge.setText(String.format(Locale.getDefault(), "Charge: RM%.2f", charge));
                    tvTotal.setText(String.format(Locale.getDefault(), "Total Payable: RM%.2f", total));
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Invalid numeric input entered", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            // Rubric Req: Share application URL + calculation results
            String githubUrl = getString(R.string.github_url);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareBody = "Check out the Excess Fee Checker application!\nApp URL: " + githubUrl + "\n\n" +
                    "Calculation Results:\n" +
                    tvExcessWeight.getText().toString() + "\n" +
                    tvCharge.getText().toString() + "\n" +
                    tvTotal.getText().toString();
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Excess Fee Checker App");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(shareIntent, "Share App via"));
            return true;

        } else if (id == R.id.action_about) {
            // Rubric Req: Developers' details, information, copyright statement, clickable URL
            String githubUrl = getString(R.string.github_url);
            String messageHtml = "<b>Application:</b> Excess Fee Checker v1.0<br/><br/>" +
                    "<b>Developer / Author:</b><br/>" +
                    getString(R.string.student_name) + "<br/>" +
                    "<b>Student Number:</b> " + getString(R.string.student_id) + "<br/>" +
                    "<b>Group:</b> " + getString(R.string.group_code) + "<br/>" +
                    "<b>Programme Code:</b> " + getString(R.string.programme_code) + "<br/><br/>" +
                    "<b>Application Website / Repository:</b><br/>" +
                    "<a href=\"" + githubUrl + "\">" + githubUrl + "</a><br/><br/>" +
                    "<b>Copyright Statement:</b><br/>" +
                    "© 2025 Mohammad Meerza Danial. All Rights Reserved.";

            TextView messageTextView = new TextView(this);
            messageTextView.setText(Html.fromHtml(messageHtml, Html.FROM_HTML_MODE_LEGACY));
            messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
            float density = getResources().getDisplayMetrics().density;
            int padding = (int) (20 * density);
            messageTextView.setPadding(padding, padding, padding, padding);
            messageTextView.setTextSize(14);

            new AlertDialog.Builder(this)
                    .setTitle("About Excess Fee Checker")
                    .setView(messageTextView)
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}