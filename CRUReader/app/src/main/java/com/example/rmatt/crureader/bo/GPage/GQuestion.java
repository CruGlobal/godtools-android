package com.example.rmatt.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleTextView;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/24/2016.
 * <p>
 * <question gtapi-trx-id="fae69b3b-e95b-4184-9bcd-211e35faa71c" translate="true">Why do you think most people
 * don't know God personally?</question>
 */
@Root(name = "question")
public class GQuestion extends GBaseTextAttributes {

    public static final String TAG = "GQuestion";

    @Attribute(required = false)
    public String mode;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        View gQuestion;
        if (mode
                != null && mode.equalsIgnoreCase("straight")) {
            gQuestion = inflater.inflate(R.layout.g_question_straight, viewGroup);
            defaultColor(position);
        } else {
            gQuestion = inflater.inflate(R.layout.g_question_default, viewGroup);
        }
        AutoScaleTextView gQuestionTextView = (AutoScaleTextView) gQuestion.findViewById(R.id.g_question_textview);
        updateBaseAttributes(gQuestionTextView);
        return gQuestionTextView.getId();
    }
}
