package com.safepayu.wallet.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.safepayu.wallet.BaseApp;
import com.safepayu.wallet.R;
import com.safepayu.wallet.api.ApiClient;
import com.safepayu.wallet.models.response.PackageListData;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BuyMembershipAdapter extends RecyclerView.Adapter<BuyMembershipAdapter.BankViewHolder>{

    private Context context;
    private List<PackageListData.BankDetails> mItem;
    private OnBankItemListener onBankItemListener;
     private int row_index = -1;

    public interface OnBankItemListener{
        void onBankItemListerne(int position, PackageListData.BankDetails mData,LinearLayout liBank);

    }

   /* public BuyMembershipAdapter(Context context, List<PackageListData.BankDetails> mItem, OnBankItemListener onBankItemListener) {
        this.context = context;
        this.mItem = mItem;
        this.onBankItemListener = onBankItemListener;
    }*/

    public BuyMembershipAdapter(Context context, List<PackageListData.BankDetails> mItem) {
        this.context = context;
        this.mItem = mItem;
    }

    @NonNull
    @Override
    public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.buy_membership_adapter,parent,false);
        return new BuyMembershipAdapter.BankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BankViewHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return mItem.size();
    }

    public class BankViewHolder   extends RecyclerView.ViewHolder  {
       private TextView  tvAccountName,tvBankName,tvAccountNum,tvIfsccode,tvAn,tvAname,tvIFSC;
       private ImageView imageView;
       private LinearLayout liBank,li_Bank,li;
       private CheckBox checkBox;

        public BankViewHolder(@NonNull View itemView) {

            super(itemView);
            tvAccountName = itemView.findViewById(R.id.tv_accountname);
            imageView = itemView.findViewById(R.id.imagebank);
            liBank = itemView.findViewById(R.id.li_bank_detail);
            li_Bank = itemView.findViewById(R.id.li_bank);
            tvBankName = itemView.findViewById(R.id.tv_bankname);
            tvAccountNum = itemView.findViewById(R.id.tv_account_num);
            tvIfsccode = itemView.findViewById(R.id.tv_ifsccode);
            checkBox = itemView.findViewById(R.id.check_box);
            tvAn = itemView.findViewById(R.id.tv_an);
            tvAname = itemView.findViewById(R.id.tv_aname);
            tvIFSC = itemView.findViewById(R.id.tv_ifsc);
            li = itemView.findViewById(R.id.li);
        }

        public void bindData(final int position) {

            String AcountNumber = mItem.get(position).getBankName();
            if (AcountNumber.equals("UPI")){
                li.setVisibility(View.GONE); }
            else { li.setVisibility(View.VISIBLE); }

            tvAccountName.setText(mItem.get(position).getAccountHolderName());
            tvBankName.setText(mItem.get(position).getBankName());
            tvAccountNum.setText(mItem.get(position).getAccountNumber());
            tvIfsccode.setText(mItem.get(position).getIfscCode());
            //*************set image****************
            Picasso.get().load(ApiClient.ImagePath+ mItem.get(position).getBankLogo()).into(imageView);


            checkBox.setChecked(position==row_index);
            if (position==row_index){
                liBank.setVisibility(View.VISIBLE);
            }
            else {
                liBank.setVisibility(View.GONE);
            }


            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position == row_index) {
                        liBank.setVisibility(View.GONE);
                        checkBox.setChecked(false);

                     //XU   row_index = -1;
                    } else {
                        row_index = position;
                        liBank.setVisibility(View.VISIBLE);
                        notifyDataSetChanged();
                    }
                }
            });
          //  liBank.setVisibility(View.VISIBLE);
           /* if (onBankItemListener != null) {
                onBankItemListener.onBankItemListerne(getLayoutPosition(),mItem.get(getLayoutPosition()),liBank );

            }*/

        /*    li_Bank.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (onBankItemListener != null) {
                        onBankItemListener.onBankItemListerne(getLayoutPosition(),mItem.get(getLayoutPosition()),liBank );

                    }
                    row_index=position;
                    notifyDataSetChanged();
                }
            });*/

          /*  if(row_index==position){
               // liBank.setVisibility(View.VISIBLE);
               // checkBox.setSelected(true);
                checkBox.setChecked(true);
            } else{
                checkBox.setChecked(false);
            // checkBox.setSelected(false);
              ///  liBank.setVisibility(View.GONE);

           }*/
        }

       /* @Override
        public void onClick(View v) {
            if (onBankItemListener != null) {
                onBankItemListener.onBankItemListerne(getLayoutPosition(),mItem.get(getLayoutPosition()),liBank );

            }
        }*/
    }
}
