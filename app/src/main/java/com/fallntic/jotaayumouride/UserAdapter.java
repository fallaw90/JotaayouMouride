package com.fallntic.jotaayumouride;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.user;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> listUsers;
    ImageView imageView;

    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;

    private ProgressDialog progressDialog;

    public UserAdapter(Context context, List<User> listUsers) {
        this.context = context;
        this.listUsers = listUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_user, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        user = listUsers.get(position);

        holder.textViewUserName.setText(user.getUserName());
        holder.textViewAddress.setText(user.getAddress());
        holder.textViewUserPhoneNumber.setText(user.getUserPhoneNumber());

        int index = user.getListDahiraID().indexOf(dahira.getDahiraID());
        if (!user.getListDahiraID().get(index).equals("Administrateur")){
            holder.textViewRole.setVisibility(View.GONE);
        }

        DataHolder.showProfileImage(context, imageView);

    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView textViewUserName;
        TextView textViewAddress;
        TextView textViewUserPhoneNumber;
        TextView textViewRole;

        public UserViewHolder(View itemView) {
            super(itemView);

            textViewUserName = itemView.findViewById(R.id.textView_userName);
            textViewAddress = itemView.findViewById(R.id.textView_address);
            textViewUserPhoneNumber = itemView.findViewById(R.id.textView_userPhoneNumber);
            textViewRole = itemView.findViewById(R.id.textView_role);
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            user = listUsers.get(getAdapterPosition());
            Intent intent = new Intent(context, UserInfoActivity.class);
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            user = listUsers.get(getAdapterPosition());
            Intent intent = new Intent(context, UserInfoActivity.class);
            context.startActivity(intent);
            return false;
        }
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

    public void toastMessage(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}