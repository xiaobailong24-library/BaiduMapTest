package com.xiaobailong24.baidumaptest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.tencent.mars.xlog.Log;


/**
 * Created by xiaobailong24 on 2017/2/28.
 */
public class MapFragment extends Fragment {
    private static final String TAG = MapFragment.class.getName();

    AppCompatActivity mAppCompatActivity;
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;

    MapView mMapView;
    BaiduMap mBaiduMap;
    boolean isFirstLoc = true; // 是否首次定位

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        // 获取地图控件引用
        mMapView = (MapView) view.findViewById(R.id.map_view);
        mBaiduMap = mMapView.getMap();
        mAppCompatActivity = (AppCompatActivity) getActivity();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        initLocation();
    }

    /**
     * 配置定位SDK参数
     * 设置定位参数包括：定位模式（高精度定位模式，低功耗定位模式和仅用设备定位模式），
     * 返回坐标类型，是否打开GPS，是否返回地址信息、位置语义化信息、POI信息等等。
     */
    private void initLocation() {
        mCurrentMode = LocationMode.NORMAL;
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(mAppCompatActivity);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        Log.d(TAG, "onViewCreated: 开始定位。。。。。。。。。。。。。。。。。。");
        if (!mLocClient.isStarted()) {
            mLocClient.start();
        } else {
            mLocClient.stop();
            mLocClient.start();
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            Log.d(TAG, "onReceiveLocation: " + latitude + "," + longitude);
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(latitude)
                    .longitude(longitude).build();
            mBaiduMap.setMyLocationData(locData);

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll1 = new LatLng(latitude + 0.04, longitude - 0.01);
                LatLng ll2 = new LatLng(latitude + 0.04, longitude - 0.04);
                LatLng ll3 = new LatLng(latitude - 0.04, longitude + 0.06);

                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
                        new MapStatus.Builder().target(ll1).build()));
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
                        new MapStatus.Builder().target(ll2).build()));
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
                        new MapStatus.Builder().target(ll3).build()));
                MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, null);
                mBaiduMap.setMyLocationConfigeration(config);
                BitmapDescriptor bitmap1 = BitmapDescriptorFactory.fromResource(R.drawable.around_park_red);
                BitmapDescriptor bitmap2 = BitmapDescriptorFactory.fromResource(R.drawable.around_park_blue);
                BitmapDescriptor bitmap3 = BitmapDescriptorFactory.fromResource(R.drawable.around_park_green);
                OverlayOptions option1 = new MarkerOptions().position(ll1).icon(bitmap1).title(getString(R.string.app_name));
                OverlayOptions option2 = new MarkerOptions().position(ll2).icon(bitmap2).title(getString(R.string.app_name));
                OverlayOptions option3 = new MarkerOptions().position(ll3).icon(bitmap3).title(getString(R.string.app_name));
                mBaiduMap.addOverlay(option1);
                mBaiduMap.addOverlay(option2);
                mBaiduMap.addOverlay(option3);
                mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Log.d(TAG, "onMarkerClick: ");
                        return false;
                    }
                });
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    public void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        isFirstLoc = true;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

}