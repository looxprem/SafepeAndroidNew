package com.safepayu.wallet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.safepayu.wallet.BaseActivity;
import com.safepayu.wallet.BaseApp;
import com.safepayu.wallet.R;
import com.safepayu.wallet.adapter.PackageListAdapter;
import com.safepayu.wallet.dialogs.LoadingDialog;
import com.safepayu.wallet.halper.RecyclerLayoutManager;
import com.safepayu.wallet.models.response.PackageListData;

public class WalletAddMoney extends BaseActivity implements PackageListAdapter.OnPackageSelectListener {

    Button AddMoneyBtn,BackBtn;
    String PackageID = "", PackageName = "";
    private LoadingDialog loadingDialog;
    private RecyclerView packageListView;
    private RecyclerLayoutManager layoutManager;
    private PackageListAdapter mAdapter;
    private PackageListData packageListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolbar(false, null, false);

        loadingDialog = new LoadingDialog(this);

        AddMoneyBtn=findViewById(R.id.btn_addMoneyType);
        BackBtn=findViewById(R.id.sendmoney_back_btn);

        ((TextView) findViewById(R.id.tv_packageName)).setText("Package");
        ((TextView) findViewById(R.id.tv_packageAmount)).setText(getResources().getString(R.string.currency) + BaseApp.getInstance().commonUtils().decimalFormat(0d));
        ((TextView) findViewById(R.id.tv_totalAmountPay)).setText(getResources().getString(R.string.currency) + BaseApp.getInstance().commonUtils().decimalFormat(0d));

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        AddMoneyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), PaymentType.class));
                overridePendingTransition(R.xml.left_to_right, R.xml.right_to_left);
                finish();
            }
        });

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.wallet_add_money;
    }

    @Override
    protected void connectivityStatusChanged(Boolean isConnected, String message) {

    }

    @Override
    public void onPackageSelect(int position, PackageListData.Packages selectedPackage) {
        showPackageDetails(selectedPackage);
    }

    public void showPackageDetails(PackageListData.Packages selectedPackage) {
        PackageID = selectedPackage.getId();
        PackageName = selectedPackage.getPackageName();
        ((TextView) findViewById(R.id.tv_packageName)).setText(selectedPackage.getPackageName());
        ((TextView) findViewById(R.id.tv_packageAmount)).setText(getResources().getString(R.string.currency) + BaseApp.getInstance().commonUtils().decimalFormat(selectedPackage.getPackageAmount()));
        ((TextView) findViewById(R.id.tv_tax)).setText(packageListData.getTax().getTaxValue() + "%");
        Double totalPayableAmount = BaseApp.getInstance().commonUtils().getAmountWithTax(selectedPackage.getPackageAmount(), Double.parseDouble(packageListData.getTax().getTaxValue()));
        ((TextView) findViewById(R.id.tv_totalAmountPay)).setText(getResources().getString(R.string.currency) + BaseApp.getInstance().commonUtils().decimalFormat(totalPayableAmount));
    }
}
