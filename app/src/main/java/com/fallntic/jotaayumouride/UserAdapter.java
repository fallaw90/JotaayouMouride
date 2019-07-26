package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.indexSelectedUser;
import static com.fallntic.jotaayumouride.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.DataHolder.showImage;
import static com.fallntic.jotaayumouride.UserInfoActivity.getAdiya;
import static com.fallntic.jotaayumouride.UserInfoActivity.getSass;
import static com.fallntic.jotaayumouride.UserInfoActivity.getSocial;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> listUsers;
    ImageView imageView;

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
        User user = listUsers.get(position);

        int indexRole = user.getListDahiraID().indexOf(dahira.getDahiraID());


        holder.textViewUserName.setText(user.getUserName());
        holder.textViewAddress.setText(user.getAddress());
        holder.textViewUserPhoneNumber.setText(user.getUserPhoneNumber());

        if (indexRole >= 0) {
            String role = user.getListRoles().get(indexRole);
            if (role.equals("Administrateur"))
                holder.textViewRole.setText(role);
            else
                holder.textViewRole.setVisibility(View.GONE);
        }

        showImage(context, "profileImage", user.getUserID(), imageView);

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
            selectedUser = listUsers.get(getAdapterPosition());
            indexSelectedUser = selectedUser.getListDahiraID().indexOf(dahira.getDahiraID());
            getAdiya();
            getSass();
            getSocial();
            Intent intent = new Intent(context, UserInfoActivity.class);
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            selectedUser = listUsers.get(getAdapterPosition());
            if (selectedUser.getListDahiraID().contains(dahira.getDahiraID())) {
                indexSelectedUser = selectedUser.getListDahiraID().indexOf(dahira.getDahiraID());
            }
            Intent intent = new Intent(context, UserInfoActivity.class);
            context.startActivity(intent);
            return false;
        }
    }
}