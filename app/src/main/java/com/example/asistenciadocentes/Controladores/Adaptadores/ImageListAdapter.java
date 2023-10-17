package com.example.asistenciadocentes.Controladores.Adaptadores;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.asistenciadocentes.R;
import com.bumptech.glide.Glide;
import java.util.List;

public class ImageListAdapter extends ArrayAdapter<Uri> {
    private Context mContext;
    private int mResource;

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

        ZoomableImageView zoomableImageView = row.findViewById(R.id.img_obtenida);

        // Obtiene la URI de la posición actual
        Uri uri = getItem(position);
        if (uri != null) {
            // Carga las imágenes con Glide
            Glide.with(mContext)
                    .load(uri)
                    .fitCenter()
                    .into(zoomableImageView);
        } else {
            zoomableImageView.setImageResource(R.drawable.baseline_error_24); // Cargar imagen de error
        }

        return row;
    }
}
