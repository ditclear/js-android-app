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

package com.jaspersoft.android.jaspermobile.ui.mapper.job;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@RunWith(JUnitParamsRunner.class)
public class MonthLocalizerTest {
    @Test
    @Parameters({
            "0, January",
            "1, February",
            "2, March",
            "3, April",
            "4, May",
            "5, June",
            "6, July",
            "7, August",
            "8, September",
            "9, October",
            "10, November",
            "11, December"
    })
    public void testLocalize(int day, String dayName) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        MonthLocalizer localizer = new MonthLocalizer();
        String localize = localizer.localize(day);
        assertThat(localize, is(dayName));
    }
}