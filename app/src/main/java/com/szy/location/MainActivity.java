package com.szy.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.SupportMapFragment;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity {
    private LocationManager mLocationMgr;
    private SensorManager mSensorMgr;
    private TextView text;
    private LinearLayout layout;
    private MapView mapView;
    private LocationSource.OnLocationChangedListener locationChangedListener;
    private MyLocationSource myLocationSource;
    private String info;


    public class MyLocationSource implements LocationSource {

        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            //这里我们将地图返回的位置监听保存为当前 Activity 的成员变量
            locationChangedListener = onLocationChangedListener;
            System.out.println("activate");
        }

        @Override
        public void deactivate() {

        }


    }


    private static final double pi = Math.PI;
    private static final double a = 6378245.0;  // 长半轴
    private static final double ee = 0.00669342162296594323;  // 偏心率平方

    public static double transformLat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat +
                0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * pi) + 20.0 *
                Math.sin(2.0 * lng * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * pi) + 40.0 *
                Math.sin(lat / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * pi) + 320 *
                Math.sin(lat * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    private static double transformLng(double lng, double lat) {
        // Replace with your implementation for _transformlng
        return 300.0 + lng + 2.0 * lat + 0.1 * lng * lng +
                0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng))
                + (20.0 * Math.sin(6.0 * lng * pi) + 20.0 * Math.sin(2.0 * lng * pi)) * 2.0 / 3.0
                + (20.0 * Math.sin(lng * pi) + 40.0 * Math.sin(lng / 3.0 * pi)) * 2.0 / 3.0
                + (150.0 * Math.sin(lng / 12.0 * pi) + 300.0 * Math.sin(lng / 30.0 * pi)) * 2.0 / 3.0;
    }

    public static boolean outOfChina(double lng, double lat) {

        return !(lng > 73.66 && lng < 135.05 && lat > 3.86 && lat < 53.55);
    }

    public static double[] wgs84ToGcj02(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new double[]{lng, lat};
        }

        double dlat = transformLat(lng - 105.0, lat - 35.0);
        double dlng = transformLng(lng - 105.0, lat - 35.0);

        double radlat = lat / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);

        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);

        double mglat = lat + dlat;
        double mglng = lng + dlng;

        return new double[]{mglng, mglat};
    }


    public static String angle2string(float angle){
        String[] type = {
                "北偏东",
                "南偏东",
                "南偏西",
                "北偏西",
        };
        Integer t = (int) angle / 90;
        String res = type[t];
        Integer a = (int)angle % 90;
        if (t == 1 || t == 3){
            a = 90 - a;
        }
        res += a+"°";
        return res;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mSensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorMgr.registerListener(
                new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        text = findViewById(R.id.text);
                        text.setText(info + "角度:" + angle2string(event.values[0]));
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                }

                , mSensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME
        );
        String ret = initLocation();
        text = findViewById(R.id.text);
        layout = findViewById(R.id.layout);
        text.setText(ret);
        TencentMapOptions options = new TencentMapOptions();
//        options.setOfflineMapEnable(true);
//        options.set
        mapView = new MapView(this, options);
        layout.addView(mapView);
//        text.setWidth(0);
//        layout.addView(SupportMapFragment);
        //地图上设置定位数据源
        TencentMap tencentMap = mapView.getMap();
        myLocationSource = new MyLocationSource();
        tencentMap.setLocationSource(myLocationSource);
//设置当前位置可见
        tencentMap.setMyLocationEnabled(true);
        MyLocationStyle style = new MyLocationStyle();
        style.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(() -> {
            tencentMap.setMyLocationStyle(style);
        }, 5000);
    }
//
@Override
protected void onStart() {
    super.onStart();
    // 在地图的 onStart 方法中调用
    mapView.onStart();
}

    @Override
    protected void onResume() {
        super.onResume();
        // 在地图的 onResume 方法中调用
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在地图的 onPause 方法中调用
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 在地图的 onStop 方法中调用
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在地图的 onDestroy 方法中调用
        mapView.onDestroy();
    }



    private String initLocation() {
        // 从系统服务中获取定位管理器


        Criteria criteria = new Criteria(); // 创建一个定位准则对象
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 设置定位精确度
        criteria.setAltitudeRequired(true); // 设置是否需要海拔信息
        criteria.setBearingRequired(true); // 设置是否需要方位信息
        criteria.setCostAllowed(false); // 设置是否允许运营商收费
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 设置对电源的需求
        // 获取定位管理器的最佳定位提供者
//        String bestProvider = mLocationMgr.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return "no permission";
        }
        mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                double altitude = location.getAltitude();
                double speed = location.getSpeed();
                double accuracy = location.getAccuracy();
                Date currentDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedDate = sdf.format(currentDate);
                String desc = String.format("更新时间:%s\n经度:%f 纬度:%f\n海拔:%f 速度:%.5fm/s 精度:%f\n",
                formattedDate,longitude,latitude,altitude,speed,accuracy);
                info = desc;
                text.setText(info);
//                text.setText("更新时间:"+formattedDate+"\n经度:"+longitude+"纬度:"+latitude+"\n海拔:"+altitude+"速度："+speed+"m/s 精度:"+accuracy);
                if(locationChangedListener!=null){
                    double[] ret = wgs84ToGcj02(longitude,latitude);
                    location.setLatitude(ret[1]);
                    location.setLongitude(ret[0]);
                    locationChangedListener.onLocationChanged(location);
//                    System.out.println(1);
                }else{
//                    System.out.println(2);
                }
            }
        });
        Location location = mLocationMgr.getLastKnownLocation(mLocationMgr.GPS_PROVIDER);
        if (location == null) {
            return "location null";
        }
//        if(locationChangedListener!=null){
//            locationChangedListener.onLocationChanged(location);
//            System.out.println(1);
//        }else{
//            System.out.println(2);
//        }
        String desc = String.format("%f，%f，" +
                        "%d米，精度为%d米。",
                location.getLongitude(), location.getLatitude(),
                Math.round(location.getAltitude()), Math.round(location.getAccuracy()));
        return desc;
    }
}