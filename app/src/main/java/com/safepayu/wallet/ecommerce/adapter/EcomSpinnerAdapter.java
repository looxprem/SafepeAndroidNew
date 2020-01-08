package com.safepayu.wallet.ecommerce.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.safepayu.wallet.R;
import com.safepayu.wallet.api.ApiClient;
import com.safepayu.wallet.models.response.OperatorResponse;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class EcomSpinnerAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflter;
    List<OperatorResponse.OperatorsBean> mOperList = new ArrayList<>();


    public EcomSpinnerAdapter(Context context, List<OperatorResponse.OperatorsBean> mOperList) {
        this.context = context;
        this.mOperList = mOperList;
    }

    @Override
    public int getCount() {
        return mOperList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.spinner_ecom_layout, null);
        ImageView icon = convertView.findViewById(R.id.imageView);
        TextView names =  convertView.findViewById(R.id.textView);

        names.setText(mOperList.get(position).getOperator_name());

        if (position==0){
            names.setText("Select Operator");
        }

        try {
            if (!TextUtils.isEmpty(mOperList.get(position).getImage())){
                Picasso.get().load(ApiClient.ImagePath+mOperList.get(position).getImage()).into(icon);
            }else {
             //   icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_list_new));
            }
        }catch (Exception e){
          //  icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_list_new));
            e.printStackTrace();
        }

        return convertView;
    }
}