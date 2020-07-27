package org.first.booketlist;

import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchViewModel extends ViewModel {

    ObservableField<String> editTextInput;
    String responseBody;
    public SearchViewModel(){
        this.editTextInput = new ObservableField<String>("text");
    }
    public ObservableField<String> getEditTextInput(){
        return editTextInput;
    }

    public void searchData(){
        Log.e("!","searchData Start");

        Observable<String> dot = Observable.create(emitter -> {
            try{
                String asd= getData();
                emitter.onNext(asd);
                emitter.onComplete();
            }catch (Exception e){
                emitter.onError(e);
            }
        });
        Observer<String> observer = new Observer<String>(){

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
        dot.subscribeOn(Schedulers.io())
                .subscribe(observer);

    }

    private String getData(){

        String clientId = "OAVpqgXpExdFQglBCJFc";
        String clientSecret = "KTRwVjuTXS";

        String text = editTextInput.get().toString();
        Log.e("text", text);
        try {
            text = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("검색어 인코딩 실패",e);
        }
        String apiURL = "https://openapi.naver.com/v1/search/book?query=" + text + "&display=10";

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);
        responseBody = get(apiURL,requestHeaders);
        Log.e("response : ", responseBody);

        JsonParser parser = new JsonParser();
        try {
            Object obj = parser.parse(responseBody.toString());
            JsonObject jsonObject = (JsonObject) obj;
            JsonArray getArray = (JsonArray) jsonObject.get("items");
            for(int i = 0; i < getArray.size(); i ++){
                    JsonObject object = (JsonObject)getArray.get(i);
                    Log.e("test", String.valueOf(object.get("title")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseBody;
    }
    private static String get(String apiUrl, Map<String, String> requestHeaders){
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 에러 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }
}