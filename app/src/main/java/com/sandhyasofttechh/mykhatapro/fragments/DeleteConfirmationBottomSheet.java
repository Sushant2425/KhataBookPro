package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sandhyasofttechh.mykhatapro.R;

public class DeleteConfirmationBottomSheet extends BottomSheetDialogFragment {

    public interface DeleteConfirmationListener {
        void onDeleteConfirmed();
    }

    private DeleteConfirmationListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_delete_confirmation, container, false);

        view.findViewById(R.id.btn_confirm_delete).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDeleteConfirmed();
            }
            dismiss();
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DeleteConfirmationListener) {
            mListener = (DeleteConfirmationListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DeleteConfirmationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}