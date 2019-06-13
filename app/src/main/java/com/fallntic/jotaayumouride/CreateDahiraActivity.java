package com.fallntic.jotaayumouride;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.fallntic.jotaayumouride.DataHolder.listCommission;
import static com.fallntic.jotaayumouride.DataHolder.listResponsible;

public class CreateDahiraActivity extends AppCompatActivity implements View.OnClickListener  {

    private static final String TAG = "CreateDahiraActivity";

    private EditText editTextDahiraName;
    private EditText editTextDieuwrine;
    private EditText editTextDahiraPhoneNumber;
    private EditText editTextSiege;
    private EditText editTextCommission;
    private EditText editTextResponsible;
    private EditText editTextAdiya;
    private EditText editTextSass;
    private EditText editTextSocial;
    private TextView textViewLabelCommission;
    private TextView getTextViewLabelResponsible;

    private ListView listViewCommission;

    // Array of strings...

    ArrayList<String> arrayList;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dahira);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Dahira info
        editTextDahiraName = findViewById(R.id.editText_dahiraName);
        editTextDieuwrine = findViewById(R.id.editText_dieuwrine);
        editTextDahiraPhoneNumber = findViewById(R.id.editText_dahiraPhoneNumber);
        editTextSiege = findViewById(R.id.editText_siege);
        editTextCommission = findViewById(R.id.editText_commission);
        editTextResponsible = findViewById(R.id.editText_responsible);
        textViewLabelCommission = (TextView) findViewById(R.id.textView_labelCommission);
        getTextViewLabelResponsible = (TextView) findViewById(R.id.textView_labelResponsible);
        listViewCommission = (ListView) findViewById(R.id.listView_commission);
        editTextResponsible = findViewById(R.id.editText_responsible);
        editTextAdiya = findViewById(R.id.editText_adiya);
        editTextSass = findViewById(R.id.editText_sass);
        editTextSocial = findViewById(R.id.editText_social);

        //Hide label commission and label responsible
        textViewLabelCommission.setVisibility(View.INVISIBLE);
        getTextViewLabelResponsible.setVisibility(View.INVISIBLE);

        //Display and modify ListView commissions
        arrayList = new ArrayList<>(new ArrayList(listCommission));
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_commission, R.id.textView_commission, arrayList);
        listViewCommission.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
                String com = listCommission.get(index);
                String resp = listResponsible.get(index);
                showUpdateDeleteDialog(com, resp, index);
                return true;
            }
        });

        findViewById(R.id.button_addCommission).setOnClickListener(this);
        findViewById(R.id.button_next).setOnClickListener(this);
        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.textView_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.button_addCommission:
                showListViewCommissions();
                break;
            case R.id.button_next:
                SaveDahiraInfo();
                break;
            case R.id.button_back:
                finish();
                break;
            case R.id.textView_login:
                startActivity(new Intent(CreateDahiraActivity.this, LoginActivity.class));
                break;
        }
    }

    private void SaveDahiraInfo() {
        //Info dahira
        final String dahiraName = editTextDahiraName.getText().toString().trim();
        final String dieuwrine = editTextDieuwrine.getText().toString().trim();
        final String dahiraPhoneNumber = editTextDahiraPhoneNumber.getText().toString().trim();
        final String siege = editTextSiege.getText().toString().trim();
        final String adiya = editTextAdiya.getText().toString().trim();
        final String sass = editTextSass.getText().toString().trim();
        final String social = editTextSocial.getText().toString().trim();

        if(!hasValidationErrors(dahiraName, dieuwrine, dahiraPhoneNumber, siege)) {
            //Create Dahira object
            DataHolder.dahira = new Dahira(dahiraName, dieuwrine, dahiraPhoneNumber, siege, adiya, sass, social);
            toastMessage("Dahira pre-enregistre");
            startActivity(new Intent(CreateDahiraActivity.this, CreateAdminActivity.class));
        }
    }


    private boolean hasValidationErrors(String dahiraName, String dieuwrine, String dahiraPhoneNumber, String siege) {

        if (dahiraName.isEmpty()) {
            editTextDahiraName.setError("Nom dahira obligatoire");
            editTextDahiraName.requestFocus();
            return true;
        }

        if (dieuwrine.isEmpty()) {
            editTextDieuwrine.setError("Champ obligatoire");
            editTextDieuwrine.requestFocus();
            return true;
        }

        if (dahiraPhoneNumber.isEmpty()) {
            editTextDahiraPhoneNumber.setError("Numero de telephone obligatoire");
            editTextDahiraPhoneNumber.requestFocus();
            return true;
        }

        if(!dahiraPhoneNumber.matches("[0-9]+") || dahiraPhoneNumber.length() != 9) {
            editTextDahiraPhoneNumber.setError("Numero de telephone incorrect");
            editTextDahiraPhoneNumber.requestFocus();
            return true;
        }

        String prefix = dahiraPhoneNumber.substring(0,2);
        boolean validatePrefix;
        switch(prefix){
            case "70":
                validatePrefix = true;
                break;
            case "76":
                validatePrefix = true;
                break;
            case "77":
                validatePrefix = true;
                break;
            case "78":
                validatePrefix = true;
                break;
            default:
                validatePrefix = false;
                break;
        }
        if(!validatePrefix) {
            editTextDahiraPhoneNumber.setError("Numero de telephone incorrect");
            editTextDahiraPhoneNumber.requestFocus();
            return true;
        }

        if (siege.isEmpty()) {
            editTextSiege.setError("Champ obligatoire");
            editTextSiege.requestFocus();
            return true;
        }

        return false;
    }

    public void showListViewCommissions(){
        String commission = editTextCommission.getText().toString().trim();
        String responsible = editTextResponsible.getText().toString().trim();

        if (commission.isEmpty()) {
            editTextCommission.setError("Veuillez remplir ce champ");
            editTextCommission.requestFocus();
            return;
        }

        if (responsible.isEmpty()) {
            editTextResponsible.setError("Veuillez remplir ce champ");
            editTextResponsible.requestFocus();
            return;
        }

        //Show label commission and label responsible
        textViewLabelCommission.setVisibility(View.VISIBLE);
        getTextViewLabelResponsible.setVisibility(View.VISIBLE);

        listCommission.add(commission);
        listResponsible.add(responsible);

        CommissionListAdapter customAdapter = new CommissionListAdapter(getApplicationContext(), listCommission, listResponsible);
        listViewCommission.setAdapter(customAdapter);

        setListViewHeightBasedOnChildren(listViewCommission);

        editTextCommission.setText("");
        editTextResponsible.setText("");
        editTextCommission.requestFocus();
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {

        Log.e("Listview Size ", "" + listView.getCount());

        CommissionListAdapter listAdapter = (CommissionListAdapter) listView.getAdapter();

        if (listAdapter == null) {

            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    private void showUpdateDeleteDialog(final String commission, String responsible, final int index) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextCommissione = (EditText) dialogView.findViewById(R.id.editText_dialogCommission);
        final EditText editTextResponsible = (EditText) dialogView.findViewById(R.id.editText_dialogResponsible);

        Button buttonUpdate = (Button) dialogView.findViewById(R.id.button_dialogUpdate);
        Button buttonDelete = (Button) dialogView.findViewById(R.id.button_dialogDelete);

        editTextCommissione.setText(commission);
        editTextResponsible.setText(responsible);

        dialogBuilder.setTitle("Modifier ou supprimer cette commission");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String com = editTextCommissione.getText().toString().trim();
                String resp = editTextResponsible.getText().toString().trim();
                listCommission.set(index, com);
                listResponsible.set(index, resp);

                CommissionListAdapter customAdapter = new CommissionListAdapter(getApplicationContext(), listCommission, listResponsible);
                listViewCommission.setAdapter(customAdapter);
                alertDialog.dismiss();
            }
        });


        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listCommission.remove(index);
                listResponsible.remove(index);

                CommissionListAdapter customAdapter = new CommissionListAdapter(getApplicationContext(), listCommission, listResponsible);
                listViewCommission.setAdapter(customAdapter);
                alertDialog.dismiss();
                setListViewHeightBasedOnChildren(listViewCommission);
            }
        });
    }

    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
