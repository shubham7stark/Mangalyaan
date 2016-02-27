package com.sdsmdg.mangalyaan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by shubham on 28/2/16.
 */
public class GameSurfaceView extends SurfaceView {

    private static int screen_height, screen_width;

    SurfaceHolder surfaceHolder;
    GameThread gameThread;
    Rocket rocket;
    Paint mpaint;

    public GameSurfaceView(Context mContext, int height, int width){
        super(mContext);
        screen_height = height; screen_width = width;

        surfaceHolder = this.getHolder();
        gameThread = new GameThread(this);
        rocket = new Rocket();
        rocket.x = width/2;rocket.y = height/2;

        mpaint = new Paint();
        mpaint.setStrokeWidth(8);
        mpaint.setStyle(Paint.Style.STROKE);
        mpaint.setColor(Color.CYAN);
        mpaint.setStyle(Paint.Style.FILL);
        mpaint.setAlpha(50);

        surfaceHolder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceDestroyed(SurfaceHolder arg0) {
                gameThread.setRunning(false);
                boolean retry = true;
                while (retry) {
                    try {
                        gameThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                        // try again shutting down the thread
                    }
                }
            }
            @Override
            public void surfaceCreated(SurfaceHolder arg0) {
                // TODO Auto-generated method stub
                gameThread.setRunning(true);
                gameThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub

            }
        });

    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);

        canvas.drawCircle(rocket.x,
                rocket.y,
                5,mpaint);
    }

    void update(long startTime){

    }

}
