package com.example.flashlight;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    CameraManager camManager;
    String camId;
    boolean torchOn = false;
    Handler handler = new Handler();
    Runnable blinkRunnable, sosRunnable;
    TextView status;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);

        camManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            camId = camManager.getCameraIdList()[0];
        } catch (CameraAccessException e) { }

        Button toggle = findViewById(R.id.btnToggle);
        Button blink = findViewById(R.id.btnBlink);
        Button timer = findViewById(R.id.btnTimer);
        Button sos = findViewById(R.id.btnSOS);
        EditText blinkRate = findViewById(R.id.blinkRate);
        EditText timerSec = findViewById(R.id.timerSec);

        toggle.setOnClickListener(v -> {
            stopAll();
            setTorch(!torchOn);
        });

        blink.setOnClickListener(v -> {
            stopAll();
            int rate = Integer.parseInt(blinkRate.getText().toString());
            blinkRunnable = new Runnable() {
                @Override public void run() {
                    setTorch(!torchOn);
                    handler.postDelayed(this, rate);
                }
            };
            handler.post(blinkRunnable);
        });

        timer.setOnClickListener(v -> {
            stopAll();
            int sec = Integer.parseInt(timerSec.getText().toString());
            setTorch(true);
            handler.postDelayed(() -> setTorch(false), sec * 1000L);
        });

        sos.setOnClickListener(v -> {
            stopAll();
            runSOS();
        });
    }

    void setTorch(boolean on) {
        try {
            camManager.setTorchMode(camId, on);
            torchOn = on;
            status.setText(on ? "ON" : "OFF");
        } catch (CameraAccessException e) { }
    }

    void stopAll() {
        if (blinkRunnable != null) handler.removeCallbacks(blinkRunnable);
        if (sosRunnable != null) handler.removeCallbacks(sosRunnable);
        setTorch(false);
    }

    void runSOS() {
        int[] pattern = {200,200,200,200,200,600,600,200,600,200,600,600,200,200,200,200,200,1000};
        int[] idx = {0};
        sosRunnable = new Runnable() {
            @Override public void run() {
                if (idx[0] >= pattern.length) { idx[0] = 0; }
                setTorch(idx[0] % 2 == 0);
                handler.postDelayed(this, pattern[idx[0]++]);
            }
        };
        handler.post(sosRunnable);
    }
}
