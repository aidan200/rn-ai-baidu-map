import {
    requireNativeComponent,
    NativeModules,
    Platform,
    DeviceEventEmitter
  } from 'react-native';


const _module = NativeModules.AiBaiduRouteModule;


export default {
    toNavigationSel(type){
        return new Promise((resolve, reject) => {
            try {
                _module.toNavigationSel(type);
            }
            catch (e) {
              reject(e);
              return;
            }
            DeviceEventEmitter.once('toNavigationSel', resp => {
              resolve(resp);
            });
        });    
    },
    toNavigation(type,index){
        _module.toNavigation(type,index);
    }
}
