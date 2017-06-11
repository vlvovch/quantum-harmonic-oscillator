package com.vlvolad.quantumoscillator;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Vladimir on 18.05.2015.
 */
public class OrbitalDialog extends Activity {
    private TextView tvk, tvl, tvm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(R.string.orbital_selection);
        setContentView(R.layout.orbital_selection);

        tvk = (TextView)findViewById(R.id.tvnn);
        tvl = (TextView)findViewById(R.id.tvln);
        tvm = (TextView)findViewById(R.id.tvmn);

        SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(
                this);

        tvk.setText("" + settings.getInt("k", 0));
        tvl.setText("" + settings.getInt("l", 0));
        tvm.setText("" + settings.getInt("m", 0));

        findViewById(R.id.bSubn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int k = Integer.parseInt(tvk.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                if (k-1>=0) {
                    k--;
                    tvk.setText("" + k);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bAddn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int k = Integer.parseInt(tvk.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                if (k+1<=25) {
                    k++;
                    tvk.setText("" + k);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bSubl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int k = Integer.parseInt(tvk.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                int m = Integer.parseInt(tvm.getText().toString());
                if (l-1>=Math.abs(m)) {
                    l--;
                    tvl.setText("" + l);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bAddl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int k = Integer.parseInt(tvk.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                int m = Integer.parseInt(tvm.getText().toString());
                if (l+1<=25) {
                    l++;
                    tvl.setText("" + l);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bSubm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int k = Integer.parseInt(tvk.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                int m = Integer.parseInt(tvm.getText().toString());
                if (Math.abs(m-1)<=l) {
                    m--;
                    tvm.setText("" + m);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bAddm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int k = Integer.parseInt(tvk.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                int m = Integer.parseInt(tvm.getText().toString());
                if (Math.abs(m+1)<=l) {
                    m++;
                    tvm.setText("" + m);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        boolean isReal = settings.getBoolean("wave_function_real", true);
        if (isReal) {
            ((RadioButton)findViewById(R.id.radioReal)).setChecked(true);
            //((RadioButton)findViewById(R.id.radioComplex)).setChecked(false);
        }
        else {
            ((RadioButton)findViewById(R.id.radioComplex)).setChecked(true);
        }
//        FlurryAgent.onStartSession(this);
//        FlurryAgent.logEvent("AboutActivity",true);
    }

    /**
     * Callback method defined by the View
     * @param v
     */
    public void okDialog(View v) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                this).edit();

        boolean tochange = false;
        //int a = PreferenceManager.getDefaultSharedPreferences(this).getInt("k", 0);
        if (PreferenceManager.getDefaultSharedPreferences(this).getInt("k", 0) != Integer.parseInt(tvk.getText().toString()))
            tochange = true;
        if (PreferenceManager.getDefaultSharedPreferences(this).getInt("l", 0) != Integer.parseInt(tvl.getText().toString()))
            tochange = true;
        if (PreferenceManager.getDefaultSharedPreferences(this).getInt("m", 0) != Integer.parseInt(tvm.getText().toString()))
            tochange = true;
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("wave_function_real", true) != ((RadioButton)findViewById(R.id.radioReal)).isChecked())
            tochange = true;

        editor.putInt("k", Integer.parseInt(tvk.getText().toString()));
        editor.putInt("l", Integer.parseInt(tvl.getText().toString()));
        editor.putInt("m", Integer.parseInt(tvm.getText().toString()));

        boolean isReal = ((RadioButton)findViewById(R.id.radioReal)).isChecked();
        editor.putBoolean("wave_function_real", isReal);

        editor.commit();

        QOGLRenderer.mOscillator.toCont = tochange;

        OrbitalDialog.this.finish();
    }
    public void cancelDialog(View v) {
        OrbitalDialog.this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        FlurryAgent.endTimedEvent("AboutActivity");
//        FlurryAgent.onEndSession(this);
    }
}
