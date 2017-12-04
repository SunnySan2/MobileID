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

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MyCertActivity extends Activity {
    private ProgressDialog pg = null;
    private Context myContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cert);

        myContext = this;
        getCertificateInfo();
        setOnClickListener();
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

    private void getCertificateInfo(){
        String iccid = utility.getMySetting(myContext, "iccid");
        if (iccid==null || iccid.length()==0){
            utility.showMessage(myContext,getString(R.string.msgUnableToGetIccid));
            return;
        }

        //資料都有了，將資料送給 server
        showWaiting(getString(R.string.pleaseWait), getString(R.string.msgDataUpdateInProgress));

        try {
            OkHttpClient client = new OkHttpClient();
            // 設定key - value 參數
            FormBody params = new FormBody.Builder()
                    .add("ICCID", iccid)
                    .build();

            // 建立請求物件，設定網址
            String url = "http://cms.gslssd.com/MobileIdServer/ajaxGetMyCertificateInfo.jsp";
            Request request = new Request.Builder().post(params).url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            disWaiting();
                            utility.showMessage(myContext, getString(R.string.msgFailToCommunicateWithServer) + ":\n" + e.toString());
                            Log.e("MobileIdMyCert", e.toString());
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    ResponseBody responseBody = response.body();
                    final String sResponse = response.body().string();
                    Log.d("MobileIdMyCert", sResponse);
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
                                    String sSerialNumber = jResponse.getString("Cert_Serial_Number");
                                    String sCertOwner = jResponse.getString("Cert_Owner");
                                    String sIssueDate = jResponse.getString("Cert_Create_Date");
                                    String sExpiryDate = jResponse.getString("Cert_End_Date");
                                    String sStatus = jResponse.getString("Status");
                                    EditText editSerialNumber = (EditText) findViewById(R.id.editTextMyCertSerialNumber);
                                    editSerialNumber.setText(sSerialNumber);
                                    EditText editCertOwner = (EditText) findViewById(R.id.editTextMyCertCertOwner);
                                    editCertOwner.setText(sCertOwner);
                                    EditText editIssueDate = (EditText) findViewById(R.id.editTextMyCertIssueDate);
                                    editIssueDate.setText(sIssueDate);
                                    EditText editExpiryDate = (EditText) findViewById(R.id.editTextMyCertExpiryDate);
                                    editExpiryDate.setText(sExpiryDate);
                                    EditText editStatus = (EditText) findViewById(R.id.editTextMyCertStatus);
                                    editStatus.setText(sStatus);
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
            Log.e("MobileIdMyCert", e.toString());
        }
    }

    private void setOnClickListener(){
        Button b1 = (Button) findViewById(R.id.buttonMyCertConfirm);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


}
