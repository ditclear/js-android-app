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

package com.jaspersoft.android.jaspermobile.domain;

import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class SaveOptionRequest {
    private final String mReportUri;
    private final String mLabel;
    private final List<ReportParameter> mParams;

    public SaveOptionRequest(String reportUri, String label, List<ReportParameter> params) {
        mReportUri = reportUri;
        mLabel = label;
        mParams = params;
    }

    public String getLabel() {
        return mLabel;
    }

    public List<ReportParameter> getParams() {
        return mParams;
    }

    public String getReportUri() {
        return mReportUri;
    }
}
