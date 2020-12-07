package kc.thefyp.ecare.activities.ui.main;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

import kc.thefyp.ecare.R;
import kc.thefyp.ecare.fragments.DonationDetailFragment;
import kc.thefyp.ecare.fragments.RequestDetailFragment;
import kc.thefyp.ecare.models.Request;


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private final RequestDetailFragment requestDetailFragment;
    private final DonationDetailFragment donationDetailFragment;

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        requestDetailFragment = new RequestDetailFragment();
        donationDetailFragment = new DonationDetailFragment();
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (position == 0) {
            return requestDetailFragment;
        } else {
            return donationDetailFragment;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    public void setRequest(Request request) {
        Log.e("RequestDetail", "Request received");
        Log.e("RequestDetail", "Request id is: " + request.getId());
        requestDetailFragment.setRequest(request);
        donationDetailFragment.setRequest(request);
    }
}