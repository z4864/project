package com.example.project_parking_area;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.ceil;

public class ParkingAreaActivity extends AppCompatActivity {

    private static String TAG = "ParkingAreaActivity";

    private static final String TAG_JSON="ParkingArea";
    private static final String TAG_NUMBER = "NUMBER";
    private static final String TAG_CAR_NUMBER_PLATE = "CAR_NUMBER_PLATE";
    private static final String TAG_X1 = "X1";
    private static final String TAG_Y1 = "Y1";
    private static final String TAG_X2 = "X2";
    private static final String TAG_Y2 = "Y2";

    private static final String TAG_JSON_FEE="fee";
    private static final String TAG_HOUR = "hour";
    private static final String TAG_MINUTE = "minute";
    private static final String TAG_SECOND = "second";

    String mJsonString_fee;
    String hour;
    String minute;
    String second;

    int count = 0;
    int hour_int = 0;
    int minute_int = 0;
    int total = 0;

    SharedPreferences sf;
    String text;


    private TextView mTextViewResult;
    private ArrayList<HashMap<String, String>> mArrayList;
    ListView mlistView;
    String mJsonString;
    String plate;

    private class Parking extends View {

        int count = 0;
        int total = 0;
        String enable = "주차 가능 대수";

        public Parking(Context context) {
            super(context);
        }

        @Override //부모가 가진 onDraw와 오버라이딩
        public void onDraw(Canvas c){
            //안드로이드에서 제공되는 canvas를 인수 값으로 준다.
            Paint paint= new Paint(); //그리기 도구 1
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setColor(Color.BLACK); //그리고 도구 1의 컬러

            Paint paint2= new Paint(); //그리기 도구 2
            paint2.setColor(Color.BLACK); //그리기 도구 2의 컬러

            Paint paint3= new Paint(); //그리기 도구 3
            paint3.setColor(Color.YELLOW); //그리기 도구 3의 컬러



            for(int i = 0; i < mArrayList.size(); i++) {
                HashMap<String,String> hashMap = mArrayList.get(i);
                int x_po1 = Integer.parseInt(hashMap.get(TAG_X1));
                int y_po1 = Integer.parseInt(hashMap.get(TAG_Y1));
                int x_po2 = Integer.parseInt(hashMap.get(TAG_X2));
                int y_po2 = Integer.parseInt(hashMap.get(TAG_Y2));

                c.drawRect(x_po1, y_po1, x_po2, y_po2, paint);

                count++;
                total++;

                if(hashMap.get(TAG_CAR_NUMBER_PLATE).equals(plate)) {
                    c.drawRect(x_po1, y_po1, x_po2, y_po2 , paint3);
                    count--;
                }
                else if(!hashMap.get(TAG_CAR_NUMBER_PLATE).equals("0")) {
                    c.drawRect(x_po1, y_po1, x_po2, y_po2, paint2);
                    count--;
                }
            }

            Paint text = new Paint();
            text.setAntiAlias(true);
            text.setColor(Color.BLACK);
            text.setTextSize(100);

            c.drawText(enable + "  " + Integer.toString(count) + "  /  " + Integer.toString(total), 200, 100, text);

            //정확하게 원이나 사각형의 모양이 나오지 않음 장비 해상도를 높이기 위해서는 보정작업이 필요하다.
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTextViewResult = (TextView)findViewById(R.id.textView_main_result);
        mlistView = (ListView) findViewById(R.id.listView_main_list);
        mArrayList = new ArrayList<>();

        sf = getSharedPreferences("sFile",MODE_PRIVATE);
        text = sf.getString("text","");

        GetData task = new GetData();
        task.execute("http://drgcream.ipdisk.co.kr:8000/apps/xe/load_DB.php");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SharedPreferences sf = getSharedPreferences("sFile",MODE_PRIVATE);
        //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
        plate = sf.getString("text","");

        Parking mv = new Parking(this);
        setContentView(mv);
    }

    public void checking_fee()
    {
        GetFee task = new GetFee();
        String url = "http://drgcream.ipdisk.co.kr:8000/apps/xe/fee_calculate.php?PLATE=" + text;
        task.execute(url);

        total = 0;

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(count == 1) {

            hour_int = Integer.parseInt(hour);
            minute_int = Integer.parseInt(minute);

            minute_int = minute_int + hour_int*60;

            if(minute_int-30 > 0) {
                total += 1000;
                total += ceil((minute_int-30)/10+1)*500;
                builder.setMessage("주차 요금       " + total);
            }
            else if(minute_int-30 <= 0) {
                total = 1000;
            }

            builder.setTitle("요금 확인");
            builder.setMessage("주차 경과 시간    " + hour + " : " + minute + " : " + second + "\n주차 요금           " + total);
        }
        else if(count == 0) {
            builder.setMessage("업데이트 중입니다. 잠시후 다시 시도해주세요.");
            count++;
        }

        builder.show();
    }

    private class GetFee extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mJsonString_fee = result;
            jsonToArray_fee();

        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();

            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    private void jsonToArray_fee(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString_fee);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON_FEE);

            JSONObject item = jsonArray.getJSONObject(0);

            hour = Integer.toString((int)item.getDouble(TAG_HOUR));
            minute = Integer.toString((int)item.getDouble(TAG_MINUTE));
            second = Integer.toString((int)item.getDouble(TAG_SECOND));

        } catch (JSONException e) {
            Log.d(TAG, "jsonToArray : ", e);
        }
    }

    private class GetData extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d(TAG, "response  - " + result);

            if (result == null){
                mTextViewResult.setText(errorString);
            }
            else {
                mJsonString = result;
                jsonToArray();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();

            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    private void jsonToArray(){
        ArrayList<HashMap<String, String>> tArrayList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                int number = item.getInt(TAG_NUMBER);
                String number_plate = item.getString(TAG_CAR_NUMBER_PLATE);
                int x_point1 = item.getInt(TAG_X1);
                int y_point1 = item.getInt(TAG_Y1);
                int x_point2 = item.getInt(TAG_X2);
                int y_point2 = item.getInt(TAG_Y2);

                HashMap<String,String> hashMap = new HashMap<>();

                hashMap.put(TAG_NUMBER, Integer.toString(number));
                hashMap.put(TAG_CAR_NUMBER_PLATE, number_plate);
                hashMap.put(TAG_X1, Integer.toString(x_point1));
                hashMap.put(TAG_Y1, Integer.toString(y_point1));
                hashMap.put(TAG_X2, Integer.toString(x_point2));
                hashMap.put(TAG_Y2, Integer.toString(y_point2));

                tArrayList.add(hashMap);
            }

            mArrayList = tArrayList;

        } catch (JSONException e) {
            Log.d(TAG, "jsonToArray : ", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //액션버튼을 클릭했을때의 동작
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.recall_home) {
            Toast.makeText(this, "지도로 이동", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.setting_plate) {
            Toast.makeText(this, "번호판 설정", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, PlateSettingActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.checking_fee) {
            Toast.makeText(this, "요금 확인", Toast.LENGTH_SHORT).show();
            checking_fee();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //액션바 숨기기
    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);

        super.onBackPressed();
    }


}
