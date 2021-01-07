package com.example.lets_findus.ui.first_boot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.example.lets_findus.R;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroCustomLayoutFragment;

import org.jetbrains.annotations.Nullable;

public class FirstOpeningInformations extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences pref = this.getSharedPreferences("com.example.lets_findus.FIRST_BOOT", MODE_PRIVATE);
        int isFirstBoot = pref.getInt("FIRST_BOOT", 0);

        if(isFirstBoot == 0) {
            startActivity(new Intent(this, ProfileCreationActivity.class));
            finish();
        }
        else {
            finish();
        }
    }
}