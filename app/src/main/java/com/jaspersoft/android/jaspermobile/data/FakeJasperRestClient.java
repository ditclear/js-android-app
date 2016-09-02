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

package com.jaspersoft.android.jaspermobile.data;

import com.jaspersoft.android.sdk.service.dashboard.DashboardService;
import com.jaspersoft.android.sdk.service.filter.FiltersService;
import com.jaspersoft.android.sdk.service.report.ReportService;
import com.jaspersoft.android.sdk.service.report.schedule.ReportScheduleService;
import com.jaspersoft.android.sdk.service.repository.RepositoryService;
import com.jaspersoft.android.sdk.service.rx.filter.RxFiltersService;
import com.jaspersoft.android.sdk.service.rx.report.RxReportService;
import com.jaspersoft.android.sdk.service.rx.report.schedule.RxReportScheduleService;
import com.jaspersoft.android.sdk.service.rx.repository.RxRepositoryService;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class FakeJasperRestClient implements JasperRestClient {

    public FakeJasperRestClient() {
    }

    @Override
    public ReportService syncReportService() {
        return null;
    }

    @Override
    public DashboardService syncDashboardService() {
        return null;
    }

    @Override
    public FiltersService syncFilterService() {
        return null;
    }

    @Override
    public RepositoryService syncRepositoryService() {
        return null;
    }

    @Override
    public ReportScheduleService syncScheduleService() {
        return null;
    }

    @Override
    public Observable<RxReportService> reportService() {
        return Observable.empty();
    }

    @Override
    public Observable<RxRepositoryService> repositoryService() {
        return Observable.empty();
    }

    @Override
    public Observable<RxFiltersService> filtersService() {
        return Observable.empty();
    }

    @Override
    public Observable<RxReportScheduleService> scheduleService() {
        return Observable.empty();
    }
}
