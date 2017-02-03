package com.jimmytai.whizpad_medical_zenbo.demo.dialog;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.jimmytai.library.utils.fragment.JDialogFragment;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadInfo;
import com.jimmytai.whizpad_medical_zenbo.demo.R;
import com.jimmytai.whizpad_medical_zenbo.demo.activity.MainActivity;
import com.jimmytai.whizpad_medical_zenbo.demo.thread.PasswordThread;

/**
 * Created by JimmyTai on 2016/8/19.
 */
public class PasswordDialog extends JDialogFragment {

    private static final String TAG = "PasswordDialog";
    private static final boolean DEBUG = true;
    public static boolean isShow = false;

    private static final String EXTRA_ITEM = "EXTRA_ITEM";
    private static final String EXTRA_PAGE = "EXTRA_PAGE";

    public enum Page {
        ALARM_PAIRED, SET_PASSWORD, IP_CHANGED
    }

    private Page page;

    private Dialog dialog;
    private MainActivity activity;

    private WhizPadInfo item;

    private TextView tv_title, tv_ipChanged, tv_paired, tv_confirm, tv_cancel;
    private EditText et_password;

    public static PasswordDialog newInstance(Page page, WhizPadInfo item) {

        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PAGE, page);
        args.putParcelable(EXTRA_ITEM, item);

        PasswordDialog fragment = new PasswordDialog();
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        dialog = new Dialog(getActivity(), R.style.DialogTheme);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_password, null);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        setWindows(dialog);

        page = (Page) getArguments().getSerializable(EXTRA_PAGE);
        item = getArguments().getParcelable(EXTRA_ITEM);

        findViews(view);
        setPage(page);

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

    /* --- Listener --- */

    private class MyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (activity != null)
                switch (view.getId()) {
                    case R.id.password_tv_cancel:
                        dialog.dismiss();
                        break;
                    case R.id.password_tv_confirm:
                        if (page == Page.ALARM_PAIRED || page == Page.IP_CHANGED) {
                            setPage(Page.SET_PASSWORD);
                        } else if (page == Page.SET_PASSWORD) {
                            String password = et_password.getText().toString();
                            activity.loadingDialog.show(activity.getFragmentManager(), "LoadingDialog");
                            tv_confirm.setEnabled(false);
                            tv_cancel.setEnabled(false);
                            et_password.setEnabled(false);
                            new PasswordThread(activity, activity.whizPadClient, item, password).start();
                        }
                        break;
                }
        }
    }

    /* --- Functions --- */

    public void setEnable() {
        tv_confirm.setEnabled(true);
        tv_cancel.setEnabled(true);
        et_password.setEnabled(true);
    }

    /* --- Views ---- */

    public void setPage(Page page) {
        this.page = page;
        if (page == Page.ALARM_PAIRED) {
            tv_ipChanged.setVisibility(View.GONE);
            tv_paired.setVisibility(View.VISIBLE);
            et_password.setVisibility(View.GONE);
            tv_confirm.setText("下一步");
        } else if (page == Page.SET_PASSWORD) {
            tv_ipChanged.setVisibility(View.GONE);
            tv_paired.setVisibility(View.GONE);
            et_password.setVisibility(View.VISIBLE);
            tv_confirm.setText("確認");
        } else if (page == Page.IP_CHANGED) {
            tv_ipChanged.setVisibility(View.VISIBLE);
            tv_paired.setVisibility(View.GONE);
            et_password.setVisibility(View.GONE);
            tv_confirm.setText("下一步");
        }
    }

    private void setWindows(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null)
            return;
        window.setGravity(Gravity.BOTTOM);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }

    private void findViews(View view) {
        tv_title = (TextView) view.findViewById(R.id.password_tv_title);
        tv_title.setTypeface(activity.FONT_LIGHT);
        tv_ipChanged = (TextView) view.findViewById(R.id.password_tv_ipChanged);
        tv_ipChanged.setTypeface(activity.FONT_LIGHT);
        tv_paired = (TextView) view.findViewById(R.id.password_tv_paired);
        tv_paired.setTypeface(activity.FONT_LIGHT);
        et_password = (EditText) view.findViewById(R.id.password_et_password);
        et_password.setTypeface(activity.FONT_LIGHT);
        tv_cancel = (TextView) view.findViewById(R.id.password_tv_cancel);
        tv_cancel.setTypeface(activity.FONT_LIGHT);
        tv_cancel.setOnClickListener(new MyClickListener());
        tv_confirm = (TextView) view.findViewById(R.id.password_tv_confirm);
        tv_confirm.setTypeface(activity.FONT_LIGHT);
        tv_confirm.setOnClickListener(new MyClickListener());
    }
}
