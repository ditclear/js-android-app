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

package com.jaspersoft.android.jaspermobile.activities.share;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.AnnotationOptionsDialog;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.domain.ScreenCapture;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.resource.SaveScreenCaptureCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.ui.view.activity.ToolbarActivity;
import com.jaspersoft.android.jaspermobile.widget.AnnotationControlView;
import com.jaspersoft.android.jaspermobile.widget.AnnotationView;
import com.jaspersoft.android.jaspermobile.widget.DraggableViewsContainer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import javax.inject.Inject;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
@EActivity(R.layout.activity_annotation)
@OptionsMenu(R.menu.annotation)
public class AnnotationActivity extends ToolbarActivity implements AnnotationControlView.EventListener {
    private static final String CACHE_AUTHORITY = "com.jaspersoft.android.jaspermobile.fileprovider";

    @ViewById(R.id.container)
    RelativeLayout container;
    @ViewById(R.id.reportImage)
    ImageView reportImage;
    @ViewById(R.id.annotationDrawingContainer)
    AnnotationView annotationDrawing;
    @ViewById(R.id.annotationControl)
    AnnotationControlView annotationControlView;
    @ViewById(R.id.annotationNotesContainer)
    DraggableViewsContainer annotationNotes;

    @Extra
    Uri imageUri;

    @Inject
    SaveScreenCaptureCase mSaveScreenCaptureCase;
    @Inject
    RequestExceptionHandler mRequestExceptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       lockScreenOrientation();
    }

    @AfterViews
    void init() {
        getBaseActivityComponent().inject(this);

        reportImage.setImageURI(imageUri);
        getSupportActionBar().setTitle(getString(R.string.annotation_title));
        annotationControlView.setEventListener(this);
        annotationControlView.setColor(annotationDrawing.getColor());
        annotationNotes.setEnabled(false);
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_annotation);
    }

    @OptionsItem(R.id.annotationDoneAction)
    void annotationDoneAction() {
        ScreenCapture reportScreenCapture = ScreenCapture.Factory.capture(container);
        mSaveScreenCaptureCase.execute(reportScreenCapture, new SimpleSubscriber<File>() {
            @Override
            public void onStart() {
                ProgressDialogFragment.builder(getSupportFragmentManager())
                        .setLoadingMessage(R.string.loading_msg)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        })
                        .show();
            }

            @Override
            public void onError(Throwable e) {
                mRequestExceptionHandler.showCommonErrorMessage(e);
            }

            @Override
            public void onNext(File item) {
                Uri sharedFileUri = FileProvider.getUriForFile(AnnotationActivity.this, CACHE_AUTHORITY, item);

                ShareCompat.IntentBuilder.from(AnnotationActivity.this)
                        .setType("image/*")
                        .setStream(sharedFileUri)
                        .setSubject(getString(R.string.share_message))
                        .setChooserTitle(R.string.share_chooser_title)
                        .startChooser();
            }

            @Override
            public void onCompleted() {
                ProgressDialogFragment.dismiss(getSupportFragmentManager());
                analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.SHARED.getValue(), null);
            }
        });
    }

    @Override
    public void onModeChanged(int mode) {
        annotationDrawing.setEnabled(mode == AnnotationControlView.DRAW_MODE);
        annotationNotes.setEnabled(mode == AnnotationControlView.TEXT_MODE);

        annotationControlView.setColor(annotationControlView.getMode() == AnnotationControlView.DRAW_MODE
                ? annotationDrawing.getColor() : annotationNotes.getColor());
    }

    @Override
    public void onClear() {
        annotationDrawing.reset();
        annotationNotes.removeAllViews();
        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.ANNOTATED.getValue(),  Analytics.EventLabel.CLEARED.getValue());
    }

    @Override
    public void onSizeChangeRequested() {
        AnnotationOptionsDialog annotationOptionsDialog = new AnnotationOptionsDialog(this);
        annotationOptionsDialog.setSize(annotationControlView.getMode() == AnnotationControlView.DRAW_MODE
                ? annotationDrawing.getSize() : annotationNotes.getSize());
        annotationOptionsDialog.setTitle(annotationControlView.getMode() == AnnotationControlView.DRAW_MODE
                ? getString(R.string.annotation_pick_line_size) : getString(R.string.annotation_pick_font_size));
        annotationOptionsDialog.setBorder(annotationControlView.getMode() == AnnotationControlView.DRAW_MODE
                ? null : annotationNotes.needsBorder());
        annotationOptionsDialog.setOnEventListener(new AnnotationOptionsDialog.OnAnnotationSizeListener() {
            @Override
            public void onAnnotationOptionsSelected(int size) {
                if (annotationControlView.getMode() == AnnotationControlView.DRAW_MODE) {
                    annotationDrawing.setSize(size);
                } else {
                    annotationNotes.setSize(size);
                }
            }

            @Override
            public void onAnnotationOptionsSelected(int size, boolean needsBorder) {
                onAnnotationOptionsSelected(size);
                annotationNotes.setNeedsBorder(needsBorder);
            }
        });
        annotationOptionsDialog.show();
    }

    @Override
    public void onColorSelected(int color) {
        if (annotationControlView.getMode() == AnnotationControlView.DRAW_MODE) {
            annotationDrawing.setColor(color);
        } else {
            annotationNotes.setColor(color);
        }
    }

    private void lockScreenOrientation() {
        if (getResources().getConfiguration().orientation == Configuration. ORIENTATION_LANDSCAPE)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
