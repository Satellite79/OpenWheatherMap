package com.example.openwheathermapjson;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.openwheathermapjson.ForecastModel.ForecastItem;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    List<ForecastItem> forecastItemList;

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView dtTextView;
        TextView descrView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public ForecastAdapter(List<ForecastItem> forecastItemList){
        this.forecastItemList = forecastItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.forecast_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.dtTextView = view.findViewById(R.id.dtText_view);
        viewHolder.descrView = view.findViewById(R.id.description_view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ForecastItem forecastItem = forecastItemList.get(position);
        viewHolder.dtTextView.setText(forecastItem.getDtText());
        viewHolder.descrView.setText(forecastItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return forecastItemList.size();
    }
}
