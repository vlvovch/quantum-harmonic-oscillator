package com.vlvolad.quantumoscillator;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Vladimir on 29.04.2015.
 */
public class HAGLActivity extends Activity {

    private HAGLSurfaceView mGLView;
    //private DSPGLRenderer mRenderer;
//    private SensorManager mSensorManager;
//    private Sensor mGravity;
//    private boolean useDynGravity;
//    private boolean useDamping;
//    private boolean isRunning;
    private Display display;

    private String mapl;

    static int frequency = 50;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (HAGLRenderer.mAtom.fin==0) {
                timerHandler.postDelayed(this, frequency);
                //findViewById(R.id.progress).setVisibility(View.VISIBLE);
                if (HAGLRenderer.mAtom.totalprogress>0) ((ProgressBar)findViewById(R.id.progress)).setProgress((HAGLRenderer.mAtom.progress * 100) / HAGLRenderer.mAtom.totalprogress);
                else ((ProgressBar)findViewById(R.id.progress)).setProgress(0);
            }
            else {
                mGLView.queueEvent(new Runnable() {
                    // This method will be called on the rendering
                    // thread:
                    public void run() {
                        //HAGLRenderer.mAtom.reallocateMemoryFinal();
                    }});
                mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                timerHandler.removeCallbacks(timerRunnable);
                ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_replay);
                findViewById(R.id.button_regenerate).setEnabled(true);
                findViewById(R.id.button_random).setEnabled(true);
                isRunning = false;
                findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                if (HAGLRenderer.mAtom.overflow)
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
        setContentView(R.layout.hydrogenatom_gl);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        //mGLView = new DPGLSurfaceView(this);
        //setContentView(mGLView);

//        setTitle(R.string.activity_name);

        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        //Log.v("onCreate", "maxMemory:" + Long.toString(maxMemory));

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final int memoryClass = am.getMemoryClass();
        //Log.v("onCreate", "memoryClass:" + Integer.toString(memoryClass));

        mapl = "spdfghi";

        HAGLRenderer.mAtom.n = PreferenceManager.getDefaultSharedPreferences(
                HAGLActivity.this).getInt("n", 1);
        HAGLRenderer.mAtom.l = PreferenceManager.getDefaultSharedPreferences(
                HAGLActivity.this).getInt("l", 0);
        HAGLRenderer.mAtom.m = PreferenceManager.getDefaultSharedPreferences(
                HAGLActivity.this).getInt("m", 0);
        HAGLRenderer.mAtom.hAtom.setsign(false);
        HAGLRenderer.mAtom.pct = PreferenceManager.getDefaultSharedPreferences(
                HAGLActivity.this).getFloat("percent", 70.f);
        HAGLRenderer.mAtom.fStepSize = PreferenceManager.getDefaultSharedPreferences(
                HAGLActivity.this).getFloat("step_size", 6.f);

        HAGLRenderer.mAtom.fin = 0;
        updateOrbitalName();

        mGLView = (HAGLSurfaceView)findViewById(R.id.gl_surface_view);

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mGLView.mDensity = displayMetrics.density;

        mGLView.queueEvent(new Runnable() {
            // This method will be called on the rendering
            // thread:
            public void run() {
                HAGLRenderer.mAtom.memoryclass = memoryClass;
                HAGLRenderer.mAtom.totalprogress = 0;
                HAGLRenderer.mAtom.reallocateMemory();
                HAGLRenderer.mAtom.setRealKsi(PreferenceManager.getDefaultSharedPreferences(
                        HAGLActivity.this).getBoolean("wave_function_real", true));
                HAGLRenderer.mAtom.regenerate();
            }});

//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

//        tvn = (TextView)findViewById(R.id.tvnn);
//        tvl = (TextView)findViewById(R.id.tvln);
//        tvm = (TextView)findViewById(R.id.tvmn);

        //mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        isRunning = false;

