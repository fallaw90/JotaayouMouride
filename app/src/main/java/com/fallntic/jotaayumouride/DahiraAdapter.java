package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.announcement;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.event;
import static com.fallntic.jotaayumouride.DataHolder.expense;
import static com.fallntic.jotaayumouride.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.DataHolder.notificationBody;
import static com.fallntic.jotaayumouride.DataHolder.notificationTitle;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showImage;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.uploadImages;

public class DahiraAdapter extends RecyclerView.Adapter<DahiraAdapter.DahiraViewHolder> {
    public static final String TAG = "DahiraAdapter";
    private Context context;
    private List<Dahira> dahiraList;

    private ImageView imageView;

    public DahiraAdapter(Context context, List<Dahira> dahiraList) {
        this.context = context;
        this.dahiraList = dahiraList;
    }

    @NonNull
    @Override
    public DahiraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DahiraViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_dahira, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull DahiraViewHolder holder, int position) {
        Dahira dahira = dahiraList.get(position);

        holder.textViewDahiraName.setText("Dahira " + dahira.getDahiraName());
        holder.textViewDieuwrine.setText("Dieuwrine: " + dahira.getDieuwrine());
        holder.textViewPhoneNumber.setText("Telephone: " + dahira.getDahiraPhoneNumber());
        holder.textViewSiege.setText("Siege: " + dahira.getSiege());

        showImage(context, "logoDahira", dahira.getDahiraID(), imageView);
    }

    @Override
    public int getItemCount() {
        return dahiraList.size();
    }

    public void getUploadImages(Context context) {
        showProgressDialog(context, "Chargement de vos depenses en cours ...");
        FirebaseFirestore.getInstance().collection("uploadImages")
                .whereEqualTo("dahiraID", dahira.getDahiraID()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                uploadImages = documentSnapshot.toObject(UploadImage.class);
                            }
                            Log.d(TAG, "Image name downloaded");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error downloading image name");
                    }
                });
    }

    class DahiraViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView textViewDahiraName;
        TextView textViewDieuwrine;
        TextView textViewPhoneNumber;
        TextView textViewSiege;

        public DahiraViewHolder(View itemView) {
            super(itemView);

            textViewDahiraName = itemView.findViewById(R.id.textview_dahiraName);
            textViewDieuwrine = itemView.findViewById(R.id.textview_dieuwrine);
            textViewPhoneNumber = itemView.findViewById(R.id.textview_phoneNumber);
            textViewSiege = itemView.findViewById(R.id.textview_siege);
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (dahiraList.size() > getAdapterPosition()) {
                dahira = dahiraList.get(getAdapterPosition());
                if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
                    indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
                }

                expense = null;
                announcement = null;
                event = null;
                uploadImages = null;

                getExistingExpenses(context);
                getExistingAnnouncements(context, dahira, null);
                getExistingEvents(context);
                getUploadImages(context);

                Intent intent = new Intent(context, DahiraInfoActivity.class);
                context.startActivity(intent);
                dahiraList.clear();
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (dahiraList.size() > getAdapterPosition()) {
                dahira = dahiraList.get(getAdapterPosition());
                if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
                    indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
                }
                Intent intent = new Intent(context, UpdateDahiraActivity.class);
                context.startActivity(intent);
            }
            return false;
        }

    }


    public static void getExistingEvents(Context context) {
        showProgressDialog(context, "Chargement des evenements en cours ...");
        FirebaseFirestore.getInstance().collection("events").
                whereEqualTo("dahiraID", dahira.getDahiraID()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                event = documentSnapshot.toObject(Event.class);
                            }
                            Log.d(TAG, "Even downloaded");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        Log.d(TAG, "Error downloading event");
                    }
                });
    }

    public static void getExistingAnnouncements(final Context context, Dahira dahira, final Intent intent) {
        if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
            showProgressDialog(context, "Chargement de vos annonces en cours ...");
            FirebaseFirestore.getInstance().collection("announcements")
                    .whereEqualTo("dahiraID", dahira.getDahiraID()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            dismissProgressDialog();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    announcement = documentSnapshot.toObject(Announcement.class);

                                    //Go to ListAnnouncementActivity when we hit the notification
                                    if (notificationTitle != null && notificationBody != null &&
                                            notificationTitle.equals("Annoncement")) {
                                        context.startActivity(new Intent(context, ListAnnouncementActivity.class));
                                        break;
                                    }

                                    if (intent != null)
                                        context.startActivity(intent);
                                }
                                Log.d(TAG, "Announcements downloaded");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            Log.d(TAG, "Error downloading Announcements");
                        }
                    });
        }
    }

    public static void getExistingExpenses(final Context context) {
        if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
            showProgressDialog(context, "Chargement de vos depenses en cours ...");
            FirebaseFirestore.getInstance().collection("expenses")
                    .whereEqualTo("dahiraID", dahira.getDahiraID()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            dismissProgressDialog();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    expense = documentSnapshot.toObject(Expense.class);
                                    //Go to ListAnnouncementActivity when we hit the notification
                                    if (notificationTitle != null && notificationBody != null &&
                                            notificationTitle.equals("Nouvelle Dépense")) {
                                        context.startActivity(new Intent(context, ListExpenseActivity.class));
                                        break;
                                    }
                                }
                                Log.d(TAG, "Expenses downloaded");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            Log.d(TAG, "Error downloading Expenses");
                        }
                    });
        }
    }
}