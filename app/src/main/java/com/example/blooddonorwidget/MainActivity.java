package com.example.blooddonorwidget;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private RadioGroup bloodTypeGroup;
    private Button saveButton;
    
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "BloodDonorPrefs";
    private static final String BLOOD_TYPE_KEY = "blood_type";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        bloodTypeGroup = findViewById(R.id.blood_type_group);
        saveButton = findViewById(R.id.save_button);
        
        // Load saved blood type
        String savedBloodType = prefs.getString(BLOOD_TYPE_KEY, "O+");
        selectRadioButton(savedBloodType);
        
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBloodType();
            }
        });
    }
    
    private void selectRadioButton(String bloodType) {
        int radioId = getRadioButtonId(bloodType);
        if (radioId != -1) {
            bloodTypeGroup.check(radioId);
        }
    }
    
    private int getRadioButtonId(String bloodType) {
        switch (bloodType) {
            case "O+": return R.id.blood_type_o_plus;
            case "O-": return R.id.blood_type_o_minus;
            case "A+": return R.id.blood_type_a_plus;
            case "A-": return R.id.blood_type_a_minus;
            case "B+": return R.id.blood_type_b_plus;
            case "B-": return R.id.blood_type_b_minus;
            case "AB+": return R.id.blood_type_ab_plus;
            case "AB-": return R.id.blood_type_ab_minus;
            default: return -1;
        }
    }
    
    private void saveBloodType() {
        int selectedId = bloodTypeGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a blood type", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String bloodType = getBloodTypeFromId(selectedId);
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(BLOOD_TYPE_KEY, bloodType);
        editor.apply();
        
        // Update widget
        BloodDonorWidget.updateWidget(this);
        
        Toast.makeText(this, "Blood type saved: " + bloodType, Toast.LENGTH_SHORT).show();
    }
    
    private String getBloodTypeFromId(int id) {
        if (id == R.id.blood_type_o_plus) return "O+";
        if (id == R.id.blood_type_o_minus) return "O-";
        if (id == R.id.blood_type_a_plus) return "A+";
        if (id == R.id.blood_type_a_minus) return "A-";
        if (id == R.id.blood_type_b_plus) return "B+";
        if (id == R.id.blood_type_b_minus) return "B-";
        if (id == R.id.blood_type_ab_plus) return "AB+";
        if (id == R.id.blood_type_ab_minus) return "AB-";
        return "O+";
    }
}
