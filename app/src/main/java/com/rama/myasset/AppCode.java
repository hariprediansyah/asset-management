package com.rama.myasset;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;

public class AppCode {
    public interface OnEvent {
        public void ok();
    }

    public static void showConfirmationDialog(String msg, Context context, OnEvent onEvent) {
        new AlertDialog.Builder(context)
                .setTitle("Konfirmasi")
                .setMessage(msg)
                .setPositiveButton("Ya", (dialog, which) -> {
                    onEvent.ok();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    public static void showAlert(String msg, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Information")
                .setMessage(msg)
                .setPositiveButton("Ok", (dialog, which) -> {

                })
                .show();
    }

    public static byte[] getByteFromUri(Context context, Uri uri){
        java.io.InputStream inputStream = null;
        try{
            inputStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1){
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }catch (Exception e){

        }
        return null;
    }
}
