package soberich.oldstylegithub.di;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import soberich.oldstylegithub.App;

/**
 *
 * Created by soberich on 9/30/17.
 */

@Module
public class AppModule {

    private Context appContext;

    public AppModule(@NonNull Context context) {
        appContext = context;
    }

    @Provides
    @NonNull
    @Singleton
    Context provideContext() {
        return appContext;
    }

    @Provides
    @NonNull
    @Singleton
    Handler provideMainHandler() {
        return new Handler();
    }
//    @Provides
//    @NonNull
//    @Singleton
//    App provideApp() {
//        return (App)appContext.getApplicationContext();
//    }
}
