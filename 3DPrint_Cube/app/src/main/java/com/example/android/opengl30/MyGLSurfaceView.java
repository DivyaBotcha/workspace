/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.opengl30;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    private int pointerCount = 0;
    private float x1 = Float.MIN_VALUE;
    private float y1 = Float.MIN_VALUE;
    private float x2 = Float.MIN_VALUE;
    private float y2 = Float.MIN_VALUE;
    private float dx1 = Float.MIN_VALUE;
    private float dy1 = Float.MIN_VALUE;
    private float dx2 = Float.MIN_VALUE;
    private float dy2 = Float.MIN_VALUE;

    private float length = Float.MIN_VALUE;
    private float previousLength = Float.MIN_VALUE;
    private float currentPress1 = Float.MIN_VALUE;
    private float currentPress2 = Float.MIN_VALUE;

    private float rotation = 0;
    private int currentSquare = Integer.MIN_VALUE;

    private boolean isOneFixedAndOneMoving = false;
    private boolean fingersAreClosing = false;
    private boolean isRotating = false;

    private boolean gestureChanged = false;
    private boolean moving = false;
    private boolean simpleTouch = false;
    private long lastActionTime;
    private int touchDelay = -2;
    private int touchStatus = -1;

    private float previousX1;
    private float previousY1;
    private float previousX2;
    private float previousY2;
    private float[] previousVector = new float[4];
    private float[] vector = new float[4];
    private float[] rotationVector = new float[4];
    private float previousRotationSquare;

    public MyGLSurfaceView(Context context) {
        super(context);


        // Create an OpenGL ES 2.0 context.  CHANGED to 3.0  JW.
        setEGLContextClientVersion(3);
        //fix for error No Config chosen, but I don't know what this does.
        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        mRenderer.shouldDrawCube = true;
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

       /* switch (e.getActionMasked()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_HOVER_EXIT:
            case MotionEvent.ACTION_OUTSIDE:
                // this to handle "1 simple touch"
                if (lastActionTime > SystemClock.uptimeMillis() - 250) {
                    simpleTouch = true;
                } else {
                    gestureChanged = true;
                    touchDelay = 0;
                    lastActionTime = SystemClock.uptimeMillis();
                    simpleTouch = false;
                }
                moving = false;
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_HOVER_ENTER:
                //   Log.v(TAG, "Gesture changed...");
                gestureChanged = true;
                touchDelay = 0;
                lastActionTime = SystemClock.uptimeMillis();
                simpleTouch = false;
                break;
            case MotionEvent.ACTION_MOVE:
                moving = true;
                simpleTouch = false;
                touchDelay++;
                break;
            default:
                //    Log.w(TAG, "Unknown state: " + motionEvent.getAction());
                gestureChanged = true;
        }

        pointerCount = e.getPointerCount();

        if (pointerCount == 1) {
            x1 = e.getX();
            y1 = e.getY();
            if (gestureChanged) {
                //  Log.v(TAG, "x:" + x1 + ",y:" + y1);
                previousX1 = x1;
                previousY1 = y1;
            }
            dx1 = x1 - previousX1;
            dy1 = y1 - previousY1;
        } else if (pointerCount == 2) {
            x1 = e.getX(0);
            y1 = e.getY(0);
            x2 = e.getX(1);
            y2 = e.getY(1);
            vector[0] = x2 - x1;
            vector[1] = y2 - y1;
            vector[2] = 0;
            vector[3] = 1;
            float len = Matrix.length(vector[0], vector[1], vector[2]);
            vector[0] /= len;
            vector[1] /= len;

            // Log.v(TAG, "x1:" + x1 + ",y1:" + y1 + ",x2:" + x2 + ",y2:" + y2);
            if (gestureChanged) {
                previousX1 = x1;
                previousY1 = y1;
                previousX2 = x2;
                previousY2 = y2;
                System.arraycopy(vector, 0, previousVector, 0, vector.length);
            }
            dx1 = x1 - previousX1;
            dy1 = y1 - previousY1;
            dx2 = x2 - previousX2;
            dy2 = y2 - previousY2;

            rotationVector[0] = (previousVector[1] * vector[2]) - (previousVector[2] * vector[1]);
            rotationVector[1] = (previousVector[2] * vector[0]) - (previousVector[0] * vector[2]);
            rotationVector[2] = (previousVector[0] * vector[1]) - (previousVector[1] * vector[0]);
            len = Matrix.length(rotationVector[0], rotationVector[1], rotationVector[2]);
            rotationVector[0] /= len;
            rotationVector[1] /= len;
            rotationVector[2] /= len;

            previousLength = (float) Math
                    .sqrt(Math.pow(previousX2 - previousX1, 2) + Math.pow(previousY2 - previousY1, 2));
            length = (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

            currentPress1 = e.getPressure(0);
            currentPress2 = e.getPressure(1);
            rotation = 0;
            rotation = TouchScreen.getRotation360(e);
            currentSquare = TouchScreen.getSquare(e);
            if (currentSquare == 1 && previousRotationSquare == 4) {
                rotation = 0;
            } else if (currentSquare == 4 && previousRotationSquare == 1) {
                rotation = 360;
            }

            // gesture detection
            isOneFixedAndOneMoving = ((dx1 + dy1) == 0) != (((dx2 + dy2) == 0));
            fingersAreClosing = !isOneFixedAndOneMoving && (Math.abs(dx1 + dx2) < 10 && Math.abs(dy1 + dy2) < 10);
            isRotating = !isOneFixedAndOneMoving && (dx1 != 0 && dy1 != 0 && dx2 != 0 && dy2 != 0)
                    && rotationVector[2] != 0;
        }*/

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * 1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * 1;
                }

                mRenderer.setAngle(
                        mRenderer.getAngle() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;

    }
       /* previousX1 = x1;
        previousY1 = y1;
        previousX2 = x2;
        previousY2 = y2;

        previousRotationSquare = currentSquare;

        System.arraycopy(vector, 0, previousVector, 0, vector.length);

        if (gestureChanged && touchDelay > 1) {
            gestureChanged = false;
            // Log.v(TAG, "Fin");
        }

        requestRender();

        return true;
    }*/
}

  /*  class TouchScreen {

        // these matrices will be used to move and zoom image
        private android.graphics.Matrix matrix = new android.graphics.Matrix();
        private android.graphics.Matrix savedMatrix = new android.graphics.Matrix();
        // we can be in one of these 3 states
        private static final int NONE = 0;
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
        private int mode = NONE;
        // remember some things for zooming
        private PointF start = new PointF();
        private PointF mid = new PointF();
        private float oldDist = 1f;
        private float d = 0f;
        private float newRot = 0f;
        private float[] lastEvent = null;

        public boolean onTouch(View v, MotionEvent event) {
            // handle touch events here
            ImageView view = (ImageView) v;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    lastEvent = null;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    lastEvent = new float[4];
                    lastEvent[0] = event.getX(0);
                    lastEvent[1] = event.getX(1);
                    lastEvent[2] = event.getY(0);
                    lastEvent[3] = event.getY(1);
                    d = getRotation(event);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    lastEvent = null;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        float dx = event.getX() - start.x;
                        float dy = event.getY() - start.y;
                        matrix.postTranslate(dx, dy);
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = (newDist / oldDist);
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                        if (lastEvent != null && event.getPointerCount() == 3) {
                            newRot = getRotation(event);
                            float r = newRot - d;
                            float[] values = new float[9];
                            matrix.getValues(values);
                            float tx = values[2];
                            float ty = values[5];
                            float sx = values[0];
                            float xc = (view.getWidth() / 2) * sx;
                            float yc = (view.getHeight() / 2) * sx;
                            matrix.postRotate(r, tx + xc, ty + yc);
                        }
                    }
                    break;
            }

            view.setImageMatrix(matrix);
            return true;
        }

        /**
         * Determine the space between the first two fingers
         */
  /*      private float spacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        }

        /**
         * Calculate the mid point of the first two fingers
         */
    /*    private void midPoint(PointF point, MotionEvent event) {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }

        /**
         * Calculate the degree to be rotated by.
         *
         * @param event
         * @return Degrees
         */
      /*  public static float getRotation(MotionEvent event) {
            double dx = (event.getX(0) - event.getX(1));
            double dy = (event.getY(0) - event.getY(1));
            double radians = Math.atan2(Math.abs(dy), Math.abs(dx));
            double degrees = Math.toDegrees(radians);
            return (float) degrees;
        }

        public static float getRotation360(MotionEvent event) {
            double dx = (event.getX(0) - event.getX(1));
            double dy = (event.getY(0) - event.getY(1));
            double radians = Math.atan2(Math.abs(dy), Math.abs(dx));
            double degrees = Math.toDegrees(radians);
            int square = 1;
            if (dx > 0 && dy == 0) {
                square = 1;
            } else if (dx > 0 && dy < 0) {
                square = 1;
            } else if (dx == 0 && dy < 0) {
                square = 2;
                degrees = 180 - degrees;
            } else if (dx < 0 && dy < 0) {
                square = 2;
                degrees = 180 - degrees;
            } else if (dx < 0 && dy == 0) {
                square = 3;
                degrees = 180 + degrees;
            } else if (dx < 0 && dy > 0) {
                square = 3;
                degrees = 180 + degrees;
            } else if (dx == 0 && dy > 0) {
                square = 4;
                degrees = 360 - degrees;
            } else if (dx > 0 && dy > 0) {
                square = 4;
                degrees = 360 - degrees;
            }
            return (float) degrees;
        }

        public static int getSquare(MotionEvent event) {
            double dx = (event.getX(0) - event.getX(1));
            double dy = (event.getY(0) - event.getY(1));
            int square = 1;
            if (dx > 0 && dy == 0) {
                square = 1;
            } else if (dx > 0 && dy < 0) {
                square = 1;
            } else if (dx == 0 && dy < 0) {
                square = 2;
            } else if (dx < 0 && dy < 0) {
                square = 2;
            } else if (dx < 0 && dy == 0) {
                square = 3;
            } else if (dx < 0 && dy > 0) {
                square = 3;
            } else if (dx == 0 && dy > 0) {
                square = 4;
            } else if (dx > 0 && dy > 0) {
                square = 4;
            }
            return square;
        }
    }
*/
