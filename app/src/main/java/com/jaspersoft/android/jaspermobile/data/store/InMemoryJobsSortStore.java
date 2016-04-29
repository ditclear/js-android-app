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

package com.jaspersoft.android.jaspermobile.data.store;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.JobsSortMapper;
import com.jaspersoft.android.jaspermobile.domain.entity.Sort;
import com.jaspersoft.android.jaspermobile.domain.store.SortStore;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.PerScreen;
import com.jaspersoft.android.sdk.service.report.schedule.JobSortType;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@PerScreen
public class InMemoryJobsSortStore implements SortStore {
    private final static JobSortType DEFAULT_SORT_TYPE = JobSortType.SORTBY_JOBNAME;
    private final PublishSubject<Void> mPublisher = PublishSubject.create();
    private final JobsSortMapper mJobsSortMapper;

    @Inject
    public InMemoryJobsSortStore(JobsSortMapper jobsSortMapper) {
        mJobsSortMapper = jobsSortMapper;
    }

    @Override
    public Sort getSortType() {
        return mJobsSortMapper.from(DEFAULT_SORT_TYPE);
    }

    @Override
    public Observable<Void> observe() {
        return mPublisher;
    }

    @Override
    public Collection<Sort> getAvailableSortTypes() {
        return Collections.singleton(mJobsSortMapper.from(DEFAULT_SORT_TYPE));
    }

    @Override
    public void saveSortType(Sort sortType) {
        throw new UnsupportedOperationException("Save not supported for job sort type");
    }
}
