package soberich.oldstylegithub;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 *
 * Created by soberich on 9/27/17.
 */

public interface RESTMethod {

    String BASE_API_URL = "https://api.github.com/";

    @GET("users")
    Call<List<UserEntity>> getUsers(@Query("since") int since);

    @GET("users/{login}")
    Call<UserEntity> getUser(@Path("login") String login);
}
