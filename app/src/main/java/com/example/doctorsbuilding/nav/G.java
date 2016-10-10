package com.example.doctorsbuilding.nav;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import com.example.doctorsbuilding.nav.Dr.Clinic.Office;
import com.example.doctorsbuilding.nav.User.User;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by hossein on 7/18/2016.
 */
public class G extends Application {
    public static final int officeId = 1;
    public static User UserInfo;
    public static Office officeInfo;
    public static Bitmap doctorImageProfile;

    private static G instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/IRANSansMobile(FaNum).ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    public static boolean isOnline() {
        if (instance == null) return false;
        ConnectivityManager cm =
                (ConnectivityManager) instance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static SharedPreferences getSharedPreferences() {
        return instance.getSharedPreferences("pirayeshyar", 0);
    }

    public static Typeface getBoldFont() {
        return Typeface.createFromAsset(instance.getAssets(), "fonts/IRANSansMobile_Bold.ttf");
    }
}
