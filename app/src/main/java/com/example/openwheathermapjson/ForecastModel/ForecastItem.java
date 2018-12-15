package com.example.openwheathermapjson.ForecastModel;

public class ForecastItem {
    private String dtText;
    private String description;

    public ForecastItem(String dtText, String description)
    {
        this.dtText = dtText;
        this.description = description;
    }

    public String getDtText() {
        return dtText;
    }

    public String getDescription() {
        return description;
    }
}
