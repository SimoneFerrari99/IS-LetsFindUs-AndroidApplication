package com.example.lets_findus.ui.first_boot;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.example.lets_findus.R;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroCustomLayoutFragment;

import org.jetbrains.annotations.Nullable;
//slide relativa alle informazioni sull'applicazione
public class FirstOpeningInformations extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //aggiungo i layout alle pagine
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_first));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_information_one));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_information_two));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_information_three));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_security_warning));
        showStatusBar(true);
        setStatusBarColorRes(R.color.colorPrimaryDark);
        setBarColor(getColor(R.color.colorSecondary));
        setSeparatorColor(getColor(R.color.colorSecondary));
        setDoneText("Ho capito");
        setColorDoneText(Color.BLACK);
        setIndicatorColor(getColor(R.color.selectedDot), getColor(R.color.unselectedDot));
        setNextArrowColor(Color.BLACK);
        setSkipButtonEnabled(false);
    }
    //Quando premo done mi manda alla pagina successiva
    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        startActivity(new Intent(this, ProfileCreationActivity.class));
    }
}