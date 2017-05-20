package com.example.vikram.sample;


import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
public interface FileUploadService {
    @Multipart
    @POST("test/script.php")
    Call<JsonObject> upload(
            @Part("name") RequestBody namedescription,
            @Part("email") RequestBody emaildescription,
            @Part("password") RequestBody pwddescription,
            @Part("gender") RequestBody genderdescription,
            @Part MultipartBody.Part file
    );
}



