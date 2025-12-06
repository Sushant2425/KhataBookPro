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
    private ArrayList<CollectionModel> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private CollectionAdapter adapter;

    public static CollectionFragment newInstance(ArrayList<CollectionModel> list) {
        CollectionFragment fragment = new CollectionFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_LIST, list); // initial load only
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ArrayList<CollectionModel> argList =
                    (ArrayList<CollectionModel>) getArguments().getSerializable(ARG_LIST);
            if (argList != null) {
                list.clear();
                list.addAll(argList);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CollectionAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        return view;
    }

    // NEW: activity se call karne ke liye
    public void updateData(ArrayList<CollectionModel> newList) {
        if (list == null) list = new ArrayList<>();
        list.clear();
        if (newList != null) list.addAll(newList);
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
