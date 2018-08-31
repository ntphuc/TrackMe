package com.thv.android.trackme.utils;

import android.app.AlertDialog;
import android.arch.persistence.room.util.StringUtil;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.thv.android.trackme.BasicApp;
import com.thv.android.trackme.R;
import com.thv.android.trackme.common.Constanst;
import com.thv.android.trackme.ui.MainActivity;

public class CommonUtils {
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public static String getString(int id) {
        return BasicApp.getInstance().getAppContext().getResources()
                .getString(id);
    }
    /**
     * confirm khi nguoi dung muon thoat khoi kunkun
     *
     * @param view
     * @author: BangHN
     * @return: void
     * @throws:
     */
    public static void showDialogConfirmStopTracking(final Fragment view) {
        String notice = getString(R.string.CONFIRM_STOP_RECORDING);
        String ok = getString(R.string.YES);
        String cancel = getString(R.string.NO);
        if (view != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(view.getActivity()).create();
            alertDialog.setMessage(notice);
            if (ok != null && !Constanst.STR_BLANK.equals(ok)) {
                alertDialog.setButton(ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // view.sentBroadcast(
                                // ActionEventConstant.EXIT_KUNKUN,
                                // new Bundle());
                                // view.loadStartAppInterestialAd();
                                view.getActivity().getSupportFragmentManager().popBackStack();
                                dialog.dismiss();
                            }
                        });
            }
            if (cancel != null && !Constanst.STR_BLANK.equals(cancel)) {
                alertDialog.setButton2(cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });
            }
            alertDialog.show();
        }
    }
}
