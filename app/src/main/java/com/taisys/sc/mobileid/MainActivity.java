package com.taisys.sc.mobileid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        String msg = intent.getStringExtra("msg");
        if (msg!=null) Log.d("FCM", "msg:" + msg);
        msg = intent.getStringExtra("msg2");
        if (msg!=null) Log.d("FCM", "msg2:" + msg);
    }
}
