package com.example.gvy.jesta;

import android.content.Context;

/**
 * Created by Tomer on 01/06/2018.
 */

public class JestaApp {

    public static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        JestaApp.context = context;
    }
}
