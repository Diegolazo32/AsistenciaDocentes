package com.example.asistenciadocentes.Controladores.Adaptadores;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.example.asistenciadocentes.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class ImageListAdapter extends ArrayAdapter<Uri> {

    private Context mContext;
    private int mResource;
    private Matrix matrix = new Matrix();
    private PointF last = new PointF();
    private PointF start = new PointF();
    private float minScale = 1f;
    private static final float MAX_SCALE = 4f;

    public ImageListAdapter(Context context, int resource, List<Uri> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            row = inflater.inflate(mResource, parent, false);
        }

        ImageView imageView = row.findViewById(R.id.img_obtenida);

        // Obtiene la URI de la posición actual
        Uri uri = getItem(position);
        if (uri != null) {
            // Carga las imágenes con Glide
            Glide.with(mContext)
                    .load(uri)
                    .into(imageView);

            imageView.setScaleType(ScaleType.MATRIX);
            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ImageView view = (ImageView) v;
                    view.setScaleType(ScaleType.MATRIX);
                    float scale;

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            matrix.set(view.getImageMatrix());
                            last.set(event.getX(), event.getY());
                            start.set(last);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (event.getPointerCount() == 2) {
                                float deltaX = event.getX(0) - event.getX(1);
                                float deltaY = event.getY(0) - event.getY(1);
                                float midX = event.getX(0) + event.getX(1) / 2;
                                float midY = event.getY(0) + event.getY(1) / 2;
                                float scaleChange = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY) / start.length();
                                scale = Math.max(minScale, Math.min(scaleChange, MAX_SCALE));
                                matrix.postScale(scale, scale, midX, midY);
                            } else {
                                float deltaX = event.getX() - last.x;
                                float deltaY = event.getY() - last.y;
                                matrix.postTranslate(deltaX, deltaY);
                            }
                            last.set(event.getX(), event.getY());
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP:
                            minScale = getMinScale(view);
                            break;
                    }

                    view.setImageMatrix(matrix);
                    return true;
                }
            });
        } else {
            imageView.setImageResource(R.drawable.baseline_error_24); // Cargar imagen de error
        }

        return row;
    }

    private float getMinScale(ImageView view) {
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        int drawableWidth = view.getDrawable().getIntrinsicWidth();
        int drawableHeight = view.getDrawable().getIntrinsicHeight();

        float widthScale = (float) viewWidth / drawableWidth;
        float heightScale = (float) viewHeight / drawableHeight;

        return Math.min(widthScale, heightScale);
    }
}
