package com.safepayu.wallet.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.safepayu.wallet.BaseActivity;
import com.safepayu.wallet.BaseApp;
import com.safepayu.wallet.R;
import com.safepayu.wallet.api.ApiClient;
import com.safepayu.wallet.api.ApiService;
import com.safepayu.wallet.dialogs.LoadingDialog;
import com.safepayu.wallet.models.request.BuyPackage;
import com.safepayu.wallet.models.response.BuyPackageResponse;
import com.safepayu.wallet.utils.PasscodeClickListener;
import com.safepayu.wallet.utils.PasscodeDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MemberBankAddPackages  extends BaseActivity implements PasscodeClickListener {

    private Button BackBtn,SubmitBtn;
    private TextView DateTv,ChooseImageBtn;
    private EditText tv_referencenumber,tv_amountpaid,UPIorbankaccount;
    private Spinner BankTypeSpinner,TransferTypeSpinner;
    private String[] TransferTypeCategories,bankcategories;
    private  String TransferTypeText="",BankNameText="",PackageID="",TransactionType="",textBase64="", Amount="";
    private LoadingDialog loadingDialog;
    private boolean CheckNetConnection=false;
    private ImageView imageView;
    private LinearLayout ImageLayout;
    BuyPackage buyPackage;
    LinearLayout LinearSPinnerAmount;
    private static final int PICK_IMAGE_CAMERA = 223;
    private static final int PICK_IMAGE_GALLERY = 623;
    private Bitmap bitmap;
    private File destination = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolbar(false, null, false);

        buyPackage=new BuyPackage();
        loadingDialog = new LoadingDialog(this);
        BackBtn=findViewById(R.id.mem_back_btn);
        SubmitBtn=findViewById(R.id.btn_packages);
        DateTv=findViewById(R.id.tv_dateofbirth);
        tv_referencenumber=findViewById(R.id.tv_referencenumber);
        tv_amountpaid=findViewById(R.id.tv_amountpaid);
        UPIorbankaccount=findViewById(R.id.tv_amountpaidfrom);
        BankTypeSpinner=findViewById(R.id.spinner_banktype);
        TransferTypeSpinner=findViewById(R.id.spinner_amountpaidto);
        LinearSPinnerAmount=findViewById(R.id.linearSPinnerAmount);
        imageView=findViewById(R.id.image_challan);
        ChooseImageBtn=findViewById(R.id.chooseImageBtn);
        ImageLayout=findViewById(R.id.imageLayout);


        try{
            TransactionType=getIntent().getStringExtra("TransactionType");
            PackageID=getIntent().getStringExtra("PackageID");
            Amount=getIntent().getStringExtra("Amount");
            tv_amountpaid.setText(Amount);
            tv_amountpaid.setEnabled(false);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (TransactionType.equalsIgnoreCase("1")){
            DateTv.setVisibility(View.GONE);
            tv_referencenumber.setVisibility(View.GONE);
            UPIorbankaccount.setVisibility(View.GONE);
            tv_amountpaid.setVisibility(View.GONE);
            TransferTypeSpinner.setVisibility(View.GONE);
            LinearSPinnerAmount.setVisibility(View.GONE);
            ImageLayout.setVisibility(View.GONE);
        }else {
            DateTv.setVisibility(View.VISIBLE);
            tv_referencenumber.setVisibility(View.VISIBLE);
            UPIorbankaccount.setVisibility(View.VISIBLE);
            tv_amountpaid.setVisibility(View.VISIBLE);
            TransferTypeSpinner.setVisibility(View.VISIBLE);
            LinearSPinnerAmount.setVisibility(View.VISIBLE);
            ImageLayout.setVisibility(View.VISIBLE);
        }


    //    bankcategories = new String[]{"Select Bank", "Hixson Technologies AXIS BANK", "Hixson Technologies UNION BANK", "UPI (safepe@upi)"};
        bankcategories = new String[]{"Select Bank", "918020100795727 AXIS BANK", "5824101010050412 UNION BANK", "UPI (safepe@upi)"};
        ArrayAdapter<String> TransferType= new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,bankcategories);
        TransferType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        BankTypeSpinner.setAdapter(TransferType);

        TransferTypeCategories = new String[]{"Payment Mode", "Net Banking", "Bank Challan", "Bank Transfer NEFT/RTGS/IMPS", "UPI Transfer"};
        ArrayAdapter<String> TransferTypeSpinnerAdapter= new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,TransferTypeCategories);
        TransferTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        TransferTypeSpinner.setAdapter(TransferTypeSpinnerAdapter);

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        DateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker();
            }
        });

        BankTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                BankNameText=BankTypeSpinner.getItemAtPosition(i).toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        TransferTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                TransferTypeText=TransferTypeSpinner.getItemAtPosition(i).toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        SubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (BaseApp.getInstance().sharedPref().getString(BaseApp.getInstance().sharedPref().EMAIL_VERIFIED).equalsIgnoreCase("0")){
                    BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.memberBankAddPackages),"Please Goto Your Profile and Verify Your Email First",true);
                }else {
                    CheckValidate();
                }

            }
        });



        // *********select image from camera and gallery*********
        ImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(MemberBankAddPackages.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(MemberBankAddPackages.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                } else {
                    ActivityCompat.requestPermissions(MemberBankAddPackages.this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, 100);
                }
            }
        });
    }



    public void datePicker(){
        final Calendar c = Calendar.getInstance();
        int  mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int  mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        DateTv.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void CheckValidate(){
        String DateText=DateTv.getText().toString().trim();
        String ReferenceNumber=tv_referencenumber.getText().toString().trim();
        String Amount=tv_amountpaid.getText().toString().trim();
        String UPI=UPIorbankaccount.getText().toString().trim();

        if (TransactionType.equalsIgnoreCase("1")){

            if (BankNameText.equalsIgnoreCase("Select Bank")) {
                BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.memberBankAddPackages), "Please Select Bank", false);
            }else {
                buyPackage.setTransaction_type(TransactionType);
                buyPackage.setPackage_id(PackageID);
                buyPackage.setBuy_date("");
                buyPackage.setPayment_mode("Wallet");
                buyPackage.setRefrence_no("");
                buyPackage.setDocument_attached("");
                buyPackage.setPaid_to_account(BankNameText);
                buyPackage.setPaid_from_account("");
                if (BaseApp.getInstance().sharedPref().getString(BaseApp.getInstance().sharedPref().PASSCODE) == null || BaseApp.getInstance().sharedPref().getString(BaseApp.getInstance().sharedPref().PASSCODE).equals("")) {
                    startActivity(new Intent(MemberBankAddPackages.this, CreatePassCodeActivity.class));
                } else {
                    PasscodeDialog passcodeDialog = new PasscodeDialog(MemberBankAddPackages.this, MemberBankAddPackages.this, "");
                    passcodeDialog.show();
                }
            }
        }else {
            if (TextUtils.isEmpty(DateText)){
                BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.memberBankAddPackages), "Please Select Date", false);
            }else {
                if (TextUtils.isEmpty(ReferenceNumber)){
                    tv_referencenumber.setError("Please Enter Reference Number");
                }else {
                    if (TextUtils.isEmpty(Amount)){
                        tv_amountpaid.setError("Please Enter Amount");
                    }else {
                        if (TextUtils.isEmpty(UPI)){
                            UPIorbankaccount.setError("Please Enter UPI");
                        }else {
                            if (BankNameText.equalsIgnoreCase("Select Bank")){
                                BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.memberBankAddPackages), "Please Select Bank", false);

                            }else {
                                if (TransferTypeText.equalsIgnoreCase("Payment Mode")){
                                    BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.memberBankAddPackages), "Please Select Payment Mode", false);
                                }else {

                                    if (TextUtils.isEmpty(textBase64)){
                                        BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.memberBankAddPackages), "Please Choose Image/Screenshot", false);
                                    }else {
                                        buyPackage.setTransaction_type(TransactionType);
                                        buyPackage.setPackage_id(PackageID);
                                        buyPackage.setBuy_date(DateText);
                                        buyPackage.setPayment_mode(TransferTypeText);
                                        buyPackage.setRefrence_no(ReferenceNumber);
                                        buyPackage.setDocument_attached("data:image/png;base64,"+textBase64);
                                        buyPackage.setPaid_to_account(BankNameText);
                                        buyPackage.setPaid_from_account(UPI);
                                        if (CheckNetConnection){
                                            BuyPackageMethod(buyPackage);
                                        }else {
                                            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.memberBankAddPackages), "Check Internet Connection", false);

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private void BuyPackageMethod(BuyPackage buyPackage){

        loadingDialog.showDialog(getResources().getString(R.string.loading_message), false);

        ApiService apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        BaseApp.getInstance().getDisposable().add(apiService.buyPackage(buyPackage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<BuyPackageResponse>() {
                    @Override
                    public void onSuccess(BuyPackageResponse response) {
                        loadingDialog.hideDialog();
                        if (response.isStatus()) {
                            Toast.makeText(MemberBankAddPackages.this, response.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.memberBankAddPackages),response.getMessage(),false);

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(BaseApp.getInstance().toastHelper().getTag(LoginActivity.class), "onError: " + e.getMessage());
                        loadingDialog.hideDialog();
                        BaseApp.getInstance().toastHelper().showApiExpectation(findViewById(R.id.memberBankAddPackages), true, e);
                    }
                }));

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.member_bank_add_packages;
    }

    @Override
    protected void connectivityStatusChanged(Boolean isConnected, String message) {
        if (isConnected){
            CheckNetConnection=true;
        }else {
            CheckNetConnection=false;
        }
    }

    @Override
    public void onPasscodeMatch(boolean isPasscodeMatched) {

        if (isPasscodeMatched){
            if (CheckNetConnection){
                BuyPackageMethod(buyPackage);
            }else {
                BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.memberBankAddPackages), "Check Internet Connection", false);
            }
        }else {
            BaseApp.getInstance().toastHelper().showSnackBar(findViewById(R.id.memberBankAddPackages), "Invalid Passcode", false);
        }

    }

