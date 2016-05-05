/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.ui.component.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.ui.component.presenter.Presenter;
import com.jaspersoft.android.jaspermobile.ui.component.presenter.PresenterControllerDelegate2;

import java.util.ArrayList;
import java.util.List;


public abstract class PresenterControllerActivity2<C>
        extends ComponentControllerActivity<C> {

    private List<PresenterControllerDelegate2> mDelegates = new ArrayList<>();

    protected final void registerPresenter(Presenter presenter) {
        PresenterControllerDelegate2 presenterDelegate = new PresenterControllerDelegate2(presenter);
        mDelegates.add(presenterDelegate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (PresenterControllerDelegate2 delegate : mDelegates) {
            delegate.onResume();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        for (PresenterControllerDelegate2 delegate : mDelegates) {
            delegate.onSaveInstanceState();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        for (PresenterControllerDelegate2 delegate : mDelegates) {
            delegate.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        for (PresenterControllerDelegate2 delegate : mDelegates) {
            delegate.onDestroy();
        }
        super.onDestroy();
    }
}
