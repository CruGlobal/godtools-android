package org.cru.godtools.api;

import org.keynote.godtools.android.business.GSSubscriber;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

@Deprecated
public interface GrowthSpacesApi {
    @Deprecated
    @POST("subscribers")
    Call<GSSubscriber> createSubscriber(@Header("Access-Id") String accessId,
                                        @Header("Access-Secret") String accessSecret, @Body GSSubscriber gsSubscriber);
}
