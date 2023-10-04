package com.example.asistenciadocentes.Controladores.Controladores;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.asistenciadocentes.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    DatabaseReference referencia;
    String codigoUsuario;
    private static final int RC_SIGN_IN = 9001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declaramos los componentes
        Button btnIngresar = findViewById(R.id.BtnLog);
        ImageView btnGoogle = findViewById(R.id.BtnGoogle);
        EditText carnet = findViewById(R.id.Edit_Carnet);
        EditText password = findViewById(R.id.Edit_Contra);

        mAuth = FirebaseAuth.getInstance();

        // Configuración de inicio de sesión con Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configuración de evento de clic para el botón de Google
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        handleAccountSelection(); // Comprobamos si el usuario ya ha iniciado sesión
        //Validamos si ingresa sesion con codigo y contraseña
        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String carnetUsuario = carnet.getText().toString();
                String passwordUsuario = password.getText().toString();
                if (carnetUsuario.isEmpty() || passwordUsuario.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Debe ingresar todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    referencia = FirebaseDatabase.getInstance().getReference("tb_usuarios");
                    //Se obtiene el codigo y se buscara el correo para iniciar sesion con correo y contraseña
                    referencia.orderByKey().equalTo(carnetUsuario).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String correo = snapshot.child("Correo").getValue().toString();
                                    mAuth.signInWithEmailAndPassword(correo, passwordUsuario)
                                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        // El usuario ha iniciado sesión exitosamente
                                                        //Enviamos el codigo del usuario a la siguiente actividad
                                                        Intent intent = new Intent(MainActivity.this, Home.class);
                                                        intent.putExtra("codigoUsuario", carnetUsuario);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        // Si falla la autenticación muestra alerta
                                                        AlertaEmail();
                                                    }
                                                }
                                            });
                                }
                            }else{
                                Toast.makeText(MainActivity.this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Manejar el error de la consulta
                        }
                    });
                }
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            // El usuario ha iniciado sesión exitosamente
                            // Validamos que el email termine en @ues.edu.sv
                            if (user.getEmail().endsWith("@ues.edu.sv")) {
                                String email = user.getEmail();
                                // Realizar consulta a la base de datos
                                DatabaseReference referencia = FirebaseDatabase.getInstance().getReference("tb_usuarios");
                                referencia.orderByChild("Correo").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                codigoUsuario = snapshot.getKey();
                                            }
                                            //Enviamos el codigo del usuario a la siguiente actividad
                                            Intent intent = new Intent(MainActivity.this, Home.class);
                                            intent.putExtra("codigoUsuario", codigoUsuario);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            AlertaEmail();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Manejar el error de la consulta
                                    }
                                });
                            }
                        } else {
                            // Si falla la autenticación
                            AlertaEmail();
                            Toast.makeText(MainActivity.this, "Fallo en inicio de sesión", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void handleAccountSelection() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, Home.class);
            startActivity(intent);
            finish();
        }
    }
    private void AlertaEmail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("No es un Usuario de la Universidad de El Salvador")
                .setTitle("Error al iniciar sesión")
                .setPositiveButton("Reintentar", (dialog, which) -> {
                    // Se cierra la sesión de Firebase
                    FirebaseAuth.getInstance().signOut();
                    // Se cierra la sesión de Google
                    mGoogleSignInClient.signOut();
                    // Se reintentará el inicio de sesión
                    signInWithGoogle();
                })
                .setNegativeButton("Salir", (dialog, which) -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    user.delete();
                    FirebaseAuth.getInstance().signOut();
                    mGoogleSignInClient.signOut();
                    finish();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
