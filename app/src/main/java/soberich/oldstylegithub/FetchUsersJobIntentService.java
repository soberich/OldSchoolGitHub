package soberich.oldstylegithub;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.os.ResultReceiver;
import android.widget.Toast;

import java.io.IOException;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An {@link JobIntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static helper methods.
 */
public class FetchUsersJobIntentService extends JobIntentService {

    // JobIntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FETCH_BULK_USERS = "soberich.oldstylegithub.action.GET_BULK_USERS";
    public static final String ACTION_FETCH_SINGLE_USER = "soberich.oldstylegithub.action.GET_SINGLE_USER";

    public static final int RESOURCE_TYPE_SINGLE_USER = 1;
    public static final int RESOURCE_TYPE_BULK_USERS = 2;

    public static final String RESOURCE_TYPE_EXTRA = "soberich.oldstylegithub.extra.RESOURCE_TYPE";
    public static final String PARAM_SINGLE_USER_EXTRA = "soberich.oldstylegithub.extra.LOGIN";
    public static final String PARAM_BULK_USERS_EXTRA = "soberich.oldstylegithub.extra.SINCE";

    public static final String SERVICE_CALLBACK = "soberich.oldstylegithub.SERVICE_CALLBACK";

    public static final String ORIGINAL_INTENT_EXTRA = "soberich.oldstylegithub.ORIGINAL_INTENT_EXTRA"
    // BEGIN_INCLUDE (IntentService)


     /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
     private Processor processor =  new Processor(getApplicationContext());
     private static RESTMethod mRESTMethod = new Retrofit.Builder()
             .baseUrl(RESTMethod.BASE_API_URL)
             .addConverterFactory(GsonConverterFactory.create())
             .build().create(RESTMethod.class);;

//     //TODO Di
//     public FetchUsersJobIntentService(RESTMethod restMethod) {
//         mRESTMethod = restMethod;
//     }

    private void handleActionGetUsers(String resourceType,
                                      ResultReceiver resultReceiver,
                                      int since) {
        // TODO: Handle action GET_BULK_USERS
        try {
            processor.dispatchBulkUsers(mRESTMethod.getUsers(since).execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */

    private void handleActionGetSingleUser(String resourceType,
                                           ResultReceiver resultReceiver,
                                           String login) {
        // TODO: Handle action GET_SINGLE_USER
        try {
            processor.dispatchSingleUser(mRESTMethod.getUser(login).execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // END_INCLUDE (IntentService)

    // BEGIN_INCLUDE (JobIntentService)

    final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1366;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, FetchUsersJobIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //TODO Do we need synchronized while it is queued?
        synchronized (this) {
            final String action = intent.getAction();
            if (ACTION_FETCH_BULK_USERS.equals(action)) {
                final String resourceType = intent.getStringExtra(RESOURCE_TYPE_EXTRA);
                final ResultReceiver resultReceiver = intent.getParcelableExtra(SERVICE_CALLBACK);
                final int since = intent.getIntExtra(PARAM_BULK_USERS_EXTRA, -1);
                handleActionGetUsers(resourceType, resultReceiver, since);
            } else if (ACTION_FETCH_SINGLE_USER.equals(action)) {
                final String resourceType = intent.getStringExtra(RESOURCE_TYPE_EXTRA);
                final ResultReceiver resultReceiver = intent.getParcelableExtra(SERVICE_CALLBACK);
                final String login = intent.getStringExtra(PARAM_SINGLE_USER_EXTRA);
                handleActionGetSingleUser(resourceType, resultReceiver, login);
            }
        }
    }

    @Override
    public boolean onStopCurrentWork() {
        return super.onStopCurrentWork();
    }

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(FetchUsersJobIntentService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // END_INCLUDE (JobIntentService)
}
