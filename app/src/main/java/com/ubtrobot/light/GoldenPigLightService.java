package com.ubtrobot.light;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.light.sal.AbstractLightService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by taoqian on 2018/8/22.
 */
public class GoldenPigLightService extends AbstractLightService {
    private static final String TAG = GoldenPigLightService.class.getSimpleName();
    private static final int DEFAULT_COLOR = 1;
    private static final String CONSTANTLY_BRIGHT_ID = "1";
    private static final String BREATHE_ID = "2";
    private static final String FLASHING_ID = "3";
    private static final int ORANGE_COLOR = 2;
    private static final int WHITE_COLOR = 3;
    private static final String LED_MAIN = "10001";
    private static final String LED_MUTE = "10002";
    private static final String LED_WIFI_AP = "10003";

    private List<String> mOpenLight;
    private Context mContext;
    private Map<String, String> mMapOpenLight;//key为灯的id，value为灯效的id
    private Map<String, Integer> mMapOpenLightColor;//key为灯的id，value为灯的颜色值

    private Timer mTimer = new Timer();
    private final Intent mIntent;
    private boolean lastFlag;

    private class MyTimerTask extends TimerTask{
        private int count;
        private String lightEffectId;
        private int color;
        @Override
        public void run() {
            Log.i(TAG, "count="+count);
            count--;
            if (count == 0) {
                mTimer.cancel();
                mTimer = null;
                controlLight(lightEffectId, color, false);
                mOpenLight.remove(LED_MAIN);
                mMapOpenLight.remove(LED_MAIN);
                mMapOpenLightColor.remove(LED_MAIN);
            } else if (count < 0) {
                mTimer.cancel();
                mTimer = null;
            }
        }
    }

    public GoldenPigLightService(Context context) {
        mContext = context;
        mOpenLight = new ArrayList<>();
        mMapOpenLight = new HashMap<>();
        mMapOpenLightColor = new HashMap<>();
        mIntent = new Intent(context, SetValueIntentService.class);
        lastFlag = true;
        readLEDMainLight();
    }

    private void readLEDMainLight() {
        String value = read().substring(2);
        if (!TextUtils.isEmpty(value)) {
            Log.i(TAG, "value:" + value);
            if (value.contains(CONSTANTLY_BRIGHT_ID)) {
                mMapOpenLight.put(LED_MAIN, CONSTANTLY_BRIGHT_ID);
                mOpenLight.add(LED_MAIN);
                mMapOpenLightColor.put(LED_MAIN, WHITE_COLOR);
            } else if (value.contains(FLASHING_ID)) {
                mMapOpenLight.put(LED_MAIN, FLASHING_ID);
                mOpenLight.add(LED_MAIN);
                mMapOpenLightColor.put(LED_MAIN, WHITE_COLOR);
            } else if (value.contains(BREATHE_ID)) {
                mMapOpenLight.put(LED_MAIN, BREATHE_ID);
                mOpenLight.add(LED_MAIN);
                mMapOpenLightColor.put(LED_MAIN, WHITE_COLOR);
            }
        }
    }

    @Override
    protected AsyncTask<List<LightDevice>, AccessServiceException> createGetLightListTask() {
        Log.i(TAG, "createGetLightListTask:");
        return new AsyncTask<List<LightDevice>, AccessServiceException>() {
            @Override
            protected void onStart() {
                resolve(getLightDevices());
            }
        };
    }

    @Override
    protected void doStartTurningOff(String id) {
        Log.i(TAG, "doStartTurningOff:");
        if (LED_MUTE.equals(id) || LED_WIFI_AP.equals(id)) {
            controlLight(id, false);
            return;
        }
        LightDevice device = getLightDevice(id);
        if (device != null) {
            if (mOpenLight.contains(id)) {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                String lightEffectId = mMapOpenLight.get(id) != null
                        ? mMapOpenLight.get(id) : BREATHE_ID;
                controlLight(lightEffectId, 0, false);
                mOpenLight.remove(id);
                mMapOpenLight.remove(id);
                mMapOpenLightColor.remove(id);
            }
        } else {
            Log.i(TAG, "设备id=" + id + "对应的LED灯不存在");
        }
    }

