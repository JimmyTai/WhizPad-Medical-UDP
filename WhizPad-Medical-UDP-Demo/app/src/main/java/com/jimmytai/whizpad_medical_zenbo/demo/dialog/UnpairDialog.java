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
import com.jimmytai.library.utils.lifecycle.JLifecycle;
import com.jimmytai.library.utils.log.JLog;
import com.jimmytai.library.whizpad_medical_udp.WhizPadClient;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadInfo;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadUnPairingResult;
import com.jimmytai.whizpad_medical_zenbo.demo.R;
import com.jimmytai.whizpad_medical_zenbo.demo.activity.PadStatusActivity;
import com.jimmytai.whizpad_medical_zenbo.demo.utils.WifiUtils;

/**
 * Created by JimmyTai on 2016/8/19.
 */
public class UnpairDialog extends JDialogFragment {

    private static final String TAG = UnpairDialog.class.getSimpleName();
    private static final boolean DEBUG = true;
    public static boolean isShow = false;

    private static final String EXTRA_ITEM = "EXTRA_ITEM";

    private Dialog dialog;
    private PadStatusActivity activity;

    private WhizPadInfo info;

    private TextView tv_title, tv_paired, tv_confirm, tv_cancel;
    private EditText et_password;

    public static UnpairDialog newInstance(WhizPadInfo info) {

        Bundle args = new Bundle();
        args.putParcelable(EXTRA_ITEM, info);

        UnpairDialog fragment = new UnpairDialog();
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
        activity = (PadStatusActivity) getActivity();
        dialog = new Dialog(getActivity(), R.style.DialogTheme);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_unpair, null);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        setWindows(dialog);

        info = getArguments().getParcelable(EXTRA_ITEM);

        findViews(view);

        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        isShow = true;
        JLog.d(DEBUG, TAG, "show UnpairDialog");
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        isShow = false;
        JLog.d(DEBUG, TAG, "dismiss UnpairDialog");
    }

    /* --- Listener --- */

    private class MyActionListener extends WhizPadClient.ActionListener {

        private WhizPadInfo info;

        public MyActionListener(WhizPadInfo info) {
            this.info = info;
        }

        @Override
        public void onUnpairingResult(WhizPadUnPairingResult result) {
            super.onUnpairingResult(result);
            if (activity.jLifeCycle != JLifecycle.ON_DESTROY) {
                activity.unPairPadResult(result);
            }
        }
    }

    private class MyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (activity != null)
                switch (view.getId()) {
                    case R.id.unpair_tv_cancel:
                        dialog.dismiss();
                        break;
                    case R.id.unpair_tv_confirm:
                        String password = et_password.getText().toString();
                        if (password.length() != 4)
                            return;
                        activity.loadingDialog.show(activity.getFragmentManager(), "LoadingDialog");
                        tv_confirm.setEnabled(false);
                        tv_cancel.setEnabled(false);
                        et_password.setEnabled(false);
                        activity.client.unpair(info, password, WifiUtils.getMacAddr(), new MyActionListener(info));
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
        tv_title = (TextView) view.findViewById(R.id.unpair_tv_title);
        tv_title.setTypeface(activity.FONT_LIGHT);
        et_password = (EditText) view.findViewById(R.id.unpair_et_password);
        et_password.setTypeface(activity.FONT_LIGHT);
        tv_cancel = (TextView) view.findViewById(R.id.unpair_tv_cancel);
        tv_cancel.setTypeface(activity.FONT_LIGHT);
        tv_cancel.setOnClickListener(new MyClickListener());
        tv_confirm = (TextView) view.findViewById(R.id.unpair_tv_confirm);
        tv_confirm.setTypeface(activity.FONT_LIGHT);
        tv_confirm.setOnClickListener(new MyClickListener());
    }
}
