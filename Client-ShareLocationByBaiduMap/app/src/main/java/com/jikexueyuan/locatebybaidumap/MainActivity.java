package com.jikexueyuan.locatebybaidumap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements ServiceConnection {

    MapView mMapView = null;
    private BaiduMap mBaiduMap;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private LocationClientOption option;
    private boolean isFirstLocation = true;
    private MyService.Binder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
//设置为普通地图
//        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
//定位服务端设置及开启
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();
        mLocationClient.start();

        Intent intent = new Intent(MainActivity.this, MyService.class);
        bindService(intent, MainActivity.this, Context.BIND_AUTO_CREATE);
            socketIndex = Math.random() * 3;//用随机数初始化本地客户端的标志
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        // 当不需要定位图层时关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.unRegisterLocationListener(myListener);    //注销监听函数
        mLocationClient.stop();   //停止定位服务

        unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    private void initLocation() {
        option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setNeedDeviceDirect(true);
        mLocationClient.setLocOption(option);
    }

    /**
     * Created by fangc on 2016/3/28.
     */
    public class MyLocationListener implements BDLocationListener {


        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
            // 开启定位图层
            mBaiduMap.setMyLocationEnabled(true);

            // 构造定位数据
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            direction = location.getDirection();//获取方位信息


            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(direction).latitude(latitude)
                    .longitude(longitude).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
//    设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
//           BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
//                   .fromResource(R.drawable.icon_openmap_focuse_mark);
//           MyLocationConfiguration config = new MyLocationConfiguration(null, true, mCurrentMarker);
//           mBaiduMap.setMyLocationConfigeration(config);

            // 当不需要定位图层时关闭定位图层
            //        mBaiduMap.setMyLocationEnabled(false);

            //第一次定位时，将地图位置移动到当前位置
            LatLng ll = null;
            if (isFirstLocation) {
                isFirstLocation = false;
                ll = new LatLng(latitude, longitude);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);

            }
            //当该客户端有定位数据，TODO: 2016/5/28 当定位失败时，getLocationDescribe返回null
            if (location.getLocationDescribe() != null && binder != null ) {
                binder.sendData("[" + socketIndex + ":" + latitude + ":" + longitude + ":" + direction + ":]");//当定位字符串有变化时同步改变Myservice的data
                localLocateData = "[" + socketIndex + ":" + latitude + ":" + longitude + ":" + direction + ":]";//将新定位字符串赋予定位数据（localLocateData）
            }

        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (MyService.Binder) service;

        binder.getService().setCallBack(new MyService.ICallBack() {
            @Override
            public void onDateChange(String data) {//直接使用一个新创建的线程来执行UI线程的资源的话，是不行的，是android的
                // 安全机制，UI线程是不允许其他辅助线程来修改其资源的；此处需要Handler。
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("data", data);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        });

    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            otherLocateData = msg.getData().getString("data");
//            System.out.println("收到的其它客户端"+otherLocateData);
//解析其它客户端的定位数据：
            Pattern overallPattern = Pattern.compile("\\[\\d+\\.\\d+:\\d+\\.\\d+:\\d+\\.\\d+:[-]?\\d+\\.\\d+:\\]");
//            String otherLocateData = "[1:52.123465453:332.245345321:-1.0:]";//测试用
            Matcher matcher = overallPattern.matcher(otherLocateData);
            Pattern pattern = Pattern.compile("([-]?\\d+[.]?\\d{0,20})(:)");

            if (matcher.find()) {
                Matcher littleMatcher = pattern.matcher(matcher.group());
                int tempIndex = 0;
                while (littleMatcher.find()) {
                    switch (tempIndex) {
                        case 0:
                            otherSocketIndex = Double.parseDouble(littleMatcher.group(1));
                            break;
                        case 1:
                            otherLatitude = Double.parseDouble(littleMatcher.group(1));
                            break;
                        case 2:
                            otherLongitude = Double.parseDouble(littleMatcher.group(1));
                            break;
                        case 3:
                            otherDirection = Double.parseDouble(littleMatcher.group(1));
                            break;
                    }
                    tempIndex++;
                }
//                //准备 marker 的图片

                int socketTag = 0;
                int socketIcon = 0;
                switch ((int) otherSocketIndex+1) {
                    case 1:
                        socketTag = R.drawable.icon_other_focuse_mark1;
                        socketIcon = R.drawable.icon1;
                        break;
                    case 2:
                        socketTag = R.drawable.icon_other_focuse_mark2;
                        socketIcon = R.drawable.icon2;
                        break;
                    case 3:
                        socketTag = R.drawable.icon_openmap_focuse_mark;
                        socketIcon = R.drawable.icon3;
                        break;
                }
                bitmap = BitmapDescriptorFactory.fromResource(socketTag);
                //准备 marker option 添加 marker 使用

                MarkerOptions markerOptions;
                if (otherDirection != -1.0) {
                    markerOptions = new MarkerOptions().icon(bitmap).position(new LatLng(otherLatitude, otherLongitude))
                            .flat(true).rotate((float) (360 - otherDirection));
                } else {
                    markerOptions = new MarkerOptions().icon(bitmap).position(new LatLng(otherLatitude, otherLongitude))
                            .flat(true);
                }

                //获取添加的 marker 这样便于后续的操作
                Marker marker = (Marker) mBaiduMap.addOverlay(markerOptions);
                marker.setPerspective(true);//近大远小
                System.out.println("收到的其它客户端" + otherSocketIndex + "：" + latitude + " " + longitude + " " + direction);
            }
        }
    };

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private String otherLocateData;

    private BitmapDescriptor bitmap;
    private double socketIndex;//先随机生成，有时间再写用preference设置语句 // TODO: 2016/5/28
    private double latitude;
    private double longitude;
    private float direction;
    private String localLocateData = null;
    private double otherLatitude = 0, otherLongitude = 0, otherDirection = 0;
    private double otherSocketIndex;
}