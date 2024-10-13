package com.safecity.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.safecity.Fragments.MenuFragment;
import com.safecity.Fragments.ReportsFragment;
import com.safecity.Fragments.UserMapsFragment;
import com.safecity.R;
import com.safecity.Utils.Utils;

public class UserActivity extends AppCompatActivity {

    BottomNavigationView bottom_navbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initialize();
        softKeyboardListener();

        bottom_navbar.getMenu().getItem(1).setChecked(true);
        bottom_navbar.setOnItemSelectedListener(item -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.miMap:
                    if (bottom_navbar.getSelectedItemId() != R.id.miMap) {
                        Fragment mapsFragment = new UserMapsFragment();
                        fragmentTransaction.replace(R.id.frameLayout, mapsFragment, "MAPS_FRAGMENT")
                                .setCustomAnimations(
                                        R.anim.slide_right_enter,  // enter
                                        R.anim.slide_right_exit,  // exit
                                        R.anim.slide_right_enter,   // popEnter
                                        R.anim.slide_right_exit  // popExit
                                )
                                .addToBackStack("MAPS_FRAGMENT")
                                .commit();
                    }
                    break;
                case R.id.miMenu:
                    if (bottom_navbar.getSelectedItemId() != R.id.miMenu) {
                        Fragment menuFragment = new MenuFragment();
                        fragmentTransaction.replace(R.id.frameLayout, menuFragment, "MENU_FRAGMENT")
                                .setCustomAnimations(
                                        R.anim.slide_left_enter,  // enter
                                        R.anim.slide_left_exit,  // exit
                                        R.anim.slide_left_enter,   // popEnter
                                        R.anim.slide_left_exit  // popExit
                                )
                                .addToBackStack("MENU_FRAGMENT")
                                .commit();
                    }
                    break;
                case R.id.miReports:
                    if (bottom_navbar.getSelectedItemId() != R.id.miReports) {
                        Fragment reportsFragment = new ReportsFragment();
                        fragmentTransaction.replace(R.id.frameLayout, reportsFragment, "REPORTS_FRAGMENT")
                                .setCustomAnimations(
                                        R.anim.slide_left_enter,  // enter
                                        R.anim.slide_left_exit,  // exit
                                        R.anim.slide_left_enter,   // popEnter
                                        R.anim.slide_left_exit  // popExit
                                )
                                .addToBackStack("REPORTS_FRAGMENT")
                                .commit();
                    }
                    break;
            }
            return true;
        });
    }

    private void initialize() {
        bottom_navbar = findViewById(R.id.bottom_navbar);

        bottomNavbarManager();

        // fragment that will start on launch
        Fragment mapsFragment = new UserMapsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, mapsFragment, "MAPS_FRAGMENT");
        fragmentTransaction.addToBackStack("MAPS_FRAGMENT");
        fragmentTransaction.commit();
    }

    private void bottomNavbarManager() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            UserMapsFragment mapsFragment = (UserMapsFragment) getSupportFragmentManager().findFragmentByTag("MAPS_FRAGMENT");
            MenuFragment menuFragment = (MenuFragment) getSupportFragmentManager().findFragmentByTag("MENU_FRAGMENT");
            ReportsFragment reportsFragment = (ReportsFragment) getSupportFragmentManager().findFragmentByTag("REPORTS_FRAGMENT");

            if (mapsFragment != null && mapsFragment.isVisible()) {
                bottom_navbar.getMenu().getItem(1).setChecked(true);
            }
            else if (menuFragment != null && menuFragment.isVisible()) {
                softKeyboardListener();
                bottom_navbar.getMenu().getItem(0).setChecked(true);
            }
            else if (reportsFragment != null && reportsFragment.isVisible()) {
                bottom_navbar.getMenu().getItem(2).setChecked(true);
            }
        });
    }

    private void softKeyboardListener() {
        final View activityRootView = findViewById(R.id.frameLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > Utils.dpToPx(UserActivity.this, 200)) {
                    // if keyboard visible
                    bottom_navbar.setVisibility(View.GONE);
                }
                else {
                    bottom_navbar.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}