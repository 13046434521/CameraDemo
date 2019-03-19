package com.jtl.vivodemo.detail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.jtl.vivodemo.CameraProxy;
import com.jtl.vivodemo.Constants;
import com.jtl.vivodemo.R;
import com.jtl.vivodemo.view.AutoFitSurfaceView;
import com.socks.library.KLog;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, CameraProxy.ProxyListener {
    private AutoFitSurfaceView mFitSurfaceView;
    private CameraProxy mCameraProxy;
    private @Constants.CAMETA_TYPE
    String type;
    private  int width =2340;
    private  int height =1080;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        KLog.e("onResume:"+"type:"+type+"width:"+width+"height:"+height);
        mFitSurfaceView.setAspectRatio(height,width);
        mFitSurfaceView.getHolder().setFixedSize(width,height);

        if (mCameraProxy!=null&&mCameraProxy.isOpenSuccess()){
            mCameraProxy.OpenCamera(width,height);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraProxy!=null){
            mCameraProxy.CloseCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraProxy.stopCameraThread();
    }

    private void init(){
        type=getIntent().getStringExtra("type");
        width=getIntent().getIntExtra("width",1920);
        height=getIntent().getIntExtra("height",1080);

        KLog.e("init:"+"type:"+type+"width:"+width+"height:"+height);

        mFitSurfaceView = findViewById(R.id.sv_main_surface);
        mCameraProxy = new CameraProxy(this,this,type);

        mFitSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void onError() {

    }

    @Override
    public Surface getSurface() {
        return mFitSurfaceView.getHolder().getSurface();
    }

    @Override
    public int getOrientation(int sensor) {
        return 0;
    }

    @Override
    public void getDistance(int distance) {

    }

    @Override
    public void getIr(int ir) {

    }

    @Override
    public void getMode(int mode) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        KLog.e("Surface:","width:"+width+"---height:"+height+"\nwidth"+mFitSurfaceView.getWidth()+":height"+mFitSurfaceView.getHeight());
        mCameraProxy.OpenCamera(width,height);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
