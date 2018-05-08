package com.vonergy.asyncTask;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vonergy.connection.ConnectionConstants;
import com.vonergy.connection.Requester;
import com.vonergy.model.Consumption;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConsumptionAsync extends AsyncTask<Integer, Void, List<Consumption>> {

    private ProgressBar bar;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (bar != null) {
            bar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected List<Consumption> doInBackground(Integer... params) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ss").create();//2018-04-15T18:47:13
        String api = "";

        switch (params[0]) {
            case Consumption.consumptionInRealTime:
                api = ConnectionConstants.consumptionInRealTime;
                break;
            case Consumption.consumptionPerHour:
                api = ConnectionConstants.consumptionPerHour;
                break;
            case Consumption.dailyConsumption:
                api = ConnectionConstants.dailyConsumption;
                break;
            case Consumption.weeklyConsumption:
                api = ConnectionConstants.weeklyConsumption;
                break;
            case Consumption.monthlyConsumption:
                api = ConnectionConstants.monthlyConsumption;
                break;
            case Consumption.annualConsumption:
                api = ConnectionConstants.annualConsumption;
                break;
        }
        try {
            String response = new Requester().get(api);
            List<Consumption> list;
            if (params[0] == Consumption.consumptionInRealTime) {
                Consumption consumption = new Consumption();
                list = new ArrayList<>();
                consumption.setPower(Float.parseFloat(response));
                list.add(consumption);
            } else {
                Type listType = new TypeToken<ArrayList<Consumption>>() {
                }.getType();
                list = gson.fromJson(response, listType);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Consumption> listConsumption) {
        super.onPostExecute(listConsumption);
        if (bar != null) {
            bar.setVisibility(View.GONE);
        }
    }

    public void setProgressBar(ProgressBar bar) {
        this.bar = bar;
    }
}
