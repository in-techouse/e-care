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
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
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
import kc.fyp.ecare.services.NotificationService;
import kc.fyp.ecare.ui.NoSwipeableViewPager;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, OneSignal.NotificationReceivedHandler, OneSignal.NotificationOpenedHandler {
    public static final String TAG = "Dashboard";
    private DrawerLayout drawer;
    private NoSwipeableViewPager pager;
    private FloatingActionsMenu floatingActionsMenu;
    private Toolbar toolbar;
    private Session session;
    private User user;
    // Fragments
    private DashboardFragment dashboardFragment;
    private MyDonationsFragment myDonationsFragment;
    private MyNotificationsFragment myNotificationsFragment;
    private MyAnnouncementsFragment myAnnouncementsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Toolbar title to HOME
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("HOME");
        setSupportActionBar(toolbar);

        // Set drawer & navigation
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set drawer listener, close the drawer automatically when an option is selected by user.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize Fragments
        dashboardFragment = new DashboardFragment();
        myDonationsFragment = new MyDonationsFragment();
        myAnnouncementsFragment = new MyAnnouncementsFragment();
        myNotificationsFragment = new MyNotificationsFragment();

        // Set fragments to view pager (ViewPager is used to display fragments)
        pager = findViewById(R.id.pager);
        pager.setOffscreenPageLimit(2); // On screen start, load all the fragments
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), 0);
        pager.setAdapter(adapter);

        // Get current logged in user from session
        session = new Session(getApplicationContext());
        user = session.getUser();
        // Show user detail on Navigation header
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

        // Floating action button listener
        floatingActionsMenu = findViewById(R.id.floatingActionsMenu);
        FloatingActionButton newProduct = findViewById(R.id.newProduct);
        FloatingActionButton newAnnouncements = findViewById(R.id.newAnnouncements);
        newProduct.setOnClickListener(this);
        newAnnouncements.setOnClickListener(this);

        // Start user push notification
        initPushNotifications();
    }

    // Start user push notification
    private void initPushNotifications() {
        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationReceivedHandler(this)
                .setNotificationOpenedHandler(this)
                .init();
        OneSignal.promptLocation();
        OneSignal.sendTag("id", user.getId());
        OneSignal.sendTag("email", user.getEmail());
        OneSignal.sendTag("phoneNumber", user.getPhoneNumber());

        NotificationService.sendNotificationToAllUsers(getApplicationContext(), "Hello User", "test", user.getId());
    }

    // This function is called whenever an option is selected from left navigation menu.
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Log.e(TAG, "" + id);
        switch (id) {
            case R.id.nav_home: { // Load Home Fragment, if user click "Home"
                toolbar.setTitle("HOME");
                pager.setCurrentItem(0);
                break;
            }
            case R.id.nav_donations: { // Load My Donations Fragment, if user click "My Donations"
                toolbar.setTitle("MY DONATIONS");
                pager.setCurrentItem(1);
                break;
            }
            case R.id.nav_announcements: { // Load My Announcements Fragment, if user click "My Announcements"
                toolbar.setTitle("MY ANNOUNCEMENTS");
                pager.setCurrentItem(2);
                break;
            }
            case R.id.nav_notifications: { // Load My Notifications Fragment, if user click "My Notifications"
                toolbar.setTitle("MY NOTIFICATIONS");
                pager.setCurrentItem(3);
                break;
            }
            case R.id.nav_profile: { // Open edit profile activity, if user click "Profile"
                Intent it = new Intent(Dashboard.this, EditProfile.class);
                startActivity(it);
                break;
            }
            case R.id.nav_logout: { // Logout user, if user click "Logout"
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

    // To capture the click of Floating action buttons
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            // If user clicks on "New Donation", Start NewDonation activity.
            case R.id.newProduct: {
                Log.e(TAG, "New Product Clicked");
                floatingActionsMenu.collapse();
                Intent it = new Intent(Dashboard.this, NewDonation.class);
                startActivity(it);
                break;
            }
            // If user clicks on "New Announcement", Start NewAnnouncement activity.
            case R.id.newAnnouncements: {
                Log.e(TAG, "New Announcements Clicked");
                floatingActionsMenu.collapse();
                Intent it = new Intent(Dashboard.this, NewAnnouncement.class);
                startActivity(it);
                break;
            }
        }
    }

    // Part of Push Notification Service, called when the notification is open
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        Log.e(TAG, "Notification Opened");
    }

    // Part of Push Notification Service, called when the notification is received
    @Override
    public void notificationReceived(OSNotification notification) {
        Log.e(TAG, "Notification Received");
    }

    // Pager Adapter is used with ViewPager to display the fragments to the user.
    private class PagerAdapter extends FragmentPagerAdapter {

        // Constructor
        public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        // Return relevant fragment function
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

        // Return total number of fragments
        @Override
        public int getCount() {
            return 4;
        }
    }
}