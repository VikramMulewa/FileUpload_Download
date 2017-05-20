package com.example.vikram.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;
    Button b, b1;
    ImageView img;
    String filename;

    Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b = (Button) findViewById(R.id.button);
        b1 = (Button) findViewById(R.id.button2);
        img = (ImageView) findViewById(R.id.image);
//
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
//
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(fileUri);
            }
        });


    }

    private void uploadFile(Uri fileUri) {

        FileUploadService service = ServiceGenerator.createService(FileUploadService.class);

        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = FileUtils.getFile(this, fileUri);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        // add another part within the multipart request
        String name = "vikram";
        RequestBody namedescription = RequestBody.create(okhttp3.MultipartBody.FORM, name);

        String email = "vikrammulewa@gmail.com";
        RequestBody emaildescription = RequestBody.create(okhttp3.MultipartBody.FORM, email);

        String password = "mypwd123";
        RequestBody pwddescription = RequestBody.create(okhttp3.MultipartBody.FORM, password);

        String gender = "male";
        RequestBody genderdescription = RequestBody.create(okhttp3.MultipartBody.FORM, gender);

        // finally, execute the request
        Call<JsonObject> call = service.upload(namedescription,emaildescription,pwddescription,genderdescription, body);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                JsonObject j = response.body();
                Log.d("ress",j.get("response").toString());
                JsonObject j1 = j.get("response").getAsJsonObject();

                Log.d("ress_nxt",j1.get("user_data").toString());
                try {
                    JSONObject jsonObj = new JSONObject(j1.get("user_data").toString());
                    Log.d("ress","hope so done");
                    String n = jsonObj.getString("name");
                    Log.d("ress_name", n);
                    String e = jsonObj.getString("email");
                    Log.d("ress_email", e);
                    String i = jsonObj.getString("image");
                    Log.d("ress_image", i);
                    String g = jsonObj.getString("gender");
                    Log.d("ress_gender", g);

                    if(i !=null)
                    {
                        downloadimg(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }




//                String n = j1.get("name").getAsString();
//                Log.d("ress_name", n);
//                String e = j1.get("email").getAsString();
//                Log.d("ress_email", e);
//                String i = j1.get("image").getAsString();
//                Log.d("ress_image", i);
//                String g = j1.get("gender").getAsString();
//                Log.d("ress_gender", g);

                Log.d("Upload",response.body().toString());
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });

    }

    private void downloadimg(String i) {
        filename = i;
        String url = "http://192.168.1.14/test/media/images/"+i;
        FileDownloadService downloadService = ServiceGenerator.createService(FileDownloadService.class);

        Call<ResponseBody> call = downloadService.downloadFileWithDynamicUrlSync(url);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("err", "server contacted and has file");

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            boolean writtenToDisk = writeResponseBodyToDisk(response.body());

                            Log.d("err", "file download was a success? " + writtenToDisk);
                            return null;
                        }
                    }.execute();

                    //Log.d("err", "file download was a success? " + writtenToDisk);
                } else {
                    Log.d("err", "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("err", "error");
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);
                Log.d("fffff",futureStudioIconFile.getAbsolutePath().toString());

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("err", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();
            fileUri = data.getData();
            Log.d("imagepath", String.valueOf(imageUri));
            img.setImageURI(imageUri);
        }
    }
}
