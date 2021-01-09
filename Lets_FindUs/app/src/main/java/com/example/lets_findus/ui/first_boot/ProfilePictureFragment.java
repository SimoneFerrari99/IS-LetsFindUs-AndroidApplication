package com.example.lets_findus.ui.first_boot;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.lets_findus.R;
import com.example.lets_findus.utilities.Person;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static android.app.Activity.RESULT_OK;

public class ProfilePictureFragment extends Fragment {
    private Future<Person> profile;
    private String myProfileFilename = "myProfile";

    private CircularImageView image;
    private FloatingActionButton nextFab;

    private ActivityResultLauncher<Intent> takePhoto;
    private ActivityResultLauncher<Intent> pickPhoto;
    private String currentPhotoPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile_picture, container, false);

        try {
            FileInputStream fis = requireContext().openFileInput(myProfileFilename);
            profile = Person.loadPersonAsync(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        image = root.findViewById(R.id.profile_picture_holder);
        image.setOnClickListener(imageSelector);

        takePhoto =  registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    Uri uri = Uri.parse(currentPhotoPath);
                    launchUCrop(uri, uri);
                }
            }
        });

        pickPhoto =  registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    Uri sourceUri = result.getData().getData(); // 1
                    File file = null; // 2
                    try {
                        file = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Uri destinationUri = Uri.fromFile(file);  // 3
                    launchUCrop(sourceUri, destinationUri);
                }
            }
        });

        nextFab = root.findViewById(R.id.profile_picture_next_fab);
        nextFab.setVisibility(View.GONE);
        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Person myProfile = profile.get();
                    myProfile.profilePath = currentPhotoPath;
                    myProfile.storePersonAsync(getContext().openFileOutput(myProfileFilename, Context.MODE_PRIVATE));
                    Navigation.findNavController(v).navigate(R.id.action_profilePictureFragment_to_insertObbligatoryDataFragment);
                } catch (ExecutionException | InterruptedException | FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            image.setImageURI(resultUri);
            nextFab.show();
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

    private final View.OnClickListener imageSelector = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final CharSequence[] options = { getString(R.string.take_photo), getString(R.string.choose_photo)};

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.choose_pro_pic);

            builder.setItems(options, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (options[item].equals(getString(R.string.take_photo))) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(getContext(),
                                    "com.example.android.fileprovider",
                                    photoFile);
                            takePhoto.launch(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, photoURI));
                        }
                    } else {
                        pickPhoto.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI));
                    }
                }
            });
            builder.show();
        }
    };

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void launchUCrop(Uri source, Uri destination){
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        options.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        UCrop.of(source, destination)
                .withOptions(options)
                .withAspectRatio(1, 1)
                .start(getActivity().getApplicationContext(), ProfilePictureFragment.this);
    }
}