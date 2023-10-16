package com.example.asistenciadocentes.Controladores.Controladores;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.asistenciadocentes.R;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    DatabaseReference referencia;
    String codigoUsuario;
    String DocenteHorario = " ";
    private static final int RC_SIGN_IN = 9001;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        TextView codigo = view.findViewById(R.id.CoidgoDoc);
        TextView DiasDocente = view.findViewById(R.id.txtcode);

        //Obtenemos el codigo del usuario de la autenticacion
        mAuth = FirebaseAuth.getInstance();
        String mail = mAuth.getCurrentUser().getEmail();
        //Buscamos el email en la base de datos
        referencia = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("tb_usuarios");
        referencia.orderByChild("Correo").equalTo(mail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            codigoUsuario = snapshot.getKey();
                        }
                        codigo.setText(codigoUsuario);
                        ObtenerInfo(codigoUsuario);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //Declaramos el button de generar permiso
        Button btnGenerarPermiso = (Button) view.findViewById(R.id.Btngenerador);
        btnGenerarPermiso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Mandamos a llamar a la clase GenerarPermisoFragment
                Bundle bundle = new Bundle();
                bundle.putString("codigoUsuario", codigoUsuario);
                GenerarPermisoFragment generarPermisoFragment = new GenerarPermisoFragment();
                generarPermisoFragment.setArguments(bundle);
                //Reemplazamos el fragmento
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, generarPermisoFragment).commit();
            }
        });

        return view;
    }


    private void ObtenerInfo(String Codigo){
        DatabaseReference referencia = FirebaseDatabase.getInstance().getReference("tb_usuarios").child(Codigo);
        referencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String nombre = dataSnapshot.child("Nombre").getValue().toString();
                String titulo = dataSnapshot.child("Titulo").getValue().toString();
                String diasString = String.valueOf(dataSnapshot.child("Dias").getValue());
                String EntradaString = String.valueOf(dataSnapshot.child("Entrada").getValue());
                String SalidaString = String.valueOf(dataSnapshot.child("Salida").getValue());
                // Removemos los corchetes "[" y "]"
                diasString = diasString.substring(1, diasString.length() - 1);
                EntradaString = EntradaString.substring(1, EntradaString.length() - 1);
                SalidaString = SalidaString.substring(1, SalidaString.length() - 1);
                // Convertimos la cadena a una lista de días
                List<String> diasList = Arrays.asList(diasString.split(", "));
                List<String> EntradaList = Arrays.asList(EntradaString.split(", "));
                List<String> SalidaList = Arrays.asList(SalidaString.split(", "));
                // Convertimos las listas de días, entradas y salidas a una cadena separada por comas
                String dias = TextUtils.join(", ", diasList);
                String Entrada = "Entradas: "+TextUtils.join(", ", EntradaList);
                String Salida = "Salidas: "+TextUtils.join(", ", SalidaList);
                TextView name = getView().findViewById(R.id.nom_docente);
                TextView title = getView().findViewById(R.id.titulo_docente);
                TextView diasDocente = getView().findViewById(R.id.txtcode);
                TextView horario = getView().findViewById(R.id.txt_horario);
                name.setText(nombre);
                title.setText(titulo);
                diasDocente.setText(dias);
                horario.setText(Entrada+"\n"+Salida);
                //Obtenemos dia actual y hora actual
                String diaActual = new java.text.SimpleDateFormat("EEEE").format(new java.util.Date());
                String horaActual = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
                String tipoRegistro = determinarTipoRegistro(horaActual, EntradaList, SalidaList);
                // Construir el contenido del QR
                String contenidoQR = diaActual + "-" + horaActual + "-" + codigoUsuario + "-" + tipoRegistro;
                // Verificamos si el día actual está en la lista de días
                if (diasList.contains(diaActual)) {
                    // Verificamos si la hora actual está dentro de 1 hora antes y 1 hora después de las horas de entrada
                    for (String horaEntrada : EntradaList) {
                        if (horaValida(horaActual, horaEntrada)) {
                            Log.d("TAG", "Hora válida: " + horaEntrada);
                            try {
                                QRCodeWriter writer = new QRCodeWriter();
                                BitMatrix bitMatrix = writer.encode(contenidoQR, BarcodeFormat.QR_CODE, 290, 339);
                                int width = bitMatrix.getWidth();
                                int height = bitMatrix.getHeight();
                                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                for (int x = 0; x < width; x++) {
                                    for (int y = 0; y < height; y++) {
                                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                                    }
                                }
                                ImageView imageViewQrCode = getView().findViewById(R.id.imgQR);
                                imageViewQrCode.setImageBitmap(bitmap);
                                return; // No es necesario seguir buscando si ya encontramos una hora válida
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                        }else{
                            for(String HoraSalida : SalidaList){
                                if(horaValida(horaActual, HoraSalida)){
                                    Log.d("TAG", "Hora válida: " + HoraSalida);
                                    try {
                                        QRCodeWriter writer = new QRCodeWriter();
                                        BitMatrix bitMatrix = writer.encode(contenidoQR, BarcodeFormat.QR_CODE, 290, 339);
                                        int width = bitMatrix.getWidth();
                                        int height = bitMatrix.getHeight();
                                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                        for (int x = 0; x < width; x++) {
                                            for (int y = 0; y < height; y++) {
                                                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                                            }
                                        }
                                        ImageView imageViewQrCode = getView().findViewById(R.id.imgQR);
                                        imageViewQrCode.setImageBitmap(bitmap);
                                        return; // No es necesario seguir buscando si ya encontramos una hora válida
                                    } catch (WriterException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
    private boolean horaValida(String horaActual, String horaEntrada) {
        // Parsear las horas
        Log.d("TAG", "Entrando en horaValida");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date horaActualDate;
        Date horaEntradaDate;
        try {
            horaActualDate = sdf.parse(horaActual);
            horaEntradaDate = sdf.parse(horaEntrada);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        // Verificar si la hora actual está dentro de 1 hora antes y 1 hora después de la hora de entrada
        Calendar calEntrada = Calendar.getInstance();
        calEntrada.setTime(horaEntradaDate);
        calEntrada.add(Calendar.HOUR, -1); // Restamos 1 hora
        Calendar calSalida = Calendar.getInstance();
        calSalida.setTime(horaEntradaDate);
        calSalida.add(Calendar.HOUR, 1); // Sumamos 1 hora
        //Mostramos en un log tag lo que retorna la funcion
        Log.d("TAG", "Datos obtenidos: " + horaEntradaDate + " " + calEntrada.getTime() + " " + calSalida.getTime() + " " + horaActualDate);
        return horaActualDate.after(calEntrada.getTime()) && horaActualDate.before(calSalida.getTime());
    }
    private String determinarTipoRegistro(String horaActual, List<String> EntradaList, List<String> SalidaList) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date horaActualDate;

        try {
            horaActualDate = sdf.parse(horaActual);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Sin marcar";
        }

        for (int i = 0; i < EntradaList.size(); i++) {
            String horaEntrada = EntradaList.get(i);
            String horaSalida = SalidaList.get(i);

            try {
                Date horaEntradaDate = sdf.parse(horaEntrada);
                Date horaSalidaDate = sdf.parse(horaSalida);

                Calendar calEntrada = Calendar.getInstance();
                calEntrada.setTime(horaEntradaDate);
                Calendar calSalida = Calendar.getInstance();
                calSalida.setTime(horaSalidaDate);

                if (horaActualDate.after(calEntrada.getTime()) && horaActualDate.before(calSalida.getTime())) {
                    return "entrada";
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return "salida";
    }

}