package com.fallntic.jotaayumouride.fragments;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fallntic.jotaayumouride.HomeActivity;
import com.fallntic.jotaayumouride.PdfViewActivity;
import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.model.ListPDFObject;
import com.fallntic.jotaayumouride.model.UploadPdf;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.fallntic.jotaayumouride.HomeActivity.loadInterstitialAd;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listUploadPDF;


@SuppressWarnings("ALL")
public class PDFFragment extends Fragment {

    private static final String TAG = "PDFFragment";
    private View view;
    //the listview
    private ListView listView;

    //database reference to get uploads data
    private DatabaseReference mDatabaseReference;

    //list to store uploads data
    //private List<UploadPdf> uploadList;
    private String[] uploads;

    private UploadPdf pdf_file;
    private long downloadID;
    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                toastMessage(getContext(), "Telechargement termine");
            } else {
                toastMessage(getContext(), "Telechargement en cours");
            }
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_pdf, container, false);

        listView = view.findViewById(R.id.listView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (listUploadPDF == null) {
            listUploadPDF = new ArrayList<>();
        }
        getPDF(listUploadPDF);

        getActivity().registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        //adding a clicklistener on listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //getting the upload
                pdf_file = listUploadPDF.get(i);
                openPDF(getContext(), pdf_file);
                loadInterstitialAd(getContext());
            }
        });
    }

    public void getPDF(final List<UploadPdf> listPDF) {
        //Retrieve all songs from FirebaseFirestore
        if (listPDF.isEmpty()) {
            showProgressBar();
            MyStaticVariables.collectionReference = MyStaticVariables.firestore.collection("PDF");
            MyStaticVariables.collectionReference.whereEqualTo("documentID", "pdf_khassida").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @SuppressWarnings("LoopStatementThatDoesntLoop")
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            ListPDFObject listPDFObject = null;
                            if (!queryDocumentSnapshots.isEmpty()) {
                                hideProgressBar();
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                //noinspection LoopStatementThatDoesntLoop
                                for (DocumentSnapshot documentSnapshot : list) {
                                    listPDFObject = documentSnapshot.toObject(ListPDFObject.class);
                                    if (listPDFObject != null) {
                                        listPDF.addAll(listPDFObject.getListPDF_Khassida());
                                    }
                                    break;
                                }

                                if (listPDF != null) {
                                    Collections.sort(listPDF);

                                    uploads = new String[listPDF.size()];
                                    for (int i = 0; i < uploads.length; i++) {
                                        uploads[i] = i + 1 + " - " + listPDF.get(i).getName();
                                    }

                                    //displaying it to list
                                    if (uploads != null && uploads.length > 0) {
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), android.R.layout.simple_list_item_1, uploads);
                                        listView.setAdapter(adapter);
                                    } else {
                                        startActivity(new Intent(getContext(), HomeActivity.class));
                                        toastMessage(getContext(), "Reessayez SVP!");
                                    }
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressBar();
                    toastMessage(getContext(), "Erreur de telechargement du repertoire audio.");
                }
            });
        } else {
            uploads = new String[listPDF.size()];
            for (int i = 0; i < uploads.length; i++) {
                uploads[i] = i + 1 + " - " + listPDF.get(i).getName();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, uploads);
            listView.setAdapter(adapter);
        }
    }

    private void beginDownload(Context context, UploadPdf pdf_file) {

        toastMessage(context, "Telechargement en cours ...");

        Uri uri = Uri.parse(pdf_file.getUrl());
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, pdf_file.getName());

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
        }
    }

    protected void openFile(UploadPdf pdf_file) {
        Intent intent = new Intent(getContext(), PdfViewActivity.class);
        intent.putExtra("pdf_file", pdf_file);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getActivity()).unregisterReceiver(onDownloadComplete);
    }

    public void openPDF(final Context context, final UploadPdf pdf_file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()), R.style.alertDialog);
        builder.setCancelable(true);
        builder.setMessage("Cliquez sur Ouvrir pour lire le fichier ou Telecharger pour l'enregistrer dans votre telephone.");
        builder.setPositiveButton("Telecharger", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                beginDownload(context, pdf_file);
            }
        });

        builder.setNegativeButton("Ouvrir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openFile(pdf_file);
            }
        });
        builder.show();
    }
}
