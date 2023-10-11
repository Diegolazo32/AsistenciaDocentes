package com.example.asistenciadocentes.Controladores.Adaptadores;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.asistenciadocentes.R;

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
        ImageView imageView = row.findViewById(R.id.img_obtenida);
        Uri uri = getItem(position);
        // Carga la imagen en el ImageView
        imageView.setImageURI(uri);
        return row;
    }
}
