package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.sandhyasofttechh.mykhatapro.R;
import java.io.Serializable;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    // --- Enums and Interface ---
    public enum FilterType { ALL, YOU_WILL_GET, YOU_WILL_GIVE, SETTLED_UP }
    public enum SortType { MOST_RECENT, HIGHEST_AMOUNT, LOWEST_AMOUNT, NAME_AZ, NAME_ZA }
    public interface FilterListener {
        void onFiltersApplied(FilterType filter, SortType sort);
    }
    
    // --- Arguments for saving state ---
    private static final String ARG_FILTER = "arg_filter";
    private static final String ARG_SORT = "arg_sort";
    private FilterListener listener;

    // --- Professional Way to Create Fragment with Arguments ---
    public static FilterBottomSheetFragment newInstance(FilterType currentFilter, SortType currentSort) {
        FilterBottomSheetFragment fragment = new FilterBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FILTER, currentFilter);
        args.putSerializable(ARG_SORT, currentSort);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof FilterListener) listener = (FilterListener) getParentFragment();
        else throw new RuntimeException("Calling fragment must implement FilterListener");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        ChipGroup filterChips = view.findViewById(R.id.chip_group_filter);
        ChipGroup sortChips = view.findViewById(R.id.chip_group_sort);
        
        restoreSelectionState(filterChips, sortChips);

        view.findViewById(R.id.btn_apply_filters).setOnClickListener(v -> {
            if (listener != null) {
                listener.onFiltersApplied(getSelectedFilter(filterChips), getSelectedSort(sortChips));
            }
            dismiss();
        });

        view.findViewById(R.id.btn_reset).setOnClickListener(v -> {
            if (listener != null) {
                listener.onFiltersApplied(FilterType.ALL, SortType.MOST_RECENT);
            }
            dismiss();
        });

        return view;
    }

    private void restoreSelectionState(ChipGroup filterChips, ChipGroup sortChips) {
        if (getArguments() == null) return;
        
        FilterType currentFilter = (FilterType) getArguments().getSerializable(ARG_FILTER);
        SortType currentSort = (SortType) getArguments().getSerializable(ARG_SORT);

        if (currentFilter == FilterType.YOU_WILL_GET) filterChips.check(R.id.chip_you_will_get);
        else if (currentFilter == FilterType.YOU_WILL_GIVE) filterChips.check(R.id.chip_you_will_give);
        else if (currentFilter == FilterType.SETTLED_UP) filterChips.check(R.id.chip_settled_up);
        else filterChips.check(R.id.chip_all);

        if (currentSort == SortType.HIGHEST_AMOUNT) sortChips.check(R.id.chip_highest_amount);
        else if (currentSort == SortType.LOWEST_AMOUNT) sortChips.check(R.id.chip_lowest_amount);
        else if (currentSort == SortType.NAME_AZ) sortChips.check(R.id.chip_name_az);
        else if (currentSort == SortType.NAME_ZA) sortChips.check(R.id.chip_name_za);
        else sortChips.check(R.id.chip_most_recent);
    }
    
    // --- FIXED: Replaced switch with if-else if ---
    private FilterType getSelectedFilter(ChipGroup group) {
        int checkedId = group.getCheckedChipId();
        if (checkedId == R.id.chip_you_will_get) {
            return FilterType.YOU_WILL_GET;
        } else if (checkedId == R.id.chip_you_will_give) {
            return FilterType.YOU_WILL_GIVE;
        } else if (checkedId == R.id.chip_settled_up) {
            return FilterType.SETTLED_UP;
        } else {
            return FilterType.ALL;
        }

    }
    
    // --- FIXED: Replaced switch with if-else if ---
    private SortType getSelectedSort(ChipGroup group) {
        int checkedId = group.getCheckedChipId();
        if (checkedId == R.id.chip_highest_amount) {
            return SortType.HIGHEST_AMOUNT;
        } else if (checkedId == R.id.chip_lowest_amount) {
            return SortType.LOWEST_AMOUNT;
        } else if (checkedId == R.id.chip_name_az) {
            return SortType.NAME_AZ;
        } else if (checkedId == R.id.chip_name_za) {
            return SortType.NAME_ZA;
        } else {
            return SortType.MOST_RECENT;
        }
    }
}