package tech.doujiang.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import tech.doujiang.launcher.util.IsonlineClient;
import tech.doujiang.launcher.util.SelfDestruction;

public class ReportLocationService extends Service {
    private static String TAG = "ReportLocationService";
    String username = "";
    Boolean isonline = true;
    Boolean infoerase = false;
    Boolean islost = false;
    public double longitude = 0.0;
    public double latitude = 0.0;
    private LocationClient locationClient = null;

    private int[] code = new int[]{61, 65, 66, 161};

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient =  new LocationClient(getApplicationContext());
        initLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "ReportLoc destroyed");
        locationClient.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (username == "") {
            if (intent == null || intent.getStringExtra("username") == "") {
                return Service.START_STICKY;
            }
            username = intent.getStringExtra("username");
        }
        //位置变化明显，会回调位置
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (location.getLocType() == 61 || location.getLocType() == 65
                        || location.getLocType() == 66 || location.getLocType() == 161) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    IsonlineClient isonlineClient = new IsonlineClient();
                    isonlineClient.onlineconnect(username, isonline, infoerase, islost, longitude, latitude);
                    Log.e(TAG, "longitude: " + longitude + " latitude: " + latitude);
                } else {
                    Log.e("ErrorCode: ", Integer.toString(location.getLocType()));
                }
            }

            @Override
            public void onConnectHotSpotMessage(String s, int i) {

            }
        });
        locationClient.start();
        Log.e(TAG, "ReportLocation created");
        Log.e(TAG, "ReportLocationSerivice is Started");
        return START_STICKY;
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000 * 60;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(false);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        locationClient.setLocOption(option);
    }
}



