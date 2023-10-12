package com.example.asistenciadocentes.Controladores.Adaptadores;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.asistenciadocentes.Controladores.DataBase.Permisos;
import com.example.asistenciadocentes.Controladores.DataBase.Usuario;
import com.example.asistenciadocentes.R;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;

public class AdapterPermisos extends BaseAdapter {
    public ArrayList<Permisos> datoPermisos;
    public Context context;
    public ArrayList<Uri> uri;
    public AdapterPermisos(ArrayList<Permisos> listadePermisos, Context context) {
        datoPermisos = listadePermisos;
        this.context = context;
        uri = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return datoPermisos.size();
    }

    @Override
    public Object getItem(int i) {
        return datoPermisos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Permisos permiso = (Permisos) getItem(i);
        view = LayoutInflater.from(context).inflate(R.layout.boxpermiso, null);
        TextView nombre = view.findViewById(R.id.Perm_nombre_comp);
        TextView codigo = view.findViewById(R.id.codigo_asig);
        TextView motivo = view.findViewById(R.id.motivo_de_i);
        TextView fecha = view.findViewById(R.id.fecha_permiso);
        Button Constancia = view.findViewById(R.id.Btn_Constancias);
        Button aceptar = view.findViewById(R.id.Btn_Aceptar_sol);
        Button rechazar = view.findViewById(R.id.Btn_Denegar_sol);
        ImageView foto = view.findViewById(R.id.img_baja);
        //Validamos el estado, dependiendo del estado mostramos diferentes vistas
        if (permiso.estado.equals("Aceptado")) {
            aceptar.setVisibility(View.GONE);
            rechazar.setVisibility(View.GONE);
            view = LayoutInflater.from(context).inflate(R.layout.box_granted_permiso, null);
            TextView nombreaceptado = view.findViewById(R.id.Perm_nombre_comp);
            TextView codigoaceptado = view.findViewById(R.id.codigo_asig);
            TextView motivoaceptado = view.findViewById(R.id.motivo_de_i);
            TextView fechaaceptado = view.findViewById(R.id.fecha_permiso);
            Button Constanciaaceptado= view.findViewById(R.id.Btn_Constancias);
            ImageView fotoaceptado = view.findViewById(R.id.img_baja);
            String iduser = permiso.id_usuario;
            codigoaceptado.setText(permiso.id_usuario);
            motivoaceptado.setText(permiso.tipo);
            String date = permiso.fecha_incio+" - "+permiso.fecha_fin;
            fechaaceptado.setText(date);
            String[] imagenes = permiso.imagen.split(",");
            DatabaseReference referencia = FirebaseDatabase.getInstance().getReference("tb_usuarios").child(iduser);
            referencia.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Usuario usuario = task.getResult().getValue(Usuario.class);
                    nombreaceptado.setText(usuario.Nombre);
                    String personPhotoUrl = usuario.PP;
                    Glide.with(context)
                            .load(personPhotoUrl)
                            .fitCenter()  // Ajusta el tamaño de la imagen al tamaño de la ImageView manteniendo la proporción
                            .into(fotoaceptado);
                }
            });
            Constanciaaceptado.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uri.clear();
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                    View mView = LayoutInflater.from(context).inflate(R.layout.modal_imagenes, null);
                    ListView listView = mView.findViewById(R.id.list_view_images);
                    if (imagenes != null && imagenes.length > 0) {
                        for (int j = 0; j < imagenes.length; j++) {
                            uri.add(Uri.parse(imagenes[j]));
                        }
                    }
                    for (Uri uri : uri) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                            // Si no hay excepciones, la URI es válida
                            Log.d("URI Check", "URI válida: " + uri.toString());
                        } catch (IOException e) {
                            // Si hay una excepción, la URI no es válida
                            Log.e("URI Check", "Error al cargar la imagen desde la URI: " + uri.toString());
                        }
                    }
                    ImageListAdapter adapter = new ImageListAdapter(context, R.layout.img_vista_isss, uri);
                    listView.setAdapter(adapter);
                    mBuilder.setView(mView);
                    mBuilder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                }
            });

        } else if (permiso.estado.equals("Rechazado")) {
            aceptar.setVisibility(View.GONE);
            rechazar.setVisibility(View.GONE);
            view = LayoutInflater.from(context).inflate(R.layout.box_denied_permiso, null);
            TextView nombredenied = view.findViewById(R.id.Perm_nombre_comp);
            TextView codigodenied = view.findViewById(R.id.codigo_asig);
            TextView motivodenied = view.findViewById(R.id.motivo_de_i);
            TextView fechadenied = view.findViewById(R.id.fecha_permiso);
            Button Constanciadenied= view.findViewById(R.id.Btn_Constancias);
            ImageView fotodenied = view.findViewById(R.id.img_baja);
            String iduser = permiso.id_usuario;
            codigodenied.setText(permiso.id_usuario);
            motivodenied.setText(permiso.tipo);
            String date = permiso.fecha_incio+" - "+permiso.fecha_fin;
            fechadenied.setText(date);
            String[] imagenes = permiso.imagen.split(",");
            DatabaseReference referencia = FirebaseDatabase.getInstance().getReference("tb_usuarios").child(iduser);
            referencia.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Usuario usuario = task.getResult().getValue(Usuario.class);
                    nombredenied.setText(usuario.Nombre);
                    String personPhotoUrl = usuario.PP;
                    Glide.with(context)
                            .load(personPhotoUrl)
                            .fitCenter()  // Ajusta el tamaño de la imagen al tamaño de la ImageView manteniendo la proporción
                            .into(fotodenied);
                }
            });
            Constanciadenied.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uri.clear();
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                    View mView = LayoutInflater.from(context).inflate(R.layout.modal_imagenes, null);
                    ListView listView = mView.findViewById(R.id.list_view_images);
                    if (imagenes != null && imagenes.length > 0) {
                        for (int j = 0; j < imagenes.length; j++) {
                            uri.add(Uri.parse(imagenes[j]));
                        }
                    }
                    for (Uri uri : uri) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                            // Si no hay excepciones, la URI es válida
                            Log.d("URI Check", "URI válida: " + uri.toString());
                        } catch (IOException e) {
                            // Si hay una excepción, la URI no es válida
                            Log.e("URI Check", "Error al cargar la imagen desde la URI: " + uri.toString());
                        }
                    }
                    ImageListAdapter adapter = new ImageListAdapter(context, R.layout.img_vista_isss, uri);
                    listView.setAdapter(adapter);
                    mBuilder.setView(mView);
                    mBuilder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                }
            });
        } else if (permiso.estado.equals("Pendiente")) {
            aceptar.setVisibility(View.VISIBLE);
            rechazar.setVisibility(View.VISIBLE);
            String iduser = permiso.id_usuario;
            codigo.setText(permiso.id_usuario);
            motivo.setText(permiso.tipo);
            String date = permiso.fecha_incio+" - "+permiso.fecha_fin;
            fecha.setText(date);
            String[] imagenes = permiso.imagen.split(",");
            Constancia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uri.clear();
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                    View mView = LayoutInflater.from(context).inflate(R.layout.modal_imagenes, null);
                    ListView listView = mView.findViewById(R.id.list_view_images);
                    if (imagenes != null && imagenes.length > 0) {
                        for (int j = 0; j < imagenes.length; j++) {
                            uri.add(Uri.parse(imagenes[j]));
                        }
                    }
                    for (Uri uri : uri) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                            // Si no hay excepciones, la URI es válida
                            Log.d("URI Check", "URI válida: " + uri.toString());
                        } catch (IOException e) {
                            // Si hay una excepción, la URI no es válida
                            Log.e("URI Check", "Error al cargar la imagen desde la URI: " + uri.toString());
                        }
                    }
                    ImageListAdapter adapter = new ImageListAdapter(context, R.layout.img_vista_isss, uri);
                    listView.setAdapter(adapter);


                    mBuilder.setView(mView);
                    mBuilder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                }
            });

            DatabaseReference referencia = FirebaseDatabase.getInstance().getReference("tb_usuarios").child(iduser);
            referencia.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Usuario usuario = task.getResult().getValue(Usuario.class);
                    nombre.setText(usuario.Nombre);
                    String personPhotoUrl = usuario.PP;
                    Glide.with(context)
                            .load(personPhotoUrl)
                            .fitCenter()  // Ajusta el tamaño de la imagen al tamaño de la ImageView manteniendo la proporción
                            .into(foto);
                }
            });
            aceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Cambiamos el estado del permiso
                    DatabaseReference referencia = FirebaseDatabase.getInstance().getReference("tb_permisos").child(permiso.id_permiso);
                    referencia.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Permisos permisos = task.getResult().getValue(Permisos.class);
                            permisos.estado = "Aceptado";
                            referencia.setValue(permisos);
                        }
                    });
                }
            });
            rechazar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Cambiamos el estado del permiso
                    DatabaseReference referencia = FirebaseDatabase.getInstance().getReference("tb_permisos").child(permiso.id_permiso);
                    referencia.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Permisos permisos = task.getResult().getValue(Permisos.class);
                            permisos.estado = "Rechazado";
                            referencia.setValue(permisos);
                        }
                    });
                }
            });
        }

        return view;
    }
}
