/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.example.android.apis.view;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

/**
 * Version of demo that uses the action bar in overlay mode. Extends
 * .view.SystemUIModes with the addition of requesting the window feature
 * Window.FEATURE_ACTION_BAR_OVERLAY which overlays the action bar rather than
 * positioning the action bar in the space above the window content.
 */
public class SystemUIModesOverlay extends SystemUIModes {
    /**
     * Called when the activity is starting. We fetch a reference to the current Window for this
     * activity and use it to request the feature FEATURE_ACTION_BAR_OVERLAY (requests an Action Bar
     * that overlays window content. Normally an Action Bar will sit in the space above window
     * content, but if this feature is requested along with FEATURE_ACTION_BAR it will be layered
     * over the window content itself), then we call through to our super's implementation of
     * {@code onCreate}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
    }
}
