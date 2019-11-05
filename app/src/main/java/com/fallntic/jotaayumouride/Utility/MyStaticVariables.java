package com.fallntic.jotaayumouride.Utility;

import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Adapter.SongAdapter;
import com.fallntic.jotaayumouride.Model.Adiya;
import com.fallntic.jotaayumouride.Model.Dahira;
import com.fallntic.jotaayumouride.Model.Event;
import com.fallntic.jotaayumouride.Model.Expense;
import com.fallntic.jotaayumouride.Model.Image;
import com.fallntic.jotaayumouride.Model.ObjNotification;
import com.fallntic.jotaayumouride.Model.Sass;
import com.fallntic.jotaayumouride.Model.Social;
import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.Model.UploadPdf;
import com.fallntic.jotaayumouride.Model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.convertDuration;

public class MyStaticVariables {

    public static final String TITLE_ANNOUNCEMENT_NOTIFICATION = "Nouvelle Annoncement.";
    public static String TITLE_EXPENSE_NOTIFICATION = "Dépense modifiée.";
    public static final String TITLE_EVENT_NOTIFICATION = "Nouveau événement.";
    public static final String TITLE_CONTRIBUTION_NOTIFICATION = "Cotisation ajouté.";
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
    public static double startTime = 0;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static TextView tv_time;
    public static SeekBar seekBar;
    public static boolean firstLaunch = true;
    public static Handler myHandler;
    public static Runnable UpdateSongTime = new Runnable() {
        public void run() {
            //seekBar.setMax(mediaPlayer.getDuration());
            startTime = mediaPlayer.getCurrentPosition();
            tv_time.setText(convertDuration(mediaPlayer.getCurrentPosition()));
            seekBar.setProgress((int) startTime);
            myHandler.postDelayed(this, 500);
        }
    };

}
