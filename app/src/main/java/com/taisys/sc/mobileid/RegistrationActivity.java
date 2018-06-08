package com.taisys.sc.mobileid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.taisys.oti.Card;
import com.taisys.oti.Card.SCSupported;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RegistrationActivity extends Activity {
    private Card mCard = new Card();
    private ProgressDialog pg = null;
    private Context myContext = null;

    private String myPublicKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        myContext = this;
        initView();
        setOnClickListener();

        //showWaiting("Read card info, please wait...");

    }

    @Override
    public void onDestroy() {
        if (mCard!=null){
            mCard.CloseSEService();
        }
        super.onDestroy();
    }

    private void showWaiting(final String title, final String msg) {
        disWaiting();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pg = new ProgressDialog(myContext);
                // }
                pg.setIndeterminate(true);
                pg.setCancelable(false);
                pg.setCanceledOnTouchOutside(false);
                pg.setTitle(title);
                pg.setMessage(msg);
                pg.show();
            }
        });
    }

    private void disWaiting() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pg != null && pg.isShowing()) {
                    pg.dismiss();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private void initView() {
        String idCardNumber = utility.getMySetting(myContext, "idCardNumber");
        if (idCardNumber!=null && idCardNumber.length()>0){
            EditText editIdCardNumber = (EditText) findViewById(R.id.editTextRegistationIdCardNumber);
            editIdCardNumber.setText(idCardNumber);
        }
    }

    private void getCardInfo(){
        //顯示Progress對話視窗
        //utility.showToast(myContext, getString(R.string.msgReadCardInfo));
        //showWaiting(getString(R.string.pleaseWait), getString(R.string.msgReadCardInfo));
        String res[] = mCard.GetCardInfo();
        String iccid = "";
        String s = "";
        int i = 0;
        int j = 0;
        //disWaiting();
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
            //utility.showToast(this, "ICCID= " + iccid);
            verifyPinCode();
        } else {
            disWaiting();
            utility.showMessage(myContext, getString(R.string.msgCannotReadCardInfo));
        }

    }

    private void setOnClickListener(){
        Button b1 = (Button) findViewById(R.id.buttonRegistrationConfirm);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        //顯示Progress對話視窗
        // showWaiting(getString(R.string.pleaseWait), getString(R.string.msgCheckCardAvailability));
        showWaiting(getString(R.string.pleaseWait), getString(R.string.msgRegistrationInProgress));
        utility.setMySetting(myContext, "iccid", "");   //先把程式裡的 iccid 設定清除
        mCard.OpenSEService(myContext, "A000000018506373697A672D63617264",
                new SCSupported() {

                    @Override
                    public void isSupported(boolean success) {
                        if (success) {
                            //手機支援OTI
                            getCardInfo();
                        } else {
                            disWaiting();
                            utility.showMessage(myContext, getString(R.string.msgDoesntSupportOti));
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

    private void verifyPinCode(){
        EditText editTextPinCode = (EditText) findViewById(R.id.editTextRegistrationPinCode);
        String pinCode = editTextPinCode.getText().toString();
        if (pinCode.length()==0){
            disWaiting();
            utility.showMessage(myContext,getString(R.string.msgPleaseEnterPinCode));
            return;
        }
        //utility.showToast(myContext, getString(R.string.msgVerifyPinCode));
        //showWaiting(getString(R.string.pleaseWait), getString(R.string.msgVerifyPinCode));
        int pinId = 0x1;
        pinCode = utility.byte2Hex(pinCode.getBytes());
        String res = mCard.VerifyPIN(pinId, pinCode);
        /*
        if (mCard!=null){
            mCard.CloseSEService();
        }
        */
        if (res != null && res.equals(Card.RES_OK)) {
            Log.d("MobileId", "PIN verification passed");
            generateRsaKeyPair();
        } else {
            disWaiting();
            Log.d("MobileId", "PIN code compared failed, user enter PIN= " + pinCode);
            utility.showMessage(myContext, getString(R.string.msgPinCodeIsIncorrect));
        }
    }

    private void generateRsaKeyPair(){
        String res[] = null;
        String s = "";
        int i = 0;
        int j = 0;

        long begintime = 0;

        String resString = "";

        //讀出 public key
        res = mCard.ReadFile(0x0201, 0x0, 264);
        if (res[0]!=null && res[0].equals("-15")) {    //key不存在，建立key
            //先執行CreateFile
            resString = mCard.CreateFile(0x0201, (byte)0x02, 0x0, (byte)0x0, (byte)0x0, (byte)0x0);
            if (resString == null || !resString.equals(Card.RES_OK)) {
                disWaiting();
                if (mCard!=null){
                    mCard.CloseSEService();
                }
                utility.showMessage(myContext, getString(R.string.msgUnableToCreatePublicKeyFile) + "error=" + resString);
                Log.e("MobileIdRegistration", "Sunny: Create public key file fail!");
                return;
            }
            Log.i("MobileIdRegistration", "Sunny: Create public key file OK!");
            resString = mCard.CreateFile(0x0301, (byte)0x03, 0x0, (byte)0x0, (byte)0x0, (byte)0x0);
            if (resString == null || !resString.equals(Card.RES_OK)) {
                disWaiting();
                if (mCard!=null){
                    mCard.CloseSEService();
                }
                utility.showMessage(myContext, getString(R.string.msgUnableToCreatePrivateKeyFile) + "error code=" + res[0]);
                Log.e("MobileIdRegistration", "Sunny: Create public key file fail!");
                return;
            }
            Log.i("MobileIdRegistration", "Sunny: Create private key file OK!");


            //產生 RSA key pair
            begintime = System.currentTimeMillis();
            resString = mCard.GenRSAKeyPair(Card.RSA_1024_BITS, 0x0201, 0x0301);
            begintime = System.currentTimeMillis() - begintime;
            if (resString != null && resString.equals(Card.RES_OK)) {
                Log.d("MobileIdRegistration", "time:" + begintime + "ms, " + "Gen key pair OK!");
            } else {
                if (mCard!=null){
                    mCard.CloseSEService();
                }
                Log.e("MobileIdRegistration", "time:" + begintime + "ms, " + "Gen key pair Failed!");
                utility.showMessage(myContext, getString(R.string.msgUnableToGenerateRsaKeyPair) + ", error code=" + resString);
                disWaiting();
                return;
            }
            //讀出 public key
            res = mCard.ReadFile(0x0201, 0x0, 264);

        }

        if (mCard!=null){
            mCard.CloseSEService();
        }
        disWaiting();
        if (res != null && res[0].equals(Card.RES_OK)) {
            myPublicKey = res[1];
            Log.d("MobileIdRegistration", "public key=" + myPublicKey);
            sendRegistrationRequestToServer();
        } else {
            utility.showMessage(myContext, getString(R.string.msgFailToReadPublicKey) + "error code=" + res[0]);
            Log.e("MobileIdRegistration", "no public key:" + res[0]);
            return;
        }
    }

    private void sendRegistrationRequestToServer(){
        EditText editTextIdCardNumber = (EditText) findViewById(R.id.editTextRegistationIdCardNumber);
        String idCardNumber = editTextIdCardNumber.getText().toString();
        if (idCardNumber==null || idCardNumber.length()==0){
            disWaiting();
            utility.showMessage(myContext,getString(R.string.msgPleaseEnterIdCardNumber));
            return;
        }

        String iccid = utility.getMySetting(myContext, "iccid");
        if (iccid==null || iccid.length()==0){
            disWaiting();
            utility.showMessage(myContext,getString(R.string.msgCannotReadCardInfo));
            return;
        }

        String fcmToken = utility.getMySetting(myContext, "fcmToken");
        if (fcmToken==null || fcmToken.length()==0){
            disWaiting();
            utility.showMessage(myContext,getString(R.string.msgCannotFindFcmToken));
            return;
        }

        utility.setMySetting(myContext, "idCardNumber", idCardNumber);

        //資料都有了，將資料送給 server
        //showWaiting(getString(R.string.pleaseWait), getString(R.string.msgSendRegistrationRequest));

        try {
            OkHttpClient client = new OkHttpClient();
            // 設定key - value 參數
            FormBody params = new FormBody.Builder()
                    .add("idCardNumber", idCardNumber)
                    .add("iccid", iccid)
                    .add("fcmToken", fcmToken)
                    .add("publicKey", myPublicKey)
                    .build();

            // 建立請求物件，設定網址
            String url = "http://cms.gslssd.com/MobileIdServer/ajaxDoCardRegistration.jsp";
            Request request = new Request.Builder().post(params).url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            disWaiting();
                            utility.showMessage(myContext, getString(R.string.msgFailToCommunicateWithServer) + ":\n" + e.toString());
                            Log.e("MobileIdRegistration", e.toString());
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    ResponseBody responseBody = response.body();
                    final String sResponse = response.body().string();
                    Log.d("MobileId", sResponse);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            disWaiting();
                            try{
                                JSONObject jResponse = new JSONObject(sResponse);
                                String sResultCode = jResponse.getString("resultCode");
                                String sResultText = jResponse.getString("resultText");
                                if (sResultCode==null || sResultCode.length()<1 || !sResultCode.equals("00000")){
                                    if (sResultText==null || sResultText.length()<1){
                                        utility.showMessage(myContext, getString(R.string.msgProcessFailed));
                                    }else{
                                        utility.showMessage(myContext, sResultText);
                                    }
                                }else{
                                    utility.showToast(myContext, getString(R.string.msgProcessSucceeded));
                                    finish();
                                }
                            }catch (Exception e){
                                utility.showMessage(myContext, getString(R.string.msgUnableToParseServerResponseData));
                            }

                        }
                    });
                }
            });
        }catch (Exception e){
            disWaiting();
            utility.showMessage(myContext, getString(R.string.msgFailToCommunicateWithServer));
            Log.e("MobileIdRegistration", e.toString());
        }
    }
}
