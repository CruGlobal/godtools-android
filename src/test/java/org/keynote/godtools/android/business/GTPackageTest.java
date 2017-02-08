package org.keynote.godtools.android.business;

import junit.framework.Assert;

import org.junit.Test;
import org.keynote.godtools.android.api.GodToolsApi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;

public class GTPackageTest {
    @Test
    public void verifyVersionComparisonEqual() throws Exception {
        final GTPackage package1 = new GTPackage();
        final GTPackage package2 = new GTPackage();

        // test equal versions
        package1.setVersion("1.3");
        package2.setVersion("1.3");
        assertThat(package1.compareVersionTo(package2), is(0));
        assertThat(package2.compareVersionTo(package1), is(0));

        // test equivalent versions
        package1.setVersion("1.3");
        package2.setVersion("1.3.0");
        assertThat(package1.compareVersionTo(package2), is(0));
        assertThat(package2.compareVersionTo(package1), is(0));
    }

    @Test
    public void verifyVersionComparisonDiffering() throws Exception {
        final GTPackage package1 = new GTPackage();
        final GTPackage package2 = new GTPackage();

        // test differing major versions
        package1.setVersion("1.1");
        package2.setVersion("2.1");
        assertThat(package1.compareVersionTo(package2), is(lessThan(0)));
        assertThat(package2.compareVersionTo(package1), is(greaterThan(0)));

        // test differing minor versions
        package1.setVersion("1.2");
        package2.setVersion("1.11");
        assertThat(package1.compareVersionTo(package2), is(lessThan(0)));
        assertThat(package2.compareVersionTo(package1), is(greaterThan(0)));

        // test minor version for rounding errors
        package1.setVersion("1.1");
        package2.setVersion("1.10");
        assertThat(package1.compareVersionTo(package2), is(lessThan(0)));
        assertThat(package2.compareVersionTo(package1), is(greaterThan(0)));

        // test newnew minor version
        package1.setVersion("1.3");
        package2.setVersion("1.3.1");
        assertThat(package1.compareVersionTo(package2), is(lessThan(0)));
        assertThat(package2.compareVersionTo(package1), is(greaterThan(0)));
    }

    @Test
    public void checkGodToolsAPIGetMeta()
    {
        Call<GTLanguages> languagesCall = GodToolsApi.INSTANCE.getListOfPackages();
        try {
            Response<GTLanguages> execute = languagesCall.execute();
            GTLanguages body = execute.body();
            Assert.assertNotNull(body);
            Assert.assertEquals(true, body.mLanguages.size() > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
