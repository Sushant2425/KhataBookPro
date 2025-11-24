package com.sandhyasofttechh.mykhatapro.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReportOptionsBottomSheet extends BottomSheetDialogFragment {

    // Interface for callback
    public interface ReportListener {
        void onReportGenerated(List<Transaction> transactions, String dateRangeLabel);
    }

    // Member variables
    private ReportListener listener;
    private List<Transaction> allTransactions;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    // UI Components
    private RadioGroup rgDateOptions;
    private LinearLayout layoutCustomDates;
    private TextInputEditText etFromDate, etToDate;
    private MaterialButton btnGeneratePdf, btnCancel;
    private ImageView btnClose;

    /**
     * Factory method to create a new instance
     */
    public static ReportOptionsBottomSheet newInstance(List<Transaction> transactions) {
        ReportOptionsBottomSheet fragment = new ReportOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("TRANSACTIONS", new ArrayList<>(transactions));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Check if the context or parent fragment implements the listener
        if (context instanceof ReportListener) {
            listener = (ReportListener) context;
        } else if (getParentFragment() instanceof ReportListener) {
            listener = (ReportListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString() + " must implement ReportListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_report_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get transactions from arguments
        if (getArguments() != null) {
            allTransactions = (List<Transaction>) getArguments().getSerializable("TRANSACTIONS");
        }

        // Initialize views
        initializeViews(view);

        // Setup listeners
        setupListeners();
    }

    /**
     * Initialize all views
     */
    private void initializeViews(View view) {
        rgDateOptions = view.findViewById(R.id.rg_date_options);
        layoutCustomDates = view.findViewById(R.id.layout_custom_dates);
        etFromDate = view.findViewById(R.id.et_from_date);
        etToDate = view.findViewById(R.id.et_to_date);
        btnGeneratePdf = view.findViewById(R.id.btn_generate_pdf);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnClose = view.findViewById(R.id.btn_close);
    }

    /**
     * Setup all click listeners
     */
    private void setupListeners() {
        // Radio group listener to show/hide custom date layout
        rgDateOptions.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isCustomRange = checkedId == R.id.rb_custom_range;
            layoutCustomDates.setVisibility(isCustomRange ? View.VISIBLE : View.GONE);

            // Clear custom dates if not selected
            if (!isCustomRange) {
                etFromDate.setText("");
                etToDate.setText("");
            }
        });

        // Date picker listeners
        etFromDate.setOnClickListener(v -> showDatePicker(etFromDate, startCalendar, "Select Start Date"));
        etToDate.setOnClickListener(v -> showDatePicker(etToDate, endCalendar, "Select End Date"));

        // Button listeners
        btnGeneratePdf.setOnClickListener(v -> generateReport());
        btnCancel.setOnClickListener(v -> dismiss());
        btnClose.setOnClickListener(v -> dismiss());
    }

    /**
     * Generate report based on selected options
     */
    private void generateReport() {
        if (listener == null || allTransactions == null) {
            Toast.makeText(getContext(), "Unable to generate report", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = rgDateOptions.getCheckedRadioButtonId();

        // Get selected date range
        DateRange dateRange = getDateRangeForSelection(checkedId);

        if (dateRange == null) {
            return; // Error already shown
        }

        // Filter transactions
        final long startTime = dateRange.startTime;
        final long endTime = dateRange.endTime;

        List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> t.getTimestamp() >= startTime && t.getTimestamp() <= endTime)
                .collect(Collectors.toList());

        // Check if there are transactions
        if (filteredTransactions.isEmpty()) {
            Toast.makeText(getContext(), "No transactions found for selected period", Toast.LENGTH_SHORT).show();
            return;
        }

        // Callback with filtered transactions
        listener.onReportGenerated(filteredTransactions, dateRange.label);
        dismiss();
    }

    /**
     * Get date range based on radio button selection
     */
    private DateRange getDateRangeForSelection(int checkedId) {
        Calendar now = Calendar.getInstance();

        if (checkedId == R.id.rb_all_time) {
            return new DateRange(0, Long.MAX_VALUE, "All Transactions");

        } else if (checkedId == R.id.rb_last_7_days) {
            Calendar sevenDaysAgo = (Calendar) now.clone();
            sevenDaysAgo.add(Calendar.DAY_OF_YEAR, -7);
            setTimeToStartOfDay(sevenDaysAgo);
            setTimeToEndOfDay(now);
            return new DateRange(
                    sevenDaysAgo.getTimeInMillis(),
                    now.getTimeInMillis(),
                    "Last 7 Days"
            );

        } else if (checkedId == R.id.rb_last_30_days) {
            Calendar thirtyDaysAgo = (Calendar) now.clone();
            thirtyDaysAgo.add(Calendar.DAY_OF_YEAR, -30);
            setTimeToStartOfDay(thirtyDaysAgo);
            setTimeToEndOfDay(now);
            return new DateRange(
                    thirtyDaysAgo.getTimeInMillis(),
                    now.getTimeInMillis(),
                    "Last 30 Days"
            );

        } else if (checkedId == R.id.rb_custom_range) {
            // Validate custom date inputs
            if (etFromDate.getText() == null || etFromDate.getText().toString().isEmpty() ||
                    etToDate.getText() == null || etToDate.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Please select start and end dates", Toast.LENGTH_SHORT).show();
                return null;
            }

            // Validate date range
            if (startCalendar.getTimeInMillis() > endCalendar.getTimeInMillis()) {
                Toast.makeText(getContext(), "End date must be after start date", Toast.LENGTH_SHORT).show();
                return null;
            }

            setTimeToStartOfDay(startCalendar);
            setTimeToEndOfDay(endCalendar);

            String label = dateFormat.format(startCalendar.getTime()) + " to " +
                    dateFormat.format(endCalendar.getTime());

            return new DateRange(
                    startCalendar.getTimeInMillis(),
                    endCalendar.getTimeInMillis(),
                    label
            );
        }

        return new DateRange(0, Long.MAX_VALUE, "All Transactions");
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker(TextInputEditText editText, Calendar calendar, String title) {
        if (getContext() == null) return;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    editText.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.setTitle(title);
        datePickerDialog.show();
    }

    /**
     * Set time to start of day (00:00:00)
     */
    private void setTimeToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Set time to end of day (23:59:59)
     */
    private void setTimeToEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    /**
     * Helper class to hold date range information
     */
    private static class DateRange {
        final long startTime;
        final long endTime;
        final String label;

        DateRange(long startTime, long endTime, String label) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.label = label;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}