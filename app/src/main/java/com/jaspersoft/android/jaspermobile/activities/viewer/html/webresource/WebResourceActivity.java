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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.webresource;

import android.net.Uri;
import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.WebViewFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.WebViewFragment_;
import com.jaspersoft.android.jaspermobile.ui.view.activity.ToolbarActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Activity which performs viewing of local web resources.
 *
 * @author Olexandr Dahno
 * @since 2.6
 */

@EActivity
public class WebResourceActivity extends ToolbarActivity
        implements WebViewFragment.OnWebViewCreated {

    @Extra
    protected String resourceUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            String title = getString(R.string.wr_title);
            WebViewFragment webViewFragment = WebViewFragment_.builder()
                    .resourceLabel(title).build();
            webViewFragment.setOnWebViewCreated(this);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, webViewFragment, WebViewFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void onWebViewCreated(WebViewFragment webViewFragment) {
        if (resourceUrl == null) {
            // TODO: throw exception?
        }
        String url = resourceUrl;
        if (isViewerUrl(url)) {
            // TODO: make showing this resource in native viewer (first get resource lookup)
            url = constructUrlForViewer(url);
        }
        webViewFragment.loadUrl(url);
    }

    private boolean isViewerUrl(String url) {
        return url.contains("viewer.html");
    }

    private String constructUrlForViewer(String url) {
        String nodecoration ="sessionDecorator=no&decorate=no";
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();
        String query = uri.getQuery();
        String fragment = uri.getFragment();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(scheme)
                .encodedAuthority(host + ":" + port)
                .path(path)
                .encodedQuery(nodecoration + query)
                .encodedFragment(fragment);
        String newUrl = builder.build().toString();
        return newUrl;
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_l_wr_s);
    }

}