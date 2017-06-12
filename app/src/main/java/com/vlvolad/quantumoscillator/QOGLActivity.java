package com.vlvolad.quantumoscillator;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by Vladimir on 29.04.2015.
 */
public class QOGLActivity extends Activity {

    private QOGLSurfaceView mGLView;
    private Display display;

    private String mapl;

    private boolean mRateState;

    static String TAG = "QOGLActivity";

    static int frequency = 50;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (QOGLRenderer.mOscillator.fin==0) {
                timerHandler.postDelayed(this, frequency);
                //findViewById(R.id.progress).setVisibility(View.VISIBLE);
                if (QOGLRenderer.mOscillator.totalprogress>0) ((ProgressBar)findViewById(R.id.progress)).setProgress((QOGLRenderer.mOscillator.progress * 100) / QOGLRenderer.mOscillator.totalprogress);
                else ((ProgressBar)findViewById(R.id.progress)).setProgress(0);
            }
            else {
                mGLView.queueEvent(new Runnable() {
                    // This method will be called on the rendering
                    // thread:
                    public void run() {
                        //HAGLRenderer.mOscillator.reallocateMemoryFinal();
                    }});
                mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                timerHandler.removeCallbacks(timerRunnable);
                if (Build.VERSION.SDK_INT >= 11)
                    ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_replay);
                else
                    ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_replay_dark);
                findViewById(R.id.button_regenerate).setEnabled(true);
                findViewById(R.id.button_random).setEnabled(true);
                isRunning = false;
                findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                //findViewById(R.id.energy_name).setVisibility(View.VISIBLE);
                if (QOGLRenderer.mOscillator.overflow)
                    Toast.makeText(getApplicationContext(), R.string.discrete_warning, Toast.LENGTH_SHORT).show();
                mGLView.requestRender();
            }
        }
    };

    private SeekBar seekBarPercent;
    private TextView textViewPercent;
    private SeekBar seekBarStepSize;
    private TextView textViewStepSize;

