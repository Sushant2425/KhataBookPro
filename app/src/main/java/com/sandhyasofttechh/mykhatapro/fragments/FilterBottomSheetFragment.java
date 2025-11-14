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
        ALL, YOU_GAVE, YOU_GOT, HIGHEST_AMOUNT, LOWEST_AMOUNT, MOST_RECENT, OLDEST
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
            FilterOption selectedOption = FilterOption.ALL; // Default
            if (checkedId == R.id.rb_all) {
                selectedOption = FilterOption.ALL;
            } else if (checkedId == R.id.rb_you_gave) {
                selectedOption = FilterOption.YOU_GAVE;
            } else if (checkedId == R.id.rb_you_got) {
                selectedOption = FilterOption.YOU_GOT;
            } else if (checkedId == R.id.rb_highest_amount) {
                selectedOption = FilterOption.HIGHEST_AMOUNT;
            } else if (checkedId == R.id.rb_lowest_amount) {
                selectedOption = FilterOption.LOWEST_AMOUNT;
            } else if (checkedId == R.id.rb_most_recent) {
                selectedOption = FilterOption.MOST_RECENT;
            } else if (checkedId == R.id.rb_oldest) {
                selectedOption = FilterOption.OLDEST;
            }
            
            listener.onFilterSelected(selectedOption);
            dismiss();
        });

        return view;
    }
}