        findViewById(R.id.button_random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isRunning) {
                    HAGLRenderer.mAtom.pickRandomOrbital(5);
                    HAGLRenderer.mAtom.fin = 0;
                    HAGLRenderer.mAtom.fStepSize = 2.f + seekBarStepSize.getProgress() / 10.f;
                    HAGLRenderer.mAtom.pct = seekBarPercent.getProgress() + 1;
                    HAGLRenderer.mAtom.setRealKsi(PreferenceManager.getDefaultSharedPreferences(
                            HAGLActivity.this).getBoolean("wave_function_real", true));
                    updateOrbitalName();
                    findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop);
                    findViewById(R.id.button_random).setEnabled(false);
                    //findViewById(R.id.button_regenerate).setEnabled(false);
                    isRunning = true;
                    timerHandler.postDelayed(timerRunnable, 0);
                    mGLView.queueEvent(new Runnable() {
                        // This method will be called on the rendering
                        // thread:
                        public void run() {
                            //HAGLRenderer.mAtom.reallocateMemory();
                            HAGLRenderer.mAtom.totalprogress = 0;
                            HAGLRenderer.mAtom.regenerate();
                            mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                        }
                    });
                }
            }
        });

        findViewById(R.id.button_orbital).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HAGLActivity.this, OrbitalDialog.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_regenerate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //HAGLRenderer.mAtom.pickRandomOrbital(5);
                if (!isRunning) {
//                    HAGLRenderer.mAtom.n = Integer.parseInt(tvn.getText().toString());
//                    HAGLRenderer.mAtom.l = Integer.parseInt(tvl.getText().toString());
//                    HAGLRenderer.mAtom.m = Integer.parseInt(tvm.getText().toString());
                    HAGLRenderer.mAtom.n = PreferenceManager.getDefaultSharedPreferences(
                            HAGLActivity.this).getInt("n", 1);
                    HAGLRenderer.mAtom.l = PreferenceManager.getDefaultSharedPreferences(
                            HAGLActivity.this).getInt("l", 0);
                    HAGLRenderer.mAtom.m = PreferenceManager.getDefaultSharedPreferences(
                            HAGLActivity.this).getInt("m", 0);
                    HAGLRenderer.mAtom.hAtom.setsign(false);
                    HAGLRenderer.mAtom.setRealKsi(PreferenceManager.getDefaultSharedPreferences(
                            HAGLActivity.this).getBoolean("wave_function_real", true));
                    HAGLRenderer.mAtom.fin = 0;
                    HAGLRenderer.mAtom.fStepSize = 2.f + seekBarStepSize.getProgress() / 10.f;
                    HAGLRenderer.mAtom.pct = seekBarPercent.getProgress() + 1;
                    updateOrbitalName();
                    findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop);
                    //findViewById(R.id.button_regenerate).setEnabled(false);
                    isRunning = true;
                    timerHandler.postDelayed(timerRunnable, 0);
                    mGLView.queueEvent(new Runnable() {
                        // This method will be called on the rendering
                        // thread:
                        public void run() {
                            //HAGLRenderer.mAtom.reallocateMemory();
                            HAGLRenderer.mAtom.totalprogress = 0;
                            HAGLRenderer.mAtom.regenerate();
                            mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                        }
                    });
                }
                else {
                    ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_replay);
                    findViewById(R.id.button_regenerate).setEnabled(false);
                    isRunning = false;
                    mGLView.queueEvent(new Runnable() {
                        // This method will be called on the rendering
                        // thread:
                        public void run() {
                            //HAGLRenderer.mAtom.reallocateMemory();
                            HAGLRenderer.mAtom.InterruptThread();
                        }
                    });
                    timerHandler.postDelayed(timerRunnable, 0);
                }
            }
        });

        findViewById(R.id.button_regenerate).setEnabled(false);

        seekBarPercent = (SeekBar)findViewById(R.id.seekBarPercent);
        textViewPercent = (TextView)findViewById(R.id.textViewPercent);

        seekBarPercent.setProgress((int)(HAGLRenderer.mAtom.pct+1e-5)-1);
        textViewPercent.setText((int)HAGLRenderer.mAtom.pct + " %");

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

        seekBarStepSize.setProgress((int)((HAGLRenderer.mAtom.fStepSize+1e-5-2.f)*10));
        textViewStepSize.setText(String.format("%.1f", HAGLRenderer.mAtom.fStepSize));

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

