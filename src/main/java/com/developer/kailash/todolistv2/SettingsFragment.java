package com.developer.kailash.todolistv2;


import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ToggleButton;


/**
 * Settings Fragment for MainActivity
 */
public class SettingsFragment extends Fragment {
    private static Bundle bundle = new Bundle();
    public static boolean vibratorOn;

    ToggleButton vibrationToggle;
    Button themesButton;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        vibrationToggle = (ToggleButton)getView().findViewById(R.id.vibrator);
        vibrationToggle.setChecked(bundle.getBoolean("ToggleButtonState",false));
        vibrationToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((ToggleButton) v).isChecked();
                if (checked) {
                    vibratorOn = true;
                    Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(500);
                } else {
                    vibratorOn = false;
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        bundle.putBoolean("ToggleButtonState", vibrationToggle.isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
        vibrationToggle.setChecked(bundle.getBoolean("ToggleButtonState",false));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

}
