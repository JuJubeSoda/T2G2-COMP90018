package com.example.myapplication.myPlantsData;

import android.content.Context;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.PlantDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyGardenDataManager {

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    private final ApiService apiService;

    public MyGardenDataManager(Context context) {
        this.apiService = ApiClient.create(context);
    }

    public void fetchLikedPlants(DataCallback<List<PlantDto>> callback) {
        Call<ApiResponse<List<PlantDto>>> call = apiService.getLikedPlantsByUser();
        call.enqueue(new Callback<ApiResponse<List<PlantDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlantDto>>> call, Response<ApiResponse<List<PlantDto>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<PlantDto>> body = response.body();
                    if (body.getCode() == 200 && body.getData() != null) {
                        callback.onSuccess(body.getData());
                    } else {
                        callback.onError(body.getMessage());
                    }
                } else {
                    callback.onError("HTTP error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlantDto>>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void likePlant(int plantId, DataCallback<String> callback) {
        Call<ApiResponse<String>> call = apiService.likePlant(plantId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> body = response.body();
                    if (body.getCode() == 200) {
                        callback.onSuccess(body.getMessage());
                    } else {
                        callback.onError(body.getMessage());
                    }
                } else {
                    callback.onError("HTTP error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void unlikePlant(int plantId, DataCallback<String> callback) {
        Call<ApiResponse<String>> call = apiService.unlikePlant(plantId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> body = response.body();
                    if (body.getCode() == 200) {
                        callback.onSuccess(body.getMessage());
                    } else {
                        callback.onError(body.getMessage());
                    }
                } else {
                    callback.onError("HTTP error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}


