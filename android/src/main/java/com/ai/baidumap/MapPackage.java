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
    
    private MapManager mapManager;
    
    public MapPackage(Context context){
        mapManager = new MapManager(context);
    }
    

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>() ;
        modules.add(new MapModule(reactContext,mapManager)) ;
        return modules;
    }
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        List<ViewManager> managers = new ArrayList<>();
        managers.add(mapManager);
        return  managers;
    }
}