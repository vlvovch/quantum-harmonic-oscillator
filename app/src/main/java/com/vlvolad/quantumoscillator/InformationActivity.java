package com.vlvolad.quantumoscillator;

/**
 * Created by Vladimir on 02.05.2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;


public class InformationActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        setTitle(R.string.information);

        TextView tver = (TextView) findViewById(R.id.version_text);
        tver.setText(getText(R.string.version).toString() + " " + BuildConfig.VERSION_NAME);

        TextView tv = (TextView) findViewById(R.id.google_play);
        makeTextViewHyperlink(tv);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (Exception e) {
//                    Log.d("Information", "Message =" + e);

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        TextView tv2 = (TextView) findViewById(R.id.wikipedia_link);
        makeTextViewHyperlink(tv2);
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://en.wikipedia.org/wiki/Hydrogen_atom")));
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/Quantum_harmonic_oscillator")));
            }
        });

        TextView tv3 = (TextView) findViewById(R.id.more_apps);
        makeTextViewHyperlink(tv3);
        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Voladd")));
                } catch (Exception e) {
//                    Log.d("Information", "Message =" + e);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=pub:Voladd")));
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Sets a hyperlink style to the textview.
     */
    public static void makeTextViewHyperlink(TextView tv) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(tv.getText());
        ssb.setSpan(new URLSpan("#"), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb, TextView.BufferType.SPANNABLE);
    }
}
