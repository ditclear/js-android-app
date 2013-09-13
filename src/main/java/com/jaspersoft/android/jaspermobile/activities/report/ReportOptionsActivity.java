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

package com.jaspersoft.android.jaspermobile.activities.report;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.BaseHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.db.tables.ReportOptions;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsValuesRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.ValidateInputControlsValuesRequest;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.*;
import com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.ui.widget.MultiSelectSpinner;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import roboguice.util.Ln;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.jaspersoft.android.jaspermobile.activities.report.DatePickerDialogHelper.*;

/**
 * @author Ivan Gadzhega
 * @since 1.5.2
 */
public class ReportOptionsActivity extends BaseReportOptionsActivity {

    private List<InputControl> inputControls;
    private boolean skipRecursiveUpdate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a cursor with saved options for current report
        JsServerProfile profile = jsRestClient.getServerProfile();
        Cursor cursor = dbProvider.fetchReportOptions(profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
        startManagingCursor(cursor);

        Map<String, ReportParameter> savedOptions = new HashMap<String, ReportParameter>();
        if (cursor.getCount() != 0) {
            // Iterate DB Records
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex(ReportOptions.KEY_NAME));
                String value = cursor.getString(cursor.getColumnIndex(ReportOptions.KEY_VALUE));
                if (savedOptions.containsKey(name)) {
                    savedOptions.get(name).getValues().add(value);
                } else {
                    savedOptions.put(name, new ReportParameter(name, value));
                }
                cursor.moveToNext();
            }
        }

        setRefreshActionButtonState(true);
        GetInputControlsRequest request = new GetInputControlsRequest(jsRestClient, reportUri,
                new ArrayList<String>(), new ArrayList<ReportParameter>(savedOptions.values()));
        serviceManager.execute(request, new GetInputControlsListener());
    }

    public void runReportButtonClickHandler(View view) {
        setRefreshActionButtonState(true);
        ValidateInputControlsValuesRequest request = new ValidateInputControlsValuesRequest(jsRestClient, reportUri, inputControls);
        serviceManager.execute(request, new ValidateInputControlsValuesListener());
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void updateDependentControls(InputControl inputControl) {
        if(!skipRecursiveUpdate && !inputControl.getSlaveDependencies().isEmpty()) {
            setRefreshActionButtonState(true);
            GetInputControlsValuesRequest request = new GetInputControlsValuesRequest(jsRestClient, reportUri, inputControls);
            serviceManager.execute(request, new GetInputControlsValuesListener());
        }
    }

    private void runReport() {
        List<ReportParameter> parameters = initParametersUsingSelectedValues();
        saveParametersToDatabase(parameters);
        runReportViewer(parameters);
    }

    private List<ReportParameter> initParametersUsingSelectedValues() {
        List<ReportParameter> parameters = new ArrayList<ReportParameter>();
        for (InputControl inputControl : inputControls) {
            parameters.add(new ReportParameter(inputControl.getId(), inputControl.getSelectedValues()));
        }
        return parameters;
    }

    private void saveParametersToDatabase(List<ReportParameter> parameters) {
        if (parameters.isEmpty()) return;
        //delete previous values from db
        JsServerProfile profile = jsRestClient.getServerProfile();
        dbProvider.deleteReportOptions(profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
        // Save new values to db
        for (ReportParameter parameter : parameters) {
            for (String value : parameter.getValues()) {
                dbProvider.insertReportOption(parameter.getName(), value, false,
                        profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
            }
        }
    }

    private void runReportViewer(List<ReportParameter> parameters) {
        String outputFormat = formatSpinner.getSelectedItem().toString();
        String reportUrl = jsRestClient.generateReportUrl(reportUri, parameters, outputFormat);
        if (RUN_OUTPUT_FORMAT_HTML.equalsIgnoreCase(outputFormat)) {
            runHtmlReportViewer(reportUrl);
        } else {
            runExternalReportViewer(reportUrl, outputFormat);
        }
    }

    private void runHtmlReportViewer(String reportUrl) {
        Intent htmlViewer = new Intent();
        htmlViewer.setClass(this, ReportHtmlViewerActivity.class);
        htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_URL, reportUrl);
        startActivity(htmlViewer);
    }

    private void runExternalReportViewer(String reportUrl, String outputFormat) {
        // generate report output according to selected format
        String contentType, extension;
        if (RUN_OUTPUT_FORMAT_PDF.equalsIgnoreCase(outputFormat)) {
            contentType = "application/pdf";
            extension = ".pdf";
        } else {
            contentType = "application/xls";
            extension = ".xls";
        }
        File outputDir = getReportOutputCacheDir();
        File outputFile = new File(outputDir, reportUri + extension);
        // get the report output file and save it to cache folder
        jsRestClient.saveReportOutputToFile(reportUrl, outputFile);
        if (outputFile.exists()) {
            // run external viewer according to selected output format
            Uri path = Uri.fromFile(outputFile);
            Intent externalViewer = new Intent(Intent.ACTION_VIEW);
            externalViewer.setDataAndType(path, contentType);
            externalViewer.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                startActivity(externalViewer);
            }
            catch (ActivityNotFoundException e) {
                // show notification if no app available to open selected format
                Toast.makeText(this, getString(R.string.ro_no_app_available_toast, formatSpinner.getSelectedItem().toString()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void hideAllValidationMessages() {
        for (InputControl control : inputControls) {
            TextView textView = (TextView) control.getErrorView();
            if (textView != null) {
                textView.setVisibility(View.GONE);
            }
        }
    }

    private void showValidationMessages(List<InputControlState> invalidStateList) {
        for (InputControl control : inputControls) {
            TextView textView = (TextView) control.getErrorView();
            if (textView != null) {
                Iterator<InputControlState> iterator = invalidStateList.iterator();
                while(iterator.hasNext()) {
                    InputControlState state = iterator.next();
                    if (control.getId().equals(state.getId())) {
                        textView.setText(state.getError());
                        textView.setVisibility(View.VISIBLE);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetInputControlsListener implements RequestListener<InputControlsList> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ReportOptionsActivity.this, true);
        }

        @Override
        public void onRequestSuccess(InputControlsList controlsList) {
            LinearLayout baseLayout =  (LinearLayout) findViewById(R.id.input_controls_layout);
            LayoutInflater inflater = getLayoutInflater();
            inputControls = controlsList.getInputControls();
            // init UI components for ICs
            for (final InputControl inputControl : inputControls) {
                String mandatoryPrefix = (inputControl.isMandatory()) ? "* " : "" ;
                switch (inputControl.getType()) {
                    case bool: {
                        // inflate view
                        View view = inflater.inflate(R.layout.ic_boolean_layout, baseLayout, false);
                        CheckBox checkBox = (CheckBox) view.findViewById(R.id.ic_checkbox);
                        checkBox.setText(inputControl.getLabel());
                        // set default value
                        if (inputControl.getState().getValue() == null)
                            inputControl.getState().setValue("false");
                        checkBox.setChecked(Boolean.parseBoolean(inputControl.getState().getValue()));
                        //listener
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                // update selected value
                                inputControl.getState().setValue(String.valueOf(isChecked));
                                // update dependent controls if exist
                                updateDependentControls(inputControl);
                            }
                        });
                        // assign views to the control
                        inputControl.setInputView(checkBox);
                        // show the control
                        baseLayout.addView(view);
                        break;
                    }
                    case singleValueText:
                    case singleValueNumber: {
                        // inflate view
                        View view = inflater.inflate(R.layout.ic_single_value_layout, baseLayout, false);
                        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                        EditText editText = (EditText) view.findViewById(R.id.ic_edit_text);
                        // allow only numbers if data type is numeric
                        if (inputControl.getType() == InputControl.Type.singleValueNumber) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER
                                    | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        }
                        // set default value
                        editText.setText(inputControl.getState().getValue());
                        // add listener
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void afterTextChanged(Editable s) {
                                // update selected value
                                inputControl.getState().setValue(s.toString());
                                // update dependent controls if exist
                                updateDependentControls(inputControl);
                            }
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {}
                        });
                        TextView errorView = (TextView) view.findViewById(R.id.ic_error_text);
                        // assign views to the control
                        inputControl.setInputView(editText);
                        inputControl.setErrorView(errorView);
                        // show the control
                        baseLayout.addView(view);
                        break;
                    }
                    case singleValueDate:
                    case singleValueDatetime: {
                        // inflate view
                        View view = inflater.inflate(R.layout.ic_single_value_date_layout, baseLayout, false);
                        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                        final EditText editText = (EditText) view.findViewById(R.id.ic_date_text);

                        String format = DEFAULT_DATE_FORMAT;
                        for (DateTimeFormatValidationRule validationRule: inputControl.getValidationRules(DateTimeFormatValidationRule.class)) {
                            format = validationRule.getFormat();
                        }
                        DateFormat formatter = new SimpleDateFormat(format);

                        // set default value
                        final Calendar startDate = Calendar.getInstance();
                        String defaultValue = inputControl.getState().getValue();
                        if (defaultValue != null) {
                            try {
                                startDate.setTime(formatter.parse(defaultValue));
                                editText.setText(defaultValue);
                            } catch (ParseException e) {
                                Ln.w("Unparseable date: %s", defaultValue);
                            }
                        }

                        // init the date picker
                        ImageButton datePicker = (ImageButton) view.findViewById(R.id.ic_date_picker_button);
                        // add a click listener
                        datePicker.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                showDateDialog(inputControl, DATE_DIALOG_ID, editText, startDate);
                            }
                        });

                        boolean isDateTime = (inputControl.getType() == InputControl.Type.singleValueDatetime);

                        if (isDateTime) {
                            // init the time picker
                            ImageButton timePicker = (ImageButton) view.findViewById(R.id.ic_time_picker_button);
                            timePicker.setVisibility(View.VISIBLE);
                            // add a click listener
                            timePicker.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    showDateDialog(inputControl, TIME_DIALOG_ID, editText, startDate);
                                }
                            });
                        }

                        // add listener for text field
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void afterTextChanged(Editable s) {
                                // update dependent controls if exist
                                updateDependentControls(inputControl);
                            }
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                /* Do nothing */
                            }
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                /* Do nothing */
                            }
                        });

                        TextView errorView = (TextView) view.findViewById(R.id.ic_error_text);
                        // assign views to the control
                        inputControl.setInputView(editText);
                        inputControl.setErrorView(errorView);
                        // show the control
                        baseLayout.addView(view);
                        break;
                    }
                    case singleSelect:
                    case singleSelectRadio: {
                        // inflate view
                        View view = inflater.inflate(R.layout.ic_single_select_layout, baseLayout, false);
                        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                        Spinner spinner = (Spinner) view.findViewById(R.id.ic_spinner);
                        spinner.setPrompt(inputControl.getLabel());

                        ArrayAdapter<InputControlOption> lovAdapter =
                                new ArrayAdapter<InputControlOption>(ReportOptionsActivity.this, android.R.layout.simple_spinner_item, inputControl.getState().getOptions());
                        lovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(lovAdapter);

                        // set initial value for spinner
                        for (InputControlOption option : inputControl.getState().getOptions()) {
                            if (option.isSelected()) {
                                int position = lovAdapter.getPosition(option);
                                spinner.setSelection(position, false);
                            }
                        }

                        // listener
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                // update selected value
                                for (InputControlOption option : inputControl.getState().getOptions()) {
                                    option.setSelected(option.equals(parent.getSelectedItem()));
                                }
                                // update dependent controls if exist
                                updateDependentControls(inputControl);
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) { /* Do nothing */ }
                        });

                        TextView errorView = (TextView) view.findViewById(R.id.ic_error_text);
                        // assign views to the control
                        inputControl.setInputView(spinner);
                        inputControl.setErrorView(errorView);
                        // show the control
                        baseLayout.addView(view);
                        break;
                    }
                    case multiSelect:
                    case multiSelectCheckbox:
                        // inflate view
                        View view = inflater.inflate(R.layout.ic_multi_select_layout, baseLayout, false);
                        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                        MultiSelectSpinner<InputControlOption> multiSpinner = (MultiSelectSpinner<InputControlOption>) view.findViewById(R.id.ic_multi_spinner);
                        multiSpinner.setPrompt(inputControl.getLabel());
                        // init values
                        multiSpinner.setItemsList(inputControl.getState().getOptions(), InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);

                        // set selected values
                        List<Integer> positions = new ArrayList<Integer>();
                        for (InputControlOption option : inputControl.getState().getOptions()) {
                            if (option.isSelected()) {
                                positions.add(multiSpinner.getItemPosition(option));
                            }
                        }
                        multiSpinner.setSelection(positions);

                        // listener
                        multiSpinner.setOnItemsSelectedListener(
                                new MultiSelectSpinner.OnItemsSelectedListener() {
                                    @Override
                                    public void onItemsSelected(List selectedItems) {
                                        // update selected values
                                        for (InputControlOption option : inputControl.getState().getOptions()) {
                                            boolean isSelected = selectedItems.contains(option);
                                            option.setSelected(isSelected);
                                        }
                                        // update dependent controls if exist
                                        updateDependentControls(inputControl);
                                    }
                                });

                        TextView errorView = (TextView) view.findViewById(R.id.ic_error_text);
                        // assign views to the control
                        inputControl.setInputView(multiSpinner);
                        inputControl.setErrorView(errorView);
                        // show the control
                        baseLayout.addView(view);
                        break;
                }
            }
            setRefreshActionButtonState(false);
        }
    }

    private class GetInputControlsValuesListener implements RequestListener<InputControlStatesList> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ReportOptionsActivity.this, false);
            setRefreshActionButtonState(false);
        }

        @Override
        public void onRequestSuccess(InputControlStatesList stateList) {
            skipRecursiveUpdate = true; // don't update recursively
            for (InputControlState state : stateList.getInputControlStates()) {
                for(InputControl slaveControl : inputControls) {
                    if (slaveControl.getId().equals(state.getId())) {
                        slaveControl.setState(state);
                        switch (slaveControl.getType()) {
                            case bool:
                                CheckBox checkBox = (CheckBox) slaveControl.getInputView();
                                checkBox.setChecked(Boolean.parseBoolean(state.getValue()));
                                break;
                            case singleValueText:
                            case singleValueNumber:
                            case singleValueDate:
                            case singleValueDatetime:
                                EditText editText = (EditText) slaveControl.getInputView();
                                editText.setText(state.getValue());
                                break;
                            case singleSelect:
                            case singleSelectRadio:
                                Spinner spinner = (Spinner) slaveControl.getInputView();
                                ArrayAdapter<InputControlOption> lovAdapter =
                                        new ArrayAdapter<InputControlOption>(ReportOptionsActivity.this, android.R.layout.simple_spinner_item, state.getOptions());
                                lovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(lovAdapter);
                                // set initial value for spinner
                                for (InputControlOption option : state.getOptions()) {
                                    if (option.isSelected()) {
                                        int position = lovAdapter.getPosition(option);
                                        spinner.setSelection(position, false);
                                    }
                                }
                                break;
                            case multiSelect:
                            case multiSelectCheckbox:
                                MultiSelectSpinner<InputControlOption> multiSpinner =
                                        (MultiSelectSpinner<InputControlOption>) slaveControl.getInputView();
                                multiSpinner.setItemsList(state.getOptions(), InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);
                                // set selected values
                                List<Integer> positions = new ArrayList<Integer>();
                                for (InputControlOption option : state.getOptions()) {
                                    if (option.isSelected()) {
                                        positions.add(multiSpinner.getItemPosition(option));
                                    }
                                }
                                multiSpinner.setSelection(positions);
                                break;
                        }
                        break;
                    }
                }
            }
            skipRecursiveUpdate = false;
            setRefreshActionButtonState(false);
        }

    }

    private class ValidateInputControlsValuesListener implements RequestListener<InputControlStatesList> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ReportOptionsActivity.this, false);
            setRefreshActionButtonState(false);
        }

        @Override
        public void onRequestSuccess(InputControlStatesList stateList) {
            hideAllValidationMessages();
            List<InputControlState> invalidStateList = stateList.getInputControlStates();
            if (invalidStateList.isEmpty()) {
                runReport();
            } else {
                showValidationMessages(invalidStateList);
            }
            setRefreshActionButtonState(false);
        }

    }

}