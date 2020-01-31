package com.fallntic.jotaayumouride.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fallntic.jotaayumouride.LoginActivity;
import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.utility.MyStaticFunctions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.logout;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firebaseAuth;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firebaseUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;

@SuppressWarnings("unused")
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ProfileFragment";

    private TextView textViewName;
    private TextView textViewAdress;
    private TextView textViewPhoneNumber;
    private TextView textViewEmail, textViewLabeEmail;
    private CircleImageView imageViewProfile;
    private LinearLayout linearLayoutVerificationNeeded;
    private LinearLayout linearLayoutVerified;

    private LinearLayout linEmail;
    private View view;
    private boolean isViewInit = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_profile, container, false);

        init(this.view);
        loadUserInformation();

        return this.view;
    }

    private void init(View view) {
        textViewAdress = view.findViewById(R.id.textView_userAddress);
        textViewEmail = view.findViewById(R.id.textView_email);
        textViewLabeEmail = view.findViewById(R.id.textView_labelEmail);
        textViewName = view.findViewById(R.id.textView_userName);
        textViewPhoneNumber = view.findViewById(R.id.textView_userPhoneNumber);
        imageViewProfile = view.findViewById(R.id.imageView);
        linearLayoutVerificationNeeded = view.findViewById(R.id.linearLayout_verificationNeeded);
        linearLayoutVerified = view.findViewById(R.id.linearLayout_verified);
        linEmail = view.findViewById(R.id.lin_email);

        this.view.findViewById(R.id.button_verifyEmail).setOnClickListener(this);

        initViewsProgressBar();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_verifyEmail) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    logout(Objects.requireNonNull(getContext()));
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    toastMessage(getContext(), "Verification Email envoyee");
                }
            });
        }
    }

    private void loadUserInformation() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null && onlineUser != null && onlineUser.getUserID() != null) {
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

            if (onlineUser.getEmail() == null || onlineUser.getEmail().equals("")) {
                textViewLabeEmail.setVisibility(View.GONE);
                textViewEmail.setVisibility(View.GONE);
            }
        }
    }

    private void initViewsProgressBar() {
        relativeLayoutData = view.findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = view.findViewById(R.id.relativeLayout_progressBar);
        progressBar = view.findViewById(R.id.progressBar);
    }
}