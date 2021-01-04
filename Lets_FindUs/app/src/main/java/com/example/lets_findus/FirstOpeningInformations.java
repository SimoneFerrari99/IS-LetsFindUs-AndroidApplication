package com.example.lets_findus;

import android.graphics.Color;
import android.os.Bundle;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroCustomLayoutFragment;

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
}