//        findViewById(R.id.bSubn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int n = Integer.parseInt(tvn.getText().toString());
//                int l = Integer.parseInt(tvl.getText().toString());
//                if (n-1>l) {
//                    n--;
//                    tvn.setText("" + n);
//                }
//            }
//        });
//
//        findViewById(R.id.bAddn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int n = Integer.parseInt(tvn.getText().toString());
//                int l = Integer.parseInt(tvl.getText().toString());
//                if (n+1<=25) {
//                    n++;
//                    tvn.setText("" + n);
//                }
//            }
//        });
//
//        findViewById(R.id.bSubl).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int n = Integer.parseInt(tvn.getText().toString());
//                int l = Integer.parseInt(tvl.getText().toString());
//                int m = Integer.parseInt(tvm.getText().toString());
//                if (l-1>=Math.abs(m)) {
//                    l--;
//                    tvl.setText("" + l);
//                }
//            }
//        });
//
//        findViewById(R.id.bAddl).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int n = Integer.parseInt(tvn.getText().toString());
//                int l = Integer.parseInt(tvl.getText().toString());
//                int m = Integer.parseInt(tvm.getText().toString());
//                if (l+1<n) {
//                    l++;
//                    tvl.setText("" + l);
//                }
//            }
//        });
//
//        findViewById(R.id.bSubm).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int n = Integer.parseInt(tvn.getText().toString());
//                int l = Integer.parseInt(tvl.getText().toString());
//                int m = Integer.parseInt(tvm.getText().toString());
//                if (Math.abs(m-1)<=l) {
//                    m--;
//                    tvm.setText("" + m);
//                }
//            }
//        });
//
//        findViewById(R.id.bAddm).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int n = Integer.parseInt(tvn.getText().toString());
//                int l = Integer.parseInt(tvl.getText().toString());
//                int m = Integer.parseInt(tvm.getText().toString());
//                if (Math.abs(m+1)<=l) {
//                    m++;
//                    tvm.setText("" + m);
//                }
//            }
//        });

        timerHandler.postDelayed(timerRunnable, 0);

//        FlurryAgent.onStartSession(this);
//        FlurryAgent.logEvent("DoublePendulum",true);
    }

