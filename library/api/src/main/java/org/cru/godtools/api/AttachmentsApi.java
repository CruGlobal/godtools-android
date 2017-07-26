package org.cru.godtools.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface AttachmentsApi {
    String PATH_ATTACHMENTS = "attachments";

    @Streaming
    @GET(PATH_ATTACHMENTS + "/{id}/download")
    Call<ResponseBody> download(@Path("id") long id);
}
