package com.fallntic.jotaayumouride.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.HomeActivity;
import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.UserInfoActivity;
import com.fallntic.jotaayumouride.model.User;
import com.fallntic.jotaayumouride.utility.MyStaticFunctions;

import java.util.List;

import static com.fallntic.jotaayumouride.utility.MyStaticVariables.adiya;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexOnlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexSelectedUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.sass;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.selectedUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.social;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> listUsers;

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

        if (indexOnlineUser < onlineUser.getListRoles().size() &&
                onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            holder.textViewAddress.setText(user.getAddress());
            holder.textViewUserPhoneNumber.setText(user.getUserPhoneNumber());
        } else {
            holder.textViewAddress.setVisibility(View.GONE);
            holder.textViewUserPhoneNumber.setVisibility(View.GONE);
        }

        if (indexRole >= 0 && user.getListRoles().size() > indexRole) {
            String role = user.getListRoles().get(indexRole);
            if (role.equals("Administrateur"))
                holder.textViewRole.setText(role);
            else
                holder.textViewRole.setVisibility(View.GONE);
        }

        MyStaticFunctions.showImage(context, user.getImageUri(), holder.imageView);

    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView imageView;
        final TextView textViewUserName;
        final TextView textViewAddress;
        final TextView textViewUserPhoneNumber;
        final TextView textViewRole;

        UserViewHolder(View itemView) {
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
            if (indexOnlineUser < onlineUser.getListRoles().size() &&
                    onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
                selectedUser = listUsers.get(getAdapterPosition());
                indexSelectedUser = selectedUser.getListDahiraID().indexOf(dahira.getDahiraID());
                adiya = null;
                sass = null;
                social = null;
                HomeActivity.showInterstitialAd(context);
                Intent intent = new Intent(context, UserInfoActivity.class);
                context.startActivity(intent);
            }
        }
    }
}