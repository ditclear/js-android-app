/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.dialog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountProvider;

import roboguice.fragment.RoboDialogFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class PasswordDialogFragment extends RoboDialogFragment {

    private static final String TAG = PasswordDialogFragment.class.getSimpleName();
    private static final String PASSWORD_EXTRA = "PASSWORD";

    private EditText mPasswordEdit;
    private String mPasswordValue;
    private OnPasswordChanged onPasswordChangedListener;

    //---------------------------------------------------------------------
    // Static methods
    //---------------------------------------------------------------------

    public static void show(FragmentManager fm, OnPasswordChanged onPasswordChanged) {
        PasswordDialogFragment dialogFragment = (PasswordDialogFragment)
                fm.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = new PasswordDialogFragment();
            dialogFragment.setOnPasswordChangedListener(onPasswordChanged);
            dialogFragment.show(fm, TAG);
        }
    }

    //---------------------------------------------------------------------
    // Public methods
    //---------------------------------------------------------------------

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View view = inflater.inflate(R.layout.ask_pwd_dialog_layout, null);
        mPasswordEdit = (EditText) view.findViewById(R.id.dialogPasswordEdit);
        builder.setView(view);

        builder.setTitle(R.string.h_ad_title_enter_password);
        builder.setCancelable(true)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (onPasswordChangedListener != null) {
                            onPasswordChangedListener.onCancel();
                        }
                        dialog.cancel();
                    }
                });


        final AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (onPasswordChangedListener != null) {
                     onPasswordChangedListener.onCancel();
                }
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String password = mPasswordEdit.getText().toString();
                                if (TextUtils.isEmpty(password)) {
                                    mPasswordEdit.setError(getString(R.string.sp_error_field_required));
                                } else {
                                    Account account = JasperAccountProvider.get(getActivity()).getAccount();
                                    AccountManager accountManager = AccountManager.get(getActivity());
                                    accountManager.setPassword(account, password);
                                    dismiss();

                                    if (onPasswordChangedListener != null) {
                                        onPasswordChangedListener.onPasswordChanged();
                                    }
                                }
                            }
                        });
            }
        });
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPasswordValue = savedInstanceState.getString(PASSWORD_EXTRA, "");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPasswordEdit.setText(mPasswordValue);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PASSWORD_EXTRA, mPasswordEdit.getText().toString());
        super.onSaveInstanceState(outState);
    }

    public void setOnPasswordChangedListener(OnPasswordChanged onPasswordChangedListener) {
        this.onPasswordChangedListener = onPasswordChangedListener;
    }

    public static interface OnPasswordChanged {
        void onPasswordChanged();
        void onCancel();
    }
}
