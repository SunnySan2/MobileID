package com.taisys.sc.mobileid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ReportActivity extends Activity {
    private ProgressDialog pg = null;
    private Context myContext = null;

    private ListView lv;
    private List<Map<String, Object>> data;

    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private Map<String, Object> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        myContext = this;
        lv = (ListView)findViewById(R.id.lvReportList);
        getData();
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

    private List<Map<String, Object>> getData()
    {
        String iccid = utility.getMySetting(myContext, "iccid");
        if (iccid==null || iccid.length()==0){
            utility.showMessage(myContext,getString(R.string.msgUnableToGetIccid));
            return null;
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
            String url = "http://cms.gslssd.com/MobileIdServer/ajaxGetTransactionInfoReceiver.jsp";
            Request request = new Request.Builder().post(params).url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            disWaiting();
                            utility.showMessage(myContext, getString(R.string.msgFailToCommunicateWithServer) + ":\n" + e.toString());
                            Log.e("MobileIdReport", e.toString());
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    ResponseBody responseBody = response.body();
                    final String sResponse = response.body().string();
                    Log.d("MobileIdReport", sResponse);
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
                                    JSONArray jaRecords = jResponse.getJSONArray("records");
                                    int iSize = jaRecords.length();
                                    for (int i = 0; i < iSize; i++)
                                    {
                                        JSONObject jItem = jaRecords.getJSONObject(i);
                                        map = new HashMap<String, Object>();
                                        if (jItem.getString("Status")!=null && jItem.getString("Status").equals("Success")) {
                                            map.put("img", R.drawable.ok);
                                        }else if (jItem.getString("Status")!=null && jItem.getString("Status").equals("Cancel")) {
                                            map.put("img", R.drawable.cancel);
                                        }else{
                                            map.put("img", R.drawable.question);
                                        }

                                        map.put("title", jItem.getString("Company_Name") + "-" + jItem.getString("Service_Name"));
                                        map.put("info", jItem.getString("Update_Date"));
                                        list.add(map);
                                    }
                                    data = list;
                                    MyAdapter adapter = new MyAdapter(myContext);
                                    lv.setAdapter(adapter);
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
            Log.e("MobileIdReport", e.toString());
        }

        return null;
    }

    static class ViewHolder
    {
        public ImageView img;
        public TextView title;
        public TextView info;
    }

    public class MyAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater = null;
        private MyAdapter(Context context)
        {
            //根据context上下文加载布局，这里的是Demo17Activity本身，即this
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            //How many items are in the data set represented by this Adapter.
            //在此适配器中所代表的数据集中的条目数
            return data.size();
        }
        @Override
        public Object getItem(int position) {
            // Get the data item associated with the specified position in the data set.
            //获取数据集中与指定索引对应的数据项
            return data.get(position);
        }
        @Override
        public long getItemId(int position) {
            //Get the row id associated with the specified position in the list.
            //获取在列表中与指定索引对应的行id
            return position;
        }

        //Get a View that displays the data at the specified position in the data set.
        //获取一个在数据集中指定索引的视图来显示数据
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            //如果缓存convertView为空，则需要创建View
            if(convertView == null)
            {
                holder = new ViewHolder();
                //根据自定义的Item布局加载布局
                convertView = mInflater.inflate(R.layout.report_list_item, null);
                holder.img = (ImageView)convertView.findViewById(R.id.imgReportListItemImage);
                holder.title = (TextView)convertView.findViewById(R.id.textReportListItemTitle);
                holder.info = (TextView)convertView.findViewById(R.id.textReportListItemInfo);
                //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                convertView.setTag(holder);
            }else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.img.setImageResource((Integer) data.get(position).get("img"));
            holder.title.setText((String)data.get(position).get("title"));
            holder.info.setText((String)data.get(position).get("info"));

            return convertView;
        }
    }

    private void setOnClickListener(){
        Button b1 = (Button) findViewById(R.id.buttonReportConfirm);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


}
