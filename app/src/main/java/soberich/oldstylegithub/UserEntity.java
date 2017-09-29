package soberich.oldstylegithub;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 *
 * Created by soberich on 9/26/17.
 */

public class UserEntity extends BaseObservable {

    public UserEntity(int id, String login, String name, String avatarUrl) {
        this.id = id;
        this.avatarUrl = avatarUrl;
        this.name = name;
        this.login = login;
    }

    public UserEntity() {
        timestampWhenLoaded = new Timestamp(Calendar.getInstance().getTime().getTime());
    }
    @Bindable
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    @Bindable
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
    @Bindable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    @Bindable
    public String getName() {
        return name;
    }
    @Bindable
    public long getTimestampWhenLoaded() {

        return timestampWhenLoaded.getTime();
    }

    public void setTimestampWhenLoaded(Timestamp timestampWhenLoaded) {

        this.timestampWhenLoaded = new Timestamp(Calendar.getInstance().getTime().getTime());
    }

    public void setName(String name) {
        this.name = name;
    }

    @SerializedName("id")
    private int id;

    @SerializedName("login")
    private String login;

    @SerializedName("name")
    private String name;

    @SerializedName("avatar_url")
    private String avatarUrl;

    private Timestamp timestampWhenLoaded;

    //timestampWhenLoaded = new Timestamp(Calendar.getInstance().getTime().getTime());

    @BindingAdapter("imageUrl")
    public static void bindImage(ImageView imageView, String url) {
        GlideApp.with(imageView.getContext())
                .load(url)
                .placeholder(R.drawable.placeholder)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView);
    }
}
