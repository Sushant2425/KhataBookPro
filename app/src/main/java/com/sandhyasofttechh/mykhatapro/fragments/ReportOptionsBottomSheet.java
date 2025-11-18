package com.sandhyasofttechh.mykhatapro.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReportOptionsBottomSheet extends BottomSheetDialogFragment {

    public interface ReportListener {
        void onReportGenerated(List<Transaction> transactions, String dateRangeLabel);
    }

    private ReportListener listener;
    private List<Transaction> allTransactions;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public static ReportOptionsBottomSheet newInstance(List<Transaction> transactions) {
        ReportOptionsBottomSheet fragment = new ReportOptionsBottomSheet();
        Bundle args = new Bundle();
        // Ensure the list is serializable
        args.putSerializable("TRANSACTIONS", new ArrayList<>(transactions));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ReportListener) listener = (ReportListener) context;
        else if (getParentFragment() instanceof ReportListener) listener = (ReportListener) getParentFragment();
        else throw new RuntimeException("Calling context must implement ReportListener");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_report_options, container, false);

        allTransactions = (List<Transaction>) getArguments().getSerializable("TRANSACTIONS");
        
        RadioGroup rgOptions = view.findViewById(R.id.rg_date_options);
        LinearLayout customDateLayout = view.findViewById(R.id.layout_custom_dates);
        TextInputEditText etFrom = view.findViewById(R.id.et_from_date);
        TextInputEditText etTo = view.findViewById(R.id.et_to_date);

        rgOptions.setOnCheckedChangeListener((group, checkedId) -> {
            customDateLayout.setVisibility(checkedId == R.id.rb_custom_range ? View.VISIBLE : View.GONE);
        });
        
        etFrom.setOnClickListener(v -> showDatePicker(etFrom, startCalendar));
        etTo.setOnClickListener(v -> showDatePicker(etTo, endCalendar));

        view.findViewById(R.id.btn_generate_pdf).setOnClickListener(v -> generateReport());
        
        return view;
    }

    private void generateReport() {
        if (listener == null) return;
        
        RadioGroup rg = getView().findViewById(R.id.rg_date_options);
        int checkedId = rg.getCheckedRadioButtonId();


        long startTimeValue;
        long endTimeValue;
        String dateRangeLabel;

        Calendar now = Calendar.getInstance();
        
        if (checkedId == R.id.rb_last_7_days) {
            Calendar sevenDaysAgo = (Calendar) now.clone();
            sevenDaysAgo.add(Calendar.DAY_OF_YEAR, -7);
            startTimeValue = sevenDaysAgo.getTimeInMillis();
            endTimeValue = now.getTimeInMillis();
            dateRangeLabel = "Last 7 Days";
        } else if (checkedId == R.id.rb_last_30_days) {
            Calendar thirtyDaysAgo = (Calendar) now.clone();
            thirtyDaysAgo.add(Calendar.DAY_OF_YEAR, -30);
            startTimeValue = thirtyDaysAgo.getTimeInMillis();
            endTimeValue = now.getTimeInMillis();
            dateRangeLabel = "Last 30 Days";
        } else if (checkedId == R.id.rb_custom_range) {
            TextInputEditText etFrom = getView().findViewById(R.id.et_from_date);
            TextInputEditText etTo = getView().findViewById(R.id.et_to_date);
            if(etFrom.getText().toString().isEmpty() || etTo.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Please select start and end dates", Toast.LENGTH_SHORT).show();
                return;
            }
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            startTimeValue = startCalendar.getTimeInMillis();
            endTimeValue = endCalendar.getTimeInMillis();
            dateRangeLabel = dateFormat.format(startTimeValue) + " to " + dateFormat.format(endTimeValue);
        } else { // All Time
            startTimeValue = 0;
            endTimeValue = Long.MAX_VALUE;
            dateRangeLabel = "All Transactions";
        }

        // --- THIS IS THE FIX ---
        // Create final variables to use inside the lambda.
        final long finalStartTime = startTimeValue;
        final long finalEndTime = endTimeValue;

        List<Transaction> filteredList = allTransactions.stream()
            .filter(t -> t.getTimestamp() >= finalStartTime && t.getTimestamp() <= finalEndTime)
            .collect(Collectors.toList());
            
        listener.onReportGenerated(filteredList, dateRangeLabel);
        dismiss();
    }
    
    private void showDatePicker(TextInputEditText editText, Calendar calendar) {
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            calendar.set(year, month, day);
            editText.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}