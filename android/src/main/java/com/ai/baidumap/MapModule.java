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
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

/**
 * Created by Administrator on 2017/11/6.
 */

public class MapModule extends BaseModule{

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
        this.init();
        myDingListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                ding(bdLocation.getLatitude(),bdLocation.getLongitude(),"auto");
                locationClient.stop();
            }
        };
    }

    @Override
    public String getName() {
        return MODILE;
    }

    //设置定位点
    public void ding(double latitude, double longitude,final String type){
        mBaiduMap = mapManager.getMapView().getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //设置地图显示状态（位置）
        MapStatus.Builder builder = new MapStatus.Builder();
        LatLng latLng = new LatLng(latitude,longitude);
        builder.target(latLng);
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
        //搜索周边监听执行
        ReverseGeoCodeOption reverseGeoCodeOption = new ReverseGeoCodeOption();
        reverseGeoCodeOption.location(latLng);

        if(type.equals("hand")){
            geoCoder2.reverseGeoCode(reverseGeoCodeOption);
        }else if(type.equals("auto")){
            geoCoder1.reverseGeoCode(reverseGeoCodeOption);
        }
    }

    //指定坐标定位
    @ReactMethod
    public void toPosition(double latitude, double longitude){
        ding(latitude,longitude,"hand");
    }

    //设置mark点
    @ReactMethod
    public void setMark(double latitude, double longitude){
        LatLng latLng = new LatLng(latitude,longitude);
        mBaiduMap.clear();
        //构建Marker图标
        BitmapDescriptor bitmap2 = BitmapDescriptorFactory.fromResource(R.mipmap.mak);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(latLng)
                .icon(bitmap2).animateType(MarkerOptions.MarkerAnimateType.grow);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
        //设置地图显示状态（位置）
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(latLng);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        //搜索周边监听执行
        ReverseGeoCodeOption reverseGeoCodeOption = new ReverseGeoCodeOption();
        reverseGeoCodeOption.location(latLng);
        geoCoder3.reverseGeoCode(reverseGeoCodeOption);
    }

    //gps定位
    @ReactMethod
    public void toPositionAuto(){
        locationClient = new LocationClient(reactApplicationContext.getApplicationContext());//声明LocationClient类
        locationClient.registerLocationListener(myDingListener);//注册监听函数
        LocationClientOption option = new LocationClientOption();//定位设置对象
        //option.setIsNeedAddress(true);获取当前的位置信息
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(0);//周期性请求定位，1秒返回一次位置
        locationClient.setLocOption(option);
        locationClient.start();
    }


    //搜索监听---自动定位用
    private GeoCoder geoCoder1 = GeoCoder.newInstance();
    //搜索监听---手动定位用
    private GeoCoder geoCoder2 = GeoCoder.newInstance();
    //搜索监听(附近poi点)---mark用
    private GeoCoder geoCoder3 = GeoCoder.newInstance();

    private void init(){
        //初始化自动定位附近检索监听
        geoCoder1 = GeoCoder.newInstance();
        geoCoder1.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                LatLng latLng = reverseGeoCodeResult.getLocation();
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                writableMap.putString("address",reverseGeoCodeResult.getAddress());
                GeoCodeBack("start","toPositionAuto",reverseGeoCodeResult,writableMap);
            }
        });
        //初始化手动定位附近检索监听
        geoCoder2 = GeoCoder.newInstance();
        geoCoder2.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                LatLng latLng = reverseGeoCodeResult.getLocation();
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                writableMap.putString("address",reverseGeoCodeResult.getAddress());
                GeoCodeBack("start","toPosition",reverseGeoCodeResult,writableMap);
            }
        });
        //初始化mark点附近检索监听
        geoCoder3.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                WritableMap writableMap =  Arguments.createMap();
                WritableArray writableArray = Arguments.createArray();
                LatLng latLng = reverseGeoCodeResult.getLocation();
                String address = reverseGeoCodeResult.getAddress();
                List<PoiInfo> poiInfos = reverseGeoCodeResult.getPoiList();
                for (PoiInfo poiInfo : poiInfos) {
                    WritableMap map = Arguments.createMap();
                    map.putString("address",poiInfo.address);
                    map.putDouble("latitude",poiInfo.location.latitude);
                    map.putDouble("longitude",poiInfo.location.longitude);
                    writableArray.pushMap(map);
                }
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                writableMap.putString("address", address);
                writableMap.putArray("poiInfos", writableArray);
                GeoCodeBack("end","setMark",reverseGeoCodeResult,writableMap);
            }
        });
    }

    //检索附件回调 （设置线路规划开始或结束点）
    private void GeoCodeBack(String type,String eventName,ReverseGeoCodeResult reverseGeoCodeResult,WritableMap writableMap){
        LatLng latLng = reverseGeoCodeResult.getLocation();
        if(type.equals("end")){
            //设置规划路线终点
            PoiState.mark = latLng;
            PoiState.markCity = reverseGeoCodeResult.getAddressDetail().city;
            PoiState.markCityCode = reverseGeoCodeResult.getCityCode();
        }else if(type.equals("start")){
            //设置规划路线起点
            PoiState.position = latLng;
            PoiState.postionCityCode = reverseGeoCodeResult.getCityCode();
            PoiState.postionCity = reverseGeoCodeResult.getAddressDetail().city;
        }
        sendEvent(eventName, writableMap);
    }

}
