package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.user;

public class ListDahiraActivity extends AppCompatActivity implements View.OnClickListener {

    private List<Dahira> dahiraList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;

    private RecyclerView recyclerViewDahira;
    private DahiraAdapter dahiraAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_dahira);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Mes dahiras");
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);

        showListDahira();

        findViewById(R.id.button_backToProfile).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.button_backToProfile:
                finish();
                startActivity(new Intent(ListDahiraActivity.this, ProfileActivity.class));
                break;
        }
    }

    private void showListDahira() {

        //Attach adapter to recyclerView
        recyclerViewDahira = findViewById(R.id.recyclerview_dahiras);
        recyclerViewDahira.setHasFixedSize(true);
        recyclerViewDahira.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDahira.setVisibility(View.VISIBLE);
        dahiraList = new ArrayList<>();
        dahiraAdapter = new DahiraAdapter(this, dahiraList);
        recyclerViewDahira.setAdapter(dahiraAdapter);

        showProgressDialog("Chargement de vos dahiras ...");
        db.collection("dahiras").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {

                                Dahira dahira = documentSnapshot.toObject(Dahira.class);

                                for (String dahiraID : DataHolder.user.getListDahiraID()) {
                                    if (dahira.getDahiraID().equals(dahiraID)) {
                                        dahira.setDahiraID(documentSnapshot.getId());
                                        dahiraList.add(dahira);
                                    }
                                }
                            }
                            dahiraAdapter.notifyDataSetChanged();
                        }
                        else {
                            dismissProgressDialog();
                            toastMessage("Vous n'etes associe a auccun dahira!");
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_dahira, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addDahira:
                startActivity(new Intent(this, AddDahiraActivity.class));
                break;
            case R.id.logout:
                logout();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
        return true;
    }

    public void logout(){
        user = null;
        dahira = null;
        FirebaseAuth.getInstance().signOut();
    }

    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void showProgressDialog(String str){
        progressDialog.setMessage(str);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        dismissProgressDialog();
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
