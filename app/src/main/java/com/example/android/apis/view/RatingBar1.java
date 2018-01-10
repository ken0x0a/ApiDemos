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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * Demonstrates how to use a rating bar. The top two RatingBar's have their
 * RatingBar.OnRatingBarChangeListener set to this and a onRatingChanged method
 * updates a TextView and two RatingBar indicators based on the values passed to it.
 */
@SuppressLint("SetTextI18n")
public class RatingBar1 extends Activity implements RatingBar.OnRatingBarChangeListener {
    /**
     * {@code RatingBar} with the ID R.id.small_ratingbar, it is used to display the value that is
     * given to either of the top two {@code RatingBar} using ?android:attr/ratingBarStyleSmall
     */
    RatingBar mSmallRatingBar;
    /**
     * {@code RatingBar} with the ID R.id.indicator_ratingbar, it is used to display the value that
     * is given to either of the top two {@code RatingBar} using ?android:attr/ratingBarStyleIndicator
     */
    RatingBar mIndicatorRatingBar;
    /**
     * {@code TextView} we use to display the textual version of the value that is given to either
     * of the top two {@code RatingBar}
     */
    TextView mRatingText;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.ratingbar_1. We
     * initialize our field {@code TextView mRatingText} by finding the view with ID R.id.rating,
     * {@code RatingBar mIndicatorRatingBar} by finding the view with ID R.id.indicator_ratingbar,
     * and {@code RatingBar mSmallRatingBar} by finding the view with ID R.id.small_ratingbar.
     * We find the view with ID R.id.ratingbar1 and set its {@code OnRatingBarChangeListener} to this
     * and do the same thing with the view with ID R.id.ratingbar2.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ratingbar_1);

        mRatingText = (TextView) findViewById(R.id.rating);

        // We copy the most recently changed rating on to these indicator-only
        // rating bars
        mIndicatorRatingBar = (RatingBar) findViewById(R.id.indicator_ratingbar);
        mSmallRatingBar = (RatingBar) findViewById(R.id.small_ratingbar);

        // The different rating bars in the layout. Assign the listener to us.
        ((RatingBar) findViewById(R.id.ratingbar1)).setOnRatingBarChangeListener(this);
        ((RatingBar) findViewById(R.id.ratingbar2)).setOnRatingBarChangeListener(this);
    }

    /**
     * Notification that the rating has changed. We initialize our variable {@code int numStars} by
     * fetching the number of stars that our parameter {@code RatingBar ratingBar} displays. Then we
     * set the text of our field {@code TextView mRatingText} to the string created by concatenating
     * the string with resource ID R.string.ratingbar_rating ("Rating:") with a space then the string
     * value of {@code rating} followed by a "/" followed by the string value of {@code numStars}.
     * If the number of stars that our field {@code RatingBar mIndicatorRatingBar} displays is not
     * equal to {@code numStars} we set the number of stars of both {@code mIndicatorRatingBar} and
     * {@code mSmallRatingBar} to {@code numStars}. If the current rating (number of stars filled) of
     * {@code mIndicatorRatingBar} is not equal to our parameter {@code rating} we set the rating  of
     * both {@code mIndicatorRatingBar} and {@code mSmallRatingBar}  to {@code rating}. We initialize
     * our variable {@code float ratingBarStepSize} by getting the step size of {@code ratingBar} and
     * if the step size of {@code mIndicatorRatingBar} is not equal to {@code ratingBarStepSize} we
     * set the step size of both {@code mIndicatorRatingBar} and {@code mSmallRatingBar} to
     * {@code ratingBarStepSize}.
     *
     * @param ratingBar The RatingBar whose rating has changed.
     * @param rating    The current rating. This will be in the range 0..numStars.
     * @param fromTouch True if the rating change was initiated by a user's
     *                  touch gesture or arrow key/horizontal trackball movement.
     */
    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromTouch) {
        final int numStars = ratingBar.getNumStars();
        mRatingText.setText(getString(R.string.ratingbar_rating) + " " + rating + "/" + numStars);

        // Since this rating bar is updated to reflect any of the other rating
        // bars, we should update it to the current values.
        if (mIndicatorRatingBar.getNumStars() != numStars) {
            mIndicatorRatingBar.setNumStars(numStars);
            mSmallRatingBar.setNumStars(numStars);
        }
        if (mIndicatorRatingBar.getRating() != rating) {
            mIndicatorRatingBar.setRating(rating);
            mSmallRatingBar.setRating(rating);
        }
        final float ratingBarStepSize = ratingBar.getStepSize();
        if (mIndicatorRatingBar.getStepSize() != ratingBarStepSize) {
            mIndicatorRatingBar.setStepSize(ratingBarStepSize);
            mSmallRatingBar.setStepSize(ratingBarStepSize);
        }
    }

}
