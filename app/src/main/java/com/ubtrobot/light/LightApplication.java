package com.ubtrobot.light;

import android.app.Application;

import com.ubtrobot.light.sal.LightFactory;
import com.ubtrobot.light.sal.LightService;
import com.ubtrobot.master.Master;

/**
 * Created by taoqian on 2018/8/23.
 */
public class LightApplication extends Application implements LightFactory{

    @Override
    public void onCreate() {
        super.onCreate();

        Master.initialize(this);
    }

    @Override
    public LightService createLightService() {
        return new GoldenPigLightService(this);
    }
}
