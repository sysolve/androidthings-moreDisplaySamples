package com.sysolve.androidthings.utils;

import android.util.Log;

import com.google.android.things.pio.Gpio;

/**
 * Created by 13311 on 2018-01-27.
 */

public class SoftPwm implements Runnable {

    public Gpio gpio;

    public SoftPwm(Gpio gpio) throws Exception {
        this.gpio = gpio;
        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
    }

    int runId;

    public void run() {
        try {
            int id = ++runId;
            while (gpio!=null && enabled && id==runId && (onTime>0 || offTime>0)) {
                if (onTime>0) {
                    gpio.setValue(true);
                    Thread.sleep(onTime);
                }
                if (offTime>0) {
                    gpio.setValue(false);
                    Thread.sleep(offTime);
                }
            }
        } catch (Exception e) {

        }
    }

    public int onTime = 500;
    public int offTime = 500;

    public void calcOnOffTime() {
        double totalTimeDouble = 1000.0/pwmFrequencyHz;
        double onTimeDouble = pwmDutyCycle*totalTimeDouble*0.01;
        int tt = (int)Math.round(totalTimeDouble);
        onTime  = (int)Math.round(onTimeDouble);
        offTime = tt - onTime;

        Log.i("SoftPwm", String.format("Soft PWM onTime=%dms, offTime=%dms", onTime, offTime));
    }

    public double getPwmDutyCycle() {
        return pwmDutyCycle;
    }

    public void setPwmDutyCycle(double pwmDutyCycle) {
        this.pwmDutyCycle = pwmDutyCycle;

        calcOnOffTime();
    }

    public double getPwmFrequencyHz() {
        return pwmFrequencyHz;
    }

    public void setPwmFrequencyHz(double pwmFrequencyHz) {
        this.pwmFrequencyHz = pwmFrequencyHz;

        calcOnOffTime();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            new Thread(this).start();
        }
    }

    public double pwmDutyCycle;
    public double pwmFrequencyHz;
    public boolean enabled;

    public void close() throws Exception {
        enabled = false;
        gpio.close();
        gpio = null;
    }
}
