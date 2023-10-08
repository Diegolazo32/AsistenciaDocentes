package com.example.asistenciadocentes.Controladores.Controladores;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.asistenciadocentes.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    String codigoUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        //Obtenemos el codigo del usuario del intent anterior
        codigoUsuario = getIntent().getStringExtra("codigoUsuario");
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Obtener el ImageView del header del NavigationView
        View headerView = navigationView.getHeaderView(0);
        ImageView navUserPhoto = headerView.findViewById(R.id.ImgNavHed);
        navUserPhoto.setDrawingCacheEnabled(true);
        navUserPhoto.buildDrawingCache();
        Bitmap bitmap = navUserPhoto.getDrawingCache();
        // Cargar la foto de perfil del usuario
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String personPhotoUrl = account.getPhotoUrl().toString();
            // Colocamos en un bitmap la imagen de perfil
            Glide.with(this).load(personPhotoUrl).into(navUserPhoto);
            if(codigoUsuario != null){
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference referencia = database.getReference("tb_usuarios").child(codigoUsuario).child("PP");
                String PPURL = account.getPhotoUrl().toString();
                //Cargamos la imagen de perfil en navUserPhoto
                Glide.with(this).load(PPURL).into(navUserPhoto);

                //Obtenemos El nombre y el titulo del usuario
                DatabaseReference referencia2 = FirebaseDatabase.getInstance().getReference("tb_usuarios").child(codigoUsuario);
                referencia2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String nombre = dataSnapshot.child("Nombre").getValue().toString();
                        String titulo = dataSnapshot.child("Titulo").getValue().toString();
                        TextView navUserName = headerView.findViewById(R.id.Head_TxtNombre);
                        TextView navUserTitulo = headerView.findViewById(R.id.Head_Txtdescp);
                        navUserName.setText(nombre);
                        navUserTitulo.setText(titulo);
                        //Cargamos la imagen de perfil en navUserPhoto
                        Glide.with(Home.this).load(PPURL).into(navUserPhoto);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }else{
                //Obtenemos el codigo del usuario de la autenticacion
                mAuth = FirebaseAuth.getInstance();
                String mail = mAuth.getCurrentUser().getEmail();
                //Buscamos el email en la base de datos
                DatabaseReference referencia = FirebaseDatabase.getInstance().getReference("tb_usuarios");
                referencia.orderByChild("Correo").equalTo(mail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                codigoUsuario = snapshot.getKey();
                            }
                            //Obtenemos El nombre y el titulo del usuario
                            DatabaseReference referencia = FirebaseDatabase.getInstance().getReference("tb_usuarios").child(codigoUsuario);
                            referencia.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String nombre = dataSnapshot.child("Nombre").getValue().toString();
                                    String titulo = dataSnapshot.child("Titulo").getValue().toString();
                                    TextView navUserName = headerView.findViewById(R.id.Head_TxtNombre);
                                    TextView navUserTitulo = headerView.findViewById(R.id.Head_Txtdescp);
                                    navUserName.setText(nombre);
                                    navUserTitulo.setText(titulo);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        }
        // Mostrar el bitmap en un ImageView
        navUserPhoto.setImageBitmap(bitmap);

        // Inicializar GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            // Mandamos codigoUsuario a HomeFragment
            Bundle bundle = new Bundle();
            bundle.putString("codigoUsuario", codigoUsuario);
            HomeFragment homeFragment = new HomeFragment();
            homeFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
        // Agregar OnClickListener a la imagen para abrir el cajón de navegación
        navUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Bundle bundle = new Bundle();
                    bundle.putString("codigoUsuario", codigoUsuario);
                    HomeFragment homeFragment = new HomeFragment();
                    homeFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
                } else if (itemId == R.id.nav_bit) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BitacoraFragment()).commit();
                } else if (itemId == R.id.nav_doc) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DocenteFragment()).commit();
                } else if (itemId == R.id.nav_incap) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new IncapaFragment()).commit();
                } else if (itemId == R.id.nav_logout) {
                    if (mGoogleSignInClient != null) {
                        mGoogleSignInClient.signOut();
                    }
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(Home.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }//Cierre del onCreate
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Si borro esto ya no funciona jaja (No lo borren)
        return false;
    }
}//Cierre de la clase Home
