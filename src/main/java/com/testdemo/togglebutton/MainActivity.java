package com.testdemo.togglebutton;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.testdemo.togglebutton.view.MToggleButton;

public class MainActivity extends Activity {
    private MToggleButton mToggleButton;
    private MToggleButton.OnStateChangeListener mOnStateChangeListener=new MToggleButton.OnStateChangeListener() {
        @Override
        public void onStateChange(boolean bool) {
            if(bool) {//打开开关
                Toast.makeText(MainActivity.this,"您打开了设置开关",Toast.LENGTH_SHORT).show();
            }else{//关闭开关
                Toast.makeText(MainActivity.this,"您关闭了设置开关",Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToggleButton = (MToggleButton) findViewById(R.id.mToggleButton);
        mToggleButton.setOnStateChangeListener(mOnStateChangeListener);
    }
}
