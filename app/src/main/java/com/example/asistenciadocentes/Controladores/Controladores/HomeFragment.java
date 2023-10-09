package com.example.asistenciadocentes.Controladores.Controladores;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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


public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    DatabaseReference referencia;
    String codigoUsuario;
    private static final int RC_SIGN_IN = 9001;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        TextView codigo = view.findViewById(R.id.CoidgoDoc);
        TextView name = view.findViewById(R.id.nom_docente);
        TextView titulo = view.findViewById(R.id.titulo_docente);
        //Obtenes el codigo del usuario de la actividad anterior
        codigoUsuario = getActivity().getIntent().getStringExtra("codigoUsuario");
        if (codigoUsuario != null){
            codigo.setText(codigoUsuario);
            ObtenerInfo(codigoUsuario);
        }else {
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
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode("https://www.youtube.com/watch?v=XE5GO-fMmDQ&t=240s", BarcodeFormat.QR_CODE, 290, 339);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ImageView imageViewQrCode = view.findViewById(R.id.imgQR);
            imageViewQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
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
                TextView name = getView().findViewById(R.id.nom_docente);
                TextView title = getView().findViewById(R.id.titulo_docente);
                name.setText(nombre);
                title.setText(titulo);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

}