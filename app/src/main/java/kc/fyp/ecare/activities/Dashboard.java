package kc.fyp.ecare.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.onesignal.OneSignal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import de.hdodenhof.circleimageview.CircleImageView;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.fragments.DashboardFragment;
import kc.fyp.ecare.fragments.MyAnnouncementsFragment;
import kc.fyp.ecare.fragments.MyDonationsFragment;
import kc.fyp.ecare.fragments.MyNotificationsFragment;
import kc.fyp.ecare.models.User;
import kc.fyp.ecare.ui.NoSwipeableViewPager;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    public static final String TAG = "Dashboard";
    private DrawerLayout drawer;
    private NoSwipeableViewPager pager;
    private Session session;
    private FloatingActionsMenu floatingActionsMenu;
    // Fragments
    private DashboardFragment dashboardFragment;
    private MyDonationsFragment myDonationsFragment;
    private MyNotificationsFragment myNotificationsFragment;
    private MyAnnouncementsFragment myAnnouncementsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize Fragments
        dashboardFragment = new DashboardFragment();
        myDonationsFragment = new MyDonationsFragment();
        myAnnouncementsFragment = new MyAnnouncementsFragment();
        myNotificationsFragment = new MyNotificationsFragment();

        pager = findViewById(R.id.pager);
        pager.setOffscreenPageLimit(2);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), 0);
        pager.setAdapter(adapter);

        session = new Session(getApplicationContext());
        User user = session.getUser();
        View header = navigationView.getHeaderView(0);

        CircleImageView imageView = header.findViewById(R.id.imageView);
        if (user.getImage() != null && user.getImage().length() > 0) {
            Glide.with(getApplicationContext()).load(user.getImage()).into(imageView);
        }
        TextView name = header.findViewById(R.id.name);
        TextView email = header.findViewById(R.id.email);
        TextView phoneNumber = header.findViewById(R.id.phoneNumber);

        name.setText(user.getName());
        email.setText(user.getEmail());
        phoneNumber.setText(user.getPhoneNumber());

        floatingActionsMenu = findViewById(R.id.floatingActionsMenu);
        FloatingActionButton newProduct = findViewById(R.id.newProduct);
        FloatingActionButton newAnnouncements = findViewById(R.id.newAnnouncements);
        newProduct.setOnClickListener(this);
        newAnnouncements.setOnClickListener(this);
        initPushNotifications();
    }


    private void initPushNotifications() {
        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Log.e(TAG, "" + id);
        switch (id) {
            case R.id.nav_home: {
                pager.setCurrentItem(0);
                break;
            }
            case R.id.nav_donations: {
                pager.setCurrentItem(1);
                break;
            }
            case R.id.nav_announcements: {
                pager.setCurrentItem(2);
                break;
            }
            case R.id.nav_notifications: {
                pager.setCurrentItem(3);
                break;
            }
            case R.id.nav_profile: {
                Intent it = new Intent(Dashboard.this, EditProfile.class);
                startActivity(it);
                break;
            }
            case R.id.nav_logout: {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    auth.signOut();
                }
                session.destroySession();
                Intent it = new Intent(Dashboard.this, Splash.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(it);
                finish();
                break;
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.newProduct: {
                Log.e(TAG, "New Product Clicked");
                floatingActionsMenu.collapse();
                Intent it = new Intent(Dashboard.this, NewDonation.class);
                startActivity(it);
                break;
            }
            case R.id.newAnnouncements: {
                Log.e(TAG, "New Announcements Clicked");
                floatingActionsMenu.collapse();
                Intent it = new Intent(Dashboard.this, NewAnnouncement.class);
                startActivity(it);
                break;
            }
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return dashboardFragment;
                }
                case 1: {
                    return myDonationsFragment;
                }
                case 2: {
                    return myAnnouncementsFragment;
                }
                case 3: {
                    return myNotificationsFragment;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}