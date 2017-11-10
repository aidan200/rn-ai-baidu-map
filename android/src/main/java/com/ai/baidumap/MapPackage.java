package com.ai.baidumap;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.baidu.mapapi.map.TextureMapView;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;



public class MapPackage implements ReactPackage {

    MapManager mapManager;
    //TextureMapView mapView;

    public MapPackage(){
        //mapManager.initSDK(context);
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        System.out.println("-------------执行Modu---------------------");
        List<NativeModule> modules = new ArrayList<>() ;
        modules.add(new MapModule(reactContext,mapManager)) ;
        //return Collections.emptyList();
        return modules;
    }
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        System.out.println("-------------执行Mana---------------------");
        List<ViewManager> managers = new ArrayList<>();
        mapManager = new MapManager(reactContext);
        managers.add(mapManager);
        return  managers;
//        return Arrays.<ViewManager>asList(
//                mapManager
//        );
    }
}