package soberich.oldstylegithub;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static soberich.oldstylegithub.FetchUsersJobIntentService.ACTION_FETCH_BULK_USERS;
import static soberich.oldstylegithub.FetchUsersJobIntentService.ACTION_FETCH_SINGLE_USER;
import static soberich.oldstylegithub.FetchUsersJobIntentService.ORIGINAL_INTENT_EXTRA;
import static soberich.oldstylegithub.FetchUsersJobIntentService.PARAM_BULK_USERS_EXTRA;
import static soberich.oldstylegithub.FetchUsersJobIntentService.PARAM_SINGLE_USER_EXTRA;
import static soberich.oldstylegithub.FetchUsersJobIntentService.RESOURCE_TYPE_BULK_USERS;
import static soberich.oldstylegithub.FetchUsersJobIntentService.RESOURCE_TYPE_EXTRA;
import static soberich.oldstylegithub.FetchUsersJobIntentService.RESOURCE_TYPE_SINGLE_USER;
import static soberich.oldstylegithub.FetchUsersJobIntentService.SERVICE_CALLBACK;
import static soberich.oldstylegithub.GitHubUsersContentProvider.ID_COLUMN_TITLE;
import static soberich.oldstylegithub.GitHubUsersContentProvider.LOGIN_COLUMN_TITLE;
import static soberich.oldstylegithub.UsersContract.Users.USERS_PATH_POSITION;
import static soberich.oldstylegithub.UsersContract.Users.USER_LOGIN_PATH_POSITION;

/**
 *
 * Created by soberich on 9/28/17.
 */

public final class ServiceHelper {
    private static final String TAG = "WholeApp";

    public static final String ACTION_REQUEST_RESULT = "REQUEST_RESULT";
    public static final String EXTRA_REQUEST_ID = "EXTRA_REQUEST_ID";
    public static final String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";

    private static final String REQUEST_ID = "REQUEST_ID";
    public static final String sBulkUsersExtra = "SINCE";
    public static final String sSingleUserExtra = "LOGIN";

    //TODO: refactor the key
    private static Map<String, Long> mPendingConcreteRequests;
    private static List<Integer> mPendingBulkRequests;
    static {
        mPendingConcreteRequests = new HashMap<>(1,1);
        mPendingBulkRequests = new ArrayList<>(3);
    }

//    // TODO: 10/1/17 @Inject Context
//    @Inject
//    public ServiceHelper(Context c) {
//        this.mContext = c;
//        this.mPendingBulkRequests = new ArrayList<>(5);
//        this.mPendingConcreteRequests = new HashMap<>();
//    }



    private ServiceHelper () {}

    public static void execute(Context context, int matchFromUriMatcher, Intent parameters) {
        //find the service resource (/path/to/remote/service with the match
        //start service with parameters
        Log.d(TAG, "ServiceHelper.execute() called with: context = [" + context + "], matchFromUriMatcher = [" + matchFromUriMatcher + "], parameters = [" + parameters + "]");
        Log.d(TAG, "ServiceHelper.execute() Thread is - " + Thread.currentThread().getName());

        switch (matchFromUriMatcher) {
            case USERS_PATH_POSITION:
                 getBulkUsers(context, parameters.getIntExtra(ID_COLUMN_TITLE, 0));
                break;
            case USER_LOGIN_PATH_POSITION:
                 getSingleUser(context, parameters.getStringExtra(LOGIN_COLUMN_TITLE));
                break;
        }

    }

    private static long getSingleUser(final Context context, final String login) {
        Log.d(TAG, "getSingleUser() called with: login = [" + login + "]");

        if(mPendingConcreteRequests.containsKey(login)){
            return mPendingConcreteRequests.get(login);
        }

        long requestId = generateRequestID();
        mPendingConcreteRequests.put(login, requestId);

        ResultReceiver serviceCallback = new ResultReceiver(null){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                resultData.putString(sSingleUserExtra, login);
                handleGetSingleUser(context, resultCode, resultData);
            }
        };

        Intent intent = new Intent(context, FetchUsersJobIntentService.class);
        intent.putExtra(RESOURCE_TYPE_EXTRA, RESOURCE_TYPE_SINGLE_USER);
        intent.putExtra(SERVICE_CALLBACK, serviceCallback);
        intent.putExtra(REQUEST_ID, requestId);
        intent.putExtra(PARAM_SINGLE_USER_EXTRA, login);
        intent.setAction(ACTION_FETCH_SINGLE_USER);

        FetchUsersJobIntentService.enqueueWork(context, intent);

        return requestId;
    }

    private static long getBulkUsers(final Context context, final int since) {
        Log.d(TAG, "ServiceHelper.getBulkUsers() called with: since = [" + since + "]");
        Log.d(TAG, "ServiceHelper.getBulkUsers() Thread is - " + Thread.currentThread().getName());
        //TODO very doubtful.. How to check if we need an update still?
        for (int i = 0; i < mPendingBulkRequests.size(); i++) {
            if (mPendingBulkRequests.get(i) >= since) {
                return mPendingBulkRequests.get(i);
            }
        }

        long requestId = generateRequestID();
        mPendingBulkRequests.add(since);

        ResultReceiver serviceCallback = new ResultReceiver(null){

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                Log.d(TAG, "onReceiveResult() called with: resultCode = [" + resultCode + "], resultData = [" + resultData + "]");
                resultData.putInt(sBulkUsersExtra, since);
                handleGetBulkUsers(context, resultCode, resultData);
            }

        };

        Intent intent = new Intent(context, FetchUsersJobIntentService.class);
        intent.putExtra(RESOURCE_TYPE_EXTRA, RESOURCE_TYPE_BULK_USERS);
        intent.putExtra(SERVICE_CALLBACK, serviceCallback);
        intent.putExtra(REQUEST_ID, requestId);
        intent.putExtra(PARAM_BULK_USERS_EXTRA, since);
        intent.setAction(ACTION_FETCH_BULK_USERS);

        FetchUsersJobIntentService.enqueueWork(context, intent);

        return requestId;
    }

    private static long generateRequestID() { return UUID.randomUUID().getLeastSignificantBits(); }

    private static boolean isRequestPending(long requestId) { return mPendingConcreteRequests.containsValue(requestId); }

    private static void handleGetBulkUsers(Context context, int resultCode, Bundle resultData) {
        Log.d(TAG, "ServiceHelper.handleGetBulkUsers() called with: resultCode = [" + resultCode + "], resultData = [" + resultData + "]");
        Intent origIntent = resultData.getParcelable(ORIGINAL_INTENT_EXTRA);
        int since = resultData.getInt(sBulkUsersExtra);

        if(origIntent != null) {
            long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

            mPendingBulkRequests.remove(Integer.valueOf(since));

            Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
            resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
            resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);

            context.sendBroadcast(resultBroadcast);

        }
    }

    private static void handleGetSingleUser(Context context, int resultCode, Bundle resultData){
        Log.d(TAG, "handleGetSingleUser() called with: resultCode = [" + resultCode + "], resultData = [" + resultData + "]");
        Intent origIntent = resultData.getParcelable(ORIGINAL_INTENT_EXTRA);
        String login = resultData.getString(sSingleUserExtra);

        if(origIntent != null) {
            long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

            mPendingConcreteRequests.remove(login);

            Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
            resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
            resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);

            context.sendBroadcast(resultBroadcast);
        }
    }

}
