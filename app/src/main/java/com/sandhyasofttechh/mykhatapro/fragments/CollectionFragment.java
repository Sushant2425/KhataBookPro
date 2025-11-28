package com.sandhyasofttechh.mykhatapro.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.CollectionAdapter;
import com.sandhyasofttechh.mykhatapro.model.CollectionModel;

import java.util.ArrayList;

public class CollectionFragment extends Fragment {

    private static final String ARG_LIST = "list";
    private ArrayList<CollectionModel> list;

    public static CollectionFragment newInstance(ArrayList<CollectionModel> list) {
        CollectionFragment fragment = new CollectionFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_LIST, list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            list = (ArrayList<CollectionModel>) getArguments().getSerializable(ARG_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        if (list != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new CollectionAdapter(getContext(), list));
        }

        return view;
    }
}
