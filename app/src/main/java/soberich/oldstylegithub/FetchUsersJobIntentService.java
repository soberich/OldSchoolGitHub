package soberich.oldstylegithub;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An {@link JobIntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static helper methods.
 */
public class FetchUsersJobIntentService extends JobIntentService {

    //private static final String TAG = "FetchUsersJobIntentServ";
    private static final String TAG = "WholeApp";

    // JobIntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FETCH_BULK_USERS = "soberich.oldstylegithub.action.GET_BULK_USERS";
    public static final String ACTION_FETCH_SINGLE_USER = "soberich.oldstylegithub.action.GET_SINGLE_USER";

    public static final int RESOURCE_TYPE_SINGLE_USER = 1;
    public static final int RESOURCE_TYPE_BULK_USERS = 2;
    public static final String RESOURCE_TYPE_EXTRA = "soberich.oldstylegithub.extra.RESOURCE_TYPE";

    public static final String PARAM_SINGLE_USER_EXTRA = "soberich.oldstylegithub.extra.LOGIN";
    public static final String PARAM_BULK_USERS_EXTRA = "soberich.oldstylegithub.extra.SINCE";

    public static final String SERVICE_CALLBACK = "soberich.oldstylegithub.SERVICE_CALLBACK";

    public static final String ORIGINAL_INTENT_EXTRA = "soberich.oldstylegithub.ORIGINAL_INTENT_EXTRA";
    // BEGIN_INCLUDE (IntentService)

    //TODO: 10/1/17  @Inject Processor RESTMethod Handler

    @Inject public Handler mHandler;
    @Inject public Processor mProcessor;
    @Inject public RESTMethod mRESTMethod;

/*    @Inject
    public FetchUsersJobIntentService(Handler h, Processor p, RESTMethod r) {
        Log.d(TAG, "FetchUsersJobIntentService().hashCode() is " + this.hashCode());
        this.mHandler = h;
        this.mProcessor = p;
        this.mRESTMethod = r;
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        ((App)getApplication()).getAppComponent().inject(this);
    }

    /**
     * Handle action GET_BULK_USERS in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetUsers(int resourceType,
                                      ResultReceiver resultReceiver,
                                      int since) {
        toast("Loading users since " + since);
        Log.d(TAG, "Service.handleActionGetUsers() called with: resourceType = [" + resourceType + "], resultReceiver = [" + resultReceiver + "], since = [" + since + "]");
        Log.d(TAG, "Service.handleActionGetUsers() Thread is - " + Thread.currentThread().getName());
        try {
            mProcessor.dispatchBulkUsers(mRESTMethod.getUsers(since).execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     /**
     * Handle action GET_SINGLE_USER in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetSingleUser(int resourceType,
                                           ResultReceiver resultReceiver,
                                           String login) {
        toast("Loading " + login);
        try {
            mProcessor.dispatchSingleUser(mRESTMethod.getUser(login).execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // END_INCLUDE (IntentService)

    // BEGIN_INCLUDE (JobIntentService)

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1366;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        Log.d(TAG, "Service.enqueueWork() Thread is - " + Thread.currentThread().getName());
        Log.d(TAG, "Service.enqueueWork() called with: context = [" + context + "], work = [" + work.getExtras() + "]");
        enqueueWork(context, FetchUsersJobIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //TODO Do we need synchronized while it is queued?
        synchronized (this) {
            Log.d(TAG, "Service.onHandleWork()  called with: intent = [" + intent + "]");
            Log.d(TAG, "Service.onHandleWork() Thread is - " + Thread.currentThread().getName());
            final String action = intent.getAction();
            if (ACTION_FETCH_BULK_USERS.equals(action)) {
                final int resourceType = intent.getIntExtra(RESOURCE_TYPE_EXTRA, -1);
                final ResultReceiver resultReceiver = intent.getParcelableExtra(SERVICE_CALLBACK);
                final int since = intent.getIntExtra(PARAM_BULK_USERS_EXTRA, -1);
                handleActionGetUsers(resourceType, resultReceiver, since);
            } else if (ACTION_FETCH_SINGLE_USER.equals(action)) {
                final int resourceType = intent.getIntExtra(RESOURCE_TYPE_EXTRA, -1);
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
