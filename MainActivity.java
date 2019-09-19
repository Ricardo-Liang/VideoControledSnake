package com.example.test_snake;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.core.content.ContextCompat;


// MainActivity是游戏开头动画，GameActivity是玩游戏的activity，点击即可进入GameActivity
public class MainActivity extends Activity {

    // 用来画画的（呈现在屏幕上）
    Canvas canvas;
    StartingView startingView;

    // 游戏开头的动画图片
    Bitmap startingBitmap;

    // 初始化用来画画的尺寸（不太懂这个，但是是必须的）
    Rect rect;

    // 屏幕高和宽
    int screenWidth;
    int screenHeight;

    // intent是跳转，这里用来从游戏开头动画界面跳转到开始真正玩游戏的GameActivity
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // find out 屏幕的长宽高
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        // 导入开头动画图片
        startingBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.starting_snake);

        // 初始化 SnakeAnimView
        startingView = new StartingView(this);
        setContentView(startingView);

        // 检查手机的语音权限（玩这个游戏app需要用户给予语音permission，即允许使用麦克风）
        permissionChecking();

        // intent：设置跳转到GameActivity
        intent = new Intent(this, GameActivity.class);
    }

    // 检查app的语音麦克风权限
    private void permissionChecking(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (!(ContextCompat
                    .checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED)){
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    // surfaceView 用来呈现描绘出来的东西。这里用来呈现开头动画图片
    class StartingView extends SurfaceView implements Runnable
    {
        // 初始化线程
        Thread thread = null;

        // surfaceHolder接口用来访问该surface
        SurfaceHolder surfaceHolder;

        // volatile 保证对所有线程的可见性,当一个线程修改了变量的值，新的值会立刻同步到主内存当中（理解为为了方便游戏）
        volatile boolean gaming;

        // 用来在画布上画画的（画出来才能在屏幕上显示）
        Paint paint;

        public StartingView(Context context) {
            super(context);
            // getHolder（）方法用来得到该surface
            surfaceHolder = getHolder();
            paint = new Paint();
        }

        @Override
        public void run() {
            while (gaming){
                StartingDraw();
            }
        }

        // 画游戏开头界面
        private void StartingDraw() {

            if (surfaceHolder.getSurface().isValid()){
                canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.WHITE);//background color
                paint.setColor(Color.argb(255,255,255,255));
                paint.setTextSize(100);
                paint.setColor(Color.BLACK);
                canvas.drawText("Click to start the game !",10,150,paint);

                // 画开头动画图片
                Rect drawSize = new Rect(screenWidth/2-350,screenHeight/2-350,screenWidth/2+350,screenHeight/2+350);
                canvas.drawBitmap(startingBitmap, rect, drawSize, paint);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void pause(){
            gaming = false;
            try {
                thread.join();
            }catch (Exception e){

            }
        }

        public void resume(){
            gaming = true;
            thread = new Thread(this);
            thread.start();
        }


        // 触屏后就会跳到GameActivity，进入玩游戏界面
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            startActivity(intent);
            return true;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        while (true){
            startingView.pause();
            break;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startingView.resume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        startingView.pause();
    }
}