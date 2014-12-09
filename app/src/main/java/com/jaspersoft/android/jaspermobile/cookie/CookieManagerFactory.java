/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://community.jaspersoft.com/project/mobile-sdk-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile SDK for Android.
 *
 * Jaspersoft Mobile SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile SDK for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.cookie;

import android.content.Context;
import android.os.Build;

import com.google.common.base.Preconditions;
import com.jaspersoft.android.sdk.client.JsRestClient;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class CookieManagerFactory {

    /**
     * Sync cookies between HttpURLConnection and WebView
     *
     * @param context required for initialization of {@link android.webkit.CookieSyncManager} instamce
     * @param jsRestClient JasperSoft sdk Rest client
     */
    public static void syncCookies(Context context, JsRestClient jsRestClient) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(jsRestClient);

        // As soon as, we utilize server profiles data
        // we need to be sure that profile exists
        if (jsRestClient.getServerProfile() == null) {
            return;
        }

        String serverUrl = jsRestClient.getServerProfile().getServerUrl();
        CookieManagerFactory.createManager(context).setCookieForServerUrl(serverUrl);
    }

    /**
     * Creates implementation of manger on the basis of current SDK version.
     */
    private static AbstractCookieManager createManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new LollipopCookieManager(context);
        } else {
            return new LegacyCookieManager(context);
        }
    }

}
