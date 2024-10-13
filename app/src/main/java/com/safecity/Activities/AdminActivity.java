package com.safecity.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.safecity.Fragments.AdminMapsFragment;
import com.safecity.Fragments.MenuFragment;
import com.safecity.Fragments.AdminReportsFragment;
import com.safecity.Fragments.ReportsFragment;
import com.safecity.Fragments.ViewReportFragment;
import com.safecity.Objects.Report;
import com.safecity.R;
import com.safecity.Utils.Utils;

public class AdminActivity extends AppCompatActivity {

    BottomNavigationView bottom_navbar;
    boolean doubleBackToExitPressedOnce = false;

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
                        Fragment mapsFragment = new AdminMapsFragment();
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
                        Fragment reportsFragment = new AdminReportsFragment();
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
        Fragment mapsFragment = new AdminMapsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, mapsFragment, "MAPS_FRAGMENT");
        fragmentTransaction.addToBackStack("MAPS_FRAGMENT");
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AdminMapsFragment mapsFragment = (AdminMapsFragment) fragmentManager.findFragmentByTag("MAPS_FRAGMENT");
        ViewReportFragment viewReportFragment = (ViewReportFragment) fragmentManager.findFragmentByTag("VIEW_REPORT_FRAGMENT");
        MenuFragment menuFragment = (MenuFragment) getSupportFragmentManager().findFragmentByTag("MENU_FRAGMENT");
        AdminReportsFragment adminReportsFragment = (AdminReportsFragment) getSupportFragmentManager().findFragmentByTag("REPORTS_FRAGMENT");

        if (fragmentManager.getBackStackEntryCount() >= 2 ) {
            int indexCurrent = fragmentManager.getBackStackEntryCount() - 1;
            FragmentManager.BackStackEntry entryCurrent = fragmentManager.getBackStackEntryAt(indexCurrent);
            String tagCurrent = entryCurrent.getName();
            Fragment fragmentCurrent = fragmentManager.findFragmentByTag(tagCurrent);

            int indexPrevious = fragmentManager.getBackStackEntryCount() - 2;
            FragmentManager.BackStackEntry entryPrevious = fragmentManager.getBackStackEntryAt(indexPrevious);
            String tagPrevious = entryPrevious.getName();
            Fragment fragmentPrevious = fragmentManager.findFragmentByTag(tagPrevious);

            if (fragmentCurrent == viewReportFragment && fragmentPrevious == mapsFragment) {
                getSupportFragmentManager().popBackStack();

                AdminReportsFragment reportsFragment = new AdminReportsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, reportsFragment, "REPORTS_FRAGMENT")
                        .addToBackStack("REPORTS_FRAGMENT")
                        .commit();
            }
            else if (fragmentCurrent == menuFragment || fragmentCurrent == mapsFragment || fragmentCurrent == adminReportsFragment) {
                doubleBackToExit();
            }
            else {
                super.onBackPressed();
            }
        }
        else if (fragmentManager.getBackStackEntryCount() == 1) {
            doubleBackToExit();
        }
    }

    private void doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private void bottomNavbarManager() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            AdminMapsFragment mapsFragment = (AdminMapsFragment) getSupportFragmentManager().findFragmentByTag("MAPS_FRAGMENT");
            MenuFragment menuFragment = (MenuFragment) getSupportFragmentManager().findFragmentByTag("MENU_FRAGMENT");
            AdminReportsFragment reportsFragment = (AdminReportsFragment) getSupportFragmentManager().findFragmentByTag("REPORTS_FRAGMENT");
            ViewReportFragment viewReportFragment = (ViewReportFragment) getSupportFragmentManager().findFragmentByTag("VIEW_REPORT_FRAGMENT");

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
            else if (viewReportFragment != null && viewReportFragment.isVisible()) {
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
                if (heightDiff > Utils.dpToPx(AdminActivity.this, 200)) {
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