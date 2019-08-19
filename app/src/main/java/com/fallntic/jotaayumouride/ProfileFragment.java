package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fallntic.jotaayumouride.Utility.MyStaticFunctions;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.Utility.DataHolder.logout;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firebaseAuth;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firebaseUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ProfileFragment";

    private TextView textViewName;
    private TextView textViewAdress;
    private TextView textViewPhoneNumber;
    private TextView textViewEmail;
    private CircleImageView imageViewProfile;
    private LinearLayout linearLayoutVerificationNeeded;
    private LinearLayout linearLayoutVerified;

    private LinearLayout linEmail;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_profile, container, false);

        init();

        loadUserInformation();

        return this.view;
    }

    private void init() {
        textViewAdress = this.view.findViewById(R.id.textView_userAddress);
        textViewEmail = this.view.findViewById(R.id.textView_email);
        textViewName = this.view.findViewById(R.id.textView_userName);
        textViewPhoneNumber = this.view.findViewById(R.id.textView_userPhoneNumber);
        imageViewProfile = this.view.findViewById(R.id.imageView);
        linearLayoutVerificationNeeded = this.view.findViewById(R.id.linearLayout_verificationNeeded);
        linearLayoutVerified = this.view.findViewById(R.id.linearLayout_verified);
        linEmail = this.view.findViewById(R.id.lin_email);

        this.view.findViewById(R.id.button_verifyEmail).setOnClickListener(this);

        initViewsProgressBar();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_verifyEmail:
                MyStaticVariables.firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logout(getContext());
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        toastMessage(getContext(), "Verification Email envoyee");
                    }
                });
                break;
        }
    }

    private void loadUserInformation() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            if (firebaseUser.getEmail() != null && !firebaseUser.getEmail().equals("")) {
                if (firebaseUser.isEmailVerified()) {
                    //Get the current user info
                    linearLayoutVerificationNeeded.setVisibility(View.GONE);
                } else {
                    linearLayoutVerified.setVisibility(View.GONE);
                }
            } else if (firebaseUser.getPhoneNumber() != null && !firebaseUser.getPhoneNumber().equals("")) {
                linearLayoutVerificationNeeded.setVisibility(View.GONE);
                linEmail.setVisibility(View.GONE);
            }

            textViewName.setText(onlineUser.getUserName());
            textViewPhoneNumber.setText(onlineUser.getUserPhoneNumber());
            textViewAdress.setText(onlineUser.getAddress());
            textViewEmail.setText(onlineUser.getEmail());

            MyStaticFunctions.showImage(getContext(), onlineUser.getImageUri(), imageViewProfile);
        }
    }

    public  void initViewsProgressBar() {
        relativeLayoutData = view.findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = view.findViewById(R.id.relativeLayout_progressBar);
        progressBar = view.findViewById(R.id.progressBar);
    }
}