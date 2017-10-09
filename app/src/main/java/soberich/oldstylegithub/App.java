package soberich.oldstylegithub;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import soberich.oldstylegithub.di.AppModule;
import soberich.oldstylegithub.di.DaggerIAppComponent;
import soberich.oldstylegithub.di.IAppComponent;
import soberich.oldstylegithub.di.LayersModule;

/**
 *
 * Created by soberich on 9/30/17.
 */

public class App extends Application {

    private static final String TAG = "WholeApp";

    private IAppComponent appComponent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "attachBaseContext() called with: base = [" + base + "]");

        app = this;

        appComponent = DaggerIAppComponent
                .builder()
                .appModule(new AppModule(this))
                .layersModule(new LayersModule())
                .build();
        appComponent.inject(this);
    }

    public IAppComponent getAppComponent() {
        Log.d(TAG, "getAppComponent() called");
        return appComponent;
    }

    // FIXME: 10/1/17 DELETE no use
    /* no-op */
    public static App getApp() {
        return app;
    }
    private static App app;
}
