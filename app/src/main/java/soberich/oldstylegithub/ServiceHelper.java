package soberich.oldstylegithub;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static soberich.oldstylegithub.FetchUsersJobIntentService.*;

/**
 *
 * Created by soberich on 9/28/17.
 */

final class ServiceHelper {

    //private ServiceHelper () {}

    public static void execute(Context context, int matchFromUriMatcher, String parameters) {
        //find the service resource (/path/to/remote/service with the match
        //start service with parameters

    }

    public static String ACTION_REQUEST_RESULT = "REQUEST_RESULT";
    public static String EXTRA_REQUEST_ID = "EXTRA_REQUEST_ID";
    public static String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";

    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String sBulkUsersExtra = "SINCE";
    private static final String sSingleUserExtra = "LOGIN";

    private static final Object lock = new Object();

    private static ServiceHelper sInstance;

    //TODO: refactor the key
    private Map<String,Long> mPendingConcreteRequests = new HashMap<>();
    private List<Integer> mPendingBulkRequests = new ArrayList<>(5);
    private Context mContext;

    private ServiceHelper(Context context){
        this.mContext = context.getApplicationContext();
    }

    public static ServiceHelper getInstance(Context ctx){
        synchronized (lock) {
            if(sInstance == null){
                sInstance = new ServiceHelper(ctx);
            }
        }

        return sInstance;
    }

    public long getSingleUser(final String login) {

        long requestId = generateRequestID();
        mPendingConcreteRequests.put(login, requestId);

        ResultReceiver serviceCallback = new ResultReceiver(null){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                resultData.putString(sSingleUserExtra, login);
                handleGetSingleUser(resultCode, resultData);
            }
        };

        Intent intent = new Intent(this.mContext, FetchUsersJobIntentService.class);
        intent.putExtra(RESOURCE_TYPE_EXTRA, RESOURCE_TYPE_SINGLE_USER);
        intent.putExtra(SERVICE_CALLBACK, serviceCallback);
        intent.putExtra(REQUEST_ID, requestId);
        intent.putExtra(PARAM_SINGLE_USER_EXTRA, login);
        intent.setAction(ACTION_FETCH_SINGLE_USER);

        FetchUsersJobIntentService.enqueueWork(mContext, intent);

        return requestId;
    }

    public long getBulkUsers(final int since){

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
                resultData.putInt(sBulkUsersExtra, since);
                handleGetBulkUsers(resultCode, resultData);
            }

        };

        Intent intent = new Intent(this.mContext, FetchUsersJobIntentService.class);
        intent.putExtra(RESOURCE_TYPE_EXTRA, RESOURCE_TYPE_BULK_USERS);
        intent.putExtra(SERVICE_CALLBACK, serviceCallback);
        intent.putExtra(REQUEST_ID, requestId);
        intent.putExtra(PARAM_BULK_USERS_EXTRA, since);
        intent.setAction(ACTION_FETCH_BULK_USERS);

        FetchUsersJobIntentService.enqueueWork(mContext, intent);

        return requestId;
    }

    private long generateRequestID() { return UUID.randomUUID().getLeastSignificantBits(); }

    public boolean isRequestPending(long requestId){ return mPendingConcreteRequests.containsValue(requestId); }

    private void handleGetBulkUsers(int resultCode, Bundle resultData) {

        Intent origIntent = resultData.getParcelable(ORIGINAL_INTENT_EXTRA);
        int since = resultData.getInt(sBulkUsersExtra);

        if(origIntent != null){
            long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

            mPendingBulkRequests.remove(Integer.valueOf(since));

            Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
            resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
            resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);

            mContext.sendBroadcast(resultBroadcast);

        }
    }

    private void handleGetSingleUser(int resultCode, Bundle resultData){

        Intent origIntent = resultData.getParcelable(ORIGINAL_INTENT_EXTRA);
        String login = resultData.getString(sSingleUserExtra);

        if(origIntent != null) {
            long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

            mPendingConcreteRequests.remove(login);

            Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
            resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
            resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);

            mContext.sendBroadcast(resultBroadcast);
        }
    }

}
