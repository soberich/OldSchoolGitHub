package soberich.oldstylegithub;

/**
 *
 * Created by soberich on 9/27/17.
 */

class UserRemoteRepository {
    private static final UserRemoteRepository ourInstance = new UserRemoteRepository();

    static UserRemoteRepository getInstance() {
        return ourInstance;
    }

    private UserRemoteRepository() {
    }



}
