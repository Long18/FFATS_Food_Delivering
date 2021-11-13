package client.william.ffats.Remote;



import client.william.ffats.Model.Response;
import client.william.ffats.Model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAdBO1O8U:APA91bG1dxBXVoowrnV_sfR1itwU3-WKI_5p0wPICMwZAA8R_Jxr0wEm1JkXr-VP2MMw4AJzwFl7_WJ_lkUr2ScNShsK553YTWseqLLkS9qA-lsTLtVLakpLzXPVhtuTooqZDTxjyOHX"
            }

    )
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
