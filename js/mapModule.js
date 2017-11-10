import {
    requireNativeComponent,
    NativeModules,
    Platform,
    DeviceEventEmitter
  } from 'react-native';


const _module = NativeModules.AiBaiduMapModule;


export default {
    dingwei(latitude,longitude){
        return new Promise((resolve, reject) => {
            try {
                _module.toPosition(latitude,longitude);
                console.log("cccccccccccccccccccccccccc")
            }
            catch (e) {
              reject(e);
              return;
            }
            DeviceEventEmitter.once('toPosition', resp => {
                console.log("ggggggggggggggggggggggg")
              resolve(resp);
            });
          });
    },
    dingweiauto(){
        return new Promise((resolve, reject) => {
            try {
                _module.toPositionAuto();
                console.log("cccccccccccccccccccccccccc2")
            }
            catch (e) {
              reject(e);
              return;
            }
            DeviceEventEmitter.once('toPositionAuto', resp => {
                console.log("ggggggggggggggggggggggg2")
              resolve(resp);
            });
          });
    }
}