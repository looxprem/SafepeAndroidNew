package com.safepayu.wallet.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.safepayu.wallet.BaseActivity;
import com.safepayu.wallet.BaseApp;
import com.safepayu.wallet.R;
import com.safepayu.wallet.api.ApiClient;
import com.safepayu.wallet.api.ApiService;
import com.safepayu.wallet.dialogs.LoadingDialog;
import com.safepayu.wallet.models.request.AddBeneficiaryRequest;
import com.safepayu.wallet.models.request.Login;
import com.safepayu.wallet.models.response.AddBeneficiaryResponse;
import com.safepayu.wallet.models.response.BaseResponse;
import com.safepayu.wallet.models.response.UserResponse;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AddBeneficiary extends BaseActivity {

    private Button BackBtn, AddBenBtn;
    private EditText AccountNameED, AccountNumberED, AccountConfirmED, IFSCED;
    private LoadingDialog loadingDialog;
    private boolean CheckNet = false;
    private ImageView showAccNo, HideAccNo;
    private ApiService apiService;
    private String Mobile = "";
    private AddBeneficiaryRequest addBeneficiaryRequest;
    //Otp Dialog
    TextView TimerTV;
    EditText OtpED;
    Button continueButton, resendButton;
    private ImageView im_cross;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolbar(false, null, false);

        loadingDialog = new LoadingDialog(this);
        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);
        addBeneficiaryRequest = new AddBeneficiaryRequest();

        Mobile = BaseApp.getInstance().sharedPref().getString(BaseApp.getInstance().sharedPref().MOBILE);

        BackBtn = findViewById(R.id.send_back_btn);
        AccountNameED = findViewById(R.id.accountName);
        AccountNumberED = findViewById(R.id.accountNumber);
        AccountConfirmED = findViewById(R.id.confirmAccountNumber);
        IFSCED = findViewById(R.id.ifscCode);
        AddBenBtn = findViewById(R.id.bankAddBtn);
        showAccNo = findViewById(R.id.password_visible);
        HideAccNo = findViewById(R.id.password_invisible);

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        AddBenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckValidate();
            }
        });

        showAccNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAccNo.setVisibility(View.GONE);
                HideAccNo.setVisibility(View.VISIBLE);
                AccountConfirmED.setTransformationMethod(null);
                try {
                    AccountConfirmED.setSelection(AccountConfirmED.getText().toString().length());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        HideAccNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAccNo.setVisibility(View.VISIBLE);
                HideAccNo.setVisibility(View.GONE);
                AccountConfirmED.setTransformationMethod(new PasswordTransformationMethod());
                try {
                    AccountConfirmED.setSelection(AccountConfirmED.getText().toString().length());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.add_beneficiary;
    }

    @Override
    protected void connectivityStatusChanged(Boolean isConnected, String message) {
        if (isConnected) {
            CheckNet = true;
        } else {
            CheckNet = false;
        }

    }

    private void CheckValidate() {

        String AccountN = AccountNameED.getText().toString().trim();
        String AccountNF = AccountConfirmED.getText().toString().trim();

        if (TextUtils.isEmpty(AccountNameED.getText().toString().trim())) {
            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.addBeneficiaryLayout), "Please Enter Name As On Account", false);
        } else {
            if (TextUtils.isEmpty(AccountNumberED.getText().toString().trim())) {
                BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.addBeneficiaryLayout), "Please Enter Account Number", false);
            } else {
                if (TextUtils.isEmpty(AccountConfirmED.getText().toString().trim())) {
                    BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.addBeneficiaryLayout), "Please Enter Account Number To Confirm", false);
                } else {
                    if (TextUtils.isEmpty(IFSCED.getText().toString().trim())) {
                        BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.addBeneficiaryLayout), "Please Enter IFSC Code", false);
                    } else {
                        if (AccountN.equals(AccountNF)) {
                            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.addBeneficiaryLayout), "Please Same Account Number In Both Account's Field", false);
                        } else {
                            addBeneficiaryRequest.setName(AccountNameED.getText().toString().trim());
                            addBeneficiaryRequest.setBank_account(AccountNumberED.getText().toString().trim());
                            addBeneficiaryRequest.setIfsc_code(IFSCED.getText().toString().trim());
                            addBeneficiaryRequest.setUpi("");
                            addBeneficiaryRequest.setPaytm("");

                            if (CheckNet) {
                                resendOtp();
                            } else {
                                BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.addBeneficiaryLayout), "Check Your Internet Connection!", false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addBenMethod(AddBeneficiaryRequest addBeneficiaryRequest) {

        loadingDialog.showDialog(getResources().getString(R.string.loading_message), false);

        BaseApp.getInstance().getDisposable().add(apiService.addBeneficiary(addBeneficiaryRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<AddBeneficiaryResponse>() {
                    @Override
                    public void onSuccess(AddBeneficiaryResponse response) {
                        loadingDialog.hideDialog();
                        if (response.isStatus()) {
                            showDialogAfterAddBen(AddBeneficiary.this, response.getMessage());
                        } else {
                            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.addBeneficiaryLayout), response.getMessage() + "\n" + response.getReason(), true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Log.e(BaseApp.getInstance().toastHelper().getTag(LoginActivity.class), "onError: " + e.getMessage());
                        loadingDialog.hideDialog();
                        BaseApp.getInstance().toastHelper().showApiExpectation(findViewById(R.id.addBeneficiaryLayout), true, e);
                    }
                }));

    }

    private void resendOtp() {

        loadingDialog.showDialog(getResources().getString(R.string.loading_message), false);
        Login request = new Login(Mobile, null);

        BaseApp.getInstance().getDisposable().add(apiService.resendOtp(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        loadingDialog.hideDialog();
                        if (response.getStatus()) {
                            countDownTimer.start();
                            showDialog(AddBeneficiary.this);

                        } else {
                            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.addBeneficiaryLayout), response.getMessage(), false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Log.e(BaseApp.getInstance().toastHelper().getTag(LoginActivity.class), "onError: " + e.getMessage());
                        loadingDialog.hideDialog();
                        BaseApp.getInstance().toastHelper().showApiExpectation(findViewById(R.id.addBeneficiaryLayout), true, e);
                    }
                }));
    }

    private void verifyOtp(String otp) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        loadingDialog.showDialog(getResources().getString(R.string.loading_message), false);
        Login request = new Login(Mobile, null);
        request.setOtp(otp);
        request.setDeviceid(BaseApp.getInstance().commonUtils().getTelephonyManager().getDeviceId());
        BaseApp.getInstance().getDisposable().add(apiService.verifyOTP(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<UserResponse>() {
                    @Override
                    public void onSuccess(UserResponse response) {
                        loadingDialog.hideDialog();
                        if (response.getStatus()) {
                            addBenMethod(addBeneficiaryRequest);
                        } else {
                            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.addBeneficiaryLayout), response.getMessage(), false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Log.e(BaseApp.getInstance().toastHelper().getTag(LoginActivity.class), "onError: " + e.getMessage());
                        loadingDialog.hideDialog();
                        BaseApp.getInstance().toastHelper().showApiExpectation(findViewById(R.id.addBeneficiaryLayout), true, e);
                    }
                }));
    }

    CountDownTimer countDownTimer = new CountDownTimer(4*60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            int seconds = (int) (millisUntilFinished / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            TimerTV.setText("" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
        }

        @Override
        public void onFinish() {
            resendButton.setVisibility(View.VISIBLE);
            TimerTV.setVisibility(View.INVISIBLE);
        }
    };

    public void showDialog(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.otp_dialog);

        TimerTV = dialog.findViewById(R.id.timerLogin);
        OtpED = dialog.findViewById(R.id.enter_otpLogin);
        im_cross = dialog.findViewById(R.id.im_cross);

        im_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        continueButton = (Button) dialog.findViewById(R.id.continue_otpLogin);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(OtpED.getText().toString().trim())) {
                    OtpED.setError("Please Enter Otp");
                    //BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.layout_mainLayout),"Please Enter Otp",false);
                } else {
                    verifyOtp(OtpED.getText().toString().trim());
                    dialog.dismiss();
                }
            }
        });

        resendButton = (Button) dialog.findViewById(R.id.resend_otpLogin);
        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendOtp();
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.getWindow().setAttributes(lp);
        dialog.show();

    }

    public void showDialogAfterAddBen(Activity activity,String Response) {
        AlertDialog.Builder dialog=new AlertDialog.Builder(activity);
        dialog.setTitle("SafePe Alert")
                .setMessage("\n"+Response+"\nPlease Wait For Minimum Of 30 Mins After Adding Of Beneficiary")
                .setCancelable(false)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        finish();
                        dialog.dismiss();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                //.setNegativeButton(android.R.string.no, null)
                .setIcon(getResources().getDrawable(R.drawable.safelogo_transparent))
                .show();
    }
}
