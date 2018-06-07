package com.vonergy.view.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.util.OnPrintTickLabel;
import com.vonergy.R;
import com.vonergy.asyncTask.ConsumptionAsync;
import com.vonergy.model.Consumption;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;

public class ConsumoTempoRealFragment extends Fragment {

    private static final String CHAVE_TIPO_CONSUMO = "CHAVE_TIPO_CONSUMO";

    private int tipoConsumo;

    private String formatoData;

    private ProgressDialog pDialog;

    private float maxValue;

    private Handler mHandler;

    private ProgressBar mProgressBar;

    SpeedView speedometer;

    public ConsumoTempoRealFragment() {
        // Required empty public constructor
    }

    public static ConsumoTempoRealFragment newInstance(int tipoConsumo) {
        ConsumoTempoRealFragment fragment = new ConsumoTempoRealFragment();
        Bundle args = new Bundle();
        args.putInt(CHAVE_TIPO_CONSUMO, tipoConsumo);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_consumo_tempo_real, container, false);
        speedometer = layout.findViewById(R.id.tempView);

        Bundle args = getArguments();
        tipoConsumo = args.getInt(CHAVE_TIPO_CONSUMO, 0);

        mHandler = new Handler();

        return layout;
    }

    private void updatePower() {
        //float maxValue = Float.MIN_VALUE, value;
        float value;
        maxValue = 30;

        try {
            ConsumptionAsync task = new ConsumptionAsync();
            task.setProgressBar(mProgressBar);
            List<Consumption> listConsumption = task.execute(tipoConsumo).get();
            if (listConsumption != null && !listConsumption.isEmpty()) {
                value = listConsumption.get(0).getPower();
                maxValue = Math.max(maxValue, value);
                setupGauger(value, 0, Math.round(maxValue));
            } else {
                dialogError(getResources().getString(R.string.noConsumption));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            dialogError(getResources().getString(R.string.consumptionError));
        } catch (ExecutionException e) {
            e.printStackTrace();
            dialogError(getResources().getString(R.string.consumptionError));
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            updatePower();
            int mInterval = 10000;
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };



    public void setupGauger(float tempValue, int minTemp, int maxTemp) {
        int hundred = Math.round(maxValue * 1);
        int seventyFive = (int) Math.round(maxValue * 0.75);
        int fifty = (int) Math.round(maxValue * 0.5);
        int twentyFive = (int) Math.round(maxValue * 0.25);

//        mTemperature.setText(String.format(getResources().getString(R.string.kilowatt), tempValue));
        speedometer.setMinSpeed(minTemp);
        speedometer.setMaxSpeed(maxTemp);
        speedometer.speedTo(tempValue);
        speedometer.setLowSpeedPercent(50);
        speedometer.setMediumSpeedPercent(75);
        speedometer.setTicks(0, twentyFive, fifty, seventyFive, hundred);
        speedometer.setUnit("kWh");
        speedometer.setOnPrintTickLabel(new OnPrintTickLabel() {
            @Override
            public String getTickLabel(int tickPosition, int tick) {
                return String.format(Locale.getDefault(), "%d kWh", tick);
            }
        });
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void dialogError(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.error));
        builder.setMessage(msg);

        builder.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        startRepeatingTask();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopRepeatingTask();
//        unbinder.unbind();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


}