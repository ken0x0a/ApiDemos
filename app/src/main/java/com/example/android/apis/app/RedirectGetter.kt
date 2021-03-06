/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.app

import android.os.Bundle
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R

/**
 * Sub-activity that is executed by the redirection example when input is needed
 * from the user.
 */
class RedirectGetter : AppCompatActivity() {
    private var mTextPref: String? = null // text loaded from shared preference file
    private var mText: TextView? = null   // EditText in layout used for text entry

    /**
     * Called when the "APPLY" Button has been clicked. First we fetch an instance of
     * SharedPreferences for our share preference file "RedirectData" into the variable
     * **preferences**, then we use **preferences** to create an instance
     * of **SharedPreferences.Editor editor** which we use to putString the contents
     * of the mText EditText under the key "text". Finally if the **editor.commit()**
     * of this change returns true (the new value was successfully written to persistent storage)
     * we set the result that our activity will return to its caller to RESULT_OK (operation
     * succeeded) and finish() this Activity.
     *
     * Parameter: View of the Button clicked
     */
    private val mApplyListener = OnClickListener {
        val preferences = getSharedPreferences("RedirectData", 0)
        val editor = preferences.edit()
        editor.putString("text", mText!!.text.toString())

        if (editor.commit()) {
            setResult(RESULT_OK)
        }

        finish()
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate, then we set our content view to our layout file R.layout.redirect_getter. Next
     * we locate the "APPLY" Button (R.id.apply) and set its OnClickListener to OnClickListener
     * mApplyListener. We initialize our field TextView mText by locating the EditText R.id.text in
     * our layout, and finally call our method loadPrefs() to read in the String stored in our shared
     * preference file under the key "text" and use that String (if any) to initialize the contents
     * of our field mTextPref, and the initial value for the EditText mText.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.redirect_getter)

        // Watch for button clicks.
        val applyButton = findViewById<Button>(R.id.apply)
        applyButton.setOnClickListener(mApplyListener)

        // The text being set.
        mText = findViewById(R.id.text)

        // Display the stored values, or if not stored initialize with an empty String
        loadPrefs()
    }

    /**
     * Retrieve the current redirect values. NOTE: because this preference is shared between
     * multiple activities, you must be careful about when you read or write it in order to
     * keep from stepping on yourself.
     *
     * First we fetch an instance of SharedPreferences for our share preference file "RedirectData"
     * into the variable **preferences**, then we use it to retrieve the String stored in
     * the file under the key "text" (defaulting to null if none is found) and use that value to
     * initialize our field mTextPref. If mTextPref is not null we set the text of our EditText
     * mText to its value, otherwise we set it to the empty String.
     *
     */
    private fun loadPrefs() {
        val preferences = getSharedPreferences("RedirectData", 0)

        mTextPref = preferences.getString("text", null)
        if (mTextPref != null) {
            mText!!.text = mTextPref
        } else {
            mText!!.text = ""
        }
    }
}