//    public void updateGravitySensorStatus() {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				if (useDynGravity) {
//					((Button) findViewById(R.id.button_sensor_gravity)).setText(R.string.button_sensor_gravity);
//				} else {
//					((Button) findViewById(R.id.button_sensor_gravity)).setText(R.string.button_sensor_gravity_off);
//				}
//			}
//		});
//	}
//
//	public void updateDampingStatus() {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				if (useDamping) {
//					((Button) findViewById(R.id.button_damping)).setText(R.string.button_damping);
//				} else {
//					((Button) findViewById(R.id.button_damping)).setText(R.string.button_damping_off);
//				}
//			}
//		});
//	}

    private void regenerate() {
        //HAGLRenderer.mAtom.pickRandomOrbital(5);
        if (!isRunning) {
//                    HAGLRenderer.mAtom.n = Integer.parseInt(tvn.getText().toString());
//                    HAGLRenderer.mAtom.l = Integer.parseInt(tvl.getText().toString());
//                    HAGLRenderer.mAtom.m = Integer.parseInt(tvm.getText().toString());
            HAGLRenderer.mAtom.n = PreferenceManager.getDefaultSharedPreferences(
                    HAGLActivity.this).getInt("n", 1);
            HAGLRenderer.mAtom.l = PreferenceManager.getDefaultSharedPreferences(
                    HAGLActivity.this).getInt("l", 0);
            HAGLRenderer.mAtom.m = PreferenceManager.getDefaultSharedPreferences(
                    HAGLActivity.this).getInt("m", 0);
            HAGLRenderer.mAtom.hAtom.setsign(false);
            HAGLRenderer.mAtom.setRealKsi(PreferenceManager.getDefaultSharedPreferences(
                    HAGLActivity.this).getBoolean("wave_function_real", true));
            HAGLRenderer.mAtom.fin = 0;
            HAGLRenderer.mAtom.fStepSize = 2.f + seekBarStepSize.getProgress() / 10.f;
            HAGLRenderer.mAtom.pct = seekBarPercent.getProgress() + 1;
            updateOrbitalName();
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop);
            //findViewById(R.id.button_regenerate).setEnabled(false);
            isRunning = true;
            timerHandler.postDelayed(timerRunnable, 0);
            mGLView.queueEvent(new Runnable() {
                // This method will be called on the rendering
                // thread:
                public void run() {
                    //HAGLRenderer.mAtom.reallocateMemory();
                    HAGLRenderer.mAtom.totalprogress = 0;
                    HAGLRenderer.mAtom.regenerate();
                    mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                }
            });
        }
        else {
            ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_replay);
            findViewById(R.id.button_regenerate).setEnabled(false);
            isRunning = false;
            mGLView.queueEvent(new Runnable() {
                // This method will be called on the rendering
                // thread:
                public void run() {
                    //HAGLRenderer.mAtom.reallocateMemory();
                    HAGLRenderer.mAtom.InterruptThread();
                }
            });
            timerHandler.postDelayed(timerRunnable, 0);
        }
    }

    private void updateOrbitalName() {
        int tmpm = HAGLRenderer.mAtom.m;
        if (HAGLRenderer.mAtom.hAtom.sign) tmpm = -tmpm;
        if (HAGLRenderer.mAtom.l<=6) ((TextView)findViewById(R.id.orbital_name)).setText(
                HAGLRenderer.mAtom.n + mapl.substring(HAGLRenderer.mAtom.l,HAGLRenderer.mAtom.l+1) + ", m=" + tmpm
                );
        else ((TextView)findViewById(R.id.orbital_name)).setText(
                "n=" + HAGLRenderer.mAtom.n + ", l=" + HAGLRenderer.mAtom.l + ", m=" + tmpm
                );

        ((Button)findViewById(R.id.button_orbital)).setText(
                " n=" + HAGLRenderer.mAtom.n + ", l=" + HAGLRenderer.mAtom.l + ", m=" + tmpm + " "
        );

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                this).edit();

        editor.putInt("n", HAGLRenderer.mAtom.n);
        editor.putInt("l", HAGLRenderer.mAtom.l);
        editor.putInt("m", tmpm);

        editor.commit();

//        tvn.setText("" + HAGLRenderer.mAtom.n);
//        tvl.setText("" + HAGLRenderer.mAtom.l);
//        tvm.setText("" + tmpm);


    }


    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        timerHandler.removeCallbacks(timerRunnable);
        mGLView.queueEvent(new Runnable() {
            // This method will be called on the rendering
            // thread:
            public void run() {
                HAGLRenderer.mAtom.InterruptThread();
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
                " n=" + PreferenceManager.getDefaultSharedPreferences(
                        HAGLActivity.this).getInt("n", 1) + ", l=" + PreferenceManager.getDefaultSharedPreferences(
                        HAGLActivity.this).getInt("l", 0) + ", m=" + PreferenceManager.getDefaultSharedPreferences(
                        HAGLActivity.this).getInt("m", 0) + " "
        );
        if (HAGLRenderer.mAtom.fin==0) {
            mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//            findViewById(R.id.button_regenerate).setEnabled(false);
            findViewById(R.id.button_regenerate).setEnabled(true);
            ((ImageButton)findViewById(R.id.button_regenerate)).setImageResource(R.drawable.ic_action_stop);
            isRunning = true;
        }
        if (HAGLRenderer.mAtom.toCont) {
            HAGLRenderer.mAtom.toCont = false;
            regenerate();
        }
        timerHandler.postDelayed(timerRunnable, 0);
        mGLView.onResume();
        mGLView.requestRender();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_information:
                //showHelp();
                Intent intentParam = new Intent(HAGLActivity.this, InformationActivity.class);
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
    protected void onStop() {
//        FlurryAgent.endTimedEvent("DoublePendulum");
//        FlurryAgent.onEndSession(this);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                this).edit();

        editor.putFloat("percent", (float)HAGLRenderer.mAtom.pct);
        editor.putFloat("step_size", HAGLRenderer.mAtom.fStepSize);

        editor.commit();
        super.onStop();
    }
}
