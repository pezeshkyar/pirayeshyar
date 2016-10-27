package com.example.doctorsbuilding.nav;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.doctorsbuilding.nav.Databases.DatabaseAdapter;
import com.example.doctorsbuilding.nav.Dr.Clinic.Office;
import com.example.doctorsbuilding.nav.User.User;
import com.example.doctorsbuilding.nav.Util.DbBitmapUtility;
import com.example.doctorsbuilding.nav.Util.MessageBox;
import com.example.doctorsbuilding.nav.Web.WebService;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by hossein on 7/24/2016.
 */
public class SplashActivity extends AppCompatActivity {
    TextView splashTv;
    Button reloadBtn;
    ImageView reloadImg;
    ProgressBar progressBar;
    public UserType menu = UserType.None;
    private SharedPreferences settings;
    DatabaseAdapter database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        splashTv = (TextView) findViewById(R.id.splash_tv);
        reloadBtn = (Button) findViewById(R.id.splash_btn);
        progressBar = (ProgressBar) findViewById(R.id.splash_prbar);
        reloadImg = (ImageView) findViewById(R.id.splash_img);
        checkIsOnline();
        database = new DatabaseAdapter(SplashActivity.this);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    private void checkIsOnline() {
        if (isOnline()) {
            progressBar.setVisibility(View.VISIBLE);
            splashTv.setVisibility(View.VISIBLE);
            loadData();
        } else {
            new MessageBox(this, "دسترسی به اینترنت امکان پذیر نمی باشد، لطفا تنظیمات اینترنت خود را چک نمایید .").show();
            reloadPage();
        }
    }

    private void loadData() {
        splashTv.setText("در حال دریافت اطلاعات ...");
        if (G.UserInfo == null)
            G.UserInfo = new User();
        if (G.officeInfo == null)
            G.officeInfo = new Office();
        loadUser();
        AsyncCallGetData task = new AsyncCallGetData();
        task.execute();
    }

    private void loadUser() {

        settings = G.getSharedPreferences();
        G.UserInfo.setUserName(settings.getString("user", ""));
        G.UserInfo.setPassword(settings.getString("pass", ""));
        if (G.UserInfo.getUserName().length() == 0 && G.UserInfo.getPassword().length() == 0) {
            G.UserInfo.setUserName("guest");
            G.UserInfo.setPassword("8512046384");
        }

    }

    private void reloadPage() {
        splashTv.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        reloadImg.setVisibility(View.VISIBLE);
        reloadBtn.setVisibility(View.VISIBLE);
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadBtn.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                reloadImg.setVisibility(View.GONE);
                checkIsOnline();
            }
        });
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private class AsyncCallGetData extends AsyncTask<String, Void, Void> {
        String msg = null;
        int role = -1;

        @Override
        protected Void doInBackground(String... strings) {
            try {

                role = WebService.invokeLoginWS(G.officeId, setUserData());
                if (role > 0 && role != UserType.Guest.ordinal()) {
                    G.UserInfo = WebService.invokeGetUserInfoWS(G.UserInfo.getUserName(), G.UserInfo.getPassword(), G.officeId);
                } else {
                    G.UserInfo.setUserName("guest");
                    G.UserInfo.setPassword("8512046384");
                    G.UserInfo.setRole(UserType.Guest.ordinal());
                }
                G.officeInfo = WebService.invokeGetOfficeInfoWS(G.UserInfo.getUserName(), G.UserInfo.getPassword(), G.officeId);
            } catch (PException ex) {
                msg = ex.getMessage();

            }
            return null;
        }

        private User setUserData() {
            User user = new User();
            user.setUserName(G.UserInfo.getUserName());
            user.setPassword(G.UserInfo.getPassword());
            return user;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (msg != null) {
                new MessageBox(SplashActivity.this, msg).show();
                reloadPage();
            } else {

                if (G.UserInfo.getRole() != UserType.None.ordinal()) {
                    database = new DatabaseAdapter(SplashActivity.this);
                    database.initialize();
                    if (database.openConnection()) {
                        G.doctorImageProfile = database.getImageProfile(1);
                        if (G.doctorImageProfile == null) {
                            G.doctorImageProfile = BitmapFactory.decodeResource(getResources(), R.mipmap.doctor);
                        }
                        database.closeConnection();
                    }

                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } else {

                    final MessageBox errorMessage = new MessageBox(SplashActivity.this, "خطای در برقراری ارتباط رخ داده است !");
                    errorMessage.setCancelable(false);
                    errorMessage.show();
                    errorMessage.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            if (errorMessage.pressAcceptButton()) {
                                finish();
                            }
                        }
                    });

                }
            }
        }
    }

}