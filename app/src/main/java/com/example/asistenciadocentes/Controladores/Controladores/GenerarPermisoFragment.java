package com.example.asistenciadocentes.Controladores.Controladores;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.asistenciadocentes.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GenerarPermisoFragment extends Fragment {

    private TextView startDateTextView;
    private TextView endDateTextView;
    private TextView diasDiferenciaTextView;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener startDateListener, endDateListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_generar_permiso, container, false);
        startDateTextView = view.findViewById(R.id.txt_date_ini);
        endDateTextView = view.findViewById(R.id.txt_date_fin);
        diasDiferenciaTextView = view.findViewById(R.id.txt_diferencias_dias);
        Spinner spTipo = view.findViewById(R.id.sp_tipo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.categorias, R.layout.sp_vista);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipo.setAdapter(adapter);
        Button selectDatesButton = view.findViewById(R.id.Btn_fechas);
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
        selectDatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateListener);
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
        long startDateInMillis = calendar.getTimeInMillis();
        Calendar endCalendar = Calendar.getInstance();
        long endDateInMillis = endCalendar.getTimeInMillis();
        long differenceInMilliseconds = startDateInMillis-endDateInMillis;
        long differenceInDays = differenceInMilliseconds / (24 * 60 * 60 * 1000);
        diasDiferenciaTextView.setText(differenceInDays+" día/s ausente");
    }
}
