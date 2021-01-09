package com.example.lets_findus;

import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Room;

import com.example.lets_findus.ui.ViewPictureActivity;
import com.example.lets_findus.utilities.AppDatabase;
import com.example.lets_findus.utilities.MeetingDao;
import com.example.lets_findus.utilities.MeetingPerson;
import com.example.lets_findus.utilities.Person;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;
import java.util.concurrent.Executors;

import uk.co.onemandan.materialtextview.MaterialTextView;

public class PersonProfileActivity extends AppCompatActivity {

    private static AppDatabase db;
    private static MeetingDao md;

    private MeetingPerson[] meeting = new MeetingPerson[1];
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_person_profile);

        final ConstraintLayout obbData = findViewById(R.id.obbligatory_data);
        final ConstraintLayout nonObbData = findViewById(R.id.other_data);
        final CircularImageView photo = findViewById(R.id.circularImageView);
        final TextView nickname = findViewById(R.id.nickname_card);


        if(getIntent().hasExtra("MEETING_ID")){
            final int meetingId = getIntent().getExtras().getInt("MEETING_ID");
            if(db == null){
                db = Room.databaseBuilder(this, AppDatabase.class, "meeting_db").build();
                md = db.meetingDao();
            }
            Futures.addCallback(md.getMeetingFromId(meetingId), new FutureCallback<MeetingPerson>() {
                @Override
                public void onSuccess(@NullableDecl final MeetingPerson result) {
                    meeting[0] = result;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            fillFields(result.person, obbData);
                            fillFields(result.person, nonObbData);
                            photo.setImageURI(Uri.parse(result.person.profilePath));
                            nickname.setText(result.person.nickname);
                            photo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(PersonProfileActivity.this, ViewPictureActivity.class);
                                    i.putExtra("PIC_PATH", result.person.profilePath);
                                    View sharedView = photo;
                                    String transitionName = getString(R.string.image_transition);
                                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(PersonProfileActivity.this, sharedView, transitionName);
                                    startActivity(i, transitionActivityOptions.toBundle());
                                }
                            });
                        }
                    });
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, Executors.newSingleThreadExecutor());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.person_profile_menu, menu);
        if(getIntent().hasExtra("IS_FROM_FAVOURITE")) {
            menu.setGroupVisible(R.id.favourite, true);
            menu.setGroupVisible(R.id.not_favourite, false);
        }
        else{
            menu.setGroupVisible(R.id.favourite, false);
            menu.setGroupVisible(R.id.not_favourite, true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.empty_star){
            menu.setGroupVisible(R.id.not_favourite, false);
            menu.setGroupVisible(R.id.favourite, true);
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    md.setFavourite(meeting[0].meeting.id);
                }
            });
        }
        else{
            if(item.getItemId() == R.id.filled_star) {
                menu.setGroupVisible(R.id.not_favourite, true);
                menu.setGroupVisible(R.id.favourite, false);
                meeting[0].meeting.setFavourite(false);
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        md.setNotFavourite(meeting[0].meeting.id);
                    }
                });
            }
            else{
                Intent mIntent = new Intent(this, MainActivity.class);
                if(getIntent().hasExtra("IS_FROM_FAVOURITE")) {
                    mIntent.putExtra("IS_FROM_FAV", true);
                }
                mIntent.putExtra("IS_FROM_VIEW", true);
                finish();
                startActivity(mIntent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillFields(Person person, ConstraintLayout container){
        Map<String, String> profileDump = person.dumpToString();
        for(int i = 0; i < container.getChildCount(); i++){
            final View v = container.getChildAt(i);
            if(v instanceof MaterialTextView){
                final String label = ((MaterialTextView) v).getLabelText().toString();
                if(profileDump.get(label) == null){
                    v.setVisibility(View.GONE);
                }
                else{
                    ((MaterialTextView) v).setContentText(profileDump.get(label), null);
                    final String content = profileDump.get(label);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(v.getContext().CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText(label, content);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(v.getContext(), R.string.copied, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }
}