    @Override
    protected void doStartTurningOn(String id, int color) {
        Log.i(TAG, "doStartTurningOn:");
        if (LED_MUTE.equals(id) || LED_WIFI_AP.equals(id)) {
            controlLight(id, true);
            return;
        }
        lastFlag = true;
        LightDevice device = getLightDevice(id);
        if (device != null) {
            if (mOpenLight.contains(id)) {
                String lightEffectId = mMapOpenLight.get(id);
                int lightColor = mMapOpenLightColor.get(id);
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                if (lightEffectId.equals(CONSTANTLY_BRIGHT_ID) && color == lightColor) {
                    return;
                }
                controlLight(lightEffectId, lightColor, false);
                controlLight(CONSTANTLY_BRIGHT_ID, color, true);
            }else {
                mOpenLight.add(id);
                controlLight(CONSTANTLY_BRIGHT_ID, color, true);
            }
            mMapOpenLightColor.put(id, color);
            mMapOpenLight.put(id, CONSTANTLY_BRIGHT_ID);
        } else {
            Log.i(TAG, "设备id=" + id + "对应的LED灯不存在");
        }
    }

    @Override
    protected boolean doGetIsOn(String id) {
        Log.i(TAG, "doGetIsOn:");
        return mOpenLight.contains(id);
    }

    @Override
    protected void doStartChangingColor(String id, int color) {
        Log.i(TAG, "doStartChangingColor:");
        if (LED_MUTE.equals(id) || LED_WIFI_AP.equals(id)) {
            return;
        }
        lastFlag = true;
        LightDevice device = getLightDevice(id);
        if (device != null) {
            if (mOpenLight.contains(id)) {
                String lightEffectId = mMapOpenLight.get(id);
                int lightColor = mMapOpenLightColor.get(id);
                if (color == lightColor) {
                    return;
                }
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                controlLight(lightEffectId, lightColor, false);
                controlLight(lightEffectId, color, true);
                mMapOpenLightColor.put(id, color);
            }
        } else {
            Log.i(TAG, "设备id=" + id + "对应的LED灯不存在");
        }
    }

    @Override
    protected int doGetColor(String id) {
        Log.i(TAG, "doGetColor:");
        if (getLightDevice(id) != null && mOpenLight.contains(id)) {
            return mMapOpenLightColor.get(id);
        }else {
            return Color.BLACK;//灯没有打开，或者没有对应的灯，颜色设置为黑色
        }
    }

    @Override
    protected AsyncTask<List<LightingEffect>, AccessServiceException> createGetEffectList() {
        Log.i(TAG, "createGetEffectList:");
        return new AsyncTask<List<LightingEffect>, AccessServiceException>() {
            @Override
            protected void onStart() {
                resolve(getLightEffects());
            }
        };
    }

    @Override
    protected void startDisplayingEffect(List<String> list, String s, DisplayOption displayOption) {
        Log.i(TAG, "startDisplayingEffect:");
        lastFlag = true;
        int loops = displayOption.getLoops();
        int color = displayOption.getColor();
        if (color == -1) {
            color = DEFAULT_COLOR;
        }
        if (list != null) {
            for (String id : list) {
                if (LED_MUTE.equals(id) || LED_WIFI_AP.equals(id)) {
                    continue;
                }
                LightingEffect lightingEffect = getLightEffect(s);
                if (lightingEffect != null) {
                    LightDevice device = getLightDevice(id);
                    if (device != null) {
                        if (mOpenLight.contains(id)) {
                            String lightEffectId = mMapOpenLight.get(id);
                            int lightColor = mMapOpenLightColor.get(id);
                            if (lightEffectId.equals(s) && color == lightColor) {
                                return;
                            }
                            controlLight(lightEffectId, lightColor, false);
                            controlLight(s, lightColor, true);
                        }else {
                            mOpenLight.add(id);
                            controlLight(s, color, true);
                        }
                        mMapOpenLightColor.put(id, color);
                        mMapOpenLight.put(id, s);
                        if (s.equals(FLASHING_ID) || s.equals(BREATHE_ID)) {
                            MyTimerTask mTimerTask = new MyTimerTask();
                            mTimerTask.count = loops;
                            if (s.equals(BREATHE_ID)) {
                                mTimerTask.count = loops*8;
                            }
                            mTimerTask.color = color;
                            mTimerTask.lightEffectId = s;
                            if (mTimer != null) {
                                mTimer.cancel();
                            }
                            mTimer = new Timer();
                            mTimer.schedule(mTimerTask, 400, 400);
                        }
                    } else {
                        Log.i(TAG, "设备id=" + id + "对应的LED灯不存在");
                    }
                } else {
                    Log.i(TAG, "没有与之相对应的灯光效果,请换一个灯光id");
                }
            }
        }
    }

