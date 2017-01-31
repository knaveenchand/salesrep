package com.frenzin.sales_rep.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frenzin.sales_rep.AddRetailerActivity;
import com.frenzin.sales_rep.R;
import com.frenzin.sales_rep.common.RetailerItems;
import com.frenzin.sales_rep.common.SessionManager;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RetailerAdapter extends RecyclerView.Adapter<RetailerAdapter.Holder> {

    private Context mcontext;
    private List<RetailerItems> list;

    public RetailerAdapter(Context context, List<RetailerItems> list) {
        this.mcontext = context;
        this.list = list;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        mcontext = parent.getContext();
        View view = LayoutInflater.from(mcontext).inflate(R.layout.list_items_retailers, parent, false);
        return new Holder(view);
    }


    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        final RetailerItems item = list.get(position);
        holder.tvShopName.setText("Customer: " + item.getShop_name());
        holder.tvAddress.setText("Address: " + item.getAddress());

        if (new SessionManager(mcontext).getRole().equalsIgnoreCase("Sales Manager")) {
            holder.ivEdit.setVisibility(View.VISIBLE);
        } else {
            holder.ivEdit.setVisibility(View.GONE);
        }
        holder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectionAvailable()) {
                    Intent i = new Intent(mcontext, AddRetailerActivity.class);
                    i.putExtra("cid", item.getId());
                    i.putExtra("shop_name", item.getShop_name());
                    i.putExtra("owner_name", item.getShop_owner_name());
                    i.putExtra("address", item.getAddress());
                    i.putExtra("lat", item.getLat());
                    i.putExtra("lng", item.getLng());
                    i.putExtra("sub_dist", item.getSub_dist());
                    i.putExtra("dist", item.getDist());
                    i.putExtra("sub_village", item.getSub_village());
                    i.putExtra("village", item.getVillage());
                    i.putExtra("phone", item.getR_phone());
                    i.putExtra("email", item.getR_email());
                    i.putExtra("distance", item.getDistance());
                    mcontext.startActivity(i);
                    ((Activity) mcontext).overridePendingTransition(R.anim.right_in, R.anim.right_out);
                } else {
                    Toast.makeText(mcontext, "Internet connection not available.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @Bind(R.id.tvShopName)
        TextView tvShopName;
        @Bind(R.id.tvAddress)
        TextView tvAddress;
        @Bind(R.id.ivEdit)
        ImageView ivEdit;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    public boolean isConnectionAvailable() {
        ConnectivityManager connectivityManager;
        NetworkInfo networkinfo;
        connectivityManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkinfo = connectivityManager.getActiveNetworkInfo();
        return (networkinfo != null && networkinfo.isConnected());

    }

}


