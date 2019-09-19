package com.example.test_snake;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;


// GameActivity是玩游戏的activity，MainActivity是游戏开头动画，点击即可进入GameActivity
public class GameActivity extends Activity {

    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    // 语音识别的
    ImageButton microphone;       // 麦克风按钮
    EditText editText;            // 语音内容显示处
    SpeechRecognizer speechRecognizer;      // 内置语音识别类
    Intent intent;                            // 用来启动语音识别的
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////


    // layout
    private LinearLayout linearLayout = null;

    // 初始化自定义surfaceView（即屏幕上三分之二以上部分，用来显示贪吃蛇的）
    SnakeView snakeSurfaceView = null;
    // 画布：用来画画的（把贪吃蛇画到屏幕上）
    Canvas canvas;

    // 蛇的头，身体，尾巴，苹果等图标，都是bitmap类
    Bitmap snakeHead;
    Bitmap snakeBody;
    Bitmap snakeTail;
    Bitmap apple;


    // 蛇的运动方向
    int snakeDirection = 0;
    //0 = up, 1 = right, 2 = down, 3= left

    // 屏幕高和宽
    int screenWidth;
    int screenHeight;

    // 吃苹果分数
    int score;

    // 屏幕最上边用来显示吃苹果分数统计的
    int showScoreTopPlace;

    // 各种用来控制速度的
    long timeForSpeechControl;

    //用来存放蛇的坐标，蛇会越吃越长嘛，list最长是300（反正不可能连续吃到300个）
    int [] snakeX;
    int [] snakeY;

    //蛇的长度（初始化是3，即头，身体，尾巴各1）
    int snakeSize;

    // 定义苹果的（x，y）坐标（只有一个，吃掉了就没了，会重新随机再放个苹果）
    int appleX;
    int appleY;

    // 关于游戏显示的一些组件像素信息
    int pixelSize;
    int snakeLayoutWidth;
    int snakeLayoutHeight;

    // onCreate是总方法
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 导入显示组件
        displayPurpose();

        // 初始化layout
        getSnakeViewLayout();

        // 把屏幕最上方显示时间啊，电池量啊，WIFI那行隐藏掉
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 初始化自定义surfaceView 用来画贪吃蛇的
        snakeSurfaceView = new SnakeView(this);
        linearLayout.addView(snakeSurfaceView);

