package org.androidtown.samplesurfaceview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout viewLayout = (LinearLayout) findViewById(R.id.viewLayout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        MySurfaceView mySurfaceView = new MySurfaceView(this);
        mySurfaceView.setLayoutParams(params);

        viewLayout.addView(mySurfaceView);
    }
}
