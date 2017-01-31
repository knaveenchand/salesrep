package com.frenzin.sales_rep;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.frenzin.sales_rep.adapters.RetailerAdapter;
import com.frenzin.sales_rep.database.DatabaseHandler;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RetailerListActivity extends BaseActivity {

    @Bind(R.id.recyclerView)
    RecyclerView rvRetailers;
    @Bind(R.id.btnAdd)
    Button btnAdd;
    @Bind(R.id.tvNot)
    TextView tvNot;
    RetailerAdapter retailerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retailer_list);

        ButterKnife.bind(this);
        setActionbar();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvRetailers.setLayoutManager(mLayoutManager);

        retailerAdapter = new RetailerAdapter(this, new DatabaseHandler(this).getAllRetailers());
        if (retailerAdapter.getItemCount() != 0) {
            rvRetailers.setAdapter(retailerAdapter);
            rvRetailers.setVisibility(View.VISIBLE);
        } else {
            tvNot.setVisibility(View.VISIBLE);
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RetailerListActivity.this, AddRetailerActivity.class));
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
            }
        });
    }

    @Override
    protected void onRestart() {
        retailerAdapter = new RetailerAdapter(this, new DatabaseHandler(this).getAllRetailers());
        if (retailerAdapter.getItemCount() != 0) {
            rvRetailers.setAdapter(retailerAdapter);
            rvRetailers.setVisibility(View.VISIBLE);
        } else {
            tvNot.setVisibility(View.VISIBLE);
        }
        super.onRestart();
    }

    @Override
    protected void onResume() {
        retailerAdapter = new RetailerAdapter(this, new DatabaseHandler(this).getAllRetailers());
        if (retailerAdapter.getItemCount() != 0) {
            rvRetailers.setAdapter(retailerAdapter);
            rvRetailers.setVisibility(View.VISIBLE);
        } else {
            tvNot.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    private void setActionbar() {
        getSupportActionBar().setTitle("Customers");
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
