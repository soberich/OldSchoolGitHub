package soberich.oldstylegithub.di;

import javax.inject.Singleton;

import dagger.Component;
import soberich.oldstylegithub.App;
import soberich.oldstylegithub.FetchUsersJobIntentService;
import soberich.oldstylegithub.GitHubUsersContentProvider;
import soberich.oldstylegithub.Processor;
import soberich.oldstylegithub.RESTMethod;
import soberich.oldstylegithub.ServiceHelper;

/**
 *
 * Created by soberich on 9/30/17.
 */

@Component(modules = {AppModule.class, LayersModule.class})
@Singleton
public interface IAppComponent {
    void inject(App app);
    void inject(FetchUsersJobIntentService jobIntentService);
    void inject(Processor processor);
    //void inject(GitHubUsersContentProvider contentProvider);
}
