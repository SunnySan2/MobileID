package com.taisys.sc.mobileid;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by sunny.sun on 2017/11/24.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FCM", "onMessageReceived:"+remoteMessage.getFrom());
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Map<String, String> messageData = remoteMessage.getData();
        if (messageData!=null && messageData.size()>0){
            for(Map.Entry<String, String> entry : messageData.entrySet()) {
                String keyName = entry.getKey();
                String valueString = entry.getValue();
                Log.d("FCM", "Message data, key=" + keyName + ", value=" + valueString);
                intent.putExtra(keyName, valueString);
            }
            ComponentName cn = new ComponentName(this, AuthenticationActivity.class);
            intent.setComponent(cn);
            startActivity(intent);
        }else{
            Log.d("FCM", "No Message data");
        }
    }
}
