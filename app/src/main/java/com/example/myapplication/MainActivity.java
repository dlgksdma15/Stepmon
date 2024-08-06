package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 필요한 권한들 정의
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            Fitness.getRecordingClient(this,
                            Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                    .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                    .addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.i(TAG, "Successfully subscribed!");
                                    } else {
                                        Log.w(TAG, "There was a problem subscribing.", task.getException());
                                    }
                                }
                            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 로그인 성공시
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                readData();
            }
        }
    }


    private void readData() {
        final Calendar cal = Calendar.getInstance();
        Date now = Calendar.getInstance().getTime();
        cal.setTime(now);

        // 시작 시간
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), 6, 0, 0);
        long startTime = cal.getTimeInMillis();

        // 종료 시간
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), 22, 0, 0);
        long endTime = cal.getTimeInMillis();

        Fitness.getHistoryClient(this,
                        Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readData(new DataReadRequest.Builder()
                        .read(DataType.TYPE_STEP_COUNT_DELTA) // Raw 걸음 수
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse response) {
                        DataSet dataSet = response.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
                        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

                        for (DataPoint dp : dataSet.getDataPoints()) {
                            Log.i(TAG, "Data point:");
                            Log.i(TAG, "\tType: " + dp.getDataType().getName());

                            Log.i(TAG, "\tStart: " + (dp.getStartTime(TimeUnit.MILLISECONDS)));
                            Log.i(TAG, "\tEnd: " + (dp.getEndTime(TimeUnit.MILLISECONDS)));
                            for (Field field : dp.getDataType().getFields()) {
                                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                            }
                        }
                    }
                });
    }


}

//public class MainActivity extends AppCompatActivity implements SensorEventListener {
//
//    SensorManager sensorManager;
//    Sensor stepCountSensor;
//    TextView stepCountView;
//    Button resetButton;
//
//    // 현재 걸음 수
//    int currentSteps = 0;
//
//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        stepCountView = findViewById(R.id.stepCountView);
//        resetButton = findViewById(R.id.resetButton);
//
//
//        // 활동 퍼미션 체크
//        if(ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
//
//            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
//        }
//
//        // 걸음 센서 연결
//        // * 옵션
//        // - TYPE_STEP_DETECTOR:  리턴 값이 무조건 1, 앱이 종료되면 다시 0부터 시작
//        // - TYPE_STEP_COUNTER : 앱 종료와 관계없이 계속 기존의 값을 가지고 있다가 1씩 증가한 값을 리턴
//        //
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
//
//        // 디바이스에 걸음 센서의 존재 여부 체크
//        if (stepCountSensor == null) {
//            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
//        }
//
//        // 리셋 버튼 추가 - 리셋 기능
//        resetButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 현재 걸음수 초기화
//                currentSteps = 0;
//                stepCountView.setText(String.valueOf(currentSteps));
//
//            }
//        });
//
//    }
//
//
//    public void onStart() {
//        super.onStart();
//        if(stepCountSensor !=null) {
//            // 센서 속도 설정
//            // * 옵션
//            // - SENSOR_DELAY_NORMAL: 20,000 초 딜레이
//            // - SENSOR_DELAY_UI: 6,000 초 딜레이
//            // - SENSOR_DELAY_GAME: 20,000 초 딜레이
//            // - SENSOR_DELAY_FASTEST: 딜레이 없음
//            //
//            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST);
//        }
//    }
//
//
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        // 걸음 센서 이벤트 발생시
//        if(event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
//
//            if(event.values[0]==1.0f){
//                // 센서 이벤트가 발생할때 마다 걸음수 증가
//                currentSteps++;
//                stepCountView.setText(String.valueOf(currentSteps));
//            }
//
//        }
//
//    }
//
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//    }
//}