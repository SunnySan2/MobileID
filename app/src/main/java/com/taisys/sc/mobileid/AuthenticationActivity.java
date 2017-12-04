package com.taisys.sc.mobileid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.taisys.oti.Card;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AuthenticationActivity extends Activity {
    private Card mCard = new Card();
    private ProgressDialog pg = null;
    private Context myContext = null;

    private String sAction = "";
    private String sServiceName = "";
    private String sCompanyName = "";
    private String sMessageContent = "";
    private String sTransactionID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        Intent intent = getIntent();
        sAction = intent.getStringExtra("Action");
        sServiceName = intent.getStringExtra("Service_Name");
        sCompanyName = intent.getStringExtra("Company_Name");
        sMessageContent = intent.getStringExtra("Message_Content");
        sTransactionID = intent.getStringExtra("Transaction_ID");
        if (sMessageContent==null || sMessageContent.length()<1 || sTransactionID==null || sTransactionID.length()<1) finish();

        TextView tvCompanyName = (TextView) findViewById(R.id.labelAuthenticationCompanyName);
        tvCompanyName.setText(getString(R.string.labelCompanyName) + sCompanyName);
        TextView tvServiceName = (TextView) findViewById(R.id.labelAuthenticationServiceName);
        tvServiceName.setText(getString(R.string.labelServiceName) + sServiceName);
        TextView tvMessageContent = (TextView) findViewById(R.id.labelAuthenticationMessageContent);
        tvMessageContent.setText(getString(R.string.labelAutheticationMessageContent) + sMessageContent);

        myContext = this;
        setOnClickListener();

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

    private void setOnClickListener(){
        Button b1 = (Button) findViewById(R.id.buttonAuthenticationConfirm);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //顯示Progress對話視窗
                // showWaiting(getString(R.string.pleaseWait), getString(R.string.msgCheckCardAvailability));
                showWaiting(getString(R.string.pleaseWait), getString(R.string.msgCreatingSignature));
                mCard.OpenSEService(myContext, "A000000018506373697A672D63617264",
                        new Card.SCSupported() {

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

        Button b2 = (Button) findViewById(R.id.buttonAuthenticationCancel);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAuthenticationResultToServer(false);
            }
        });
    }

    private void getCardInfo(){
        //顯示Progress對話視窗
        //utility.showToast(myContext, getString(R.string.msgReadCardInfo));
        //showWaiting(getString(R.string.pleaseWait), getString(R.string.msgReadCardInfo));
        Log.d("MobileIdAuthentication", "Get Card Info");
        String res[] = mCard.GetCardInfo();
        //disWaiting();
        if (res != null && res[0].equals(Card.RES_OK)) {
            verifyPinCode();
        } else {
            utility.showMessage(myContext, getString(R.string.msgUnableToGetIccid));
        }

    }

    private void verifyPinCode(){
        Log.d("MobileIdAuthentication", "Verify PIN code");
        EditText editTextPinCode = (EditText) findViewById(R.id.editTextAuthenticationPinCode);
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
        if (mCard!=null){
            mCard.CloseSEService();
        }
        if (res != null && res.equals(Card.RES_OK)) {
            Log.d("MobileId", "PIN verification passed");
            sendAuthenticationResultToServer(true);
        } else {
            disWaiting();
            Log.d("MobileId", "PIN code compared failed, user enter PIN= " + pinCode + ", response= " + res);
            //utility.showMessage(myContext, pinCode);
            utility.showMessage(myContext, getString(R.string.msgPinCodeIsIncorrect));
        }
    }

    private void sendAuthenticationResultToServer(boolean bPass){
        String idCardNumber = utility.getMySetting(myContext, "idCardNumber");
        if (idCardNumber==null || idCardNumber.length()==0){
            disWaiting();
            utility.showMessage(myContext,getString(R.string.msgUnableToGetIdCardNumber));
            return;
        }

        String iccid = utility.getMySetting(myContext, "iccid");
        if (iccid==null || iccid.length()==0){
            disWaiting();
            utility.showMessage(myContext,getString(R.string.msgUnableToGetIccid));
            return;
        }

        String sResult = "";
        if (bPass) sResult="00000"; else sResult="99999";

        //資料都有了，將資料送給 server
        //showWaiting(getString(R.string.pleaseWait), getString(R.string.msgSendRegistrationRequest));

        try {
            OkHttpClient client = new OkHttpClient();
            // 設定key - value 參數
            FormBody params = new FormBody.Builder()
                    .add("TransactionID", sTransactionID)
                    .add("TransactionResult", sResult)
                    .add("ICCID", iccid)
                    .build();

            // 建立請求物件，設定網址
            String url = "http://cms.gslssd.com/MobileIdServer/ajaxTransactionResultReceiver.jsp";
            Request request = new Request.Builder().post(params).url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            disWaiting();
                            utility.showMessage(myContext, getString(R.string.msgFailToCommunicateWithServer) + ":\n" + e.toString());
                            Log.e("MobileIdAuthentication", e.toString());
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    ResponseBody responseBody = response.body();
                    final String sResponse = response.body().string();
                    Log.d("MobileIdAuthentication", sResponse);
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
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(myContext);
                                    dialog.setTitle(R.string.msgSystemInfo)
                                            .setMessage(getString(R.string.msgProcessSucceeded))
                                            .setIcon(R.drawable.ic_launcher)
                                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    finish();
                                                }
                                            })
                                            .show();
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
            Log.e("MobileIdAuthentication", e.toString());
        }
    }


}
