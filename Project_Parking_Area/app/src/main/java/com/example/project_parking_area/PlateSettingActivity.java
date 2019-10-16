package com.example.project_parking_area;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

import static java.lang.Math.ceil;

public class PlateSettingActivity extends AppCompatActivity {

    private static final String TAG = "googlemap_example";
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

    private TextView textView1;
    private EditText num_plate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plates_setting);

        sf = getSharedPreferences("sFile",MODE_PRIVATE);
        text = sf.getString("text","");

        num_plate = (EditText)findViewById(R.id.num_plate);
        textView1 = (TextView)findViewById(R.id.resultText1);

        textView1.setText(text);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);

        super.onBackPressed();
    }



    public void savePlate(View v) {
        //여기에다 할 일을 적어주세요.
        SharedPreferences sharedPreferences = getSharedPreferences("sFile",MODE_PRIVATE);

        //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String text = num_plate.getText().toString(); // 사용자가 입력한 저장할 데이터
        editor.putString("text",text); // key, value를 이용하여 저장하는 형태
        //다양한 형태의 변수값을 저장할 수 있다.
        //editor.putString();
        //editor.putBoolean();
        //editor.putFloat();
        //editor.putLong();
        //editor.putInt();
        //editor.putStringSet();

        //최종 커밋
        editor.commit();
        finish();

        startActivity(new Intent(PlateSettingActivity.this, PlateSettingActivity.class));


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


}