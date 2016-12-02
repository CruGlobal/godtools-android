package com.example.rmatt.crureader.bo.GDocument;

import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/24/2016.
 * <packagename gtapi-trx-id="45e65db9-957f-4ca5-b4ca-b60c4ce424c1" translate="true">Knowing God Personally</packagename>
 */

@Root(name = "packagename")
public class GPackageName extends Gtapi {

    @Text
    public String content;

    @Override
    public View render(ViewGroup viewGroup, int position) {
        return null;
    }
}
