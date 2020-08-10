package com.example.filesynchor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class BottomSheet extends BottomSheetDialogFragment {

    RecyclerView recyclerView;
    AdapterSync adapterSync;
    private List<Data> dataList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, null);
        dataList = new ArrayList<>();
        loadDataIntoArrayList(dataList);
        adapterSync = new AdapterSync(dataList, getActivity());
        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapterSync);
        return view;
    }

    private List<Data> loadDataIntoArrayList(List<Data> dataList) {

        Data data1 = new Data("7th August,2020 11:25:28", 10, "5.7 GB", "Completed", "");
        Data data2 = new Data("7th August,2020 09:20:10", 5, "3.7 GB", "Completed", "");
        Data data3 = new Data("6th August,2020 01:35:08", 8, "4.5 GB", "Completed", "");
        Data data4 = new Data("6th August,2020 17:45:46", 10, "5.2 GB", "Completed", "");
        Data data5 = new Data("5th August,2020 23:22:35", 15, "9.1 GB", "Completed", "");
        dataList.add(data1);
        dataList.add(data2);
        dataList.add(data3);
        dataList.add(data4);
        dataList.add(data5);
        return dataList;
    }

}