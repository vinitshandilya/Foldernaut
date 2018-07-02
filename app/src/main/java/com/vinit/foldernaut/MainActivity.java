package com.vinit.foldernaut;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vinit.foldernaut.fragments.BooksFragment;
import com.vinit.foldernaut.fragments.CloudFragment;
import com.vinit.foldernaut.fragments.DesktopAccessFragment;
import com.vinit.foldernaut.fragments.DocumentsFragment;
import com.vinit.foldernaut.fragments.DownloadsFragment;
import com.vinit.foldernaut.fragments.ExploreFragment;
import com.vinit.foldernaut.fragments.FavoritesFragment;
import com.vinit.foldernaut.fragments.MoviesFragment;
import com.vinit.foldernaut.fragments.MusicFragment;
import com.vinit.foldernaut.fragments.PicturesFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int navIndex=0;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE=100;
    private boolean storageReadAllowed = false;

    String[] titles = {"Explore", "Favorites", "Documents", "Downloads",
            "Pictures", "Movies", "Music", "Books", "Cloud", "Desktop Access", "Settings"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.openDrawer(Gravity.LEFT);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if(drawer!=null)
            drawer.addDrawerListener(toggle);
        toggle.syncState();

        //==================== Navigation drawer header element click listeners ====================
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if(navigationView!=null) {
            View headerview = navigationView.getHeaderView(0);
            ImageView closeApp = (ImageView)headerview.findViewById(R.id.navcloseapp);
            closeApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        //===================== Check and request runtime permissions if required ==================
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE);
            } else {
                storageReadAllowed = true; // Below JB, allow permission by default (defined in manifest)
            }
        } else
            storageReadAllowed = true;

        if(storageReadAllowed) { //If permission has already been granted..
            TextView permissionText = (TextView)findViewById(R.id.permissionText);
            if(permissionText!=null)
                permissionText.setText("");
            Toast.makeText(getBaseContext(),"Permission granted", Toast.LENGTH_SHORT).show();
            //Initialize NavigationDrawer at startup
            if(navigationView!=null) {
                navigationView.setNavigationItemSelectedListener(this);
                navigationView.getMenu().getItem(navIndex).setChecked(true);
            }
            setTitle(titles[navIndex]);
            //Load Explore Fragment at startup
            Fragment fragment = new ExploreFragment();
            FragmentManager fm=getSupportFragmentManager();
            FragmentTransaction ft=fm.beginTransaction();
            ft.replace(R.id.frame,fragment);
            ft.commit();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer!=null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) { //IF DRAWER IS OPEN, THEN CLOSE IT!
                drawer.closeDrawer(GravityCompat.START);
            }
            else {
                List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
                if (fragmentList != null) {
                    for(Fragment fragment : fragmentList){
                        if(fragment instanceof OnBackPressedListener){
                            ((OnBackPressedListener)fragment).onBackPressed();
                            //This allows onBackPressed() execution in the respective
                            //fragment class
                        }
                    }
                }
            }
        }
    }

    /*    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer!=null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
            else if(!drawer.isDrawerOpen(GravityCompat.START) && navIndex!=0) {
                Fragment fragment = new ExploreFragment();
                FragmentManager fm=getSupportFragmentManager();
                FragmentTransaction ft=fm.beginTransaction();
                ft.setCustomAnimations(R.anim.enter_from_left,R.anim.exit_to_right,R.anim.enter_from_right,R.anim.exit_to_left);
                ft.replace(R.id.frame,fragment);
                ft.commit();
                navIndex=0;
                setTitle(titles[navIndex]);
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                if(navigationView!=null)
                    navigationView.getMenu().getItem(navIndex).setChecked(true);
            } else {
                super.onBackPressed();
            }
        }
    } */

    public void setTitle(String str) {
        ActionBar ab = getSupportActionBar();
        if(ab!=null)
            ab.setTitle(str);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        (new ClickSound(getBaseContext(), R.raw.buttonglassmp3)).play();
        Fragment fragment=null;
        int id = item.getItemId();

        if (id == R.id.nav_explore && navIndex!=0) {
            fragment = new ExploreFragment();
            navIndex=0;
        }
        else if (id == R.id.nav_favorite && navIndex!=1 ) {
            fragment = new FavoritesFragment();
            navIndex = 1;
        }
        else if (id == R.id.nav_document && navIndex!=2 ) {
            fragment = new DocumentsFragment();
            navIndex = 2;
        }
        else if (id == R.id.nav_downloads && navIndex!=3 ) {
            fragment = new DownloadsFragment();
            navIndex = 3;
        }
        else if (id == R.id.nav_pictures && navIndex!=4 ) {
            fragment = new PicturesFragment();
            navIndex = 4;
        }
        else if (id == R.id.nav_movies && navIndex!=5 ) {
            fragment = new MoviesFragment();
            navIndex=5;
        }
        else if (id == R.id.nav_music && navIndex!=6 ) {
            fragment = new MusicFragment();
            navIndex=6;
        }
        else if (id == R.id.nav_books && navIndex!=7 ) {
            fragment = new BooksFragment();
            navIndex=7;
        }
        else if (id == R.id.nav_cloud && navIndex!=8 ) {
            fragment = new CloudFragment();
            navIndex=8;
        }
        else if (id == R.id.nav_remote && navIndex!=9 ) {
            fragment = new DesktopAccessFragment();
            navIndex=9;
        }
        else if (id == R.id.nav_settings ) {

        }
        loadFragment(fragment);

        return true;
    }

    public void loadFragment(final Fragment fragment) {
        /*Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(fragment!=null) {
                    FragmentManager fm=getSupportFragmentManager();
                    FragmentTransaction ft=fm.beginTransaction();
                    ft.setCustomAnimations(R.anim.enter_from_left,R.anim.exit_to_right,R.anim.enter_from_right,R.anim.exit_to_left);
                    ft.replace(R.id.frame,fragment);
                    ft.commit();
                    setTitle(titles[navIndex]);
                }
            }
        }, 100); */

        if(fragment!=null) {
            FragmentManager fm=getSupportFragmentManager();
            FragmentTransaction ft=fm.beginTransaction();
            ft.setCustomAnimations(R.anim.enter_from_left,R.anim.exit_to_right,R.anim.enter_from_right,R.anim.exit_to_left);
            ft.replace(R.id.frame,fragment);
            ft.commit();
            setTitle(titles[navIndex]);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer!=null)
            drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    storageReadAllowed = true;
                    TextView permissionText = (TextView)findViewById(R.id.permissionText);
                    if(permissionText!=null)
                        permissionText.setText("");
                    Toast.makeText(getBaseContext(),"Permission granted", Toast.LENGTH_SHORT).show();

                    //Initialize NavigationDrawer at startup
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    if(navigationView!=null) {
                        navigationView.setNavigationItemSelectedListener(this);
                        navigationView.getMenu().getItem(navIndex).setChecked(true);
                    }
                    setTitle(titles[navIndex]);
                    //Load Explore Fragment at startup
                    Fragment fragment = new ExploreFragment();
                    FragmentManager fm=getSupportFragmentManager();
                    FragmentTransaction ft=fm.beginTransaction();
                    ft.replace(R.id.frame,fragment);
                    ft.commit();
                }
                else {
                    storageReadAllowed = false;
                    Toast.makeText(getBaseContext(),"Permission denied", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }
}