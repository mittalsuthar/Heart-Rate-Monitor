package com.example.testing_for_latest;


import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button flashButton;
    private TextView measuing_text;
    private boolean isFlashOn = false;
    ImageProcessing imageProcessing = new ImageProcessing();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to the views
        surfaceView = findViewById(R.id.camera_preview);
        flashButton = findViewById(R.id.flash_button);
        measuing_text = findViewById(R.id.measuring_text);

        // Configure the SurfaceHolder
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setKeepScreenOn(true);

        // Set click listener on flash button
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFlash();
            }
        });

    }

    private void startTimerAndTurnFlashOn() {
        final int delayMillis = 1000; // 1 second delay
        final int totalTimeMillis = 5000; // Total time in milliseconds
        final int totalTimeSeconds = totalTimeMillis / 1000; // Total time in seconds

        Handler handler = new Handler();
        handler.post(new Runnable() {
            int remainingTimeSeconds = totalTimeSeconds;

            @Override
            public void run() {
                // Update the measuring_text with the remaining time
                flashButton.setVisibility(View.INVISIBLE);
                measuing_text.setText("Heart rate measurement will start in " + remainingTimeSeconds + " seconds");
                measuing_text.setTextSize(14);
                if (remainingTimeSeconds > 0) {
                    remainingTimeSeconds--;
                    handler.postDelayed(this, delayMillis);
                }else{
                    turnFlashOn();
                }
            }
        });
    }



    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // Do nothing here
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        Camera.Parameters parameters = camera.getParameters();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        // Release the camera when the SurfaceView is destroyed
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void toggleFlash() {
        // Toggle the flash on and off
        if (isFlashOn) {
            turnFlashOff();
        } else {
            measuing_text.setVisibility(View.VISIBLE);
            startTimerAndTurnFlashOn();
//            turnFlashOn();
        }
    }



    private void turnFlashOn() {
        // Turn the flash on
        try {
            measuing_text.setTextSize(18);
            measuing_text.setText(getString(R.string.measuring));
            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            surfaceView.setVisibility(View.VISIBLE);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            isFlashOn = true;

            // Turn off the flash and release the camera after 15 seconds
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    turnFlashOff();
                    measuing_text.setText("Heart rate : "+imageProcessing.resultantHeartBeat()+" BPM");
                    releaseCamera();
                }
            }, 15000); // Delay for 15 seconds (15000 milliseconds)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void turnFlashOff() {
        // Turn the flash off
        if (camera != null && isFlashOn) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            isFlashOn = false;
            flashButton.setText(R.string.flash_on);
            measuing_text.setVisibility(View.VISIBLE);
            surfaceView.setVisibility(View.INVISIBLE);
            flashButton.setVisibility(View.VISIBLE);
        }
    }

    private void releaseCamera() {
        // Release the camera
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

}