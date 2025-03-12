package com.happy.tracku.utils;


import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://maps.googleapis.com/"; // Google Maps API base URL

    public static Retrofit getClient() {

        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Or BASIC/HEADERS

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                   // .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io())) // RxJava integration
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}