    /**
     * 得到本机上所支持的所有LED灯光效果
     * @return 灯光效果集合
     */
    private List<LightingEffect> getLightEffects() {
        List<LightingEffect> lightingEffects = new ArrayList<>();
        LightingEffect lightingEffect1 = new LightingEffect.Builder(CONSTANTLY_BRIGHT_ID)
                .setName("常亮灯").build();
        lightingEffects.add(lightingEffect1);
        LightingEffect lightingEffect2 = new LightingEffect.Builder(BREATHE_ID)
                .setName("呼吸灯").build();
        lightingEffects.add(lightingEffect2);
        LightingEffect lightingEffect3 = new LightingEffect.Builder(FLASHING_ID)
                .setName("闪烁灯").build();
        lightingEffects.add(lightingEffect3);
        return lightingEffects;
    }

    /**
     * 根据灯效id得到自定义的灯光效果
     * @param id 灯效id
     * @return 灯光效果
     */
    private LightingEffect getLightEffect(String id) {
        for (LightingEffect effect : getLightEffects()) {
            if (id != null && id.equals(effect.getId())) {
                return effect;
            }
        }
        return null;
    }

    /**
     * 得到本机上所有的LED灯
     * id值转为int型后，作为通知的id
     * @return 灯的集合
     */
    private List<LightDevice> getLightDevices() {
        LightDevice lightDevice1 = new LightDevice.Builder(LED_MAIN, "led_main").build();
        LightDevice lightDevice2 = new LightDevice.Builder(LED_MUTE, "led_mute").build();
        LightDevice lightDevice3 = new LightDevice.Builder(LED_WIFI_AP, "led_wifi_ap").build();
        List<LightDevice> lightDevices = new ArrayList<>();
        lightDevices.add(lightDevice1);
        lightDevices.add(lightDevice2);
        lightDevices.add(lightDevice3);
        return lightDevices;
    }

    /**
     * 根据灯的id得到LED灯
     * @param id LED灯id
     * @return 单个灯
     */
    private LightDevice getLightDevice(String id) {
        for (LightDevice device : getLightDevices()) {
            if(id.equals(device.getId()))
                return device;
        }
        return null;
    }

    private static final String SPACE = " ";
    private static final String OPEN = "1";
    private static final String SYNC = "sync";
    private static final String BLINK = "blink";
    private static final String CLOSE = "0";
    private long lastTime = 0;

    private void controlLight(String lightEffectId, int color, boolean flag) {
        String value;
        if (color == ORANGE_COLOR) {
            value = "255 50 0";
        } else if (color == WHITE_COLOR) {
            value = "255 140 140";
        } else {
            value = "255 0 0";
        }
        value="50 255 255";
        showLight(lightEffectId, value, flag);
    }

    private void showLight(String lightEffectId, String value, boolean flag) {
        String lightEffectValue;
        if (FLASHING_ID.equals(lightEffectId)) {
            lightEffectValue = BLINK + SPACE + 200 + SPACE +
                    (flag ? value : (0 + SPACE + 0 + SPACE + 0));
        } else {
            lightEffectValue = SYNC + SPACE + lightEffectId + SPACE +
                    (flag ? value : (0 + SPACE + 0 + SPACE + 0));
        }
        write(lightEffectValue, flag);
    }

    private void controlLight(String lightId, boolean flag) {
        LightDevice lightDevice = getLightDevice(lightId);
        if(lightDevice==null)
            return;
        String value;
        if (flag) {
            value = lightDevice.getName() + SPACE + OPEN;
        }else {
            value = lightDevice.getName() + SPACE + CLOSE;
        }
        write(value, flag);
    }



    private void write(String value, boolean flag) {
        //使用IntentService会牺牲效率
       /* mIntent.putExtra("value", value);
        mIntent.putExtra("flag", flag);
        mContext.startService(mIntent);*/
        long currentTime = System.currentTimeMillis();
        if (lastFlag == false && flag == true) {
            try {
                //关闭一个灯后，需要等一会儿再打开，否则有可能打不开
                //试过150，有可能打不开，设置为200，就没有出现打不开的情况，为了保险，设置为250
                if (currentTime - lastTime < 300) {
                    Thread.sleep(250);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "sleep:");
        }
        lastFlag = flag;
        writeValue(value);
        lastTime = System.currentTimeMillis();
    }

    private void writeValue(String value) {
        String path="dev/mid";
        File file = new File(path);
        Log.i(TAG, "value:" + value);
        if (file.exists()) {
            Writer fstream = null;
            try {
                fstream = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                fstream.write(value + "\n");
                Log.i(TAG, "value11:" + value);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fstream != null) {
                    try {
                        fstream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String read() {
        String value = "";
        String path="dev/mid";
        File file = new File(path);
        if (file.exists()) {
            BufferedReader bufferedReader = null;
            try {
                InputStreamReader inputStream=new InputStreamReader(new FileInputStream(file));
                bufferedReader=new BufferedReader(inputStream);
                value=bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return value;
    }
}
