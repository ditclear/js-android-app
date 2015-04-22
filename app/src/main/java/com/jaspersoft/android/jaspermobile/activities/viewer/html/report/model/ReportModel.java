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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.ReportParamsSerializer;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.ReportParamsSerializerImpl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ReportModel implements Parcelable {
    private final ReportParamsSerializer serializer = new ReportParamsSerializerImpl();
    private List<InputControl> inputControls = new ArrayList<InputControl>();
    private List<ReportParameter> reportParameters = new ArrayList<ReportParameter>();

    public List<InputControl> getInputControls() {
        return inputControls;
    }

    public ArrayList<ReportParameter> getReportParameters() {
        return new ArrayList<ReportParameter>(reportParameters);
    }

    public void setReportParameters(ArrayList<ReportParameter> reportParameters) {
        this.reportParameters = reportParameters;
    }

    public String getJsonReportParameters() {
        return serializer.toJson(reportParameters);
    }

    public void updateReportParameters() {
        ArrayList<ReportParameter> parameters = new ArrayList<ReportParameter>();
        if (inputControls != null) {
            for (InputControl inputControl : inputControls) {
                parameters.add(new ReportParameter(inputControl.getId(), inputControl.getSelectedValues()));
            }
        }
        this.reportParameters = parameters;
    }

    public void setInputControls(ArrayList<InputControl> inputControls) {
        this.inputControls = inputControls;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(inputControls);
        dest.writeTypedList(reportParameters);
    }

    public ReportModel() {
    }

    private ReportModel(Parcel in) {
        in.readTypedList(inputControls, InputControl.CREATOR);
        in.readTypedList(reportParameters, ReportParameter.CREATOR);
    }

    public static final Parcelable.Creator<ReportModel> CREATOR = new Parcelable.Creator<ReportModel>() {
        public ReportModel createFromParcel(Parcel source) {
            return new ReportModel(source);
        }

        public ReportModel[] newArray(int size) {
            return new ReportModel[size];
        }
    };
}