        ////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////
        /// 语音识别的
        editText = findViewById(R.id.editText);
        microphone = findViewById(R.id.microphone);
        // Create the SpeechRecognizer for capturing voice
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        // Create the recognizer intent
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        // 语音识别后，会在以下onResult方法中进行操作。比如收到了语音命令"left"，就控制蛇向左走
        // Modify the "onResults" method to deal with captured voice
        // PS: 其他Override方法不用管
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            // 收到语音命令后，会把命令在editText中显示出来
            // 然后根据命令来控制蛇的行走方向
            public void onResults(Bundle bundle) {
                ArrayList<String> keeper  = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                // 接收命令后看是left, right, up还是down
                if (keeper != null){
                    String command = keeper.get(0);
                    command = command.toLowerCase();
                    if (command.equals("left") || command.indexOf("left") != -1){
                        editText.setText("left");
                        snakeDirection = 3;
                    } else if (command.equals("right") || command.indexOf("right") != -1){
                        editText.setText("right");
                        snakeDirection = 1;
                    } else if (command.equals("up") || command.indexOf("right") != -1) {
                        editText.setText("up");
                        snakeDirection = 0;
                    } else if (command.equals("down") || command.indexOf("right") != -1) {
                        editText.setText("down");
                        snakeDirection = 2;
                    } else{
                        editText.setText("Command invalid, try again");
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        /////////////////////////////////////////////////////////////////////////////////////
        // 语音识别的：点击麦克风按钮开始接收语音信息，松开就停止接收
        microphone.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){

                switch(motionEvent.getAction()){

                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        editText.setHint("Click and say 'up', 'down', 'left' or 'right' :)");
                        break;
                    case MotionEvent.ACTION_DOWN:
                        editText.setText("");
                        editText.setHint("Listening to your command ...");
                        speechRecognizer.startListening(intent);
//                        String value = editText.getText().toString();
//                        if (value.equals("left")){
//                            directionOfTravel = 3;
//                        } else if (value.equals("right")){
//                            directionOfTravel = 1;
//                        } else if (value.equals("up")){
//                            directionOfTravel = 0;
//                        } else if (value.equals("down")){
//                            directionOfTravel = 2;
//                        }
                        break;
                }
                return false;
            }
        });

    }

    // 初始化layout
    private void getSnakeViewLayout() {
        if (linearLayout == null){
            linearLayout = (LinearLayout)findViewById(R.id.snakeViewLayout);
        }
    }

    // SurfaceView 是适合于游戏的view，指定某区域内的内容可见（屏幕三分之二以上部分是贪吃蛇）
    class SnakeView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

        // 初始化线程
        Thread thread = null;

        // SurfaceHolder接口用来进入surfaceView
        SurfaceHolder surfaceHolder = null;

        // volatile允许一个线程做出的改变立即让其他线程都接收到（反正就是方便游戏的）
        volatile boolean gaming;

        // 画笔（可以在画布（屏幕）上画画）
        Paint paint;

        public SnakeView(Context context) {
            super(context);
            setFocusable(true);
            // 使用getHolder来接收surface
            if (surfaceHolder == null){
                surfaceHolder = getHolder();
                surfaceHolder.addCallback(this);
            }

            // 初始化画笔
            paint = new Paint();

            // 初始化蛇的长度，反正不可能连续吃到300个苹果
            snakeX = new int[300];
            snakeY = new int[300];

            // 初始化的蛇进去
            getSnake();
            // 放苹果进去
            getApple();
        }

        // 放蛇进去的方法
        public void getSnake() {
            // 初始化蛇头，身体，尾巴仨部分的（x，y）坐标
            // 初始化蛇长度为3
            snakeSize = 3;
            //初始化蛇头在屏幕中间
            snakeX[0] = snakeLayoutWidth / 2;
            snakeY[0] = snakeLayoutHeight / 2;

            //然后是身体
            snakeX[1] = snakeX[0] - 1;
            snakeY[1] = snakeY[0];

            //然后是尾巴
            snakeX[1] = snakeX[1] - 1;
            snakeY[1] = snakeY[0];
        }

        // 方苹果进去的方法
        public void getApple() {
            // 随机产生一个苹果
            Random random = new Random();
            appleX = random.nextInt(snakeLayoutWidth - 1) + 1;
            appleY = random.nextInt(snakeLayoutHeight - 1) + 1;
        }

        @Override
        public void run() {
            while (gaming) {
                // 当游戏在进行时时，会调用以下仨方法协调合作
                gameProcessing();   // 游戏开局一些update
                gameDrawing();     // 显示在屏幕上
                snakeMoving(); // 控制速度的
            }
        }


        // 游戏开局属性，规则啥的
        public void gameProcessing() {

            //看玩家是否吃到了苹果 （即头的x，y都是苹果的坐标）
            if (snakeX[0] == appleX && snakeY[0] == appleY) {
                //吃到了苹果，长身体一个身体位，即长度增加1
                snakeSize++;

                // 苹果被吃掉后，重新随机一个新苹果
                getApple();

                // score 增加5 （每多一个苹果加5分，我觉得可以去掉这个score功能）
                score = score + 5;
            }

            //移动蛇的身体，不然只有头在跑，身体不动的
            for (int i = snakeSize; i > 0; i--) {
                snakeX[i] = snakeX[i - 1];
                snakeY[i] = snakeY[i - 1];
            }

            //移动头的方向（朝向：上下左右）
            switch (snakeDirection) {
                case 0://up
                    snakeY[0]--;
                    break;

                case 1://right
                    snakeX[0]++;
                    break;

                case 2://down
                    snakeY[0]++;
                    break;

                case 3://left
                    snakeX[0]--;
                    break;
            }

            //看看有没有发生意外
            boolean dead = false;
            // 撞墙
            if (snakeX[0] == -1) dead = true;
            if (snakeX[0] >= snakeLayoutWidth) dead = true;
            if (snakeY[0] == -1) dead = true;
            if (snakeY[0] >= snakeLayoutHeight) dead = true;
            // 吃自己
            for (int i = snakeSize - 1; i > 0; i--) {
                if ((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                    dead = true;
                }
            }

            if (dead) {
                // 死了就重新开始, score清零，重新初始化蛇
                score = 0;
                getSnake();
            }
        }


        // 把所有东西显示在画布上（屏幕上画出来）
        public void gameDrawing() {
            // 如果getHolder可以接入surface的话
            if (surfaceHolder.getSurface().isValid()) {
                // SurfaceView来锁定画布，在画布上面画自己想要的东西，完了，通过解锁画布，把内容渲染出来
                canvas = surfaceHolder.lockCanvas();
                //Paint paint = new Paint();
                canvas.drawColor(Color.BLACK);//背景颜色是黑色

                // 好像是渲染暗度啥的，都是255是高亮
                paint.setColor(Color.argb(255, 255, 255, 255));

                // 最上边score统计的字体大小
                paint.setTextSize(showScoreTopPlace / 2);
                // 最上边写score等字
                canvas.drawText("Score:" + score, 10, showScoreTopPlace - 6, paint);

                // 画出线边缘
                paint.setStrokeWidth(3);//4 pixel border
                canvas.drawLine(1, showScoreTopPlace, screenWidth - 1, showScoreTopPlace, paint);
                canvas.drawLine(screenWidth - 1, showScoreTopPlace, screenWidth - 1, showScoreTopPlace + (snakeLayoutHeight * pixelSize), paint);
                canvas.drawLine(screenWidth - 1, showScoreTopPlace + (snakeLayoutHeight * pixelSize), 1, showScoreTopPlace + (snakeLayoutHeight * pixelSize), paint);
                canvas.drawLine(1, showScoreTopPlace, 1, showScoreTopPlace + (snakeLayoutHeight * pixelSize), paint);

                // 画蛇头（显示在画布（屏幕）上）
                canvas.drawBitmap(snakeHead, snakeX[0] * pixelSize, (snakeY[0] * pixelSize) + showScoreTopPlace, paint);
                //画身体
                for (int i = 1; i < snakeSize - 1; i++) {
                    canvas.drawBitmap(snakeBody, snakeX[i] * pixelSize, (snakeY[i] * pixelSize) + showScoreTopPlace, paint);
                }
                //画尾巴（显示在画布（屏幕）上）
                canvas.drawBitmap(snakeTail, snakeX[snakeSize - 1] * pixelSize, (snakeY[snakeSize - 1] * pixelSize) + showScoreTopPlace, paint);

                //画苹果（显示在画布（屏幕）上）
                canvas.drawBitmap(apple, appleX * pixelSize, (appleY * pixelSize) + showScoreTopPlace, paint);

                // 画完之后解锁画布
                surfaceHolder.unlockCanvasAndPost(canvas);
            }

        }


        // 这个方法是依靠时间差来控制蛇移动速度的
        public void snakeMoving() {
            long temp = (System.currentTimeMillis() - timeForSpeechControl);
            long timeToSleep = 100 - temp;

            if (timeToSleep > 0) {
                try {
                    thread.sleep(timeToSleep);
                } catch (InterruptedException e) {

                }
            }
            timeForSpeechControl = System.currentTimeMillis();
        }


        public void pause() {
            // 游戏状态ing变成false
            gaming = false;
            try {
                // join（）让主线程等待子线程的终止（再结束）
                thread.join();
            } catch (InterruptedException e) {
            }

        }

        public void resume() {
            // 游戏状态ing回复称true
            gaming = true;
            // 开启新的线程 并开始
            thread = new Thread(this);
            thread.start();
        }


        // 触屏方法（控制蛇）
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            // ACTION_MASK是用来多点触摸的
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                // 没按的时候：
                case MotionEvent.ACTION_UP:
                    if (motionEvent.getX() >= screenWidth / 2) {
                        // 向右转
                        snakeDirection++;
                        if (snakeDirection == 4) {//no such direction
                            //loop back to 0(up)
                            snakeDirection = 0;
                        }
                    } else {
                        //向左转
                        snakeDirection--;
                        if (snakeDirection == -1) {//no such direction
                            //loop back to 0(up)
                            snakeDirection = 3;
                        }
                    }
            }
            return true;
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        while (true) {
            snakeSurfaceView.pause();
            break;
        }

        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        snakeSurfaceView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeSurfaceView.pause();
    }

    // 点击后退键，会退到游戏开头动画界面
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            snakeSurfaceView.pause();

            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return false;
    }

    // 初始化显示组件
    public void displayPurpose() {
        //find out 屏幕的长宽高
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y - 480;
        showScoreTopPlace = screenHeight / 10;

        //Determine the size of each block/place on the game board
        // 决定一个block的大小
        pixelSize = screenWidth / 40;

        //Determine how many game blocks will fit into the height and width
        //Leave one block for the score at the top
        // 看看屏幕的长宽高可以弄成多少个block，并在最上方留一横block给分数统计
        snakeLayoutWidth = 40;
        snakeLayoutHeight = ((screenHeight - showScoreTopPlace)) / pixelSize;

        // 载入蛇头，身体，尾巴和苹果
        snakeHead = BitmapFactory.decodeResource(getResources(), R.drawable.head);
        snakeBody = BitmapFactory.decodeResource(getResources(), R.drawable.body);
        snakeTail = BitmapFactory.decodeResource(getResources(), R.drawable.tail);
        apple = BitmapFactory.decodeResource(getResources(), R.drawable.apple);

        // 使蛇头，身体，尾巴，苹果是block的size
        snakeHead = Bitmap.createScaledBitmap(snakeHead, pixelSize, pixelSize, false);
        snakeBody = Bitmap.createScaledBitmap(snakeBody, pixelSize, pixelSize, false);
        snakeTail = Bitmap.createScaledBitmap(snakeTail, pixelSize, pixelSize, false);
        apple = Bitmap.createScaledBitmap(apple, pixelSize, pixelSize, false);

    }
}

