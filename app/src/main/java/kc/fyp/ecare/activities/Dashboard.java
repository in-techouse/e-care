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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.fragments.DashboardFragment;
import kc.fyp.ecare.fragments.MyAnnouncementsFragment;
import kc.fyp.ecare.fragments.MyDonationsFragment;
import kc.fyp.ecare.fragments.MyNotificationsFragment;
import kc.fyp.ecare.fragments.RequestFragment;
import kc.fyp.ecare.models.Announcement;
import kc.fyp.ecare.models.User;
import kc.fyp.ecare.services.NotificationService;
import kc.fyp.ecare.ui.NoSwipeableViewPager;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, OneSignal.NotificationReceivedHandler, OneSignal.NotificationOpenedHandler {
    public static final String TAG = "Dashboard";
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private ValueEventListener listener;
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
    private RequestFragment requestFragment;

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
        requestFragment = new RequestFragment();

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
    }

    // This function is called whenever an option is selected from left navigation menu.
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
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
            case R.id.nav_requests: { // Load My Requests Fragment, if user click "My Requesrs"
                toolbar.setTitle("MY REQUESTS");
                pager.setCurrentItem(4);
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
//        Log.e(TAG, "Notification Opened");
//        Log.e(TAG, "Notification Opened, Result: " + result.toString());
//        Log.e(TAG, "Notification Opened, Result JSON: " + result.toJSONObject().toString());
        try {
            JSONObject jsonObject = result.toJSONObject();
            JSONObject notification = jsonObject.getJSONObject("notification");
//            Log.e(TAG, "Notification is: " + notification.toString());
            JSONObject payload = notification.getJSONObject("payload");
//            Log.e(TAG, "Payload is: " + payload.toString());
            JSONObject additionalData = payload.getJSONObject("additionalData");
//            Log.e(TAG, "Additional Data is: " + additionalData.toString());
            if (additionalData != null) {
                String id = additionalData.getString("id");
                String type = additionalData.getString("type");
                if (type != null && id != null) {
                    if (type.equals("ViewAnnouncement")) {
                        loadAnnouncement(id);
                    } else if (type.equals("ViewRequest")) {
                        toolbar.setTitle("MY REQUESTS");
                        pager.setCurrentItem(4);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON Parsing Exception Occur");
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    // Part of Push Notification Service, called when the notification is received
    @Override
    public void notificationReceived(OSNotification notification) {
        Log.e(TAG, "Notification Received");
    }

    private void loadAnnouncement(final String id) {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listener != null) {
                    reference.child(Constants.ANNOUNCEMENT_TABLE).child(id).removeEventListener(listener);
                }
                if (listener != null) {
                    reference.removeEventListener(listener);
                }
                Announcement announcement = snapshot.getValue(Announcement.class);
                if (announcement != null) {
                    Intent it = new Intent(Dashboard.this, AnnouncementDetail.class);
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("announcement", announcement);
                    it.putExtras(bundle);
                    startActivity(it);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) {
                    reference.child(Constants.ANNOUNCEMENT_TABLE).child(id).removeEventListener(listener);
                }
                if (listener != null) {
                    reference.removeEventListener(listener);
                }
            }
        };
        reference.child(Constants.ANNOUNCEMENT_TABLE).child(id).addValueEventListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            reference.removeEventListener(listener);
        }
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
                case 4: {
                    return requestFragment;
                }
            }
            return null;
        }

        // Return total number of fragments
        @Override
        public int getCount() {
            return 5;
        }
    }
}