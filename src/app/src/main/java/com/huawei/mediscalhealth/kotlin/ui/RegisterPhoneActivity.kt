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


package com.huawei.mediscalhealth.kotlin.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.Constants
import com.huawei.mediscalhealth.kotlin.utils.UserContactPrefManager

class RegisterPhoneActivity : AppCompatActivity() {
    //Layout input fields
    var et_primary_contact_no: EditText? = null
    var et_secondary_contact_no: EditText? = null
    var et_primary_contact_name: EditText? = null
    var et_secondary_contact_name: EditText? = null

    //Constants
    var PRIMARY_CONTACT_RESULT = 1
    var SECONDARY_CONTACT_RESULT = 2

    //internal variables
    private var uriContact: Uri? = null
    private var contactID: String? = null

    //User session
    var mUserContactManager: UserContactPrefManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_phone)
        init()
    }

    private fun init() {
        et_primary_contact_no = findViewById(R.id.activity_register_et_primary_contact_no)
        et_secondary_contact_no = findViewById(R.id.activity_register_et_secondary_contact_no)
        et_primary_contact_name = findViewById(R.id.activity_register_et_primary_contact_name)
        et_secondary_contact_name = findViewById(R.id.activity_register_et_secondary_contact_name)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PRIMARY_CONTACT_RESULT && resultCode == Activity.RESULT_OK) {
            uriContact = data!!.data
            retrievePrimaryContactName()
            retrievePrimaryContactNumber()
        } else if (requestCode == SECONDARY_CONTACT_RESULT && resultCode == Activity.RESULT_OK) {
            uriContact = data!!.data
            retrieveSecondaryContactName()
            retrieveSecondaryContactNumber()
        }
    }

    private fun retrieveSecondaryContactName() {
        var secondaryContactName: String? = null
        // querying contact data store
        val cursor = contentResolver.query(uriContact!!, null, null, null, null)
        if (cursor!!.moveToFirst()) {
            secondaryContactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            et_secondary_contact_name!!.setText(secondaryContactName)
        }
        cursor.close()
    }

    private fun retrieveSecondaryContactNumber() {
        var secondaryContactNumber: String? = null
        // getting contacts ID
        val cursorID = contentResolver.query(uriContact!!, arrayOf(ContactsContract.Contacts._ID),
                null, null, null)
        if (cursorID!!.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID))
        }
        cursorID.close()
        // Using the contact ID now we will get contact phone number
        val cursorPhone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + Constants.QRY_AND +
                        ContactsContract.CommonDataKinds.Phone.TYPE + Constants.QRY_EQUAL +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, arrayOf(contactID),
                null)
        if (cursorPhone!!.moveToFirst()) {
            secondaryContactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            et_secondary_contact_no!!.setText(secondaryContactNumber)
        }
        cursorPhone.close()
    }

    private fun retrievePrimaryContactNumber() {
        var primaryContactNumber: String? = null
        // getting contacts ID
        val cursorID = contentResolver.query(uriContact!!, arrayOf(ContactsContract.Contacts._ID),
                null, null, null)
        if (cursorID!!.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID))
        }
        cursorID.close()
        // Using the contact ID now we will get contact phone number
        val cursorPhone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + Constants.QRY_AND +
                        ContactsContract.CommonDataKinds.Phone.TYPE + Constants.QRY_EQUAL +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, arrayOf(contactID),
                null)
        if (cursorPhone!!.moveToFirst()) {
            primaryContactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            et_primary_contact_no!!.setText(primaryContactNumber)
        }
        cursorPhone.close()
    }

    private fun retrievePrimaryContactName() {
        var primaryContactName: String? = null
        // querying contact data store
        val cursor = contentResolver.query(uriContact!!, null, null, null, null)
        if (cursor!!.moveToFirst()) {
            primaryContactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            et_primary_contact_name!!.setText(primaryContactName)
        }
        cursor.close()
    }

    fun clickEventConfirmDetails(view: View?) {
        val StrPrimaryContactName = et_primary_contact_name!!.text.toString()
        val StrPrimaryContactNo = et_primary_contact_no!!.text.toString()
        val StrSecondaryContactName = et_secondary_contact_name!!.text.toString()
        val StrSecondaryContactNo = et_secondary_contact_no!!.text.toString()
        if (StrPrimaryContactName.isEmpty()) {
            et_primary_contact_name!!.error = getString(R.string.error_primary_contact)
            return
        } else if (StrPrimaryContactNo.isEmpty()) {
            et_primary_contact_no!!.error = getString(R.string.error_primary_contact_number)
            return
        } else if (StrSecondaryContactName.isEmpty()) {
            et_secondary_contact_name!!.error = getString(R.string.error_contact_name)
            return
        } else if (StrSecondaryContactNo.isEmpty()) {
            et_secondary_contact_no!!.error = getString(R.string.error_secondary_contact_number)
            return
        }
        mUserContactManager = UserContactPrefManager(this)
        mUserContactManager!!.createEmergencyContactInfo(StrPrimaryContactName, StrSecondaryContactName, StrPrimaryContactNo, StrSecondaryContactNo)
        startActivity(Intent(this, FallDetectionCalibrationActivity::class.java))
    }

    fun eventClickSecondaryContact(view: View?) {
        val i = Intent(Intent.ACTION_PICK)
        i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResult(i, SECONDARY_CONTACT_RESULT)
    }

    fun eventClickPrimaryContact(view: View?) {
        val i = Intent(Intent.ACTION_PICK)
        i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResult(i, PRIMARY_CONTACT_RESULT)
    }
}