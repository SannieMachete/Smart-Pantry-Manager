package com.sannie.smartpantry.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.sannie.smartpantry.R;

/**
 * Settings screen: lets the user toggle expiring-soon alerts and pick a preferred
 * unit system. Preferences are stored in SharedPreferences and persist
 * across app restarts.
 */
public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "smart_pantry_prefs";
    private static final String KEY_EXPIRING_ALERTS = "expiring_alerts_enabled";
    private static final String KEY_UNITS_METRIC = "units_metric";

    private SwitchMaterial switchExpiringAlerts;
    private RadioGroup radioGroupUnits;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchExpiringAlerts = view.findViewById(R.id.switch_expiring_alerts);
        radioGroupUnits = view.findViewById(R.id.radio_group_units);
        android.widget.RadioButton radioMetric = view.findViewById(R.id.radio_metric);
        android.widget.RadioButton radioImperial = view.findViewById(R.id.radio_imperial);
        android.widget.Button btnSave = view.findViewById(R.id.btn_save_settings);

        SharedPreferences prefs = getPrefs();
        switchExpiringAlerts.setChecked(prefs.getBoolean(KEY_EXPIRING_ALERTS, true));
        boolean isMetric = prefs.getBoolean(KEY_UNITS_METRIC, true);
        radioMetric.setChecked(isMetric);
        radioImperial.setChecked(!isMetric);

        btnSave.setOnClickListener(v -> {
            boolean metricSelected = radioGroupUnits.getCheckedRadioButtonId() == R.id.radio_metric;
            getPrefs().edit()
                    .putBoolean(KEY_EXPIRING_ALERTS, switchExpiringAlerts.isChecked())
                    .putBoolean(KEY_UNITS_METRIC, metricSelected)
                    .apply();
            Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
        });
    }

    private SharedPreferences getPrefs() {
        return requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
    }
}
