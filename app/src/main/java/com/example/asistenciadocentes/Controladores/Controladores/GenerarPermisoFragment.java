package com.example.asistenciadocentes.Controladores.Controladores;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.ValueEventListener;


import com.example.asistenciadocentes.Controladores.Adaptadores.ImageListAdapter;
import com.example.asistenciadocentes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class GenerarPermisoFragment extends Fragment {

    private TextView startDateTextView;
    AtomicReference<String> imagenesFinales = new AtomicReference<>("");
    private static final int GALLERY_REQUEST_CODE = 1001;
    private List<Uri> uris = new ArrayList<>();
    private ListView listaImagenes;
    private TextView endDateTextView;
    private TextView diasDiferenciaTextView;
    private Calendar calendar;
    FirebaseAuth mAuth;
    DatabaseReference referencia;

    String codigoUsuario;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private DatePickerDialog.OnDateSetListener startDateListener, endDateListener;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            codigoUsuario = bundle.getString("codigoUsuario");
        }
        referencia = FirebaseDatabase.getInstance().getReference();
        View view = inflater.inflate(R.layout.fragment_generar_permiso, container, false);
        startDateTextView = view.findViewById(R.id.txt_date_ini);
        endDateTextView = view.findViewById(R.id.txt_date_fin);
        diasDiferenciaTextView = view.findViewById(R.id.txt_diferencias_dias);
        Spinner spTipo = view.findViewById(R.id.sp_tipo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.categorias, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipo.setAdapter(adapter);
        Button selectDatesButton = view.findViewById(R.id.Btn_fechas);
        ImageButton fotoisss = view.findViewById(R.id.Btn_isss);
        TextView difdias = view.findViewById(R.id.txt_diferencias_dias);
        TextView fechaini = view.findViewById(R.id.txt_date_ini);
        TextView fechafin = view.findViewById(R.id.txt_date_fin);
        EditText descp = view.findViewById(R.id.isss_descp);

        ImageView enviar = view.findViewById(R.id.edit_sent);
        ImageView cancelar = view.findViewById(R.id.cancelButton2);

        calendar = Calendar.getInstance();
        startDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateStartDateLabel();
            }
        };
        endDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateEndDateLabel();
            }
        };
        fotoisss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Limpiamos la lista de URIs
                uris.clear();

                showFotoDialog();
            }
        });
        selectDatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateListener);
            }
        });

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tipo = spTipo.getSelectedItem().toString();
                String fecha_ini = fechaini.getText().toString();
                String fecha_fin = fechafin.getText().toString();
                String dias = difdias.getText().toString();
                String descripcion = descp.getText().toString();
                String fecha_creacion = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                if (tipo.isEmpty() || fecha_ini.isEmpty() || fecha_fin.isEmpty() || dias.isEmpty() || descripcion.isEmpty() || uris.isEmpty() || codigoUsuario.isEmpty()) {
                    Toast.makeText(getContext(), "Debe llenar todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    List<String> listaUrls = new ArrayList<>();

                    for (Uri uri2 : uris) {
                        StorageReference riversRef = storageRef.child("images/" + uri2.getLastPathSegment());
                        riversRef.putFile(uri2)
                                .addOnSuccessListener(taskSnapshot -> {
                                    riversRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        String imageUrl = uri.toString();
                                        listaUrls.add(imageUrl);
                                        if (listaUrls.size() == uris.size()) {
                                            referencia.child("tb_permisos").orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    String lastKey = "";
                                                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                                        lastKey = childSnapshot.getKey();
                                                    }
                                                    // Generar el nuevo ID
                                                    int nuevoID = Integer.parseInt(lastKey) + 1;
                                                    String nuevoIDFormateado = String.format("%04d", nuevoID);
                                                    String imagenesFinales = TextUtils.join(",", listaUrls);
                                                    // Guardar las URLs en la base de datos
                                                    DatabaseReference permisosRef = referencia.child("tb_permisos").child(nuevoIDFormateado);
                                                    permisosRef.child("id_permiso").setValue(nuevoIDFormateado);
                                                    permisosRef.child("tipo").setValue(tipo);
                                                    permisosRef.child("descripcion").setValue(descripcion);
                                                    permisosRef.child("fecha_creacion").setValue(fecha_creacion);
                                                    permisosRef.child("fecha_incio").setValue(fecha_ini);
                                                    permisosRef.child("fecha_fin").setValue(fecha_fin);
                                                    permisosRef.child("estado").setValue("Pendiente");
                                                    permisosRef.child("id_usuario").setValue(codigoUsuario);
                                                    permisosRef.child("imagen").setValue(imagenesFinales);
                                                    Toast.makeText(getContext(), "Permiso enviado a revisión", Toast.LENGTH_SHORT).show();
                                                    uris.clear();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("codigoUsuario", codigoUsuario);
                                                    HomeFragment homeFragment = new HomeFragment();
                                                    homeFragment.setArguments(bundle);
                                                    getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    // Manejar errores al obtener el último ID
                                                    Log.e("Firebase", "Error al obtener el último ID" + databaseError.getMessage());
                                                }
                                            });
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Error al subir la imagen" + e.getMessage());
                                });
                    }
                }
            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uris.clear();
                Bundle bundle = new Bundle();
                bundle.putString("codigoUsuario", codigoUsuario);
                HomeFragment homeFragment = new HomeFragment();
                homeFragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
            }
        });
        return view;
    }
    private void showDatePickerDialog(DatePickerDialog.OnDateSetListener dateListener) {
        new DatePickerDialog(
                requireContext(),
                dateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
    private void updateStartDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        startDateTextView.setText(sdf.format(calendar.getTime()));
        showDatePickerDialog(endDateListener);
    }
    private void updateEndDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        endDateTextView.setText(sdf.format(calendar.getTime()));
        // Calcular la diferencia de días
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date startDate = sdfDate.parse(startDateTextView.getText().toString());
            Date endDate = sdfDate.parse(endDateTextView.getText().toString());
            long differenceInMilliseconds = endDate.getTime() - startDate.getTime();
            long differenceInDays = differenceInMilliseconds / (24 * 60 * 60 * 1000);
            diasDiferenciaTextView.setText(differenceInDays + " día/s ausente");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFotoDialog(){
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.issss_box);
        ImageView btnagregar = dialog.findViewById(R.id.Add_Img);
        ImageView Guardar = dialog.findViewById(R.id.Save_Img);
        ImageView Cancelar = dialog.findViewById(R.id.cancel_isss);

        listaImagenes = dialog.findViewById(R.id.list_imgs);

        btnagregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir la galería
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });
        Guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();

                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    uris.add(uri); // Agrega la URI a la lista
                }
                // Después de agregar las URIs, puedes cargarlas en el ListView
                cargarImagenesEnListView();
            }
        }
    }
    private void cargarImagenesEnListView() {
        ImageListAdapter adapter = new ImageListAdapter(requireContext(), R.layout.img_vista_isss, uris);
        listaImagenes.setAdapter(adapter);
    }




}
