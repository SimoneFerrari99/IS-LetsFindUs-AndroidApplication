package com.example.lets_findus.ui.matching;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.room.Room;

import com.example.lets_findus.R;
import com.example.lets_findus.utilities.AppDatabase;
import com.example.lets_findus.utilities.MeetingDao;
import com.example.lets_findus.utilities.Person;
import com.example.lets_findus.utilities.PersonDao;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.concurrent.Executors;
//classe adapter per la visualizzazione del popup al click del marker sulla mappa
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    private static AppDatabase db;
    private static MeetingDao md;
    private static PersonDao pd;

    private String uri = ""; 

    public CustomInfoWindowAdapter(Activity context){
        this.context = context;
        if(db == null){
            db = Room.databaseBuilder(context, AppDatabase.class, "meeting_db").build();
            md = db.meetingDao();
            pd = db.personDao();
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        View view = context.getLayoutInflater().inflate(R.layout.custominfowindow, null);

        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        final CircularImageView tvImage = (CircularImageView) view.findViewById(R.id.tv_image);
        tvTitle.setText(marker.getTitle());

        if(uri.compareTo("") == 0) { //non ho ancora ottenuto la foto da visualizzare 
            //prendo la person associata all'intero salvato nello snippet del marker
            ListenableFuture<Person> person = pd.getPersonById(Integer.parseInt(marker.getSnippet()));
            //quando terminerà questa query aggiorno l'uri della foto e richiamo showInfoWindow per ricaricare il marker
            Futures.addCallback(person, new FutureCallback<Person>() {
                @Override
                public void onSuccess(@NullableDecl final Person result) {
                    Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            uri = result.profilePath;
                            marker.showInfoWindow();
                        }
                    });
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, Executors.newSingleThreadExecutor());
        }
        else{
            tvImage.setImageURI(Uri.parse(uri));
        }
        return view;
    }
}
