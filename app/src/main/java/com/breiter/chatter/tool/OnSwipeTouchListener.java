package com.breiter.chatter.tool;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener, GestureDetector.OnGestureListener{
    private GestureDetector gestureDetector;
    private float SWIPE_THRESHOLD = 500;

    protected OnSwipeTouchListener (Context context){
        gestureDetector = new GestureDetector(context, this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        view.performClick(); ///SPRAWDZIC TO
        return true;
    }

    // GestureDetector
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        boolean result = false;
        try {
            float diffY = motionEvent1.getY() - motionEvent.getY();
            float diffX = motionEvent1.getX() - motionEvent.getX();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(v) > SWIPE_THRESHOLD) {
                    if (diffX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();

                    result = true;
                }
            }

            else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(v1) > SWIPE_THRESHOLD) {
                if (diffY > 0)
                    onSwipeDown();
                else
                    onSwipeUp();

                result = true;
            }
        } catch (Exception exception) {

            exception.printStackTrace();
        }
        return result;

    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
       return  false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float v, float v1) {
        return false;
    }


    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }


    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeUp() {
    }

    public void onSwipeDown() {
    }

}