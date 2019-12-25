package com.ubtrobot.light;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Created by taoqian on 2018/11/12.
 */
public class SetValueIntentService extends IntentService {
    private static final String TAG = SetValueIntentService.class.getSimpleName();
    private boolean mFlag;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public SetValueIntentService() {
        super(TAG);
        mFlag = true;
        Log.i(TAG, "init:");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            boolean flag = intent.getBooleanExtra("flag", true);
            if (mFlag == false && flag == true) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "sleep:");
            }
            mFlag = flag;
            String value = intent.getStringExtra("value");
            write(value);
        }
    }

    private void write(String value) {
        String path="dev/mid";
        File file = new File(path);
        Log.i(TAG, "value:" + value);
        if (file.exists()) {
            Writer fstream = null;
            try {
                fstream = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                fstream.write(value + "\n");
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
}
