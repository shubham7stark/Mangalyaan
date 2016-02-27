package com.sdsmdg.mangalyaan;

import android.graphics.Canvas;

public class GameThread extends Thread {
    // 2
    static long FPS = 10;
    // 2//
    private GameSurfaceView gameSurfaceView;
    protected boolean running = false;

    public GameThread(GameSurfaceView gameview){
        this.gameSurfaceView = gameview;
    }

    public void setRunning(boolean run){
        running = run;
    }

    @Override
    public void run(){

        long ticksPS = 500 / FPS;
        long startTime;
        long sleepTime;
        while (running) {
            Canvas c = null;
            startTime = System.currentTimeMillis();
            try {
                c = gameSurfaceView.getHolder().lockCanvas();

                synchronized (gameSurfaceView.getHolder()) {
                    gameSurfaceView.update(startTime);
                    gameSurfaceView.draw(c);
                }
            } finally {
                if (c != null) {
                    gameSurfaceView.getHolder().unlockCanvasAndPost(c);
                }
            }
            sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0)
                    sleep(sleepTime);
                else
                    sleep(10);
            } catch (Exception e){
            }
        }
    }


}
