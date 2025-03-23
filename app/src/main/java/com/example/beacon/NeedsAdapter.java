package com.example.beacon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class NeedsAdapter extends BaseAdapter {

    public interface OnDeleteClick {
        void onDelete(String need);
    }

    private final Context context;
    private final List<String> needs;
    private final OnDeleteClick deleteClick;

    public NeedsAdapter(Context context, List<String> needs, OnDeleteClick deleteClick) {
        this.context = context;
        this.needs = needs;
        this.deleteClick = deleteClick;
    }

    @Override
    public int getCount() {
        return needs.size();
    }

    @Override
    public Object getItem(int position) {
        return needs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String need = needs.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_need, parent, false);
        }

        TextView needText = convertView.findViewById(R.id.needText);
        ImageButton deleteBtn = convertView.findViewById(R.id.btnDeleteNeed);

        needText.setText(need);
        deleteBtn.setOnClickListener(v -> deleteClick.onDelete(need));

        return convertView;
    }
}
