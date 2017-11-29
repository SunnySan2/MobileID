package com.taisys.sc.mobileid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.taisys.oti.Card;
import com.taisys.oti.Card.SCSupported;

import static java.lang.Thread.sleep;

public class RegistrationActivity extends Activity {
    private Card mCard = new Card();
    private ProgressDialog pg = null;
    private Context myContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        myContext = this;
        setOnClickListener();
        utility.setMySetting(myContext, "iccid", "");   //先把程式裡的 iccid 設定清除

        //showWaiting("Read card info, please wait...");

    }

    @Override
    public void onDestroy() {
        if (mCard!=null){
            mCard.CloseSEService();
        }
        super.onDestroy();
    }

    private void showWaiting(String msg) {
        // if(pg == null) {
        pg = new ProgressDialog(this);
        // }
        pg.setIndeterminate(true);
        pg.setCancelable(false);
        pg.setCanceledOnTouchOutside(false);
        pg.setMessage(msg);
        pg.show();
    }

    private void disWaiting() {
        if (pg != null && pg.isShowing()) {
            pg.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private void getCardInfo(){
        String res[] = mCard.GetCardInfo();
        String iccid = "";
        String s = "";
        int i = 0;
        int j = 0;
        if (res != null && res[0].equals(Card.RES_OK)) {
            /*
                            res[1] 的結構如下：
                            假设拿到回复信息为：040001C3D908123456789012345601010412000100
                            其中   040001C3D9           LV结构 04长度，0001C3D9文件系统剩余空间大小，0x0001C3D9 = 115673 byte；
                            081234567890123456   LV结构 08长度，081234567890123456为卡号；
                            0101                 LV结构 01长度，01卡片版本号；
                            0412000100           LV结构 04长度，12000100 Cos版本号；
                         */
            s = res[1].substring(0, 2);
            i = Integer.parseInt(s);
            s = res[1].substring((i+1)*2, (i+1)*2 + 2);
            //utility.showMessage(myContext, s);
            j = Integer.parseInt(s);
            iccid = res[1].substring((i+1)*2+2, (i+1)*2+2 + j*2);
            //utility.showMessage(myContext, s);
            //i = s.length();
            //utility.showMessage(myContext, String.valueOf(i));
            utility.setMySetting(myContext, "iccid", iccid);
            //utility.showToast(this, "CardInfor: " + res[1]);
            utility.showToast(this, "ICCID= " + iccid);
        } else {
            utility.showMessage(myContext, getString(R.string.msgCannotReadCardInfo));
        }

    }

    private void setOnClickListener(){
        Button b1 = (Button) findViewById(R.id.buttonRegistrationConfirm);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //顯示Progress對話視窗
                pg = ProgressDialog.show(myContext, getString(R.string.pleaseWait), getString(R.string.msgReadCardInfo), true);
                RegistrationActivity.this.runOnUiThread(new Runnable() {
                    public void run(){
                        try{
                            mCard.OpenSEService(myContext, "A000000018506373697A672D63617264",
                                    new SCSupported() {

                                        @Override
                                        public void isSupported(boolean success) {
                                            //disWaiting();
                                            if (success) {
                                                //showToast("该手机支持OTI！");
                                                getCardInfo();
                                            } else {
                                                utility.showMessage(myContext, getString(R.string.msgDoesntSupportOti));
                                            }
                                        }
                                    });
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        finally{
                            pg.dismiss();
                        }
                    }
                });

            }
        });

        Button b2 = (Button) findViewById(R.id.buttonRegistrationCancel);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


}
