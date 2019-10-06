package com.safepayu.wallet.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.safepayu.wallet.BaseApp;
import com.safepayu.wallet.R;
import com.safepayu.wallet.api.ApiClient;
import com.safepayu.wallet.api.ApiService;
import com.safepayu.wallet.dialogs.LoadingDialog;
import com.safepayu.wallet.halper.Config;
import com.safepayu.wallet.models.request.Login;
import com.safepayu.wallet.models.request.ResetPasscodeModel;
import com.safepayu.wallet.models.response.BaseResponse;
import com.safepayu.wallet.models.response.UserResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ForgotPasscode extends AppCompatActivity {

    EditText edit_number, enter_otp, enter_password;
    Button btn_request_otp, btn_continue, btn_conform_password, resend_btn;
    TextView timer, label, back_forgot_password;
    String str_edit_otp, str_edit_conf_pass, str_edit_number, password = "918429";
    private int randomPIN, Otpval;
    private Integer otpToSend;
    boolean resend_top = false;
    String session_id, userid;
    LinearLayout layout1, layout2, layout3;
    private LoadingDialog loadingDialog;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_passcode);

        loadingDialog = new LoadingDialog(this);
        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);
        edit_number = (EditText) findViewById(R.id.number_forgot);
        label = findViewById(R.id.label);
        timer = findViewById(R.id.timer);
        enter_otp = (EditText) findViewById(R.id.enter_otp);
        enter_password = (EditText) findViewById(R.id.enter_password);

        otpToSend = 0;
        Random r = new Random();
        Otpval = r.nextInt(9999 - 1000) + 1000;


        btn_request_otp = (Button) findViewById(R.id.request_otp);
        resend_btn = (Button) findViewById(R.id.resend_otp);
        btn_continue = (Button) findViewById(R.id.continue_otp);
        btn_conform_password = (Button) findViewById(R.id.conformPassword);
        back_forgot_password = (TextView) findViewById(R.id.back_forgot_password);
        layout1 = (LinearLayout) findViewById(R.id.layout1);
        layout2 = (LinearLayout) findViewById(R.id.layout2);
        layout3 = (LinearLayout) findViewById(R.id.layout3);

        resend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resend_top = true;
                resendOtp();

            }
        });

        btn_request_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit_number.getText().toString().length() != 10) {
                    Toast.makeText(getApplicationContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                } else {
                    str_edit_number = edit_number.getText().toString();
                    resendOtp();
                }
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueOtp();
            }
        });
        btn_conform_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conformPassword();
            }
        });
        back_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    void continueOtp() {
        str_edit_otp = enter_otp.getText().toString().trim();

        if (TextUtils.isEmpty(str_edit_otp)) {
            enter_otp.setError("Please Enter OPT");
            enter_otp.requestFocus();
            return;
        }else {
            verifyOtp( enter_otp.getText().toString().trim());
        }
    }

    void conformPassword() {
        str_edit_conf_pass = enter_password.getText().toString().trim();

        if (TextUtils.isEmpty(str_edit_conf_pass) || enter_password.getText().toString().trim().length() < 4) {

            enter_password.setError("Please Enter Valid Passcode");
            enter_password.requestFocus();
            return;
        } else {
            resetPasscode(str_edit_conf_pass);
        }

    }

    private void resendOtp() {
        loadingDialog.showDialog(getResources().getString(R.string.loading_message), false);
        Login request = new Login(edit_number.getText().toString().trim(), null);

        BaseApp.getInstance().getDisposable().add(apiService.resendOtp(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        loadingDialog.hideDialog();
                        if (response.getStatus()) {
                            timer.setVisibility(View.VISIBLE);
                            layout2.setVisibility(View.VISIBLE);
                            btn_request_otp.setVisibility(View.GONE);
                            resend_btn.setVisibility(View.GONE);

                            new CountDownTimer(59000, 1000) {

                                public void onTick(long millisUntilFinished) {

                                    timer.setText("00:" + millisUntilFinished / 1000);
                                    //here you can have your logic to set text to edittext
                                }

                                public void onFinish() {
                                    timer.setVisibility(View.GONE);
                                    resend_btn.setVisibility(View.VISIBLE);
                                }

                            }.start();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(BaseApp.getInstance().toastHelper().getTag(LoginActivity.class), "onError: " + e.getMessage());
                        loadingDialog.hideDialog();
                        BaseApp.getInstance().toastHelper().showApiExpectation(findViewById(R.id.forgetPasscodeId), true, e);
                    }
                }));
    }

    private void verifyOtp(String otp) {

        loadingDialog.showDialog(getResources().getString(R.string.loading_message), false);
        Login request = new Login(edit_number.getText().toString().trim(), null);
        request.setOtp(otp);
        BaseApp.getInstance().getDisposable().add(apiService.verifyOTP(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<UserResponse>() {
                    @Override
                    public void onSuccess(UserResponse response) {
                        loadingDialog.hideDialog();
                        if (response.getStatus()) {
                            label.setHint("Enter New Passcode");
                            layout1.setVisibility(View.GONE);
                            layout2.setVisibility(View.GONE);
                            layout3.setVisibility(View.VISIBLE);

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(BaseApp.getInstance().toastHelper().getTag(LoginActivity.class), "onError: " + e.getMessage());
                        loadingDialog.hideDialog();
                        BaseApp.getInstance().toastHelper().showApiExpectation(findViewById(R.id.forgetPasscodeId), true, e);
                    }
                }));
    }

    private void resetPasscode(final String Passcode) {

        loadingDialog.showDialog(getResources().getString(R.string.loading_message), false);
        ResetPasscodeModel resetPasscodeModel=new ResetPasscodeModel();
        resetPasscodeModel.setPasscode(Passcode);

        BaseApp.getInstance().getDisposable().add(apiService.resetPasscode(resetPasscodeModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        loadingDialog.hideDialog();
                        if (response.getStatus()) {
                            Toast.makeText(ForgotPasscode.this, response.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.forgetPasscodeId),response.getMessage(),false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(BaseApp.getInstance().toastHelper().getTag(LoginActivity.class), "onError: " + e.getMessage());
                        loadingDialog.hideDialog();
                        BaseApp.getInstance().toastHelper().showApiExpectation(findViewById(R.id.forgetPasscodeId), true, e);
                    }
                }));
    }
}