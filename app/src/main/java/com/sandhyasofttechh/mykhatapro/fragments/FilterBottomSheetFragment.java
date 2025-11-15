package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sandhyasofttechh.mykhatapro.R;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    public enum FilterOption {
        // Sort Options
        MOST_RECENT, OLDEST, HIGHEST_AMOUNT, LOWEST_AMOUNT, BY_NAME_AZ, BY_NAME_ZA,
        // Filter Options
        ALL, YOU_GAVE, YOU_GOT, SETTLED_UP, HAS_DUE_BALANCE
    }

    private FilterListener listener;

    public interface FilterListener {
        void onFilterSelected(FilterOption option);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof FilterListener) {
            listener = (FilterListener) getParentFragment();
        } else {
            throw new RuntimeException("Calling fragment must implement FilterListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        RadioGroup rgFilters = view.findViewById(R.id.rg_filters);
        rgFilters.setOnCheckedChangeListener((group, checkedId) -> {
            FilterOption selectedOption = FilterOption.MOST_RECENT; // Default
            if (checkedId == R.id.rb_most_recent) {
                selectedOption = FilterOption.MOST_RECENT;
            } else if (checkedId == R.id.rb_oldest) {
                selectedOption = FilterOption.OLDEST;
            } else if (checkedId == R.id.rb_highest_amount) {
                selectedOption = FilterOption.HIGHEST_AMOUNT;
            } else if (checkedId == R.id.rb_lowest_amount) {
                selectedOption = FilterOption.LOWEST_AMOUNT;
            } else if (checkedId == R.id.rb_name_az) {
                selectedOption = FilterOption.BY_NAME_AZ;
            } else if (checkedId == R.id.rb_name_za) {
                selectedOption = FilterOption.BY_NAME_ZA;
            } else if (checkedId == R.id.rb_all_transactions) {
                selectedOption = FilterOption.ALL;
            } else if (checkedId == R.id.rb_you_gave) {
                selectedOption = FilterOption.YOU_GAVE;
            } else if (checkedId == R.id.rb_you_got) {
                selectedOption = FilterOption.YOU_GOT;
            } else if (checkedId == R.id.rb_settled_up) {
                selectedOption = FilterOption.SETTLED_UP;
            } else if (checkedId == R.id.rb_has_due) {
                selectedOption = FilterOption.HAS_DUE_BALANCE;
            }
            
            if (listener != null) {
                listener.onFilterSelected(selectedOption);
            }
            dismiss();
        });

        return view;
    }
}