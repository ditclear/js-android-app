package com.jaspersoft.android.jaspermobile.presentation.component;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ErrorEvent {
    @NonNull
    private final String mErrorMessage;

    public ErrorEvent(@NonNull String errorMessage) {
        mErrorMessage = errorMessage;
    }

    @NonNull
    public String getErrorMessage() {
        return mErrorMessage;
    }
}
