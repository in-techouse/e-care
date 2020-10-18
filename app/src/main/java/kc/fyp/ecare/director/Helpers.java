package kc.fyp.ecare.director;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

import java.util.Objects;

import kc.fyp.ecare.R;

public class Helpers {

    // Check Internet Connection
    public static boolean isConnected(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED || Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }

    // Show Error and Close Activity
    public static void showErrorWithActivityClose(final Activity a, String title, String message) {
        new FancyAlertDialog.Builder(a)
                .setTitle(title)
                .setMessage(message)
                .setBackgroundColor(a.getResources().getColor(R.color.colorPrimaryDark))
                .setNegativeBtnText("CLOSE")
                .setNegativeBtnBackground(a.getResources().getColor(R.color.colorDanger))
                .setPositiveBtnText("OKAY")
                .setPositiveBtnBackground(a.getResources().getColor(R.color.colorPrimary))
                .setAnimation(Animation.POP)
                .isCancellable(false)
                .setIcon(R.drawable.ic_action_close, Icon.Visible)
                .OnPositiveClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        a.finish();
                    }
                })
                .OnNegativeClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        a.finish();
                    }
                })
                .build();
    }

    // Show Error
    public static void showError(Activity a, String title, String message) {
        new FancyAlertDialog.Builder(a)
                .setTitle(title)
                .setMessage(message)
                .setBackgroundColor(a.getResources().getColor(R.color.colorPrimaryDark))
                .setNegativeBtnText("CLOSE")
                .setNegativeBtnBackground(a.getResources().getColor(R.color.colorDanger))
                .setPositiveBtnText("OKAY")
                .setPositiveBtnBackground(a.getResources().getColor(R.color.colorPrimary))
                .setAnimation(Animation.POP)
                .isCancellable(false)
                .setIcon(R.drawable.ic_action_close, Icon.Visible)
                .OnPositiveClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                    }
                })
                .OnNegativeClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {

                    }
                })
                .build();
    }

    // Show Error and Close Activity
    public static void showSuccessWithActivityClose(final Activity a, String title, String message) {
        new FancyAlertDialog.Builder(a)
                .setTitle(title)
                .setMessage(message)
                .setBackgroundColor(a.getResources().getColor(R.color.colorPrimaryDark))
                .setNegativeBtnText("CLOSE")
                .setNegativeBtnBackground(a.getResources().getColor(R.color.colorDanger))
                .setPositiveBtnText("OKAY")
                .setPositiveBtnBackground(a.getResources().getColor(R.color.colorPrimary))
                .setAnimation(Animation.POP)
                .isCancellable(false)
                .setIcon(R.drawable.ic_action_okay, Icon.Visible)
                .OnPositiveClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        a.finish();
                    }
                })
                .OnNegativeClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        a.finish();
                    }
                })
                .build();
    }
}
