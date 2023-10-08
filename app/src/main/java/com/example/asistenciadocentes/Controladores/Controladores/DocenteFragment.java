package com.example.asistenciadocentes.Controladores.Controladores;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.asistenciadocentes.Controladores.Adaptadores.AdapterDocente;
import com.example.asistenciadocentes.Controladores.DataBase.Usuario;
import com.example.asistenciadocentes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class DocenteFragment extends Fragment {
    String idaux;
    public ListView listvdocente;
    ArrayList<String> diasSeleccionados = new ArrayList<>();
    ArrayList<String> horasEntrada = new ArrayList<>();
    ArrayList<String> horasSalida = new ArrayList<>();

    public ArrayList<Usuario> listadeDocentes;
    public AdapterDocente adapter;
    FirebaseAuth mAuth;
    Button btnaggDocente;

    DatabaseReference referencia = FirebaseDatabase.getInstance().getReference(); // Inicialización directa

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_docente, container, false);
        btnaggDocente = view.findViewById(R.id.Add_Doc); // Asignación de 'btnaggDocente'
        Cargar_Docente();
        // Conectamos la base de datos
        referencia = FirebaseDatabase.getInstance().getReference();
        listvdocente = view.findViewById(R.id.list_docentes); // Asignación de 'listvdocente'

        btnaggDocente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Valida si ya hay un bottom dialog abierto si ya esta abierto no abre otro
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                if (fragmentManager.findFragmentByTag("bottom_dialog") == null) {
                    showBottomDialog();
                }
            }
        });

        return view; // Retorna la vista inflada.
    }

    public void Cargar_Docente() {
        listadeDocentes = new ArrayList<Usuario>();
        referencia.child("tb_usuarios").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().getValue() != null) {
                HashMap<String, Object> data = (HashMap<String, Object>) task.getResult().getValue();
                for (String key : data.keySet()) {
                    HashMap<String, Object> data2 = (HashMap<String, Object>) data.get(key);
                    Usuario docente = new Usuario();
                    docente.Nombre = data2.get("Nombre").toString();
                    docente.Titulo = data2.get("Titulo").toString();
                    docente.Correo = data2.get("Correo").toString();
                    docente.Dias = data2.get("Dias").toString();
                    docente.Entrada = data2.get("Entrada").toString();
                    docente.Salida = data2.get("Salida").toString();
                    docente.Rol = data2.get("Rol").toString();
                    docente.Estado = Boolean.parseBoolean(data2.get("Estado").toString());
                    docente.PP = data2.get("PP").toString();
                    docente.Codigo = data2.get("Codigo").toString();

                    // Agregar el docente a la lista
                    listadeDocentes.add(docente);
                }
                adapter = new AdapterDocente(listadeDocentes, getContext());
                listvdocente.setAdapter(adapter);
            }
        });
    }
    private void mostrarAlertDialogDiasTrabajados(final TextView textView, final TextView textView2, final TextView textView3) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Selecciona los días trabajados");
        final boolean[] seleccionados = new boolean[7]; // Array para almacenar las selecciones
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        builder.setMultiChoiceItems(diasSemana, seleccionados, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    mostrarTimePickerDialog("Salida para " + diasSemana[which], horasSalida, which);
                    mostrarTimePickerDialog("Entrada para " + diasSemana[which], horasEntrada, which);
                } else {
                    horasEntrada.remove(which);
                    horasSalida.remove(which);
                    textView2.setText(horasEntrada.toString());
                    textView3.setText(horasSalida.toString());
                }
                seleccionados[which] = isChecked;
            }
        });

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                diasSeleccionados.clear();
                for (int i = 0; i < seleccionados.length; i++) {
                    if (seleccionados[i]) {
                        diasSeleccionados.add(diasSemana[i]);
                    }
                }
                // Mostrar los días seleccionados en el TextView
                textView.setText(diasSeleccionados.toString());
                textView2.setText(horasEntrada.toString());
                textView3.setText(horasSalida.toString());
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                diasSeleccionados.clear();
                horasEntrada.clear();
                horasSalida.clear();
                dialog.dismiss();
            }
        });
        builder.show();
    }
    private void mostrarTimePickerDialog(String title, final ArrayList<String> listaHoras, final int indexDia) {
        Calendar cal = Calendar.getInstance();
        int hora = cal.get(Calendar.HOUR_OF_DAY);
        int minuto = cal.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Formatear la hora seleccionada como string
                String amPm;
                if (hourOfDay >= 12) {
                    amPm = "PM";
                    if (hourOfDay > 12) {
                        hourOfDay -= 12;
                    }
                } else {
                    amPm = "AM";
                }

                String horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d %s", hourOfDay, minute, amPm);

                // Guardar la hora seleccionada en el ArrayList
                listaHoras.add(indexDia, horaSeleccionada);
            }
        }, hora, minuto, false);

        timePickerDialog.setTitle(title);
        timePickerDialog.show();
    }
    private void showBottomDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.adddocente);

        EditText nombre = dialog.findViewById(R.id.txt_nom);
        EditText correo = dialog.findViewById(R.id.txt_correo);
        EditText titulo = dialog.findViewById(R.id.txt_titulo);
        Button dias = dialog.findViewById(R.id.btn_dia);
        Button guardar = dialog.findViewById(R.id.btn_confirmar);
        ImageView cerrar = dialog.findViewById(R.id.cancelButton);
        TextView txtentrada = dialog.findViewById(R.id.txt_entrada);
        TextView txtsalida = dialog.findViewById(R.id.txt_salida);
        TextView txtdias = dialog.findViewById(R.id.txt_diastra);

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtdias.setText(diasSeleccionados.toString());
                txtentrada.setText(horasEntrada.toString());
                txtsalida.setText(horasSalida.toString());
                mostrarAlertDialogDiasTrabajados(txtdias, txtentrada, txtsalida);
            }
        });

        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View mainView = getActivity().getWindow().getDecorView();
                mainView.setBackgroundResource(android.R.color.transparent);
                dialog.dismiss();
            }
        });
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                referencia.child("tb_usuarios").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getValue() != null) {
                        mAuth = FirebaseAuth.getInstance();
                        HashMap<String, Object> data = (HashMap<String, Object>) task.getResult().getValue();
                        int maxID = -1;
                        for (String key : data.keySet()) {
                            int currentID = Integer.parseInt(key);
                            if (currentID > maxID) {
                                maxID = currentID;
                            }
                        }
                        // Incrementar el ID
                        int nuevoID = maxID + 1;
                        String nuevoIDFormateado = String.format("%04d", nuevoID);
                        // Guardar el docente con el nuevo ID
                        String nuevoIDString = String.valueOf(nuevoIDFormateado);
                        Usuario usuario = new Usuario(nombre.getText().toString(), titulo.getText().toString(), correo.getText().toString(), txtdias.getText().toString(), txtentrada.getText().toString(), txtsalida.getText().toString(), true, "Docente", "https://firebasestorage.googleapis.com/v0/b/asistencia-docente-1e9f0.appspot.com/o/Imagenes%2Fperfil.png?alt=media&token=8b5b8b1a-5b0a-4b0a-9b0a-5b0a4b0a9b0a", nuevoIDString);
                        referencia.child("tb_usuarios").child(nuevoIDString).setValue(usuario);
                        Toast.makeText(getContext(), "Docente agregado correctamente", Toast.LENGTH_SHORT).show();
                        mAuth.createUserWithEmailAndPassword(correo.getText().toString(), "Minerva.23");
                        dialog.dismiss();
                    }
                });
            }
        });
        View dialogContainer = dialog.findViewById(R.id.dialogContainer);
        final float[] startY = {0};
        final float[] offsetY = {0};
        final int maxHeight = getResources().getDisplayMetrics().heightPixels - 100; // Establece la altura máxima permitida
        dialogContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startY[0] = event.getRawY();
                        offsetY[0] = dialogContainer.getY() - startY[0];
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float newY = event.getRawY() + offsetY[0];
                        if (newY < 0) {
                            newY = 0;
                        }
                        if (newY > maxHeight) {
                            newY = maxHeight;
                        }
                        dialogContainer.setY(newY);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (dialogContainer.getY() + dialogContainer.getHeight() >= maxHeight) {
                            dialog.dismiss();
                        }
                        return true;
                }
                return false;
            }
        });

    }
}
