package com.jimmytai.whizpad_medical_zenbo.demo.dialog;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.jimmytai.library.utils.fragment.JDialogFragment;
import com.jimmytai.whizpad_medical_zenbo.demo.R;


/**
 * Created by JimmyTai on 2016/9/12.
 */
public class LoadingDialog extends JDialogFragment {

    private static final String TAG = LoadingDialog.class.getSimpleName();
    private static final boolean DEBUG = false;
    public static boolean isShow = false;

    private Dialog dialog;

    public static LoadingDialog newInstance() {

        Bundle args = new Bundle();

        LoadingDialog fragment = new LoadingDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String setTag() {
        return TAG;
    }

    @Override
    public boolean setDebug() {
        return DEBUG;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getActivity(), R.style.DialogLoadingTheme);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading, null);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        isShow = true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        isShow = false;
    }
}
