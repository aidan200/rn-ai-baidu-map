import React, { Component } from 'react'

import {
    requireNativeComponent,
    Platform,
    StyleSheet,
    Text,
    View
  } from 'react-native';

import PropTypes from 'prop-types';

export default class MapView extends Component {
    static propTypes = {
        ...View.propTypes,
        zoom: PropTypes.number,
    }

    static defaultProps = {
        zoom: 10
    };

    constructor() {
        super();
    }
    
    _onChange(event) {
        if (typeof this.props[event.nativeEvent.type] === 'function') {
          this.props[event.nativeEvent.type](event.nativeEvent.params);
        }
      }

    render() {
        console.log(this)
        return (
            <BaiduMapView {...this.props} onChange={this._onChange.bind(this)}/>
        )
    }
}

const BaiduMapView = requireNativeComponent('RCTAiBaiduMapView', MapView);