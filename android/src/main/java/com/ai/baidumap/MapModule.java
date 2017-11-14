package com.ai.baidumap;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

/**
 * Created by Administrator on 2017/11/6.
 */

public class MapModule extends BaseModule {

    private static final String MODILE = "RCTAiBaiduMapModule";
    private MapManager mapManager;
    private BaiduMap mBaiduMap;
    private ReactApplicationContext reactApplicationContext;
    private LocationClient locationClient;
    private BDLocationListener myDingListener;

    public MapModule(ReactApplicationContext reactContext,MapManager mapManager) {
        super(reactContext);
        this.mapManager = mapManager;
        this.reactApplicationContext = reactContext;
    }

    @Override
    public String getName() {
        return MODILE;
    }


    public void ding(double latitude, double longitude){
        mBaiduMap = mapManager.getMapView().getMap();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        //设置地图显示状态（位置）
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(new LatLng(latitude,longitude));
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(0)//精度
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(0).latitude(latitude)
                .longitude(longitude).build();

        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);
        //自定义图标
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.mipmap.poi);
        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true,mCurrentMarker);
        mBaiduMap.setMyLocationConfiguration(config);


    }

    //指定坐标定位
    @ReactMethod
    public void toPosition(double latitude, double longitude){
        ding(latitude,longitude);
        //通知回调
        WritableMap writableMap = Arguments.createMap();
        writableMap.putDouble("latitude", latitude);
        writableMap.putDouble("longitude", longitude);
        sendEvent("toPosition",writableMap);
    }

    //gps定位
    @ReactMethod
    public void toPositionAuto(){
        locationClient = new LocationClient(reactApplicationContext.getApplicationContext());//声明LocationClient类
        myDingListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                ding(bdLocation.getLatitude(),bdLocation.getLongitude());
                locationClient.stop();
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", bdLocation.getLatitude());
                writableMap.putDouble("longitude", bdLocation.getLongitude());
                sendEvent("toPositionAuto",writableMap);
            }
        };
        locationClient.registerLocationListener(myDingListener);//注册监听函数
        LocationClientOption option = new LocationClientOption();//定位设置对象
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(0);//周期性请求定位，1秒返回一次位置
        locationClient.setLocOption(option);
        locationClient.start();
    }
}
