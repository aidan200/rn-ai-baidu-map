import {
    requireNativeComponent,
    NativeModules,
    Platform,
    DeviceEventEmitter
  } from 'react-native';


const _module = NativeModules.AiBaiduMapModule;


export default {
    toPosition(latitude,longitude){
        return new Promise((resolve, reject) => {
            try {
                _module.toPosition(latitude,longitude);
            }
            catch (e) {
              reject(e);
              return;
            }
            DeviceEventEmitter.once('toPosition', resp => {
              resolve(resp);
            });
          });
    },
    toPositionAuto(){
        return new Promise((resolve, reject) => {
            try {
                _module.toPositionAuto();
            }
            catch (e) {
              reject(e);
              return;
            }
            DeviceEventEmitter.once('toPositionAuto', resp => {
              resolve(resp);
            });
          });
    }
}