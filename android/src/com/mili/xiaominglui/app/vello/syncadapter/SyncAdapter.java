package com.mili.xiaominglui.app.vello.syncadapter;

import android.accounts.Account;
import android.app.NotificationManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.io.IOException;
import java.util.regex.Pattern;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

	private static final String TAG = SyncAdapter.class.getSimpleName();
	private static final Pattern sSanitizeAccountNamePattern = Pattern.compile("(.).*?(.?)@");

	private final Context mContext;
	String mToken;
	private SyncHelper mSyncHelper;

	Messenger mService = null;
	private boolean mIsBound;
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mService = new Messenger(service);
			// Tell the user about this for our demo.
			Toast.makeText(mContext, R.string.local_service_connected,
					Toast.LENGTH_SHORT).show();

			try {
				Message msg = Message.obtain(null,
						VelloService.MSG_REGISTER_CLIENT);
				// msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do
				// anything with it
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
			mService = null;
			Toast.makeText(mContext, R.string.local_service_disconnected,
					Toast.LENGTH_SHORT).show();
		}
	};

	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContext = context;
		
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {

		final String logSanitizedAccountName = sSanitizeAccountNamePattern
                .matcher(account.name).replaceAll("$1...$2@");
		String chosenAccountName = AccountUtils.getChosenAccountName(mContext);
		boolean isAccountSet = !TextUtils.isEmpty(chosenAccountName);
		boolean isChosenAccount = isAccountSet && chosenAccountName.equals(account.name);
		if (isAccountSet) {
            ContentResolver.setIsSyncable(account, authority, isChosenAccount ? 1 : 0);
        }
		if (!isChosenAccount) {
            Log.d(TAG, "Tried to sync account " + logSanitizedAccountName + " but the chosen " +
                    "account is actually " + chosenAccountName);
            ++syncResult.stats.numAuthExceptions;
            return;
        }
		// sync notification
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				mContext)
				.setSmallIcon(R.drawable.ic_stat_vaa)
				.setContentTitle(
						mContext.getText(R.string.notif_sync_content_title))
				.setTicker(mContext.getText(R.string.notif_sync_ticker))
				.setProgress(0, 0, true).setOngoing(true).setAutoCancel(false);
		notificationManager.notify(1, builder.build());
		// Perform a sync using SyncHelper
		if (mSyncHelper == null) {
			mSyncHelper = new SyncHelper(mContext);
		}

		try {
			mSyncHelper.performSync(syncResult, SyncHelper.FLAG_SYNC_REMOTE);

		} catch (IOException e) {
			++syncResult.stats.numIoExceptions;
		}

		notificationManager.cancel(1);
	}

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		mContext.bindService(new Intent(mContext, VelloService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			mContext.unbindService(mConnection);
			mIsBound = false;
		}
	}
}
