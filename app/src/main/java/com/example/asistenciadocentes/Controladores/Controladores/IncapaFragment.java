package com.example.asistenciadocentes.Controladores.Controladores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.asistenciadocentes.Controladores.Adaptadores.AdapterDocente;
import com.example.asistenciadocentes.Controladores.Adaptadores.AdapterPermisos;
import com.example.asistenciadocentes.Controladores.DataBase.Permisos;
import com.example.asistenciadocentes.Controladores.DataBase.Usuario;
import com.example.asistenciadocentes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IncapaFragment extends Fragment {
    public ListView listaIncapacidades;

    public AdapterPermisos adapter;
    FirebaseAuth mAuth;
    DatabaseReference referencia = FirebaseDatabase.getInstance().getReference(); // Inicialización directa
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_incapa, container, false);
        listaIncapacidades = rootView.findViewById(R.id.Lista_permi);
        cargarLista();
        return rootView;
    }

    //Cargamos la lista
    private void cargarLista() {
        List<Permisos> lista = new ArrayList<>();
        referencia.child("tb_permisos").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().getValue() != null) {
                HashMap<String, Object> data = (HashMap<String, Object>) task.getResult().getValue();
                for (String key : data.keySet()) {
                    HashMap<String, Object> data2 = (HashMap<String, Object>) data.get(key);
                    Permisos incapacidad = new Permisos(
                            data2.get("id_permiso").toString(),
                            data2.get("tipo").toString(),
                            data2.get("descripcion").toString(),
                            data2.get("fecha_creacion").toString(),
                            data2.get("fecha_incio").toString(),
                            data2.get("fecha_fin").toString(),
                            data2.get("imagen").toString(),
                            data2.get("estado").toString(),
                            data2.get("id_usuario").toString()
                    );

                    // Agregar el docente a la lista
                    lista.add(incapacidad);
                }
                adapter = new AdapterPermisos((ArrayList<Permisos>) lista, getContext());
                listaIncapacidades.setAdapter(adapter);
            }
        });
    }

}