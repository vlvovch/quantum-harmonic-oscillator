package com.vlvolad.quantumoscillator;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by Vladimir on 29.04.2015.
 */
public class HAGLSurfaceView extends GLSurfaceView {

    HAGLRenderer mRenderer;
    private ScaleGestureDetector mScaleDetector;
    private int count;
    public float mDensity;

    public HAGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new HAGLRenderer();
        setRenderer(mRenderer);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        count = 0;

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public HAGLSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new HAGLRenderer();
        setRenderer(mRenderer);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        count = 0;

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mPreviousScale = false;
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    private boolean mPreviousScale;

    /*public void setGravity(float gx, float gy, float gz)
    {
    	mRenderer.mPendulum.setGravity(gx, gy, gz);
    }*/

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();



        if (e.getPointerCount()<2)
        {
            switch (e.getAction()) {
//        	case MotionEvent.ACTION_DOWN:
//        		SP2DGLRenderer.mPendulum.setCoord(x, y, mRenderer.Width, mRenderer.Height);
//        		requestRender();
                case MotionEvent.ACTION_UP:
//                    DPGLRenderer.mPendulum.moved = false;
//                    DPGLRenderer.mPendulum.moveIndex = 0;
//                    DPGLRenderer.mPendulum.timeInterval2 = -1;
                    HAGLRenderer.mAtom.motion = false;
                    requestRender();
                    count = 0;
                    break;
                case MotionEvent.ACTION_MOVE:

                    count++;

                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;

                    //if (Math.abs(dx)>mRenderer.Width/10.f) dx = 0.f;
                    //if (Math.abs(dy)>mRenderer.Width/10.f) dy = 0.f;

//                    if (count<=1) DPGLRenderer.mPendulum.SetPendulumIndex(x, y, mRenderer.Width, mRenderer.Height);
//
//                    if (count>2) DPGLRenderer.mPendulum.setCoord(x, y, dx, dy, mRenderer.Width, mRenderer.Height);

                /*// reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                  dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                  dy = dy * -1 ;
                }*/

                    //mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
                    //SP2DGLRenderer.mPendulum.moveX += dx * 0.25f / SP2DGLRenderer.mPendulum.zoomIn;
                    //SP2DGLRenderer.mPendulum.moveY -= dy * 0.25f / SP2DGLRenderer.mPendulum.zoomIn;
                    //Log.d("SurfaceView", "dx: " + dx + "  dy: " + dy);
                    if (!mPreviousScale) {
                        HAGLRenderer.mAtom.camera_rot[0] += dy / 10.f * 4.f / mDensity;
                        HAGLRenderer.mAtom.camera_rot[1] += -dx / 10.f * 4.f / mDensity * 2.0 * (y - getHeight()/2 + getHeight()/8) / (getHeight()/2);
                        //HAGLRenderer.mAtom.camera_rot[1] += -dx / 10.f * Math.cos(HAGLRenderer.mAtom.camera_rot[0] * Math.PI / 180.) * 1.5 * (y - getHeight()/2) / (getHeight()/2);
                        //HAGLRenderer.mAtom.camera_rot[2] += dx / 10.f * 4.f / mDensity * 2.0 * (7.f*getHeight()/8.f-Math.abs(y - getHeight()/2 + getHeight()/8)) / (getHeight()/2);
                        HAGLRenderer.mAtom.motion = true;
                        requestRender();
                    }
                    else mPreviousScale = false;
            }
        }

        mPreviousX = x;
        mPreviousY = y;
        mScaleDetector.onTouchEvent(e);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = HAGLRenderer.mAtom.zoomIn;
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.20f, Math.min(mScaleFactor, 10.0f));

            HAGLRenderer.mAtom.zoomIn = mScaleFactor;
            HAGLRenderer.mAtom.motion = true;
            mPreviousScale = true;
//            HAGLRenderer.mAtom.camera_trans[2] += detector.getScaleFactor();
            requestRender();

            invalidate();
            return true;
        }
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        if (mRenderer.mAtom.fin>0) setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//    }

    // Hides superclass method.
// 	public void setRenderer(LessonSixRenderer renderer, float density)
// 	{
// 		mRenderer = renderer;
// 		mDensity = density;
// 		super.setRenderer(renderer);
// 	}

}
