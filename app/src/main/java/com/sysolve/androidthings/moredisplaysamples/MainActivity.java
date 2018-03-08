/*
 * @author Ray, ray@sysolve.com
 * Copyright 2018, Sysolve IoT Open Source
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sysolve.androidthings.moredisplaysamples;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.tm1637.NumericDisplay;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;
import com.sysolve.androidthings.utils.BoardSpec;
import com.sysolve.androidthings.utils.SoftPwm;

import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    Gpio mButtonGpio = null;

    private Pwm pwmRed;
    private Pwm pwmGreen;

    private SoftPwm pwmBlue;

    private OledScreen oledScreen = null;

    private static double PWM_FREQUENCY_HZ = 50;

    private static  double A_RED = 1;
    private static  double A_GREEN = 0.5;
    private static  double A_BLUE = 1;

    int buttonPressedTimes = 0;

    boolean displayWhite = true;

    NumericDisplay mSegmentDisplay;

    DecimalFormat decimalFormat = new DecimalFormat("0000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManager service = PeripheralManager.getInstance();
        try {
            pwmRed = service.openPwm(BoardSpec.getInstance().getPwm(0));
            pwmGreen = service.openPwm(BoardSpec.getInstance().getPwm(1));

            pwmBlue = new SoftPwm(service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_35)));

            pwmRed.setPwmDutyCycle(100*A_RED);       //percent, 0-100
            pwmRed.setPwmFrequencyHz(PWM_FREQUENCY_HZ);
            pwmRed.setEnabled(true);

            pwmGreen.setPwmDutyCycle(100*A_GREEN);
            pwmGreen.setPwmFrequencyHz(PWM_FREQUENCY_HZ);
            pwmGreen.setEnabled(true);

            pwmBlue.setPwmDutyCycle(100*A_BLUE);
            pwmBlue.setPwmFrequencyHz(PWM_FREQUENCY_HZ);
            pwmBlue.setEnabled(true);

            toggleDisplayMode();

            //define a button for counter
            mButtonGpio = service.openGpio(BoardSpec.getGoogleSampleButtonGpioPin());
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButtonGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    buttonPressedTimes++;

                    String d = decimalFormat.format(buttonPressedTimes);
                    try {
                        mSegmentDisplay.setColonEnabled(false);  //是否显示冒号
                        mSegmentDisplay.display(d);    //显示数字
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.i(TAG, "GPIO changed, button pressed "+ d);
                    toggleDisplayMode();

                    //如果OLED屏连接，切换OLED屏的显示内容
                    if (oledScreen!=null) oledScreen.changeMode();

                    // Return true to continue listening to events
                    return true;
                }
            });

            try {
                mSegmentDisplay = new NumericDisplay(
                        BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_36),    //Data
                        BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_38)     //Clock
                );
                mSegmentDisplay.setBrightness(1.0f);     //设置亮度
                mSegmentDisplay.setColonEnabled(true);  //是否显示冒号
                mSegmentDisplay.display("1234");    //显示数字
            } catch (IOException e) {
                Log.e(TAG, "Error configuring display", e);
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        //要显示OLED显示屏，先连接好开发板到OLED屏的IIC连接线，再取消下面行的注释。开启OLED显示屏，会导致蓝色灯显示闪烁
        oledScreen = new OledScreen(this);
    }

    public void setRGB(int r, int g, int b) {
        try {
            pwmRed.setPwmDutyCycle(100-r * A_RED);
            pwmGreen.setPwmDutyCycle(100-g * A_GREEN);
            pwmBlue.setPwmDutyCycle(100-b * A_BLUE);

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

        if (oledScreen!=null) oledScreen.close();

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

        if (mButtonGpio!=null) try {
            mButtonGpio.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (mSegmentDisplay != null) {
            Log.i(TAG, "Closing display");
            try {
                mSegmentDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mSegmentDisplay = null;
            }
        }
    }

}
