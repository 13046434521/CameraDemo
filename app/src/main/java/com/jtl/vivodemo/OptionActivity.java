package com.jtl.vivodemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

public class OptionActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    private RadioGroup mSelectRGroup;
    private RadioGroup mTypeRGroup;
    private Button mPreviewBtn;
    private int width;
    private int height;
    private @Constants.CAMETA_TYPE String type= Constants.CAMETA_TYPE.RGB_BACK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        initView();
    }

    private void initView() {
        mSelectRGroup =findViewById(R.id.rg_option_select);
        mSelectRGroup.setOnCheckedChangeListener(this);

        mTypeRGroup = findViewById(R.id.rg_option_type);
        mTypeRGroup.setOnCheckedChangeListener(this);

        mPreviewBtn=findViewById(R.id.btn_option_preview);
        mPreviewBtn.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.rb_select_1:
                width=2340;
                height=1080;
                break;
            case R.id.rb_select_2:
                width=1920;
                height=1440;
                break;
            case R.id.rb_select_3:
                width=1920;
                height=1080;
                break;
            case R.id.rb_select_4:
                width=1440;
                height=1080;
                break;
            case R.id.rb_select_5:
                width=1280;
                height=720;
                break;
            case R.id.rb_select_6:
                width=640;
                height=480;
                break;
            case R.id.rb_select_rgb_back:
                selectType(Constants.CAMETA_TYPE.RGB_BACK);
                break;
            case R.id.rb_select_rgb_front:
                selectType(Constants.CAMETA_TYPE.RGB_FRONT);
                break;
            case R.id.rb_select_depth:
                selectType(Constants.CAMETA_TYPE.DEPTH);
                break;
            case R.id.rb_select_ir:
                selectType(Constants.CAMETA_TYPE.IR);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_option_preview:
                if (mSelectRGroup.getCheckedRadioButtonId()==-1){
                    Toast.makeText(this,"请选择分辨率",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mTypeRGroup.getCheckedRadioButtonId()==-1){
                    Toast.makeText(this,"请选择类型",Toast.LENGTH_SHORT).show();
                    return;
                }
                startPreview();
                break;
        }
    }

    /**
     * 预览跳转页面
     */
    private void startPreview(){
        if (PermissionHelper.hasCameraPermission(this)){
            Intent intent=new Intent(this, CameraActivity.class);
            intent.putExtra("width",width);
            intent.putExtra("height",height);
            intent.putExtra("type",type);
            startActivity(intent);
        } else{
            PermissionHelper.requestCameraPermission(this);
        }
    }

    public void selectType(@Constants.CAMETA_TYPE String type){
        height=0;
        width=0;
        mSelectRGroup.clearCheck();

        if (type== Constants.CAMETA_TYPE.RGB_BACK||type== Constants.CAMETA_TYPE.RGB_FRONT){
            findViewById(R.id.rb_select_1).setVisibility(View.VISIBLE);
            findViewById(R.id.rb_select_2).setVisibility(View.VISIBLE);
            findViewById(R.id.rb_select_3).setVisibility(View.VISIBLE);
            findViewById(R.id.rb_select_4).setVisibility(View.VISIBLE);
            findViewById(R.id.rb_select_5).setVisibility(View.VISIBLE);
            findViewById(R.id.rb_select_6).setVisibility(View.VISIBLE);
        }
        else if (type== Constants.CAMETA_TYPE.DEPTH){
            findViewById(R.id.rb_select_1).setVisibility(View.GONE);
            findViewById(R.id.rb_select_2).setVisibility(View.GONE);
            findViewById(R.id.rb_select_3).setVisibility(View.GONE);
            findViewById(R.id.rb_select_4).setVisibility(View.GONE);
            findViewById(R.id.rb_select_5).setVisibility(View.GONE);
            findViewById(R.id.rb_select_6).setVisibility(View.VISIBLE);
        }else if (type== Constants.CAMETA_TYPE.IR){
            findViewById(R.id.rb_select_1).setVisibility(View.GONE);
            findViewById(R.id.rb_select_2).setVisibility(View.GONE);
            findViewById(R.id.rb_select_3).setVisibility(View.GONE);
            findViewById(R.id.rb_select_4).setVisibility(View.GONE);
            findViewById(R.id.rb_select_5).setVisibility(View.GONE);
            findViewById(R.id.rb_select_6).setVisibility(View.VISIBLE);
        }

        this.type=type;
    }
}
