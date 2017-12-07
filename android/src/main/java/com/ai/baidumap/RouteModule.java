package com.ai.baidumap;

import android.view.textservice.TextInfo;

import com.ai.baidumap.util.BikingRouteOverlay;
import com.ai.baidumap.util.DrivingRouteOverlay;
import com.ai.baidumap.util.MassTransitRouteOverlay;
import com.ai.baidumap.util.OverlayManager;
import com.ai.baidumap.util.TransitRouteOverlay;
import com.ai.baidumap.util.WalkingRouteOverlay;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.core.TaxiInfo;
import com.baidu.mapapi.search.core.VehicleInfo;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

/**
 * Created by Administrator on 2017/11/24.
 */

public class RouteModule extends BaseModule {

    private static final String MODILE = "RCTAiBaiduRouteModule";
    private MapManager mapManager;
    private BaiduMap mBaiduMap;
    //路线规划
    private RoutePlanSearch mSearch;
    private OnGetRoutePlanResultListener routeListener;
    private OverlayManager overlayManager; //覆盖物父类
    private List<MassTransitRouteLine> massTransitRouteLines;//跨城路线集合
    private List<TransitRouteLine> transitRouteLines;//同城路线集合
    private List<WalkingRouteLine> walkingRouteLines;//步行路线集合
    private List<BikingRouteLine> bikingRouteLines;//骑行路线集合
    private List<DrivingRouteLine> drivingRouteLines;//驾车路线集合

    public RouteModule(ReactApplicationContext reactContext,MapManager mapManager) {
        super(reactContext);
        this.mapManager = mapManager;
        this.init();
    }

    @Override
    public String getName() {
        return MODILE;
    }