//    private TextView tvn, tvl, tvm;

    private boolean isRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.quantumoscillator_gl);

        if(Build.VERSION.SDK_INT >= 14)
            getActionBar().setIcon(R.drawable.ic_action_oscillator);

        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        //Log.v("onCreate", "maxMemory:" + Long.toString(maxMemory));

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final int memoryClass = am.getMemoryClass();
        //Log.v("onCreate", "memoryClass:" + Integer.toString(memoryClass));

        mapl = "spdfghi";

        mRateState = PreferenceManager.getDefaultSharedPreferences(
                QOGLActivity.this).getBoolean("rate_clicked", false);

        QOGLRenderer.mOscillator.k = PreferenceManager.getDefaultSharedPreferences(
                QOGLActivity.this).getInt("k", 0);
        QOGLRenderer.mOscillator.l = PreferenceManager.getDefaultSharedPreferences(
                QOGLActivity.this).getInt("l", 0);
        QOGLRenderer.mOscillator.m = PreferenceManager.getDefaultSharedPreferences(
                QOGLActivity.this).getInt("m", 0);
        QOGLRenderer.mOscillator.qOscillator.setsign(false);
        QOGLRenderer.mOscillator.pct = PreferenceManager.getDefaultSharedPreferences(
                QOGLActivity.this).getFloat("percent", 70.f);
        QOGLRenderer.mOscillator.fStepSize = PreferenceManager.getDefaultSharedPreferences(
                QOGLActivity.this).getFloat("step_size", 5.f);

        QOGLRenderer.mOscillator.fin = 0;
        updateOrbitalName();

        mGLView = (QOGLSurfaceView)findViewById(R.id.gl_surface_view);

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mGLView.mDensity = displayMetrics.density;

        mGLView.queueEvent(new Runnable() {
            // This method will be called on the rendering
            // thread:
            public void run() {
                QOGLRenderer.mOscillator.memoryclass = memoryClass;
                QOGLRenderer.mOscillator.totalprogress = 0;
                QOGLRenderer.mOscillator.reallocateMemory();
                QOGLRenderer.mOscillator.setRealKsi(PreferenceManager.getDefaultSharedPreferences(
                        QOGLActivity.this).getBoolean("wave_function_real", true));
                QOGLRenderer.mOscillator.regenerate();
            }});


        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        isRunning = false;

        findViewById(R.id.button_random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isRunning) {
                    QOGLRenderer.mOscillator.pickRandomOrbital(5);
                    QOGLRenderer.mOscillator.fin = 0;
                    QOGLRenderer.mOscillator.fStepSize = 2.f + seekBarStepSize.getProgress() / 10.f;
                    QOGLRenderer.mOscillator.pct = seekBarPercent.getProgress() + 1;
                    QOGLRenderer.mOscillator.setRealKsi(PreferenceManager.getDefaultSharedPreferences(
                            QOGLActivity.this).getBoolean("wave_function_real", true));
                    updateOrbitalName();
                    findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    //findViewById(R.id.energy_name).setVisibility(View.INVISIBLE);
                    if (Build.VERSION.SDK_INT >= 11)
                        ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop);
                    else
                        ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop_dark);
                    findViewById(R.id.button_random).setEnabled(false);
                    //findViewById(R.id.button_regenerate).setEnabled(false);
                    isRunning = true;
                    timerHandler.postDelayed(timerRunnable, 0);
                    mGLView.queueEvent(new Runnable() {
                        // This method will be called on the rendering
                        // thread:
                        public void run() {
                            //HAGLRenderer.mOscillator.reallocateMemory();
                            QOGLRenderer.mOscillator.totalprogress = 0;
                            QOGLRenderer.mOscillator.regenerate();
                            mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                        }
                    });
                }
            }
        });

        findViewById(R.id.button_orbital).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QOGLActivity.this, OrbitalDialog.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_regenerate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //HAGLRenderer.mOscillator.pickRandomOrbital(5);
                if (!isRunning) {
                    QOGLRenderer.mOscillator.k = PreferenceManager.getDefaultSharedPreferences(
                            QOGLActivity.this).getInt("k", 0);
                    QOGLRenderer.mOscillator.l = PreferenceManager.getDefaultSharedPreferences(
                            QOGLActivity.this).getInt("l", 0);
                    QOGLRenderer.mOscillator.m = PreferenceManager.getDefaultSharedPreferences(
                            QOGLActivity.this).getInt("m", 0);
                    QOGLRenderer.mOscillator.qOscillator.setsign(false);
                    QOGLRenderer.mOscillator.setRealKsi(PreferenceManager.getDefaultSharedPreferences(
                            QOGLActivity.this).getBoolean("wave_function_real", true));
                    QOGLRenderer.mOscillator.fin = 0;
                    QOGLRenderer.mOscillator.fStepSize = 2.f + seekBarStepSize.getProgress() / 10.f;
                    QOGLRenderer.mOscillator.pct = seekBarPercent.getProgress() + 1;
                    updateOrbitalName();
                    findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= 11)
                        ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop);
                    else
                        ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop_dark);
                    isRunning = true;
                    timerHandler.postDelayed(timerRunnable, 0);
                    mGLView.queueEvent(new Runnable() {
                        // This method will be called on the rendering
                        // thread:
                        public void run() {
                            QOGLRenderer.mOscillator.totalprogress = 0;
                            QOGLRenderer.mOscillator.regenerate();
                            mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                        }
                    });
                }
                else {
                    if (Build.VERSION.SDK_INT >= 11)
                        ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_replay);
                    else
                        ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_replay_dark);
                    findViewById(R.id.button_regenerate).setEnabled(false);
                    isRunning = false;
                    mGLView.queueEvent(new Runnable() {
                        // This method will be called on the rendering
                        // thread:
                        public void run() {
                            //HAGLRenderer.mOscillator.reallocateMemory();
                            QOGLRenderer.mOscillator.InterruptThread();
                        }
                    });
                    timerHandler.postDelayed(timerRunnable, 0);
                }
            }
        });

        findViewById(R.id.button_regenerate).setEnabled(false);

        seekBarPercent = (SeekBar)findViewById(R.id.seekBarPercent);
        textViewPercent = (TextView)findViewById(R.id.textViewPercent);

        seekBarPercent.setProgress((int)(QOGLRenderer.mOscillator.pct+1e-5)-1);
        textViewPercent.setText((int) QOGLRenderer.mOscillator.pct + " %");

        //findViewById(R.id.energy_name).setVisibility(View.INVISIBLE);

        seekBarPercent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                textViewPercent.setText((progress+1) + " %");
