package com.example.doctorsbuilding.nav;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doctorsbuilding.nav.Util.MessageBox;
import com.example.doctorsbuilding.nav.Util.MoneyTextWatcher;
import com.example.doctorsbuilding.nav.Util.Util;
import com.example.doctorsbuilding.nav.Web.WebService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by hossein on 9/3/2016.
 */
public class ActivityReception extends AppCompatActivity {
    private TextView nameTxt;
    private TextView taskTxt;
    private EditText costTxt;
    private EditText detailsTxt;
    private Button insertBtn;
    private Button addNextBtn;
    private Button backBtn;
    private Button showFileBtn;
    private PatientInfo patientInfo = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_reception);
        initViews();
        eventListener();
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    private void initViews() {
        nameTxt = (TextView) findViewById(R.id.reception_name);
        taskTxt = (TextView) findViewById(R.id.reception_task);
        costTxt = (EditText) findViewById(R.id.reception_price);
        detailsTxt = (EditText) findViewById(R.id.reception_detail);
        insertBtn = (Button) findViewById(R.id.reception_addBtn);
        addNextBtn = (Button) findViewById(R.id.reception_addNextBtn);
        backBtn = (Button) findViewById(R.id.reception_backBtn);
        showFileBtn = (Button) findViewById(R.id.reception_showFileBtn);
        patientInfo = (PatientInfo) getIntent().getSerializableExtra("patientInfo");
        if (patientInfo != null) {
            nameTxt.setText(patientInfo.getFirstName().concat(" " + patientInfo.getLastName()));
            taskTxt.setText(patientInfo.getTaskName());

            if (patientInfo.getPayment() != 0)
                costTxt.setText(Util.getCurrency(patientInfo.getPayment()));

            if (!patientInfo.getDescription().equals(""))
                detailsTxt.setText(patientInfo.getDescription());
        }

    }

    private void eventListener() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        insertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                asyncCallReception task = new asyncCallReception();
                task.execute();
            }
        });
        addNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityReception.this, ActivityAddNextTurn.class);
                intent.putExtra("patientInfo", patientInfo);
                startActivity(intent);
            }
        });
        showFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityReception.this, ActivityPatientFile.class);
                intent.putExtra("patientUserName", patientInfo.getUsername());
                startActivity(intent);
            }
        });

        costTxt.addTextChangedListener(new MoneyTextWatcher(costTxt));
    }


    private class asyncCallReception extends AsyncTask<String, Void, Void> {

        ProgressDialog dialog;
        boolean result = false;
        int reservationId, payment;
        String description;
        String msg = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(ActivityReception.this, "", "در حال ثبت اطلاعات ...");
            dialog.getWindow().setGravity(Gravity.END);
            reservationId = patientInfo.getReservationId();
            if (costTxt.getText().toString().trim().equals("")) {
                payment = 0;
            } else {
                payment = Integer.parseInt(Util.getNumber(costTxt.getText().toString().trim()));
            }
            description = detailsTxt.getText().toString().trim();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                result = WebService.invokeReceptionWS(G.UserInfo.getUserName(), G.UserInfo.getPassword(), G.officeId,
                        reservationId, payment, description);
            } catch (PException ex) {
                msg = ex.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (msg != null) {
                dialog.dismiss();
                new MessageBox(ActivityReception.this, msg).show();
            } else {
                dialog.dismiss();
                Toast.makeText(ActivityReception.this, "ثبت اطلاعات با موفقیت انجام شده است .", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
