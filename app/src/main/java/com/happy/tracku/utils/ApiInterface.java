package com.happy.tracku.utils;

import com.happy.tracku.models.events.Result;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface ApiInterface {

    @GET("maps/api/directions/json")
    Single<Result> getDirections(@Query("mode") String mode,
                                 @Query("transit_routing_preference") String routingPreference,
                                 @Query("origin") String origin,
                                 //@Query("destination") String destination,
                                 @Query("key") String apiKey);
}