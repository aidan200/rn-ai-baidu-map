package com.ai.baidumap;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomControls;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;



public class MapManager extends ViewGroupManager<MapView>{
    private static final String REACT_CLASS = "RCTAiBaiduMapView";
    private ThemedReactContext mReactContext;
    private MapView mapView;

    public MapManager(Context context){
        SDKInitializer.initialize(context);
    }

    public String getName() {
        return REACT_CLASS;
    }

    public MapView createViewInstance(ThemedReactContext context) {
        mReactContext = context;
        mapView =  new MapView(context);
        //去除百度logo--
        View child = mapView.getChildAt(1);
        if (child != null&&
                (child instanceof ImageView ||child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }//--end
        this.setListeners(mapView);
        return mapView;
    }

    public MapView getMapView() {
        return mapView;
    }

    private void setListeners(final MapView mapView){
        BaiduMap map = mapView.getMap();
        map.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            // 地图点击
            @Override
            public void onMapClick(LatLng latLng) {
                WritableMap writableMap =  Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                sendEvent(mapView, "onMapClick", writableMap);
            }
            //poi点点击
            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putString("name", mapPoi.getName());
                writableMap.putString("uid", mapPoi.getUid());
                writableMap.putDouble("latitude", mapPoi.getPosition().latitude);
                writableMap.putDouble("longitude", mapPoi.getPosition().longitude);
                sendEvent(mapView, "onMapPoiClick", writableMap);
                return true;
            }
        });
    }

    @ReactProp(name="zoom")
    public void setZoom(MapView mapView, float zoom) {
        MapStatus mapStatus = new MapStatus.Builder().zoom(zoom).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mapView.getMap().setMapStatus(mapStatusUpdate);
    }

    private void sendEvent(MapView mapView, String eventName,@Nullable WritableMap params) {
        WritableMap event = Arguments.createMap();
        event.putMap("params", params);
        event.putString("type", eventName);
        mReactContext
                .getJSModule(RCTEventEmitter.class)
                .receiveEvent(mapView.getId(),
                        "topChange",
                        event);
    }
}