package com.example.gvy.jesta;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Tomer on 26/05/2018.
 */

public class Loading {

    private static ProgressDialog mDialog;

    public static void startLoading(Context context){
        stopLoading();
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void stopLoading(){
        if (mDialog != null){
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
