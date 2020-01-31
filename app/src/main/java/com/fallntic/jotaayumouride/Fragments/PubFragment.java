package com.fallntic.jotaayumouride.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.adapter.PubImageAdapter;
import com.fallntic.jotaayumouride.model.PubImage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listPubImage;

public class PubFragment extends Fragment {

    private RecyclerView recyclerViewPubImage;
    private View view;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public PubFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_pub, container, false);

        recyclerViewPubImage = view.findViewById(R.id.recyclerview_pub);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerViewPubImage = view.findViewById(R.id.recyclerview_pub);

        firestore = FirebaseFirestore.getInstance();

        getListPubImage();

        /*if (listPubImage != null && listPubImage.size() > 0){
            showListPubImage();
        }*/
    }

    private void showListPubImage() {
        //Attach adapter to recyclerView
        recyclerViewPubImage.setHasFixedSize(true);
        recyclerViewPubImage.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPubImage.setVisibility(View.VISIBLE);
        PubImageAdapter pubImageAdapter = new PubImageAdapter(getContext(), listPubImage);
        recyclerViewPubImage.setAdapter(pubImageAdapter);
    }

    private void getListPubImage() {
        if (firestore != null && listPubImage == null || listPubImage.size() <= 0) {
            listPubImage = new ArrayList<>();
            firestore.collection("advertisements").document("my_ads").collection("image_ads").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    PubImage pubImage = documentSnapshot.toObject(PubImage.class);
                                    listPubImage.add(pubImage);
                                }
                                showListPubImage();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage(getContext(), "Error charging pubs!");
                        }
                    });
        } else {
            showListPubImage();
        }
    }
}