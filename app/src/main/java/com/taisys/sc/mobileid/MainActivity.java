package com.taisys.sc.mobileid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
                Intent intent = getIntent();
                String msg = intent.getStringExtra("msg");
                if (msg!=null) Log.d("FCM", "msg:" + msg);
                msg = intent.getStringExtra("msg2");
                if (msg!=null) Log.d("FCM", "msg2:" + msg);
                */
        String msisdn = utility.getMySetting(this, "msisdn");
        if (msisdn.length()<1){
            //editPhoneNumber();
        }
        //Toast.makeText(this, msisdn, Toast.LENGTH_SHORT).show();
        //utility.setMySetting(this, "msisdn", "123");
    }

    // 加入載入選單資源的方法
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // 使用者選擇所有的選單項目都會呼叫這個方法
    public void clickMenuItem(MenuItem item) {
        // 使用參數取得使用者選擇的選單項目元件編號
        int itemId = item.getItemId();

        // 判斷該執行什麼工作，目前還沒有加入需要執行的工作
        switch (itemId) {
            case R.id.set_phone_number:
                break;
            case R.id.edit_settings:
                break;
        }

        // 測試用的程式碼，完成測試後記得移除
        AlertDialog.Builder dialog =
                new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("MenuItem Test")
                .setMessage(item.getTitle())
                .setIcon(item.getIcon())
                .show();
    }
}
