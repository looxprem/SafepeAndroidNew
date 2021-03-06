package com.safepayu.wallet.activity.recharge;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.safepayu.wallet.BaseActivity;
import com.safepayu.wallet.BaseApp;
import com.safepayu.wallet.R;
import com.safepayu.wallet.activity.LoginActivity;
import com.safepayu.wallet.activity.PaymentType;
import com.safepayu.wallet.api.ApiClient;
import com.safepayu.wallet.api.ApiService;
import com.safepayu.wallet.dialogs.LoadingDialog;
import com.safepayu.wallet.models.response.CustOperatorResponse;
import com.safepayu.wallet.models.response.OperatorResponse;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class GasPay extends BaseActivity {

    Button GasPayBtn,BackBtn;
    private Spinner OperatorSpinner;
    private EditText AmountED,GasIdED;
    String OperatorText="",OperatorCode="",OperatorId="";
    private LoadingDialog loadingDialog;
    private ArrayList<String> OperatorNameList,IdList,OperatorCodeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolbar(false, null, false);
        loadingDialog = new LoadingDialog(this);
        
        GasPayBtn=findViewById(R.id.gasPaybtn);
        BackBtn=findViewById(R.id.gas_back_btn);
        OperatorSpinner=findViewById(R.id.operatorsGas);
        AmountED=findViewById(R.id.amountGas);
        GasIdED=findViewById(R.id.gasID);

        OperatorNameList=new ArrayList<>();
        IdList=new ArrayList<>();
        OperatorCodeList=new ArrayList<>();

        OperatorNameList.clear();
        IdList.clear();
        OperatorCodeList.clear();

        OperatorNameList.add("Select Operator");
        IdList.add("0");
        OperatorCodeList.add("0");

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        GasPayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BaseApp.getInstance().sharedPref().getString(BaseApp.getInstance().sharedPref().PACKAGE_PURCHASED).equalsIgnoreCase("0")){
                    BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.gasPayLayout),"Please Buy Membership To Enjoy App's Features",false);
                }else {
                    CheckValidate();
                }
            }
        });

        OperatorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                OperatorText=OperatorSpinner.getItemAtPosition(i).toString();
                OperatorCode=OperatorCodeList.get(i);
                OperatorId=IdList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        GasIdED.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // TODO Auto-generated method stub

                if (s.length()==10 || s.length()>10){
                    getCustomerOperator();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                // TODO Auto-generated method stub
            }
        });

        if (isNetworkAvailable()){
            getAllOperators();
        }else {
            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.gasPayLayout),"Check Your Internet Connection",false);
        }


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.gas_pay;
    }

    @Override
    protected void connectivityStatusChanged(Boolean isConnected, String message) {

    }

    private void CheckValidate(){
        int Amount=0;
        String GasId="";
        try{
            GasId= GasIdED.getText().toString().trim();
            Amount= Integer.parseInt(AmountED.getText().toString().trim());
        }catch (Exception e){
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(GasIdED.getText().toString().trim())){
            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.gasPayLayout),"Please Enter Gas Id",false);
        }else {
            if (TextUtils.isEmpty(AmountED.getText().toString().trim())){
                BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.gasPayLayout),"Please Enter Amount",false);
            }else {
                if (Amount==0 || Amount<0){
                    BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.gasPayLayout),"Please Enter Amount",false);
                }else {

                    if (TextUtils.isEmpty(OperatorCode) || OperatorCode.equals("0")){

                        BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.gasPayLayout),"Please Select Operator",false);
                    }else {
                        Intent intent=new Intent(GasPay.this,PaymentType.class);
                        overridePendingTransition(R.xml.left_to_right, R.xml.right_to_left);
                        intent.putExtra("RechargePaymentId",GasId);
                        intent.putExtra("Amount",String.valueOf(Amount));
                        intent.putExtra("PaymentType","Bill Payment");
                        intent.putExtra("PaymentFor","Gas");
                        intent.putExtra("RechargeTypeId","5");
                        intent.putExtra("OperatorCode",OperatorCode);
                        intent.putExtra("CircleCode","51");
                        intent.putExtra("OperatorId",OperatorId);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }
    }

    private void getAllOperators(){

        loadingDialog.showDialog(getResources().getString(R.string.loading_message), false);

        ApiService apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        BaseApp.getInstance().getDisposable().add(apiService.getOperators("5")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<OperatorResponse>() {
                    @Override
                    public void onSuccess(OperatorResponse response) {
                        loadingDialog.hideDialog();
                        if (response.isStatus()) {
                            for (int i=0;i<response.getOperators().size();i++){
                                OperatorNameList.add(response.getOperators().get(i).getOperator_name());
                                IdList.add(String.valueOf(response.getOperators().get(i).getId()));
                                OperatorCodeList.add(response.getOperators().get(i).getOperator_code());
                            }

                            ArrayAdapter<String> TransferType= new ArrayAdapter<>(GasPay.this,android.R.layout.simple_spinner_item,OperatorNameList);
                            TransferType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            OperatorSpinner.setAdapter(TransferType);
                        }else {
                            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.gasPayLayout),response.getMessage(),false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(BaseApp.getInstance().toastHelper().getTag(LoginActivity.class), "onError: " + e.getMessage());
                        loadingDialog.hideDialog();
                        BaseApp.getInstance().toastHelper().showApiExpectation(findViewById(R.id.gasPayLayout), true, e);
                    }
                }));

    }

    private void getCustomerOperator(){
        ApiService apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        BaseApp.getInstance().getDisposable().add(apiService.getMobileOperator(GasIdED.getText().toString().trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<CustOperatorResponse>() {
                    @Override
                    public void onSuccess(CustOperatorResponse response) {

                        if (response.isStatus()) {
                            try{
                                OperatorText=response.getOperator().getOperator_name();
                                OperatorCode=response.getOperator().getOperator_code();
                                int indexx=OperatorCodeList.indexOf(OperatorCode);
                                OperatorId=IdList.get(indexx);
                                OperatorSpinner.setSelection(indexx);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(BaseApp.getInstance().toastHelper().getTag(LoginActivity.class), "onError: " + e.getMessage());
                        BaseApp.getInstance().toastHelper().showApiExpectation(findViewById(R.id.mobileRechargeLayout), true, e);
                    }
                }));

    }
}


