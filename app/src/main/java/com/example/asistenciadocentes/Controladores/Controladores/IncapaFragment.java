package com.example.asistenciadocentes.Controladores.Controladores;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.asistenciadocentes.Controladores.Adaptadores.AdapterPermisos;
import com.example.asistenciadocentes.Controladores.DataBase.Permisos;
import com.example.asistenciadocentes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class IncapaFragment extends Fragment {

    public ListView listaIncapacidades;
    public AdapterPermisos adapter;
    DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    ArrayList<Permisos> lista = new ArrayList<>(); // Definir la lista a nivel de clase

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_incapa, container, false);
        listaIncapacidades = rootView.findViewById(R.id.Lista_permi);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }

        // Cargar la lista una vez cuando se inicia el fragmento
        cargarLista();
        // Escuchar cambios en la base de datos
        referencia.child("tb_permisos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lista.clear(); // Limpiar la lista actual

                // Aquí puedes mantener tu código existente
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HashMap<String, Object> data2 = (HashMap<String, Object>) snapshot.getValue();
                    Permisos incapacidad = new Permisos(
                            //Validamos que no sean nulos
                            data2.get("id_permiso").toString(),
                            data2.get("tipo").toString(),
                            data2.get("descripcion").toString(),
                            data2.get("fecha_creacion").toString(),
                            data2.get("fecha_incio").toString(),
                            data2.get("fecha_fin").toString(),
                            (data2.get("imagen") != null) ? data2.get("imagen").toString() : "",
                            data2.get("estado").toString(),
                            data2.get("id_usuario").toString()
                    );
                    // Agregar el docente a la lista
                    lista.add(incapacidad);
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged(); // Notificar al adapter que los datos han cambiado
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores al obtener datos
                Log.e("Error Add", databaseError.getMessage());
            }
        });

        return rootView;
    }
    private void cargarLista() {
        referencia.child("tb_permisos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HashMap<String, Object> data2 = (HashMap<String, Object>) snapshot.getValue();
                    Permisos incapacidad = new Permisos(

                            data2.get("id_permiso").toString(),
                            data2.get("tipo").toString(),
                            data2.get("descripcion").toString(),
                            data2.get("fecha_creacion").toString(),
                            data2.get("fecha_incio").toString(),
                            data2.get("fecha_fin").toString(),
                            (data2.get("imagen") != null) ? data2.get("imagen").toString() : "",
                            data2.get("estado").toString(),
                            data2.get("id_usuario").toString()
                    );
                    // Agregar el docente a la lista
                    lista.add(incapacidad);
                }

                // Crear y configurar el adapter después de cargar la lista
                adapter = new AdapterPermisos(lista, getContext());
                listaIncapacidades.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores al obtener datos
                Log.e("Error Add", databaseError.getMessage());
                //Previene que se cierre la aplicacion y reinicia la actividad
                startActivity(getActivity().getIntent());
                getActivity().finish();
            }
        });
    }
}

