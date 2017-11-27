package com.taisys.sc.mobileid;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

public class MainActivity extends Activity {

    //CarouselView圖片輪播，參考 https://github.com/sayyam/carouselview
    CarouselView carouselView;
    //int[] sampleImages = {R.drawable.banner_tw_01, R.drawable.banner_tw_02, R.drawable.banner_tw_03, R.drawable.banner_tw_04, R.drawable.banner_tw_05, R.drawable.banner_tw_06, R.drawable.banner_tw_07, R.drawable.banner_tw_08, R.drawable.banner_tw_09, R.drawable.banner_tw_10, R.drawable.banner_tw_11};
    int[] sampleImages = {R.drawable.banner_tw_12, R.drawable.banner_tw_13, R.drawable.banner_tw_14, R.drawable.banner_tw_15};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setCarouselView();

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

    //設定輪播 banner
    private void setCarouselView(){
        carouselView = (CarouselView) findViewById(R.id.carouselView);
        carouselView.setPageCount(sampleImages.length);
        carouselView.setImageListener(imageListener);
        ViewGroup.LayoutParams para = carouselView.getLayoutParams();
        // 取得螢幕解析度
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int i = dm.widthPixels;
        //Log.d("FCM", "i=" + String.valueOf(i));
        //i = (108*i/730)-32; //原始檔案是730*108的，減32是因為Layout有paddingLeft及paddingRight各16
        //i = (i/2)-32; //原始檔案是730*108的，減32是因為Layout有paddingLeft及paddingRight各16
        i = i/2;
        //Log.d("FCM", "i=" + String.valueOf(i));
        para.height = i;
        carouselView.setLayoutParams(para);

        i=dm.heightPixels;
        Log.d("FCM", "i=" + String.valueOf(i));
        i=(i-para.height-32)/2;
        Log.d("FCM", "i=" + String.valueOf(i));
        //para.height = i;
        Button btn = (Button) findViewById(R.id.iconRegistration);
        //btn.setHeight(i);
        //btn.setLayoutParams(para);
    }   //private void setCarouselView(){

    //給輪播 banner用的
    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(sampleImages[position]);
        }
    };

}
