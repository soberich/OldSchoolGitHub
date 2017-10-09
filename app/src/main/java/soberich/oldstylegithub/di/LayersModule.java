package soberich.oldstylegithub.di;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import soberich.oldstylegithub.Processor;
import soberich.oldstylegithub.RESTMethod;
import soberich.oldstylegithub.ServiceHelper;

/**
 *
 * Created by soberich on 9/30/17.
 */

@Module
public class LayersModule {

    @Provides
    @NonNull
    @Singleton
    public Processor provideProcessor(Context context) {
        return new Processor(context);
    }

    @Provides
    @NonNull
    @Singleton
    public RESTMethod provideRestMethod() {
        return new Retrofit.Builder()
                .baseUrl(RESTMethod.BASE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RESTMethod.class);
    }

}