//                Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }
//
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewPercent.setText((progress+1) + " %");
//                Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });


        seekBarStepSize = (SeekBar)findViewById(R.id.seekBarStepSize);
        textViewStepSize = (TextView)findViewById(R.id.textViewStepSize);

        seekBarStepSize.setProgress((int)((QOGLRenderer.mOscillator.fStepSize+1e-5-2.f)*10));
        textViewStepSize.setText(String.format("%.1f", QOGLRenderer.mOscillator.fStepSize));

        seekBarStepSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                textViewStepSize.setText(String.format("%.1f", 2. + progress / 10.));
//                Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            //
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewStepSize.setText(String.format("%.1f", 2. + progress / 10.));
//                Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });

        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void regenerate() {
        if (!isRunning) {
            QOGLRenderer.mOscillator.k = PreferenceManager.getDefaultSharedPreferences(
                    QOGLActivity.this).getInt("k", 0);
            QOGLRenderer.mOscillator.l = PreferenceManager.getDefaultSharedPreferences(
                    QOGLActivity.this).getInt("l", 0);
            QOGLRenderer.mOscillator.m = PreferenceManager.getDefaultSharedPreferences(
                    QOGLActivity.this).getInt("m", 0);
            QOGLRenderer.mOscillator.qOscillator.setsign(false);
            QOGLRenderer.mOscillator.setRealKsi(PreferenceManager.getDefaultSharedPreferences(
                    QOGLActivity.this).getBoolean("wave_function_real", true));
            QOGLRenderer.mOscillator.fin = 0;
            QOGLRenderer.mOscillator.fStepSize = 2.f + seekBarStepSize.getProgress() / 10.f;
            QOGLRenderer.mOscillator.pct = seekBarPercent.getProgress() + 1;
            updateOrbitalName();
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            //findViewById(R.id.energy_name).setVisibility(View.INVISIBLE);
            if (Build.VERSION.SDK_INT >= 11)
                ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop);
            else
                ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop_dark);
            //findViewById(R.id.button_regenerate).setEnabled(false);
            isRunning = true;
            timerHandler.postDelayed(timerRunnable, 0);
            mGLView.queueEvent(new Runnable() {
                // This method will be called on the rendering
                // thread:
                public void run() {
                    //HAGLRenderer.mOscillator.reallocateMemory();
                    QOGLRenderer.mOscillator.totalprogress = 0;
                    QOGLRenderer.mOscillator.regenerate();
                    mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                }
            });
        }
        else {
            if (Build.VERSION.SDK_INT >= 11)
                ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_replay);
            else
                ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_replay_dark);
            findViewById(R.id.button_regenerate).setEnabled(false);
            isRunning = false;
            mGLView.queueEvent(new Runnable() {
                // This method will be called on the rendering
                // thread:
                public void run() {
                    //HAGLRenderer.mOscillator.reallocateMemory();
                    QOGLRenderer.mOscillator.InterruptThread();
                }
            });
            timerHandler.postDelayed(timerRunnable, 0);
        }
    }

    private void updateOrbitalName() {
        int tmpm = QOGLRenderer.mOscillator.m;
        if (QOGLRenderer.mOscillator.qOscillator.sign) tmpm = -tmpm;
        /*if (HAGLRenderer.mOscillator.l<=6) ((TextView)findViewById(R.id.orbital_name)).setText(
                HAGLRenderer.mOscillator.k + mapl.substring(HAGLRenderer.mOscillator.l,HAGLRenderer.mOscillator.l+1) + ", m=" + tmpm
                );
        else ((TextView)findViewById(R.id.orbital_name)).setText(
                "k=" + HAGLRenderer.mOscillator.k + ", l=" + HAGLRenderer.mOscillator.l + ", m=" + tmpm
                );*/

        ((Button)findViewById(R.id.button_orbital)).setText(
                " k=" + QOGLRenderer.mOscillator.k + ", l=" + QOGLRenderer.mOscillator.l + ", m=" + tmpm + " "
        );

        ((TextView)findViewById(R.id.energy_name)).setText(Html.fromHtml(
                "E = <sup><small><small>" + (2*(2* QOGLRenderer.mOscillator.k + QOGLRenderer.mOscillator.l)+3) + "</small></small></sup><small>/</small><sub><small><small>2</small></small></sub>&#8463;&omega;")
        );

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                this).edit();

        editor.putInt("k", QOGLRenderer.mOscillator.k);
        editor.putInt("l", QOGLRenderer.mOscillator.l);
        editor.putInt("m", tmpm);

        editor.commit();

    }


    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mRateState = PreferenceManager.getDefaultSharedPreferences(
                QOGLActivity.this).getBoolean("rate_clicked", false);
        timerHandler.removeCallbacks(timerRunnable);
        mGLView.queueEvent(new Runnable() {
            // This method will be called on the rendering
            // thread:
            public void run() {
                QOGLRenderer.mOscillator.InterruptThread();
            }});
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        ((Button)findViewById(R.id.button_orbital)).setText(
                " k=" + PreferenceManager.getDefaultSharedPreferences(
                        QOGLActivity.this).getInt("k", 0) + ", l=" + PreferenceManager.getDefaultSharedPreferences(
                        QOGLActivity.this).getInt("l", 0) + ", m=" + PreferenceManager.getDefaultSharedPreferences(
                        QOGLActivity.this).getInt("m", 0) + " "
        );
        if (QOGLRenderer.mOscillator.fin==0) {
            mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//            findViewById(R.id.button_regenerate).setEnabled(false);
            findViewById(R.id.button_regenerate).setEnabled(true);
            if (Build.VERSION.SDK_INT >= 11)
                ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop);
            else
                ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop_dark);
            isRunning = true;
        }
        if (QOGLRenderer.mOscillator.toCont) {
            QOGLRenderer.mOscillator.toCont = false;
            regenerate();
        }

        if (Build.VERSION.SDK_INT >= 11 && mRateState != PreferenceManager.getDefaultSharedPreferences(
                QOGLActivity.this).getBoolean("rate_clicked", false))
            invalidateOptionsMenu();

        timerHandler.postDelayed(timerRunnable, 0);
        mGLView.onResume();
        mGLView.requestRender();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_rate);
        item.setVisible(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("rate_clicked", false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getText(R.string.share_subject).toString());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getText(R.string.share_text).toString());
                startActivity(Intent.createChooser(sharingIntent, getText(R.string.share_via).toString()));
                return true;
            case R.id.action_wiki:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getText(R.string.wikipedia_url).toString())));
                return true;
            case R.id.action_rate:
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (Exception e) {
//                    Log.d("Information", "Message =" + e);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }

                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("rate_clicked", true).apply();
                if(Build.VERSION.SDK_INT >= 11)
                    invalidateOptionsMenu();

                return true;
            case R.id.action_information:
                //showHelp();
                Intent intentParam = new Intent(QOGLActivity.this, InformationActivity.class);
                startActivity(intentParam);
                return true;
//            case R.id.action_more_apps:
//                //showHelp();
//                try {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Voladd")));
//                } catch (Exception e) {
////                    Log.d("Information", "Message =" + e);
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:Voladd")));
//                }
//                return true;
//            case R.id.DP_restart:
//                //newGame();
//            	mGLView.queueEvent(new Runnable() {
//                    // This method will be called on the rendering
//                    // thread:
//                    public void run() {
//                    	DPGLRenderer.mPendulum.restart();
//                    }});
//                return true;
//            case R.id.toggleGravity:
//                GLRenderer.mPendulum.toggleGravity();
//                useDynGravity = !useDynGravity;
//                if (useDynGravity) mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_GAME);
//                else mSensorManager.unregisterListener(this);
//                return true;
//            case R.id.HA_parameters:
//                //showHelp();
//                Intent intentParam = new Intent(DPGLActivity.this, DPParametersActivity.class);
//                startActivity(intentParam);
//                return true;
//            case R.id.about:
//                //showHelp();
//            	Intent intent = new Intent(GLActivity.this, AboutActivity.class);
//                startActivity(intent);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        if(Build.VERSION.SDK_INT >= 14 && featureId == Window.FEATURE_ACTION_BAR && menu != null){
            if(menu.getClass().getSimpleName().equals("MenuBuilder")){
                try{
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                }
                catch(NoSuchMethodException e){
                    Log.e(TAG, "onMenuOpened", e);
                }
                catch(Exception e){
                    throw new RuntimeException(e);
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                this).edit();

        editor.putFloat("percent", (float) QOGLRenderer.mOscillator.pct);
        editor.putFloat("step_size", QOGLRenderer.mOscillator.fStepSize);

        editor.commit();
        super.onStop();
    }
}