/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

               // Bitmap original = BitmapFactory.decodeStream(getAssets().open("1024x768.jpg"));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                imageView.setImageBitmap(decoded);
                textBase64=ConvertToBase64(decoded);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
*/

    private String ConvertToBase64(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encoded;
    }

    private void selectImage() {
        try {
            PackageManager pm = MemberBankAddPackages.this.getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, MemberBankAddPackages.this.getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};
              AlertDialog.Builder builder = new AlertDialog.Builder(MemberBankAddPackages.this);
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        if (options[item].equals("Take Photo")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        } else if (options[item].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_GALLERY);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else
                Toast.makeText(MemberBankAddPackages.this, "Camera Permission error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MemberBankAddPackages.this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_CAMERA) {
            try {
                bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);


                destination = new File(Environment.getExternalStorageDirectory() + "/" +
                        getString(R.string.app_name), "IMG_" + System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Drawable d = new BitmapDrawable(getResources(), bitmap);
                imageView.setImageDrawable(d);
                textBase64 = ConvertToBase64(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_GALLERY) {
            /*if (data != null) {
                Uri uri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                    // Bitmap original = BitmapFactory.decodeStream(getAssets().open("1024x768.jpg"));
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
                    Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                    imageView.setImageBitmap(decoded);
                    textBase64=ConvertToBase64(decoded);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/

            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(MemberBankAddPackages.this.getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    textBase64 = ConvertToBase64(bitmap);
                    Drawable d = new BitmapDrawable(getResources(), bitmap);
                    imageView.setImageDrawable(d);
                }
            }

        }
    }



}
