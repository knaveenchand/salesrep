package com.frenzin.sales_rep.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.frenzin.sales_rep.MainActivity;
import com.frenzin.sales_rep.R;
import com.frenzin.sales_rep.common.RetailerItems;

import java.util.List;

public class ListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<RetailerItems> retailerItemsList;
    private RadioButton selected = null;
    private int mSelectedPosition = -1;

    SelectedItem selectedItem;

    public void setSelectedItem(SelectedItem selectedItem) {
        this.selectedItem = selectedItem;
    }


    public ListAdapter(Activity activity, List<RetailerItems> retailerItemsList) {
        this.activity = activity;
        this.retailerItemsList = retailerItemsList;
    }

    @Override
    public int getCount() {
        return retailerItemsList.size();
    }

    @Override
    public Object getItem(int location) {
        return retailerItemsList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_items, null);

        TextView tvIndex = (TextView) convertView.findViewById(R.id.tvName);
        final RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.radioButton);
        final RetailerItems model = retailerItemsList.get(position);
        tvIndex.setText(model.getShop_name());

        radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    MainActivity.pos = position;
                }
            }
        });

        radioButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (position != mSelectedPosition && selected != null) {
                    selected.setChecked(false);
                }
                mSelectedPosition = position;
                selected = (RadioButton) view;
                selectedItem.onItemSelected();
            }
        });
        if (mSelectedPosition != position) {
            radioButton.setChecked(false);
        } else {
            radioButton.setChecked(true);
            selected = radioButton;
        }

        return convertView;
    }

    public interface SelectedItem {
        public void onItemSelected();
    }
}