    //路线规划选择
    @ReactMethod
    public void toNavigationSel(String type){
        if(mSearch!=null){
            mSearch.destroy();
        }
        mSearch = RoutePlanSearch.newInstance();

        PlanNode stNode = PlanNode.withLocation(PoiState.position);
        PlanNode enNode = PlanNode.withLocation(PoiState.mark);

        mSearch.setOnGetRoutePlanResultListener(routeListener);

        WritableMap writableMap = Arguments.createMap();//查询意外回执
        //公交路线
        if(type.equals("hct")){
            if(PoiState.postionCityCode==PoiState.markCityCode){//判断同城
                mSearch.transitSearch(new TransitRoutePlanOption().city(PoiState.markCity).from(stNode).to(enNode));
            }else{
                mSearch.masstransitSearch(new MassTransitRoutePlanOption().from(stNode).to(enNode));
            }
        }else if(type.equals("bx")){
            mSearch.walkingSearch(new WalkingRoutePlanOption().from(stNode).to(enNode));
        }else if(type.equals("qx")){
            mSearch.bikingSearch(new BikingRoutePlanOption().from(stNode).to(enNode));
        }else if(type.equals("jc")){
            mSearch.drivingSearch(new DrivingRoutePlanOption().currentCity(PoiState.markCity).from(stNode).to(enNode));
        }else{
            writableMap.putString("state","err");
            writableMap.putString("msg","未指定路线类型");
            sendEvent("toNavigationSel",writableMap);
        }

    }
    //路线规划设置
    @ReactMethod
    public void toNavigation(String type,int index){
        if(overlayManager!=null){//删除之前的路线
            overlayManager.removeFromMap();
        }
        mBaiduMap = mapManager.getMapView().getMap();

        if(type.equals("hct")){
            if(PoiState.postionCityCode==PoiState.markCityCode) {
                overlayManager = new TransitRouteOverlay(mBaiduMap);
                TransitRouteOverlay overlay = (TransitRouteOverlay) overlayManager;
                overlay.setData(transitRouteLines.get(index));
                overlay.addToMap();
                overlay.zoomToSpan();
            }else{
                overlayManager = new MassTransitRouteOverlay(mBaiduMap);
                MassTransitRouteOverlay overlay = (MassTransitRouteOverlay) overlayManager;
                overlay.setData(massTransitRouteLines.get(index));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        }else if(type.equals("qx")){
            overlayManager = new BikingRouteOverlay(mBaiduMap);
            BikingRouteOverlay overlay = (BikingRouteOverlay) overlayManager;
            overlay.setData(bikingRouteLines.get(index));
            overlay.addToMap();
            overlay.zoomToSpan();
        }else if(type.equals("bx")){
            overlayManager = new WalkingRouteOverlay(mBaiduMap);
            WalkingRouteOverlay overlay = (WalkingRouteOverlay) overlayManager;
            overlay.setData(walkingRouteLines.get(index));
            overlay.addToMap();
            overlay.zoomToSpan();
        }else if(type.equals("jc")){
            overlayManager = new DrivingRouteOverlay(mBaiduMap);
            DrivingRouteOverlay overlay = (DrivingRouteOverlay) overlayManager;
            overlay.setData(drivingRouteLines.get(index));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    public void func(SearchResult result, String type) {
        WritableMap writableMap = Arguments.createMap();
        if(type.equals("hct")) {//公交地铁路线
            TransitRouteResult tr = (TransitRouteResult) result;
            transitRouteLines = tr.getRouteLines();
            WritableArray writableArray = Arguments.createArray();
            int index = 0;
            for (TransitRouteLine transitRouteLine : transitRouteLines) {
                WritableMap wm = Arguments.createMap();
                wm.putInt("distance",transitRouteLine.getDistance());//距离（单位：米）
                wm.putInt("duration",transitRouteLine.getDuration());//耗时 (单位：秒)
                wm.putInt("index",index++);
                WritableArray wa = Arguments.createArray();
                for (TransitRouteLine.TransitStep transitStep : transitRouteLine.getAllStep()) {
                    WritableMap wm2 = Arguments.createMap();
                    wm2.putString("stepType",transitStep.getStepType().toString());
                    wm2.putString("instructions",transitStep.getInstructions());
                    wm2.putInt("distance",transitStep.getDistance());
                    wm2.putInt("duration",transitStep.getDuration());
                    if(transitStep.getVehicleInfo()!=null){
                        wm2.putString("entrance",transitStep.getEntrance().getTitle());
                        wm2.putString("exit",transitStep.getEntrance().getTitle());
                        VehicleInfo vehicleInfo = transitStep.getVehicleInfo();
                        wm2.putString("vehicleName",vehicleInfo.getTitle());
                        wm2.putInt("vehiclePassStationNum",vehicleInfo.getPassStationNum());
                        wm2.putInt("vehicleZonePrice",vehicleInfo.getZonePrice());
                    }
                    wa.pushMap(wm2);
                }
                wm.putArray("transitSteps",wa);
                writableArray.pushMap(wm);
            }
            writableMap.putArray("Lines",writableArray);
            writableMap.putString("state","ok");

        }else if(type.equals("hc")){
            MassTransitRouteResult mr = (MassTransitRouteResult) result;
            massTransitRouteLines = mr.getRouteLines();
            WritableArray writableArray = Arguments.createArray();
            int index = 0;
            for (MassTransitRouteLine massTransitRouteLine : massTransitRouteLines) {
                WritableMap wm = Arguments.createMap();
                wm.putInt("distance",massTransitRouteLine.getDistance());//距离（单位：米）
                wm.putInt("duration",massTransitRouteLine.getDuration());//耗时 (单位：秒)
                wm.putString("title",massTransitRouteLine.getTitle());
                wm.putString("time",massTransitRouteLine.getArriveTime());
                wm.putInt("index",index++);
                WritableArray wa = Arguments.createArray();
                for (List<MassTransitRouteLine.TransitStep> transitSteps : massTransitRouteLine.getNewSteps()) {
                    for (MassTransitRouteLine.TransitStep transitStep : transitSteps) {
                        WritableMap wm2 = Arguments.createMap();
                        wm2.putString("stepType",transitStep.getVehileType().toString());
                        wm2.putString("instructions",transitStep.getInstructions());
                        wm2.putInt("distance",transitStep.getDistance());
                        wm2.putInt("duration",transitStep.getDuration());
                        wa.pushMap(wm2);
                    }
                }
                wm.putArray("transitSteps",wa);
                writableArray.pushMap(wm);
            }
            writableMap.putArray("Lines",writableArray);
            writableMap.putString("state","ok");
        }else if(type.equals("qx")){//骑行路线
            BikingRouteResult bt = (BikingRouteResult) result;
            bikingRouteLines = bt.getRouteLines();
            WritableArray writableArray = Arguments.createArray();
            int index = 0;
            for (BikingRouteLine bikingRouteLine : bikingRouteLines) {
                WritableMap wm = Arguments.createMap();
                wm.putInt("distance",bikingRouteLine.getDistance());
                wm.putInt("duration",bikingRouteLine.getDuration());
                wm.putInt("index",index++);
                WritableArray wa = Arguments.createArray();
                for (BikingRouteLine.BikingStep bikingStep : bikingRouteLine.getAllStep()) {
                    WritableMap wm2 = Arguments.createMap();
                    wm2.putString("exitInstructions",bikingStep.getExitInstructions());//获取路段出口指示信息
                    wm2.putString("instructions",bikingStep.getInstructions());//获取路段整体指示信息
                    wm2.putString("turnType",bikingStep.getTurnType());//获取行驶转向方向（如"直行", "左前方转弯"）
                    wa.pushMap(wm2);
                }
                wm.putArray("bikingSteps",wa);
                writableArray.pushMap(wm);
            }
            writableMap.putArray("Lines",writableArray);
            writableMap.putString("state","ok");
        }else if(type.equals("bx")){//步行路线
            WalkingRouteResult wt = (WalkingRouteResult) result;
            walkingRouteLines = wt.getRouteLines();
            WritableArray writableArray = Arguments.createArray();
            int index = 0;
            for (WalkingRouteLine walkingRouteLine : walkingRouteLines) {
                WritableMap wm = Arguments.createMap();
                wm.putInt("distance",walkingRouteLine.getDistance());
                wm.putInt("duration",walkingRouteLine.getDuration());
                wm.putInt("index",index++);
                WritableArray wa = Arguments.createArray();
                for (WalkingRouteLine.WalkingStep walkingStep : walkingRouteLine.getAllStep()) {
                    WritableMap wm2 = Arguments.createMap();
                    wm2.putString("exitInstructions",walkingStep.getExitInstructions());//获取路段出口指示信息
                    wm2.putString("instructions",walkingStep.getInstructions());//获取路段整体指示信息
                    wa.pushMap(wm2);
                }
                wm.putArray("walkingSteps",wa);
                writableArray.pushMap(wm);
            }
            writableMap.putArray("Lines",writableArray);
            writableMap.putString("state","ok");
        }else if(type.equals("jc")){//驾车路线
            DrivingRouteResult dt = (DrivingRouteResult) result;
            drivingRouteLines = dt.getRouteLines();
            WritableArray writableArray = Arguments.createArray();
            int index = 0;
            for (DrivingRouteLine drivingRouteLine : drivingRouteLines) {
                WritableMap wm = Arguments.createMap();
                wm.putInt("distance",drivingRouteLine.getDistance());
                wm.putInt("duration",drivingRouteLine.getDuration());
                wm.putInt("index",index++);
                WritableArray wa = Arguments.createArray();
                for (DrivingRouteLine.DrivingStep drivingStep : drivingRouteLine.getAllStep()) {
                    WritableMap wm2 = Arguments.createMap();
                    wm2.putString("exitInstructions",drivingStep.getExitInstructions());//获取路段出口指示信息
                    wm2.putString("instructions",drivingStep.getInstructions());//获取路段整体指示信息

                    wa.pushMap(wm2);
                }
                wm.putArray("drivingSteps",wa);
                writableArray.pushMap(wm);
            }
            writableMap.putArray("Lines",writableArray);
            writableMap.putString("state","ok");
        }else if(type.equals("err")){
            writableMap.putString("state","err");
            writableMap.putString("msg","线路规划失败");
        }
        sendEvent("toNavigationSel",writableMap);
    }

    private void init(){
        routeListener = new OnGetRoutePlanResultListener() {
            //步行路线结果回调
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    func(walkingRouteResult,"bx");
                }else{
                    func(walkingRouteResult,"err");
                }
            }
            //换乘路线结果回调
            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
                if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    func(transitRouteResult,"hct");
                }else{
                    func(transitRouteResult,"err");
                }
            }
            //跨城公共交通路线结果回调
            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
                if (massTransitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    func(massTransitRouteResult,"hc");
                }else{
                    func(massTransitRouteResult,"err");
                }
            }
            //驾车路线结果回调
            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    func(drivingRouteResult,"jc");
                }else{
                    func(drivingRouteResult,"err");
                }
            }
            //室内路线规划回调
            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }
            //骑行路线结果回调
            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
                if (bikingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    func(bikingRouteResult,"qx");
                }else{
                    func(bikingRouteResult,"err");
                }
            }
        };
    }

}
