/*
 * Copyright (C) 2012-2013 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.repository;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockListActivity;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity;
import com.jaspersoft.android.jaspermobile.activities.SettingsActivity;
import com.jaspersoft.android.jaspermobile.activities.report.ReportOptionsActivity;
import com.jaspersoft.android.jaspermobile.activities.resource.ResourceInfoActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.BaseHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.DashboardHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.octo.android.robospice.SpiceManager;
import roboguice.inject.InjectView;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
public abstract class BaseRepositoryActivity extends RoboSherlockListActivity {

    // Extras
    public static final String EXTRA_BC_TITLE_SMALL = "BaseRepositoryActivity.EXTRA_BC_TITLE_SMALL";
    public static final String EXTRA_BC_TITLE_LARGE = "BaseRepositoryActivity.EXTRA_BC_TITLE_LARGE";
    public static final String EXTRA_RESOURCE_URI = "BaseRepositoryActivity.EXTRA_RESOURCE_URI";
    // Context menu IDs
    protected static final int ID_CM_OPEN = 10;
    protected static final int ID_CM_RUN = 11;
    protected static final int ID_CM_VIEW_DETAILS = 12;
    protected static final int ID_CM_ADD_TO_FAVORITES = 13;

    // Action Bar IDs
    private static final int ID_AB_SETTINGS = 30;

    @InjectView(R.id.nothingToDisplayText)      protected TextView nothingToDisplayText;
    @InjectView(android.R.id.list)              protected ListView listView;

    @Inject
    protected JsRestClient jsRestClient;
    protected DatabaseProvider dbProvider;
    protected SpiceManager serviceManager;

    protected ResourceLookup resourceLookup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repository_layout);
        // set empty view
        listView.setEmptyView(nothingToDisplayText);
        // Get the database provider
        dbProvider = new DatabaseProvider(this);
        // Register a context menu to be shown for the given view
        registerForContextMenu(listView);
        // bind to service
        serviceManager = new SpiceManager(JsXmlSpiceService.class);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        ResourceLookup resource = (ResourceLookup) getListView().getItemAtPosition(position);
        switch (resource.getResourceType()) {
            case folder:
                openFolder(resource);
                break;
            case reportUnit:
                runReport(resource.getLabel(), resource.getUri());
                break;
            case dashboard:
                runDashboard(resource.getUri());
                break;
            default:
                viewResource(resource.getUri());
                break;
        }
    }

    @Override
    protected void onStart() {
        serviceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        serviceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // close any open database object
        if (dbProvider != null) dbProvider.close();
        super.onDestroy();
    }

    //---------------------------------------------------------------------
    // Options Menu
    //---------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use the App Icon for Navigation
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Add actions to the action bar
        menu.add(Menu.NONE, ID_AB_SETTINGS, Menu.NONE, R.string.ab_settings)
                .setIcon(R.drawable.ic_action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_AB_SETTINGS:
                // Launch the settings activity
                Intent settingsIntent = new Intent();
                settingsIntent.setClass(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case android.R.id.home:
                HomeActivity.goHome(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //---------------------------------------------------------------------
    // Context menu
    //---------------------------------------------------------------------

    @Override
    public void onCreateContextMenu(ContextMenu menu,View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        // Determine on which item in the ListView the user long-clicked and get corresponding resource descriptor
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ResourceLookup resource = (ResourceLookup) getListView().getItemAtPosition(info.position);

        // Retrieve the label for that particular item and use as title for the menu
        menu.setHeaderTitle(resource.getLabel());
        // Add all the menu options
        switch (resource.getResourceType()) {
            case folder:
                menu.add(Menu.NONE, ID_CM_OPEN, Menu.FIRST, R.string.r_cm_open);
                break;
            case reportUnit:
            case dashboard:
                menu.add(Menu.NONE, ID_CM_RUN, Menu.FIRST, R.string.r_cm_run);
                break;
        }

        menu.add(Menu.NONE, ID_CM_VIEW_DETAILS, Menu.CATEGORY_SECONDARY, R.string.r_cm_view_details);
        menu.add(Menu.NONE, ID_CM_ADD_TO_FAVORITES, Menu.CATEGORY_SECONDARY, R.string.r_cm_add_to_favorites);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Determine on which item in the ListView the user long-clicked and get corresponding resource descriptor
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        resourceLookup = (ResourceLookup) getListView().getItemAtPosition(info.position);

        // Handle item selection
        switch (item.getItemId()) {
            case ID_CM_OPEN:
                openFolder(resourceLookup);
                return true;
            case ID_CM_RUN:
                switch (resourceLookup.getResourceType()) {
                    case reportUnit:
                        runReport(resourceLookup.getLabel(), resourceLookup.getUri());
                        break;
                    case dashboard:
                        runDashboard(resourceLookup.getUri());
                        break;
                }
                return true;
            case ID_CM_VIEW_DETAILS:
                viewResource(resourceLookup.getUri());
                return true;
            case ID_CM_ADD_TO_FAVORITES:
                String label = resourceLookup.getLabel();
                String uri = resourceLookup.getUri();
                String description = resourceLookup.getDescription();
                String wsType = resourceLookup.getResourceType().toString();
                String userName = jsRestClient.getServerProfile().getUsername();
                String organization = jsRestClient.getServerProfile().getOrganization();
                long serverProfileId = jsRestClient.getServerProfile().getId();
                dbProvider.insertFavoriteItem(label, "", uri, description, wsType, serverProfileId, userName, organization);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void openFolder(ResourceLookup resource) {
        Intent intent = new Intent();
        intent.setClass(this, BrowserActivity.class);
        intent.putExtra(EXTRA_BC_TITLE_SMALL, getIntent().getExtras().getString(EXTRA_BC_TITLE_LARGE));
        intent.putExtra(EXTRA_BC_TITLE_LARGE, resource.getLabel());
        intent.putExtra(EXTRA_RESOURCE_URI , resource.getUri());
        startActivity(intent);
    }

    private void viewResource(String resourceUri) {
        Intent intent = new Intent();
        intent.setClass(this, ResourceInfoActivity.class);
        intent.putExtra(EXTRA_BC_TITLE_SMALL, getIntent().getExtras().getString(EXTRA_BC_TITLE_LARGE));
        intent.putExtra(EXTRA_RESOURCE_URI , resourceUri);
        startActivityForResult(intent, ID_CM_VIEW_DETAILS);
    }

    private void runReport(String reportLabel, String reportUri) {
        Intent intent = new Intent();
        intent.setClass(BaseRepositoryActivity.this, ReportOptionsActivity.class);
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_LABEL , reportLabel);
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_URI , reportUri);
        startActivity(intent);
    }

    private void runDashboard(String dashboardUri) {
        // generate url
        String dashboardUrl = jsRestClient.getServerProfile().getServerUrl()
                + "/flow.html?_flowId=dashboardRuntimeFlow&viewAsDashboardFrame=true&dashboardResource="
                + dashboardUri;
        // run the html dashboard viewer
        Intent htmlViewer = new Intent();
        htmlViewer.setClass(this, DashboardHtmlViewerActivity.class);
        htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_URL, dashboardUrl);
        startActivity(htmlViewer);
    }


}
