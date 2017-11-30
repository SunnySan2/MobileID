package com.taisys.sc.mobileid;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by sunny on 2017/11/25.
 */

public class utility {

    public static String getMySetting(Context context, String keyName){
        // 建立SharedPreferences物件
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String valueString = sharedPreferences.getString(keyName, "");
        return valueString;
    }

    public static void setMySetting(Context context, String keyName, String value){
        // 建立SharedPreferences物件
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(keyName, value);
        editor.apply();
    }

    public static void showToast(Context context, String msg) {
        if (msg==null || msg.length()==0) return;
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void showMessage(Context context, String msg){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.msgSystemInfo)
                .setMessage(msg)
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //取得 byte array 每個 byte 的 16 進位碼
    public static String byte2Hex(byte[] b) {
        String result = "";
        for (int i=0 ; i<b.length ; i++)
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        return result;
    }

    //將 16 進位碼的字串轉為 byte array
    public static byte[] hex2Byte(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i=0 ; i<bytes.length ; i++)
            bytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
        return bytes;
    }

}
