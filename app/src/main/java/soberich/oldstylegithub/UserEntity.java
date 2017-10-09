package soberich.oldstylegithub;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 *
 * Created by soberich on 9/26/17.
 */

public class UserEntity extends BaseObservable {

//    public UserEntity(int id, String login, String name, String avatarUrl) {
//        this.id = id;
//        this.avatarUrl = avatarUrl;
//        this.name = name;
//        this.login = login;
//    }

    public UserEntity() {
        timestampWhenLoaded = new Timestamp(Calendar.getInstance().getTime().getTime());
    }
    @Bindable
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }
    @Bindable
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
        notifyPropertyChanged(BR.login);
    }
    @Bindable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        notifyPropertyChanged(BR.avatarUrl);
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
        notifyPropertyChanged(BR.timestampWhenLoaded);
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
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

    @BindingAdapter({"app:imageUrl"})
    public static void bindImage(ImageView imageView, String url) {
        GlideApp.with(imageView.getContext())
                .load(url)
                .placeholder(R.drawable.placeholder)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserEntity newUser = (UserEntity) o;

        if (this.id != newUser.id) {
            return false;
        }
        if (this.login != null ? !this.login.equals(newUser.login) : newUser.login != null) {
            return false;
        }
        return avatarUrl != null ? this.avatarUrl.equals(newUser.avatarUrl) : newUser.avatarUrl == null;
    }

    @Override
    public int hashCode() {
        int result = login.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (avatarUrl != null ? avatarUrl.hashCode() : 0);
        return result;
    }
}
