package com.example.devin.testwebapijson;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    //the URL having the json data
    private static final String CURRENCY_LIST_URL = "https://api.coindesk.com/v1/bpi/supported-currencies.json";
    private static final String PARTIAL_CURRENCY_URL = "https://api.coindesk.com/v1/bpi/currentprice/";

    private static final String TAG = "MAIN ACT";
    private OkHttpClient okHttpClient = new OkHttpClient();
    private String selectedCurrency = "null";

    ArrayList<String> currencyList = new ArrayList<>();
    ArrayList<String> countryList = new ArrayList<>();
    ArrayList<String> ccList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadCurrencies();

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(int i = 0; i < ccList.size(); i++){
                    if(Objects.equals(selectedCurrency, ccList.get(i))){
                        selectedCurrency = currencyList.get(i);
                        Log.d(TAG, ccList.get(i));
                        Log.d(TAG, selectedCurrency);
                        break;
                    }
                }
                Log.d(TAG, selectedCurrency);

                loadSelectedCurrency();
            }
        });

    }


    private void loadSelectedCurrency() {
        Request request = new Request.Builder()
                .url(PARTIAL_CURRENCY_URL + selectedCurrency + ".json")
                .build();
        Log.d(TAG, "in loadSelectedCurrency Method");
        Log.d(TAG, "loadSelectedCurrency: " + PARTIAL_CURRENCY_URL + selectedCurrency + ".json");

//        progressDialog.show();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                Toast.makeText(MainActivity.this, "Error during BPI loading : "
                        + e.getMessage(), LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {

                final String body = response.body().string();
                Log.d(TAG, body);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        progressDialog.dismiss();
                        parseSelectedCurrency(body);
                    }
                });
            }
        });

    }

    private void parseSelectedCurrency(String body) {
        try {
            StringBuilder builder = new StringBuilder();

            JSONObject jsonObject = new JSONObject(body);
            JSONObject timeObject = jsonObject.getJSONObject("time");
            builder.append(timeObject.getString("updated")).append("\n\n");

            JSONObject bpiObject = jsonObject.getJSONObject("bpi");
            JSONObject usdObject = bpiObject.getJSONObject(selectedCurrency);
            builder.append("$ ").append(usdObject.getString("rate")).append(" ").append(selectedCurrency).append("\n");

            Log.d(TAG, "in parseSelectedCurrency Method");

            TextView tv = findViewById(R.id.textViewOutput);
//            Log.d(TAG, "parseSelectedCurrency: " + builder);
            tv.setText(builder.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadCurrencies() {
        Request request = new Request.Builder()
                .url(CURRENCY_LIST_URL)
                .build();

//        progressDialog.show();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(MainActivity.this, "Error during BPI loading : "
                        + e.getMessage(), LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {

                final String body = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        progressDialog.dismiss();
                        parseCurrencyList(body);
                    }
                });
            }
        });

    }

    private void parseCurrencyList(String body) {
        try {

            JSONArray jsonArray = new JSONArray(body);

            Log.d(TAG, "body:      " + body);
            Log.d(TAG, "jsonArray: " + jsonArray.toString());

            int USDI = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
//                Log.d(TAG, jsonArray.get(i).toString());

                currencyList.add(i, jsonArray.getJSONObject(i).getString("currency"));
                countryList.add(i, jsonArray.getJSONObject(i).getString("country"));
                ccList.add(i, currencyList.get(i) + ", " + countryList.get(i));

                if(Objects.equals(currencyList.get(i), "USD")) {
                    USDI = i;
                }

//                Log.d(TAG, "Currency: " + currencyList.get(i));
//                Log.d(TAG, "Country:  " + countryList.get(i));

            }

            Spinner spinner = findViewById(R.id.spinnerCurrency);
            // Spinner click listener
            spinner.setOnItemSelectedListener(this);
            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ccList);
            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // attaching data adapter to spinner
            spinner.setAdapter(dataAdapter);
            spinner.setSelection(USDI);

        } catch (Exception ignored) {}
    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         selectedCurrency = parent.getItemAtPosition(pos).toString();
         Log.d(TAG, parent.getItemAtPosition(pos) + " selected");
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

}
