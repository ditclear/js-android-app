/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.ComponentProviderDelegate;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResourceType;
import com.jaspersoft.android.jaspermobile.util.resource.ReportResource;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import javax.inject.Inject;


/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ReportResourceBinder extends ResourceBinder {
    private ImageView thumbnail;

    @Inject
    Analytics mAnalytics;

    public ReportResourceBinder(Context context) {
        super(context);

        ComponentProviderDelegate.INSTANCE
                .getBaseActivityComponent((FragmentActivity) context)
                .inject(this);
    }

    @Override
    public void setIcon(ImageView imageView, JasperResource jasperResource) {
        imageView.setBackgroundResource(R.drawable.bg_resource_icon_grey);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        loadFromNetwork(imageView, jasperResource, getDisplayImageOptions(R.drawable.ic_report));
    }

    @Override
    public void setThumbnail(ImageView imageView, JasperResource jasperResource) {
        imageView.setBackgroundResource(R.drawable.bg_gradient_grey);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        loadFromNetwork(imageView, jasperResource, getDisplayImageOptions(R.drawable.im_thumbnail_report));
    }

    private void loadFromNetwork(ImageView imageView, JasperResource jasperResource, DisplayImageOptions displayImageOptions) {
        if (jasperResource.getResourceType() == JasperResourceType.report) {
            String thumbnailUri = ((ReportResource) jasperResource).getThumbnailUri();
            thumbnail = imageView;
            ImageLoader.getInstance().displayImage(thumbnailUri, thumbnail, displayImageOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (loadedImage == null) return;
                    mAnalytics.setThumbnailsExist();
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }
    }

    private DisplayImageOptions getDisplayImageOptions(@DrawableRes int placeholderResource) {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(placeholderResource)
                .showImageForEmptyUri(placeholderResource)
                .considerExifParams(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .preProcessor(new CustomBitmapProcessor(getContext(), placeholderResource))
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }
}
