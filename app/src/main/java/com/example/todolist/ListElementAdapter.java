package com.example.todolist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

public class ListElementAdapter extends ArrayAdapter<ToDoElement> {

    private Context mContext;
    private int mResource;

    public ListElementAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String title = Objects.requireNonNull(getItem(position)).getTitle();
        String date = Objects.requireNonNull(getItem(position)).getDeadline();
        boolean status = Objects.requireNonNull(getItem(position)).isStatus();
        int priority = Objects.requireNonNull(getItem(position)).getPriority();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvTitle = convertView.findViewById(R.id.tvItemTitle);
        tvTitle.setText(title);

        TextView tvDate = convertView.findViewById(R.id.tvItemDeadline);
        tvDate.setText(date);

        CheckBox cbStatus = convertView.findViewById(R.id.cbItemStatus);
        if(status) {
            cbStatus.setText("Zrobione");
        } else {
            cbStatus.setText("Do zrobienia");
        }
        cbStatus.setEnabled(false);
        cbStatus.setChecked(status);


        TextView tvPriority = convertView.findViewById(R.id.tvItemPriority);
        String strPriority = "";

        if(priority == 1) {
            strPriority = "Bardzo ważne";
        } else if(priority == 2) {
            strPriority = "Ważne";
        } else if(priority == 3) {
            strPriority = "Normalne";
        } else if(priority == 4) {
            strPriority = "Mało ważne";
        }

        tvPriority.setText(strPriority);

        return convertView;
    }
}
