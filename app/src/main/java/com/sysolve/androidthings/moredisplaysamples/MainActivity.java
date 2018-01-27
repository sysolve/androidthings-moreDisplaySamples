package com.sysolve.androidthings.moredisplaysamples;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;
import com.sysolve.androidthings.utils.BoardSpec;
import com.sysolve.androidthings.utils.SoftPwm;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Pwm pwmRed;
    private Pwm pwmGreen;

    private SoftPwm pwmBlue;

    public double A_RED = 1;
    public double A_GREEN = 0.5;
    public double A_BLUE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            pwmRed = service.openPwm(BoardSpec.getInstance().getPwm(0));
            pwmGreen = service.openPwm(BoardSpec.getInstance().getPwm(1));

            pwmBlue = new SoftPwm(service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_35)));

            pwmRed.setPwmDutyCycle(100*A_RED);       //percent, 0-100
            pwmRed.setPwmFrequencyHz(100);
            pwmRed.setEnabled(true);

            pwmGreen.setPwmDutyCycle(100*A_GREEN);
            pwmGreen.setPwmFrequencyHz(100);
            pwmGreen.setEnabled(true);

            pwmBlue.setPwmDutyCycle(100*A_BLUE);
            pwmBlue.setPwmFrequencyHz(100);
            pwmBlue.setEnabled(true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (true) {
                        for (int i=0;i<=100;i+=5) setRGB(100-i, i, 0);     //Red --> Green
                        for (int i=0;i<=100;i+=5) setRGB(0, 100-i, i);     //Green --> Blue
                        for (int i=0;i<=100;i+=5) setRGB(i, 0, 100-i);     //Blue --> Red
                    }
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void setRGB(int r, int g, int b) {
        try {
            pwmRed.setPwmDutyCycle(r * A_RED);
            pwmGreen.setPwmDutyCycle(g * A_GREEN);
            pwmBlue.setPwmDutyCycle(b * A_BLUE);

            Thread.sleep(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            pwmRed.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        try {
            pwmGreen.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        try {
            pwmBlue.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

}
