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

package com.example.android.apis.view;

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;

/**
 * Demonstrates scrolling with a ScrollView, android:scrollbarTrackVertical
 * and android:scrollbarTrackVertical are set to colored drawables created in xml
 * using {@code <shape>} and android:scrollbarSize="12dip" makes it wider than normal.
 * http://developer.android.com/guide/topics/resources/drawable-resource.html#Shape
 * The drawable/scrollbar_vertical_thumb has {@code <corners android:radius="6dp" />} which
 * gives a rounded look to the top and bottom of the scrollbar.
 */
public class ScrollBar2 extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.scrollbar2.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scrollbar2);
    }
}
