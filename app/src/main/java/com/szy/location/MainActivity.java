package com.szy.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
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


public class MainActivity extends Activity {
    private LocationManager mLocationMgr;
    private TextView text;
    private LinearLayout layout;
    private MapView mapView;
    private LocationSource.OnLocationChangedListener locationChangedListener;
    private MyLocationSource myLocationSource;


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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
                text.setText("经度:"+longitude+"纬度:"+latitude+"\n海拔:"+altitude+"速度："+speed+"精度:"+accuracy);
                if(locationChangedListener!=null){
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