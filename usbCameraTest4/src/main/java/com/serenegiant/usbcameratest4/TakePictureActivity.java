package com.serenegiant.usbcameratest4;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.BundleCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.serenegiant.dialog.MessageDialogFragment;
import com.serenegiant.encoder.MediaMuxerWrapper;
import com.serenegiant.service.IUVCService;
import com.serenegiant.service.UVCService;
import com.serenegiant.serviceclient.CameraClient;
import com.serenegiant.serviceclient.ICameraClient;
import com.serenegiant.serviceclient.ICameraClientCallback;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.utils.PermissionCheck;

import java.util.HashMap;
import java.util.List;

/**
 * Created by cuizhou on 18-6-4.
 */

public class TakePictureActivity extends AppCompatActivity {
    private boolean DEBUG = true;
    private String TAG = "TakePictureActivity";

    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;

    private USBMonitor mUSBMonitor;

    private ICameraClient mCameraClient1;
    private ICameraClient mCameraClient2;
    private ICameraClient mCameraClient3;

    private final Object mSync = new Object();

    // Step1: 设置图片保存路径
    String imgpath0 = "/mnt/internal_sd/DCIM/USBCameraTest/c0.jpg";
    String imgpath1 = "/mnt/internal_sd/DCIM/USBCameraTest/c1.jpg";
    String imgpath2 = "/mnt/internal_sd/DCIM/USBCameraTest/c2.jpg";
    String imgpath3 = "/mnt/internal_sd/DCIM/USBCameraTest/c3.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture_activity);

        if (mUSBMonitor == null) {
            mUSBMonitor = new USBMonitor(getApplicationContext(), mOnDeviceConnectListener);
            final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(getApplicationContext(), R.xml.device_filter);
            mUSBMonitor.setDeviceFilter(filters);
        }
        mUSBMonitor.register();
    }

    @Override
    protected void onResume(){
        super.onResume();


        // Step2: 开启子线程发送拍照请求

//        TakePIctureReqThread t0 = new TakePIctureReqThread(imgpath0, 0);
//        t0.start();
        new Thread(new TakePIctureReqThread(imgpath0,0)).start();
        new Thread(new TakePIctureReqThread(imgpath1,1)).start();
        new Thread(new TakePIctureReqThread(imgpath2,2)).start();
        new Thread(new TakePIctureReqThread(imgpath3,3)).start();

    }



    public class TakePIctureReqThread extends Thread {
        private String TAG_TTHREAD  = "TakePIctureReqThread";
        private String mImgpath;
        private int mCameraIdx;
        private long mPreSleepTime;
        final Intent intent = new Intent(getApplication(), UVCService.class);

        private ICameraClient mCameraClient;
        private final ICameraClientCallback mCameraListener = new ICameraClientCallback() {
            @Override
            public void onConnect() {
                if (DEBUG) Log.v(TAG, "onConnect: 000000");
            }

            @Override
            public void onDisconnect() {
                if (DEBUG) Log.v(TAG, "onDisconnect: 000000");
            }

        };

        public TakePIctureReqThread(String imgpath,int cameraidx){
            Log.e(TAG_TTHREAD,"starts");
            mImgpath=imgpath;
            mCameraIdx = cameraidx;
            switch (mCameraIdx){
                case 0:
                    mPreSleepTime=0;
                    break;
                case 1:
                    mPreSleepTime=800;
                    break;
                case 2:
                    mPreSleepTime=1600;
                    break;
                case 3:
                    mPreSleepTime=2400;
                    break;
                default:
                    break;
            }
        }
//        @Override
//        public void run(){
//            Log.e(TAG_TTHREAD,"SSSSSSSSSSSSSSSSSSSSSSSTTTTTTTTTARTS");
//
//            //间隔两秒保存一次图片
//            try{
//                Thread.sleep(1000);
//            }catch(Exception e){
//                System.exit(0);//退出程序
//            }
//            while(true) {
//                if(mCameraIdx==0){
//
//                    final List<UsbDevice> list = mUSBMonitor.getDeviceList();
//                    // camera0
//                    mCameraClient = new CameraClient(getApplicationContext(), mCameraListener);
//                    mCameraClient.select(list.get(mCameraIdx));
//                    mCameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//                    mCameraClient.connect();
//                    try{
//                        Thread.sleep(1000);
//                    }catch(Exception e){
//                        System.exit(0);//退出程序
//                    }
//                    mCameraClient.captureStill(mImgpath);
//                    mCameraClient.disconnect();
//                    mCameraClient.release();
//                    // start UVCService
//                    Log.e(TAG_TTHREAD, "trigger stop service");
//                    break;
//                }else{
//                    try{
//                        Thread.sleep(1000);
//                    }catch(Exception e){
//                        System.exit(0);//退出程序
//                    }
////                    if (DEBUG) Log.e(TAG, "MainActivity.mServiceIsAlive: "+Data.getA());
////                    if (Data.getA().equals("dead")) {
//                        final List<UsbDevice> list = mUSBMonitor.getDeviceList();
//                        // camera0
//                        mCameraClient = new CameraClient(getApplicationContext(), mCameraListener);
//                        mCameraClient.select(list.get(mCameraIdx));
//                        mCameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//                        mCameraClient.connect();
//                        try{
//                            Thread.sleep(1000);
//                        }catch(Exception e){
//                            System.exit(0);//退出程序
//                        }
//                        mCameraClient.captureStill(mImgpath);
//                        mCameraClient.disconnect();
//                        mCameraClient.release();
//                        // start UVCService
//                        Log.e(TAG_TTHREAD, "trigger stop service");
//                        break;
////                    }
//                }
////                Thread th=Thread.currentThread();
////                Log.e(TAG,"Tread name:"+th.getName());
//            }
//        }

        @Override
        public void run(){
            Log.e(TAG_TTHREAD,"SSSSSSSSSSSSSSSSSSSSSSSTTTTTTTTTARTS");

            try{
                Thread.sleep(mPreSleepTime);
            }catch(Exception e){
                System.exit(0);//退出程序
            }
            final List<UsbDevice> list = mUSBMonitor.getDeviceList();
            // camera0
            mCameraClient = new CameraClient(getApplicationContext(), mCameraListener);
            mCameraClient.select(list.get(mCameraIdx));
            mCameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            mCameraClient.connect();
            try{
                Thread.sleep(1000);
            }catch(Exception e){
                System.exit(0);//退出程序
            }
            mCameraClient.captureStill(mImgpath);
            mCameraClient.disconnect();
            mCameraClient.release();
            // start UVCService
            Log.e(TAG_TTHREAD, "trigger stop service");

        }

    }


    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onAttach:");
//            tryOpenUVCCamera(true);
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onConnect:");
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDisconnect:");
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDettach:");
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onCancel:");
        }
    };

}
