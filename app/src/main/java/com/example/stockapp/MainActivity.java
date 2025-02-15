package com.example.stockapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity {

    private SectionedRecyclerViewAdapter sectionedAdapter;
    private RecyclerView recyclerView;
    private List<Stock> portfolioList;
    private List<Stock> favoriteList;
    private List<LocalStock> localPortfolio = new ArrayList<>();
    private List<LocalStock> localFavorite = new ArrayList<>();
    private String TAG = "MainActivity";
    private RequestQueue queue;
    private LinearLayout progressBarArea;
    AtomicInteger requestsCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_StockApp);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.i(TAG, "onCreate");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setIconifiedByDefault(true); // 是否預設顯示圖示
        searchView.setSubmitButtonEnabled(false);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(this, SearchableActivity.class); // 指定Activity
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(componentName);
        searchView.setSearchableInfo(searchableInfo);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // initial before request queue
        requestsCounter = new AtomicInteger(2);
        portfolioList = new ArrayList<>();
        favoriteList = new ArrayList<>();

        sectionedAdapter = new SectionedRecyclerViewAdapter();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        progressBarArea = (LinearLayout) findViewById(R.id.progressbar_area);
        progressBarArea.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        queue = Volley.newRequestQueue(this);

        // TODO: for UI test
//        progressBarArea.setVisibility(View.GONE);
//        recyclerView.setVisibility(View.VISIBLE);
//        initLists();
//        setAllSections();
//        makeLocalLists();  //



        // TODO: call real API
        readListsFromLocal();
        fetchLatestPrice();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    private void initLists() {
        for (int i = 0; i < 15; i++) {
            Random random = new Random();
            float price = random.nextFloat() * 5000;
            int shareNum = random.nextInt(300) + 10;   // positive integers for portfolio
            float change = random.nextFloat() * 40 - 20;
            Stock stock = new Stock("AAPL", "Apple Corp.", change, price, shareNum);
            portfolioList.add(stock);
        }
        Stock stock_tmp = new Stock("AAPL", "Apple Corp.", 0, 23.52f, 43);
        portfolioList.add(stock_tmp);


        for (int i = 0; i < 10; i++) {
            Random random = new Random();
            float price = random.nextFloat() * 5000;
            int shareNum = random.nextBoolean() ? random.nextInt(300) + 10 : 0;   // 0 or positive integers
            float change = random.nextFloat() * 40 - 20;
            Stock stock = new Stock("DIS", "Disney Land", change, price, shareNum);
            favoriteList.add(stock);
        }
    }

    private void makeLocalLists() {
        localPortfolio = new ArrayList<>();
        localFavorite = new ArrayList<>();

        LocalStock s1 = new LocalStock("AAPL", "Apple Inc", 1);
        localPortfolio.add(s1);
        LocalStock s2 = new LocalStock("TSLA", "Tesla Inc", 1);
        localPortfolio.add(s2);


        LocalStock s5 = new LocalStock("MSFT", "Microsoft Corporation", 0);
        localFavorite.add(s5);
        LocalStock s6 = new LocalStock("NVDA", "NVIDIA Corporation", 0);
        localFavorite.add(s6);

        writeListsToLocal();
    }

    private void readListsFromLocal() {
        SharedPreferences sharedPreferences = getSharedPreferences("LocalStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson1 = new Gson();
        String favResponse = sharedPreferences.getString("localFavorite", "[]");
        localFavorite = gson1.fromJson(favResponse,
                new TypeToken<List<LocalStock>>() {
                }.getType());
        String portResponse = sharedPreferences.getString("localPortfolio", "[]");
        Gson gson2 = new Gson();
        localPortfolio = gson2.fromJson(portResponse,
                new TypeToken<List<LocalStock>>() {
                }.getType());
        Log.w(TAG, "readListsFromLocal: localPortfolio: " + localPortfolio.toString());
    }

    private void writeListsToLocal() {
        // write
        SharedPreferences sharedPreferences = getSharedPreferences("LocalStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson1 = new Gson();
        Gson gson2 = new Gson();
        String jsonlocalFavorite = gson1.toJson(localFavorite);
        editor.putString("localFavorite", jsonlocalFavorite);
        String jsonlocalPortfolio = gson2.toJson(localPortfolio);
        editor.putString("localPortfolio", jsonlocalPortfolio);
        editor.commit();
    }


    private void fetchLatestPrice() {
        String favTicker = "";
        String portTicker = "";

        for (int i = 0; i < localFavorite.size(); i++) {
            favTicker = favTicker + "," + localFavorite.get(i).getTickerName();
        }
        for (int i = 0; i < localPortfolio.size(); i++) {
            portTicker = portTicker + "," + localPortfolio.get(i).getTickerName();
        }

        BackendUrlMaker portUrlMaker = new BackendUrlMaker(portTicker);
        BackendUrlMaker favUrlMaker = new BackendUrlMaker(favTicker);

        String portUrl = portUrlMaker.getMultiLatestUrl();
        String favUrl = favUrlMaker.getMultiLatestUrl();


        JsonArrayRequest portReq = new JsonArrayRequest(
                Request.Method.GET,
                portUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray portRes) {
                        try {

                            // TODO: set order according to localList
                            for (int j = 0; j < localPortfolio.size(); j++) {
                                LocalStock localItem = localPortfolio.get(j);
                                String companyName = localItem.getCompanyName();
                                String ticker = localItem.getTickerName();
                                int shareNum = localItem.getShareNum();
                                JSONObject jsonStock = portRes.getJSONObject(0); // init as first obj
                                for (int jj = 0; jj < portRes.length(); jj++) {
                                    jsonStock = portRes.getJSONObject(jj);
                                    if (jsonStock.getString("ticker").equals(ticker)) {
                                        break;
                                    } else {
                                        continue;
                                    }
                                }
                                Double currentPrice = jsonStock.getDouble("last");
                                Double prevClose = jsonStock.getDouble("prevClose");
                                Double change = currentPrice - prevClose;
                                Stock stock = new Stock(ticker, companyName, change, currentPrice, shareNum);
                                portfolioList.add(stock);
                            }
                        } catch (JSONException portE) {
                            portE.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError portError) {
                Log.i(TAG, "onErrorResponse: Portfolio fetch FAILED: " + portError.toString());
            }
        });


        // TODO: fetch favReq
        JsonArrayRequest favReq = new JsonArrayRequest(
                Request.Method.GET,
                favUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray favRes) {
                        try {
                            for (int j = 0; j < localFavorite.size(); j++) {
                                LocalStock localItem = localFavorite.get(j);
                                String companyName = localItem.getCompanyName();
                                String ticker = localItem.getTickerName();
                                int shareNum = localItem.getShareNum();
                                JSONObject jsonStock = favRes.getJSONObject(0); // init as first obj
                                for (int jj = 0; jj < favRes.length(); jj++) {
                                    jsonStock = favRes.getJSONObject(jj);
                                    if (jsonStock.getString("ticker").equals(ticker)) {
                                        break;
                                    } else {
                                        continue;
                                    }
                                }
                                Double currentPrice = jsonStock.getDouble("last");
                                Double prevClose = jsonStock.getDouble("prevClose");
                                Double change = currentPrice - prevClose;
                                Stock stock = new Stock(ticker, companyName, change, currentPrice, shareNum);
                                favoriteList.add(stock);
                            }
                        } catch (JSONException favE) {
                            favE.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError favError) {
                Log.i(TAG, "onErrorResponse: Favorites fetch FAILED: " + favError.toString());
            }
        });

        queue.add(portReq);
        queue.add(favReq);
        queue.addRequestFinishedListener(req -> {
            requestsCounter.decrementAndGet();
            if (requestsCounter.get() == 0) {
                setAllSections(); // feed data and set sections

                // TODO: close spinner, set visibility GONE for progress bar, show recyclerView
                progressBarArea.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setAllSections() {
        sectionedAdapter.addSection(new DateSection());
        sectionedAdapter.addSection(new PortfolioSection(portfolioList));
        sectionedAdapter.addSection(new FavoriteSection(favoriteList));
        sectionedAdapter.addSection(new TiingoSection());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sectionedAdapter);
    }
}