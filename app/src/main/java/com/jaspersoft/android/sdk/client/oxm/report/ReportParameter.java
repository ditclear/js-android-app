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

package com.jaspersoft.android.sdk.client.oxm.report;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ivan Gadzhega
 * @since 1.4
 */
public class ReportParameter implements Parcelable {

    @Expose
    private String name;

    @Expose
    @SerializedName("value")
    private Set<String> values = new HashSet<String>();

    public ReportParameter() {}

    public ReportParameter(String name, Set<String> values) {
        this.name = name;
        this.values = values;
    }

    public ReportParameter(String name, String value) {
        this.name = name;
        this.values = new HashSet<String>();
        this.values.add(value);
    }

    //---------------------------------------------------------------------
    // Parcelable
    //---------------------------------------------------------------------

    public ReportParameter(Parcel source) {
        this.name = source.readString();
        this.values = new HashSet<String>(source.createStringArrayList());
    }

    public static final Creator CREATOR = new Creator() {
        public ReportParameter createFromParcel(Parcel source) {
            return new ReportParameter(source);
        }

        public ReportParameter[] newArray(int size) {
            return new ReportParameter[size];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeStringList(new ArrayList<String>(values));
    }

    //---------------------------------------------------------------------
    // Getters & Setters
    //---------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.values = new HashSet<String>();
        this.values.add(value);
    }

    public Set<String> getValues() {
        return values;
    }

    public void setValues(Set<String> values) {
        this.values = values;
    }

}
