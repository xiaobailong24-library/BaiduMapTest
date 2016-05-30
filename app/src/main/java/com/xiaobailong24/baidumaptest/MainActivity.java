package com.xiaobailong24.baidumaptest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private LocationClient mLocationClient = null;
    private BDLocationListener myListener = new MyLocationListener();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private BitmapDescriptor mCurrentMarker;
    private MyLocationData locData;

    String address;
    double latitude, longitude;
    float radius;
    TextView latitudeText, longitudeText, locationText;

    private static final int OK_LOCATION = 0;   //定位成功
    private static final int GPS_LOCATION = 1;
    private static final int NETWORK_LOCATION = 2;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            //            this.obtainMessage();
            switch (msg.what) {
                case OK_LOCATION:
                    latitudeText.setText("本地纬度" + latitude);
                    longitudeText.setText("本地经度" + longitude);
                    mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                    // 设置定位数据
                    mBaiduMap.setMyLocationData(locData);
                    //把地图移动到当前点
                    LatLng ll = new LatLng(latitude, longitude);
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    mBaiduMap.animateMapStatus(u);
                    // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
                    //                    mCurrentMarker = BitmapDescriptorFactory
                    //                            .fromResource(R.drawable.ic_launcher);
                    MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, null);
                    mBaiduMap.setMyLocationConfigeration(config);
                    break;
                case GPS_LOCATION:
                    locationText.setText("本地位置" + address);
                    break;
                case NETWORK_LOCATION:
                    locationText.setText("本地位置" + address);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //在应用程序创建时初始化 SDK引用的Context 全局变量
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 在使用SDK各组件之前初始化context信息，传入ApplicationContext
        // 注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        // 获取地图控件引用
        mMapView = (MapView) findViewById(R.id.id_bmapView);
        mBaiduMap = mMapView.getMap();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //初始化LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数

        initLocation();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "开始定位", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (!mLocationClient.isStarted()) {
                    mLocationClient.start();
                } else {
                    mLocationClient.stop();
                    mLocationClient.start();
                }

            }
        });

        locationText = (TextView) findViewById(R.id.location);
        latitudeText = (TextView) findViewById(R.id.latitude);
        longitudeText = (TextView) findViewById(R.id.longitude);
    }

    /**
     * 配置定位SDK参数
     * 设置定位参数包括：定位模式（高精度定位模式，低功耗定位模式和仅用设备定位模式），
     * 返回坐标类型，是否打开GPS，是否返回地址信息、位置语义化信息、POI信息等等。
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 0;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
    }

    /**
     * 实现BDLocationListener接口
     * BDLocationListener接口有1个方法需要实现： 1.接收异步返回的定位结果，参数是BDLocation类型参数
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlongitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            //发送消息更新UI
            Message msg = mHandler.obtainMessage();
            msg.what = 0;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            radius = location.getRadius();
            // 构造定位数据
            locData = new MyLocationData.Builder()
                    .accuracy(radius)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(latitude)
                    .longitude(longitude).build();
            mHandler.dispatchMessage(msg);
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

                //
                msg.what = GPS_LOCATION;
                address = location.getAddrStr();
                mHandler.dispatchMessage(msg);

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
                msg.what = NETWORK_LOCATION;
                address = location.getAddrStr();
                mHandler.dispatchMessage(msg);
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mMapView = null;
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
            mLocationClient = null;
        }
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        if (mLocationClient.isStarted())
            mLocationClient.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}
