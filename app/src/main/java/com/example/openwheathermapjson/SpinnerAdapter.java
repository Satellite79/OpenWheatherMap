package com.example.openwheathermapjson;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.openwheathermapjson.db.CitiesTable;

public class SpinnerAdapter extends SimpleCursorAdapter {

    public SpinnerAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        Cursor  cursor = getCursor();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_item,
                parent,
                false);

        TextView city    = view.findViewById(R.id.spinner_city);
        TextView country = view.findViewById(R.id.spinner_country);

        city.setText(cursor.getString(cursor.getColumnIndex(CitiesTable.COLUMN_NAME)));
        country.setText(cursor.getString(cursor.getColumnIndex(CitiesTable.COLUMN_COUNTRY)));

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
}
