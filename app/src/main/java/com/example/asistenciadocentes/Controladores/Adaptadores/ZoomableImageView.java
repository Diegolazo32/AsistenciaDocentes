package com.example.asistenciadocentes.Controladores.Adaptadores;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class ZoomableImageView extends AppCompatImageView {

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix(); // Agregamos la variable savedMatrix
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private float oldDist = 1f;
    private GestureDetector gestureDetector;
    private boolean isZoomed = false;

    public ZoomableImageView(Context context) {
        super(context);
        init();
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!isZoomed) {
                    // Zoom in
                    matrix.postScale(2f, 2f, e.getX(), e.getY());
                    isZoomed = true;
                } else {
                    // Zoom out
                    matrix.reset();
                    isZoomed = false;
                }
                setImageMatrix(matrix);
                return true;
            }
        });
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_POINTER_DOWN && event.getPointerCount() == 2) {
            return false; // Indica que el evento no fue manejado
        }
        gestureDetector.onTouchEvent(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startPoint.set(event.getX(), event.getY());
                savedMatrix.set(matrix); // Guardamos la matriz actual
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    midPoint(midPoint, event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (oldDist > 10f) {
                    float newDist = spacing(event);
                    matrix.set(savedMatrix);
                    float scale = newDist / oldDist;
                    matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                }
                break;
            case MotionEvent.ACTION_UP:  // Agrega este caso para resetear el zoom
                if (!isZoomed) {
                    float scaleX = (float) getWidth() / getDrawable().getIntrinsicWidth();
                    float scaleY = (float) getHeight() / getDrawable().getIntrinsicHeight();
                    float scale = Math.min(scaleX, scaleY);

                    matrix.setScale(scale, scale);
                    setImageMatrix(matrix);
                }
                break;
        }
        setImageMatrix(matrix);
        return true;
    }



    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}
