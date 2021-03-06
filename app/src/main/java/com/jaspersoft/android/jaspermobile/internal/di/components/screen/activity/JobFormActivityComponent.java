/*
 * Copyright � 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.internal.di.components.screen.activity;

import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.job.JobFormActivityModule;
import com.jaspersoft.android.jaspermobile.ui.component.presenter.HasPresenter;
import com.jaspersoft.android.jaspermobile.ui.presenter.ScheduleFormPresenter;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.EditScheduleFormFragment;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.NewScheduleFormFragment;
import com.jaspersoft.android.jaspermobile.ui.view.widget.ScheduleFormView;

import dagger.Subcomponent;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@PerActivity
@Subcomponent(
        modules = {
                JobFormActivityModule.class
        }
)
public interface JobFormActivityComponent extends HasPresenter<ScheduleFormPresenter> {
    ScheduleFormView inject(ScheduleFormView scheduleFragment);

    void inject(NewScheduleFormFragment formFragment);
    void inject(EditScheduleFormFragment formFragment);
}
