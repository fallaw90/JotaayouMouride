package com.fallntic.jotaayumouride.fragments;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
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
import com.fallntic.jotaayumouride.adapter.AdvertisementAdapter;
import com.fallntic.jotaayumouride.model.PubImage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listPubImage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;

public class PubFragment extends Fragment {

    private RecyclerView recyclerViewPubImage;
    private View view;

    public PubFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_pub, container, false);

        recyclerViewPubImage = view.findViewById(R.id.recyclerview_pub);

        initViewsProgressBar(view);

        firestore = FirebaseFirestore.getInstance();


        return view;
    }

    private void initViewsProgressBar(View view) {
        relativeLayoutData = view.findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = view.findViewById(R.id.relativeLayout_progressBar);
        progressBar = view.findViewById(R.id.progressBar);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new MyTask().execute();

    }

    private void showListPubImage() {
        //Attach adapter to recyclerView
        recyclerViewPubImage.setHasFixedSize(true);
        recyclerViewPubImage.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPubImage.setVisibility(View.VISIBLE);
        AdvertisementAdapter advertisementAdapter = new AdvertisementAdapter(getContext(), listPubImage);
        recyclerViewPubImage.setAdapter(advertisementAdapter);
    }


    private void getListPubImage() {
        if (firestore != null && listPubImage == null || listPubImage.size() <= 0) {
            listPubImage = new ArrayList<>();
            firestore.collection("advertisements")
                    .document("my_ads")
                    .collection("image_ads")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    PubImage pubImage = documentSnapshot.toObject(PubImage.class);
                                    listPubImage.add(pubImage);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage(getContext(), "Error charging pubs!");
                        }
                    });
        }
    }


    @SuppressLint("StaticFieldLeak")
    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getListPubImage();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            showListPubImage();
        }
    }

}