package com.sysolve.androidthings.moredisplaysamples;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;
import com.sysolve.androidthings.utils.BoardSpec;
import com.sysolve.androidthings.utils.SoftPwm;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    Gpio mButtonGpio = null;

    private Pwm pwmRed;
    private Pwm pwmGreen;

    private SoftPwm pwmBlue;

    public double A_RED = 1;
    public double A_GREEN = 0.5;
    public double A_BLUE = 1;

    int buttonPressedTimes = 0;

    boolean displayWhite = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            pwmRed = service.openPwm(BoardSpec.getInstance().getPwm(0));
            pwmGreen = service.openPwm(BoardSpec.getInstance().getPwm(1));

            pwmBlue = new SoftPwm(service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_35)));

            pwmRed.setPwmDutyCycle(100*A_RED);       //percent, 0-100
            pwmRed.setPwmFrequencyHz(50);
            pwmRed.setEnabled(true);

            pwmGreen.setPwmDutyCycle(100*A_GREEN);
            pwmGreen.setPwmFrequencyHz(50);
            pwmGreen.setEnabled(true);

            pwmBlue.setPwmDutyCycle(100*A_BLUE);
            pwmBlue.setPwmFrequencyHz(50);
            pwmBlue.setEnabled(true);

            //define a button for counter
            mButtonGpio = service.openGpio(BoardSpec.getGoogleSampleButtonGpioPin());
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButtonGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    buttonPressedTimes++;
                    Log.i(TAG, "GPIO changed, button pressed "+ buttonPressedTimes);
                    toggleDisplayMode();

                    // Return true to continue listening to events
                    return true;
                }
            });

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

    public int runId;
    public void toggleDisplayMode() {
        displayWhite = !displayWhite;
        if (displayWhite) {
            setRGB(100, 100, 100);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int id = ++runId;
                    while (!displayWhite && id==runId) {
                        for (int i=0;i<=100;i+=5) {
                            if (!displayWhite && id==runId)
                                setRGB(100-i, i, 0);     //Red --> Green
                            else
                                break;
                        }
                        for (int i=0;i<=100;i+=5) {
                            if (!displayWhite && id==runId)
                                setRGB(0, 100-i, i);     //Green --> Blue
                            else
                                break;
                        }
                        for (int i=0;i<=100;i+=5) {
                            if (!displayWhite && id==runId)
                                setRGB(i, 0, 100-i);     //Blue --> Red
                            else
                                break;
                        }
                    }
                }
            }).start();
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
