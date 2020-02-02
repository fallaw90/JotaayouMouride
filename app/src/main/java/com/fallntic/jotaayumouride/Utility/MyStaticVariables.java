package com.fallntic.jotaayumouride.utility;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.adapter.SongAdapter;
import com.fallntic.jotaayumouride.model.Adiya;
import com.fallntic.jotaayumouride.model.Dahira;
import com.fallntic.jotaayumouride.model.Event;
import com.fallntic.jotaayumouride.model.Expense;
import com.fallntic.jotaayumouride.model.Image;
import com.fallntic.jotaayumouride.model.ObjNotification;
import com.fallntic.jotaayumouride.model.PubImage;
import com.fallntic.jotaayumouride.model.Sass;
import com.fallntic.jotaayumouride.model.Social;
import com.fallntic.jotaayumouride.model.Song;
import com.fallntic.jotaayumouride.model.UploadImage;
import com.fallntic.jotaayumouride.model.UploadPdf;
import com.fallntic.jotaayumouride.model.User;
import com.fallntic.jotaayumouride.notifications.CreateNotificationMusic;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.pushNext;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.pushPlay;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.pushPrevious;

@SuppressWarnings("unused")
public class MyStaticVariables {

    public static final String TITLE_ANNOUNCEMENT_NOTIFICATION = "Nouvelle Annoncement.";
    public static final String TITLE_EVENT_NOTIFICATION = "Nouveau événement.";
    public static final String TITLE_CONTRIBUTION_NOTIFICATION = "Cotisation ajouté.";
    public static final String TITLE_EXPENSE_NOTIFICATION = "Dépense modifiée.";
    public static String displayDahira;
    public static String displayEvent;
    public static Boolean updateStorage = false;

    //Contains all songs that belong to one dahira
    public static List<Song> listSong;
    public static List<Song> listAudiosQuran;
    public static List<Song> listAudiosSerigneMbayeDiakhate;
    public static List<Song> listAudiosSerigneMoussaKa;
    public static List<Song> listAudiosHT;
    public static List<Song> listAudiosHTDK;
    public static List<Song> listAudiosMagal2019HT;
    public static List<Song> listAudiosMagal2019HTDK;
    public static List<Song> listAudiosAM;
    public static List<Song> listAudiosRadiass;
    public static List<Song> listAudiosMixedWolofal;
    public static List<Song> listAudiosZikr;
    public static List<PubImage> listPubImage;

    public static List<User> listUser;
    public static Adiya adiya;
    public static Sass sass;
    public static Social social;

    //************* ProgressBar ************
    public static RelativeLayout relativeLayoutProgressBar, relativeLayoutData;
    public static ProgressBar progressBar;

    //Contains all songs that belong to one dahira
    public static List<Event> myListEvents;
    public static List<Event> listAllEvent;

    //Contains all user dahira
    public static List<Dahira> myListDahira;
    //Contains all existing dahira
    public static List<Dahira> listAllDahira;
    public static List<Dahira> listDahiraFound;

    //Contains all existing expenses
    public static List<Expense> listExpenses;

    public static List<Image> listImage;

    public static ObjNotification objNotification;

    public static List<UploadPdf> listUploadPDF;

    //*************************** Firebase ****************************
    public static FirebaseAuth firebaseAuth;
    public static FirebaseUser firebaseUser;
    public static FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    public static FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    public static StorageReference storageReference;
    public static CollectionReference collectionReference;


    //************************MediaPlayer********************************************
    public static SongAdapter mAdapter;
    public static RecyclerView recycler;
    public static int currentIndex = 0;
    public static Toolbar toolbar, toolbar_bottom;
    public static TextView tb_title, tv_empty, tv_duration;
    public static ImageView iv_play, iv_next, iv_previous;
    public static ProgressBar pb_loader, pb_main_loader;
    public static long currentSongLength;
    public static FloatingActionButton fab_search;
    public static boolean isPlaying = false;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static TextView tv_time;
    public static boolean firstLaunch = true;
    public static SeekBar seekBar;
    public static Handler myHandler;
    public static final Runnable UpdateSongTime = new Runnable() {
        public void run() {
            //seekBar.setMax(mediaPlayer.getDuration());
            if (mediaPlayer != null) {
                try {
                    double startTime = mediaPlayer.getCurrentPosition();
                    tv_time.setText(convertDuration(mediaPlayer.getCurrentPosition()));
                    seekBar.setProgress((int) startTime);
                    myHandler.postDelayed(this, 500);
                } catch (Exception ignored) {
                }

            }
        }
    };

    //************* Notification Music ********************
    public static int testVal = 0;
    public static int counterHAonPause = 0;
    public static int counterHAonResume = 0;
    public static boolean isNotificationMPUsed = false;
    public static boolean wasHAonStop = false;
    public static boolean wasHAonResume = false;
    public static boolean isTabQuranOpened = false;
    public static boolean isTabAudioOpened = false;
    public static String songChosen;
    public static Notification notificationMediaPlayer;
    public static NotificationManager notificationManagerMediaPlayer;
    public static List<Song> listTracks = new ArrayList<>();
    public static final BroadcastReceiver broadcastReceiverMediaPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = Objects.requireNonNull(intent.getExtras()).getString("actionname");
            if (action != null) {
                switch (action) {
                    case CreateNotificationMusic.ACTION_PREVIOUS:
                        //toastMessage(context, "CreateNotificationMusic.ACTION_PREVIOUS");
                        pushPrevious(context);
                        break;
                    case CreateNotificationMusic.ACTION_PLAY:
                        if (counterHAonPause == 0) {
                            isNotificationMPUsed = true;
                        }
                        pushPlay(context, MyStaticVariables.mediaPlayer);
                        break;
                    case CreateNotificationMusic.ACTION_NEXT:
                        //toastMessage(context, "CreateNotificationMusic.ACTION_NEXT");
                        pushNext(context);
                        break;
                }
            }
        }
    };

    public static String dahiraID = null;
    public static String userID;
    public static String actionSelected = "";
    public static String typeOfContribution = "";
    public static UploadImage uploadImages = null;
    public static boolean boolAddToDahira;
    public static User onlineUser = null;
    public static User selectedUser = null;
    public static Dahira dahira = null;
    public static int indexOnlineUser = -1;
    public static int indexSelectedUser = -1;
    public static ProgressDialog progressDialog;

    @SuppressLint("DefaultLocale")
    private static String convertDuration(long duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}