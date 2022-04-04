/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.mediscalhealth.java.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.utils.Constants;
import com.huawei.mediscalhealth.java.utils.UserContactPrefManager;

public class RegisterPhoneActivity extends AppCompatActivity {
    // Layout input fields
    EditText et_primary_contact_no;
    EditText et_secondary_contact_no;
    EditText et_primary_contact_name;
    EditText et_secondary_contact_name;

    // Constants
    int PRIMARY_CONTACT_RESULT = 1;
    int SECONDARY_CONTACT_RESULT = 2;

    // Internal variables
    private Uri uriContact;
    private String contactID;

    // User session
    UserContactPrefManager mUserContactManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone);
        init();
    }

    private void init() {
        et_primary_contact_no = findViewById(R.id.activity_register_et_primary_contact_no);
        et_secondary_contact_no = findViewById(R.id.activity_register_et_secondary_contact_no);
        et_primary_contact_name = findViewById(R.id.activity_register_et_primary_contact_name);
        et_secondary_contact_name = findViewById(R.id.activity_register_et_secondary_contact_name);     
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PRIMARY_CONTACT_RESULT && resultCode == RESULT_OK) {
            uriContact = data.getData();
            retrievePrimaryContactName();
            retrievePrimaryContactNumber();
        } else if (requestCode == SECONDARY_CONTACT_RESULT && resultCode == RESULT_OK) {
            uriContact = data.getData();
            retrieveSecondaryContactName();
            retrieveSecondaryContactNumber();
        }
    }

    private void retrieveSecondaryContactName() {
        String secondaryContactName = null;
        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {
            secondaryContactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            et_secondary_contact_name.setText(secondaryContactName);
        }
        cursor.close();
    }

    private void retrieveSecondaryContactNumber() {
        String secondaryContactNumber = null;
        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
            new String[]{ContactsContract.Contacts._ID},
            null, null, null);
        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }
        cursorID.close();
        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +Constants.QRY_AND +
                ContactsContract.CommonDataKinds.Phone.TYPE + Constants.QRY_EQUAL +
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
            new String[]{contactID},
            null);
        if (cursorPhone.moveToFirst()) {
            secondaryContactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            et_secondary_contact_no.setText(secondaryContactNumber);
        }
        cursorPhone.close();
    }

    private void retrievePrimaryContactNumber() {
        String primaryContactNumber = null;
        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
            new String[]{ContactsContract.Contacts._ID},
            null, null, null);
        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }
        cursorID.close();
        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + Constants.QRY_AND +
                ContactsContract.CommonDataKinds.Phone.TYPE + Constants.QRY_EQUAL +
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
            new String[]{contactID},
            null);
        if (cursorPhone.moveToFirst()) {
            primaryContactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            et_primary_contact_no.setText(primaryContactNumber);
        }
        cursorPhone.close();
    }

    private void retrievePrimaryContactName() {
        String primaryContactName = null;
        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {
            primaryContactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            et_primary_contact_name.setText(primaryContactName);
        }
        cursor.close();
    }

    public void clickEventConfirmDetails(View view) {
        String strPrimaryContactName = et_primary_contact_name.getText().toString();
        String strPrimaryContactNo = et_primary_contact_no.getText().toString();
        String strSecondaryContactName = et_secondary_contact_name.getText().toString();
        String strSecondaryContactNo = et_secondary_contact_no.getText().toString();
        if (strPrimaryContactName.isEmpty()) {
            et_primary_contact_name.setError(getString(R.string.error_primary_contact));
            return;
        } else if (strPrimaryContactNo.isEmpty()) {
            et_primary_contact_no.setError(getString(R.string.error_primary_contact_number));
            return;
        } else if (strSecondaryContactName.isEmpty()) {
            et_secondary_contact_name.setError(getString(R.string.error_contact_name));
            return;
        } else if (strSecondaryContactNo.isEmpty()) {
            et_secondary_contact_no.setError(getString(R.string.error_secondary_contact_number));
            return;
        }
        mUserContactManager = new UserContactPrefManager(this);
        mUserContactManager.createEmergencyContactInfo(strPrimaryContactName, strSecondaryContactName, strPrimaryContactNo, strSecondaryContactNo);
        startActivity(new Intent(this, FallDetectionCalibrationActivity.class));
    }

    public void eventClickSecondaryContact(View view) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(i, SECONDARY_CONTACT_RESULT);
    }

    public void eventClickPrimaryContact(View view) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(i, PRIMARY_CONTACT_RESULT);
    }
}