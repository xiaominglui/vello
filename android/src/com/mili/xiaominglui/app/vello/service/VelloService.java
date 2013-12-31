package com.mili.xiaominglui.app.vello.service;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.http.HttpStatus;

import wei.mark.standout.StandOutWindow;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.Board;
import com.mili.xiaominglui.app.vello.data.model.DirtyCard;
import com.mili.xiaominglui.app.vello.data.model.List;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestManager;
import com.mili.xiaominglui.app.vello.dialogs.ConnectionErrorDialogFragment.ConnectionErrorDialogListener;
import com.mili.xiaominglui.app.vello.ui.FloatDictCardWindow;
import com.mili.xiaominglui.app.vello.ui.MainActivity;
import com.mili.xiaominglui.app.vello.ui.SettingsActivity;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class VelloService extends Service implements RequestListener, ConnectionErrorDialogListener, OnPrimaryClipChangedListener {
	private static final String TAG = VelloService.class.getSimpleName();
	private static boolean isRunning = false;
	private NotificationManager mNM;
	private ClipboardManager mCM;
	private WindowManager mWM;
	private MonitorTask mTask = null;
	private String mLastFakeClipText = "";
	private SharedPreferences mPrefs;
	
	/**
     * Time interval of monitor checking in milliseconds
     * <p>TYPE: int</p>
     */
    public static final String KEY_MONITOR_INTERVAL = "monitor.interval";
    public static final int DEF_MONITOR_INTERVAL = 1000;

	HashMap<String, DirtyCard> mDirtyCards = new HashMap<String, DirtyCard>();
//	ArrayList<TrelloCard> mMergeCards = new ArrayList<TrelloCard>();
	HashMap<String, TrelloCard> mMergeCards = new HashMap<String, TrelloCard>();

	// Keeps track of all current registered clients.
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	int mValue = 0; // Holds last value set by a client.
	public static final int MSG_UNREGISTER_CLIENT = -1;
	public static final int MSG_REGISTER_CLIENT = 0;

	public static final int MSG_SPINNER_ON = 1;
	public static final int MSG_SPINNER_OFF = 2;
	public static final int MSG_DIALOG_BAD_DATA_ERROR_SHOW = 3;
	public static final int MSG_DIALOG_CONNECTION_ERROR_SHOW = 4;
	public static final int MSG_SHOW_RESULT_WORDCARD = 5;
	public static final int MSG_VALID_TRELLO_CONNECTION = 6;
	public static final int MSG_INVALID_TRELLO_CONNECTION = 7;
	public static final int MSG_SHOW_FLOAT_WINDOW = 8;
	
	public static final int MSG_STATUS_WEBHOOK_DELETED = 16;
	public static final int MSG_STATUS_WEBHOOK_CREATED = 17;
	
	public static final int MSG_STATUS_INIT_ACCOUNT_BEGIN = 50;
	public static final int MSG_STATUS_INIT_ACCOUNT_END = 51;
	public static final int MSG_STATUS_SYNC_BEGIN = 52;
	public static final int MSG_STATUS_SYNC_END = 53;
	public static final int MSG_STATUS_SYNC_BLANK = 54;
	public static final int MSG_STATUS_CONNECTION_TIMEOUT = 55;
	public static final int MSG_STATUS_REVOKE_BEGIN = 56;
	public static final int MSG_STATUS_REVOKE_END = 57;

	public static final int MSG_CHECK_VOCABULARY_BOARD = 100;
	public static final int MSG_GET_DUE_REVIEW_CARD_LIST = 101;
	public static final int MSG_CLOSE_WORDCARD = 104;
	public static final int MSG_SYNC_LOCAL_CACHE = 106;
	public static final int MSG_TRIGGER_QUERY_WORD = 107;
	public static final int MSG_CREATE_WEBHOOK = 108;
	public static final int MSG_DELETE_WEBHOOK = 109;
	public static final int MSG_CHECK_TRELLO_CONNECTION = 110;
	public static final int MSG_READ_TRELLO_ACCOUNT_INFO = 111;
	public static final int MSG_RETURN_TRELLO_USERNAME = 112;
	public static final int MSG_SHUTDOWN_CLIPBOARD_MONITOR = 113;
	
	public static final int REQ_DATA_CHANGED = 200;

	final Messenger mMessenger = new Messenger(new IncomingHandler(this));

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
		private final WeakReference<VelloService> mService;

		IncomingHandler(VelloService service) {
			mService = new WeakReference<VelloService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			VelloService service = mService.get();
			if (service != null) {
				switch (msg.what) {
				case MSG_REGISTER_CLIENT:
					service.mClients.add(msg.replyTo);
					break;
				case MSG_UNREGISTER_CLIENT:
					service.mClients.remove(msg.replyTo);
					break;
				case MSG_CHECK_VOCABULARY_BOARD:
					service.checkVocabularyBoard();
					break;
				case MSG_GET_DUE_REVIEW_CARD_LIST:
					int startId = msg.arg1;
					service.getDueReviewCardList(startId);
					break;
				case MSG_TRIGGER_QUERY_WORD:
					String fakedClipText = (String) msg.obj;
					if (!service.mLastFakeClipText.equals("")) {
						service.queryInRemoteStorage(fakedClipText.trim().toLowerCase());
					}
					service.mLastFakeClipText = fakedClipText;
				    break;
				case MSG_DELETE_WEBHOOK:
					service.deleteWebHook();
					break;
				case MSG_CREATE_WEBHOOK:
					service.createWebHook();
					break;
				case MSG_CHECK_TRELLO_CONNECTION:
					service.checkTrelloConnection();
					break;
				case MSG_READ_TRELLO_ACCOUNT_INFO:
					String token = (String) msg.obj;
					service.readTrelloAccountInfo(token);
					break;
				case MSG_SHUTDOWN_CLIPBOARD_MONITOR:
					service.shutdownClipboardMonitor();
					break;
				case MSG_SHOW_FLOAT_WINDOW:
					break;
				default:
					super.handleMessage(msg);
				}
			}

		}
	}

	private void sendMessageToUI(int type, Object obj) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
			    Message msg = Message.obtain(null, type);
			    if (obj != null) {
			        msg.obj = obj;
			    }
				mClients.get(i).send(msg);
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	boolean monitor = intent.getBooleanExtra("monitor", false);
    	if (monitor) {
    		if (mTask == null) {
    			mTask = new MonitorTask();
    		}
    		mTask.start();
    	} else {
    		if (VelloConfig.DEBUG_SWITCH) {
        		Log.d(TAG, "command started---#" + startId);
        	}
        	Message m = Message.obtain(null, VelloService.MSG_GET_DUE_REVIEW_CARD_LIST);
        	m.arg1 = startId;
        	m.arg2 = flags;
        	m.obj = intent.getExtras();
        	try {
    			mMessenger.send(m);
    		} catch (RemoteException e) {
    			e.printStackTrace();
    		}
    	}
    	
		return monitor ? START_REDELIVER_INTENT : START_NOT_STICKY;
	}

	public static boolean isRunning() {
		return isRunning;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "VelloService Started.");
		mRequestManager = VelloRequestManager.from(this);
		mRequestList = new ArrayList<Request>();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mWM = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		mCM = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//		mCM.addPrimaryClipChangedListener(this);
		// https://code.google.com/p/android/issues/detail?id=58043
		// TODO add a option in setting
		
		isRunning = true;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
//		mCM.removePrimaryClipChangedListener(this);
		if (mTask != null && mTask.isRunning()) {
			mTask.cancel();
		}

		Log.i(TAG, "VelloService Stopped.");
		isRunning = false;
	}

	protected VelloRequestManager mRequestManager;
	protected ArrayList<Request> mRequestList;
	
	private void archiveWordCard(String idCard) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "archiveWordCard start...");
		}
		Request archiveWordCard = VelloRequestFactory
				.archiveWordCardRequest(idCard);
		mRequestManager.execute(archiveWordCard, this);
		mRequestList.add(archiveWordCard);
	}

	private void reviewedWordCard(String idCard, int position) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "reviewedWordCard start...");
		}
		Request reviewedWordCard = VelloRequestFactory.reviewedWordCardRequest(
				idCard, position);
		mRequestManager.execute(reviewedWordCard, this);
		mRequestList.add(reviewedWordCard);
	}

	private void checkVocabularyBoard() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "checkVocabularyBoard start...");
		}
		sendMessageToUI(VelloService.MSG_STATUS_INIT_ACCOUNT_BEGIN, null);

		Request checkVocabularyBoardRequest = VelloRequestFactory.checkVocabularyBoardRequest();
		mRequestManager.execute(checkVocabularyBoardRequest, this);
		mRequestList.add(checkVocabularyBoardRequest);
	}

	private void createVocabularyBoard() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "createVocabularyBoard start...");
		}
		Request createVocabularyBoardRequest = VelloRequestFactory
				.createVocabularyBoardRequest();
		mRequestManager.execute(createVocabularyBoardRequest, this);
		mRequestList.add(createVocabularyBoardRequest);
	}

	private void createVocabularyList(int position) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "createVocabularyList start...");
		}
		Request createVocabularyListRequest = VelloRequestFactory
				.createVocabularyListRequest(position);
		mRequestManager.execute(createVocabularyListRequest, this);
		mRequestList.add(createVocabularyListRequest);
	}

	private void reOpenVocabulayList(int position, String id) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "reOpenVocabulayList start...");
		}
		Request reOpenVocabularyListRequest = VelloRequestFactory
				.reOpenVocabularyListRequest(position, id);
		mRequestManager.execute(reOpenVocabularyListRequest, this);
		mRequestList.add(reOpenVocabularyListRequest);
	}

	private void checkVocabularyLists() {
		for (int i = 0; i < AccountUtils.VOCABULARY_LISTS_TITLE_ID.length; i++) {
			if (VelloConfig.DEBUG_SWITCH) {
				Log.d(TAG, "checkVocabularyLists start..." + i);
			}
			Request checkVocabularyListReqest = VelloRequestFactory.checkVocabularyListRequest(i);

			mRequestManager.execute(checkVocabularyListReqest, this);
			mRequestList.add(checkVocabularyListReqest);
		}

	}

	private void configureVocabularyBoard(String id) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "configureVocabularyBoard start...");
		}
		Request configureVocabularyBoardRequest = VelloRequestFactory
				.configureVocabularyBoardRequest(id);
		mRequestManager.execute(configureVocabularyBoardRequest, this);
		mRequestList.add(configureVocabularyBoardRequest);
	}

	private void getOpenTrelloCardList(int startId, boolean force) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "getOpenTrelloCardList with startId = " + startId + ", force = " + force + " start...");
		}

		Request getOpenTrelloCardList = VelloRequestFactory.getOpenTrelloCardListRequest(startId, force);
		mRequestManager.execute(getOpenTrelloCardList, this);
		mRequestList.add(getOpenTrelloCardList);

	}
	
	private void getDueReviewCardList(int startId) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "getDueReviewCardList with startId = " + startId + " start...");
		}
		sendMessageToUI(VelloService.MSG_STATUS_SYNC_BEGIN, null);
		// step 1: get open trello cards
		getOpenTrelloCardList(startId, false);
	}

	private void queryInRemoteStorage(String query) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "checkTrelloCardStatusRequest start...");
		}
		Request queryInRemoteStorage = VelloRequestFactory.queryInRemoteStorageRequest(query);
		mRequestManager.execute(queryInRemoteStorage, this);
		mRequestList.add(queryInRemoteStorage);
	}
	
	private void lookUpInDictionary(String keyword) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "look up in dictionary...");
		}
		Request lookUpInDictionary = VelloRequestFactory.lookUpInDictionaryRequest(keyword);
		mRequestManager.execute(lookUpInDictionary, this);
		mRequestList.add(lookUpInDictionary);
	}

	private void addWordCard(String keyword, String data) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "addWordCard start...");
		}
		Request addWordCard = VelloRequestFactory.addWordCardRequest(keyword, data);
		mRequestManager.execute(addWordCard, this);
		mRequestList.add(addWordCard);
	}

	private void initializeWordCard(String idCard) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "initializeWordCard start...");
		}
		Request initializeWordCard = VelloRequestFactory.initializeWordCardRequest(idCard);
		mRequestManager.execute(initializeWordCard, this);
		mRequestList.add(initializeWordCard);
	}

	private void reStartWordCard(String idCard) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "reStartWordCard start...");
		}
		Request reStartWordCard = VelloRequestFactory
				.reStartWordCardRequest(idCard);
		mRequestManager.execute(reStartWordCard, this);
		mRequestList.add(reStartWordCard);
	}
	
	private void createWebHook() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "createWebHook start...");
		}
		Request createWebHook = VelloRequestFactory.createWebHook();
		mRequestManager.execute(createWebHook, this);
		mRequestList.add(createWebHook);
	}
	
	private void deleteWebHook() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "deleteWebHook start...");
		}
		Request setWebHookActive = VelloRequestFactory.deleteWebHook();
		mRequestManager.execute(setWebHookActive, this);
		mRequestList.add(setWebHookActive);
	}
	
	private void checkTrelloConnection() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "checkTrelloConnection start...");
		}
		Request checkTrelloConnection = VelloRequestFactory.checkTrelloConnection();
		mRequestManager.execute(checkTrelloConnection, this);
		mRequestList.add(checkTrelloConnection);
	}
	
	private void readTrelloAccountInfo(String token) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "readTrelloAccountInfo start...");
		}
		Request readTrelloAccountInfo = VelloRequestFactory.readTrelloAccountInfo(token);
		mRequestManager.execute(readTrelloAccountInfo, this);
		mRequestList.add(readTrelloAccountInfo);
	}
	
	private void deleteRemoteTrelloCard(TrelloCard card, int startId) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "deleteRemoteTrelloCard start...");
		}
		Request deleteRemoteTrelloCard = VelloRequestFactory.deleteRemoteTrelloCard(card, startId);
		mRequestManager.execute(deleteRemoteTrelloCard, this);
		mRequestList.add(deleteRemoteTrelloCard);
	}
	
	private void updateRemoteTrelloCard(TrelloCard card, int startId) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "updateRemoteTrelloCard start...");
		}
		Request updateRemoteTrelloCard = VelloRequestFactory.updateRemoteTrelloCard(card, startId);
		mRequestManager.execute(updateRemoteTrelloCard, this);
		mRequestList.add(updateRemoteTrelloCard);
	}
	
	private void shutdownClipboardMonitor() {
		if (mTask != null && mTask.isRunning()) {
			mTask.cancel();
		}
	}

	private void showFloatDictCard(TrelloCard card) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "showFloatDictCard --- " + card.name);
		}
		Bundle data = new Bundle();
		data.putParcelable("changedData", card);
		StandOutWindow.show(getApplicationContext(), FloatDictCardWindow.class, StandOutWindow.DEFAULT_ID);
		StandOutWindow.sendData(getApplicationContext(), FloatDictCardWindow.class, StandOutWindow.DEFAULT_ID, REQ_DATA_CHANGED, data, StandOutWindow.class, StandOutWindow.DEFAULT_ID);
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				StandOutWindow.closeAll(getApplicationContext(), FloatDictCardWindow.class);
			}
		}, VelloConfig.FLOAT_DICT_CARD_DISMISS_TIME);
	}
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onRequestFinished(Request request, Bundle resultData) {
		if (mRequestList.contains(request)) {
			mRequestList.remove(request);

			switch (request.getRequestType()) {
			case VelloRequestFactory.REQUEST_TYPE_REVIEWED_WORDCARD:
				TrelloCard reviewedWordCard = resultData.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (reviewedWordCard != null) {
					// reviewed

				} else {
					// reviewed failed
					// do nothing at present
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "reviewedWordCard end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CHECK_VOCABULARY_BOARD:
				ArrayList<Board> boardList = resultData.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_BOARD_LIST);
				for (Board board : boardList) {
					if (board.name.equals(AccountUtils.getVocabularyBoardName(getApplicationContext()))
							&& board.desc.equals(AccountUtils.getVocabularyBoardVerification())) {
						// s - 0 find out vocabulary board, check the closed
						// flag.

						if (!board.closed.equals("true")) {
							// s - 0.0 vocabulary board is NOT well
							// configured.
							configureVocabularyBoard(board.id);
							return;
						} else {
							// s - 0.1 well configured vocabulary board,
							// save id
							AccountUtils.setVocabularyBoardId(getApplicationContext(), board.id);
							checkVocabularyLists();
							return;
						}
					}
				}

				// s - 1 NO vocabulary board found, need create one
				createVocabularyBoard();
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "check vocabulary board end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD:
				String boardId = request.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_BOARD_ID);
				if (resultData != null) {
					// configure board successfully & save it
					String id = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);
					AccountUtils.setVocabularyBoardId(getApplicationContext(), id);
					Log.d(TAG, "configure board successfully! vocabulary board id = " + id);

					// continue to check vocabulary list if not well formed
					checkVocabularyLists();
				} else {
					// configure board failed, try again
					configureVocabularyBoard(boardId);
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CHECK_VOCABULARY_LIST:
				ArrayList<List> listList = resultData.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_LIST);
				int position = request.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
				for (List list : listList) {
					if (list.name.equals(AccountUtils.VOCABULARY_LISTS_TITLE_ID[position])) {
						// get the vocabulary list[position]
						if (!list.closed.equals("false")) {
							// list[position] be closed unexpectedly
							reOpenVocabulayList(position, list.id);
							return;
						} else {
							// list[position]'s status ok, save listId
							AccountUtils.setVocabularyListId(getApplicationContext(), list.id, position);
							if (AccountUtils.isVocabularyBoardWellFormed(getApplicationContext())) {
								sendMessageToUI(VelloService.MSG_STATUS_INIT_ACCOUNT_END, null);
							}
							return;
						}
					}
				}
				
				// no vocabulary list found
				createVocabularyList(position);
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "check vocabulary list end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_REOPEN_VOCABULARY_LIST:
				int positonList = request
						.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
				String idList = request
						.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
				if (resultData != null) {
					// reopen list successfully
					String id = resultData
							.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
					int pos = resultData
							.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
					AccountUtils.setVocabularyListId(getApplicationContext(), id, pos);
				} else {
					// reopen failed, try again
					reOpenVocabulayList(positonList, idList);
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_LIST:
				int positionList = request
						.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
				if (resultData != null) {
					// create list successfully
					String id = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
					int pos = request.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
					AccountUtils.setVocabularyListId(getApplicationContext(), id, pos);
				} else {
					// create list failed, try again
					createVocabularyList(positionList);
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "create vocabulary list end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_BOARD:
				if (resultData != null) {
					// create vocabulary board successfully
					String id = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);

					configureVocabularyBoard(id);
				} else {
					// create vocabulary board failed, try again
					createVocabularyBoard();
				}
				return;
			case VelloRequestFactory.REQUEST_TYPE_LOOK_UP_IN_DICTIONARY:
			    String keywordInQuery = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
				String wsResponse = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_DICTIONARY_WS_RESPONSE);
				if (!wsResponse.equals("null\n")) {
				    addWordCard(keywordInQuery, wsResponse);
				} else {
				    // no result in dictionary server. TODO
				}
				
				return;

			case VelloRequestFactory.REQUEST_TYPE_QUERY_IN_REMOTE_STORAGE:
				ArrayList<TrelloCard> existedWordCardList = resultData.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_CARD_LIST);
				String keyword = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
				if (existedWordCardList.isEmpty()) {
					// not exist in remote storage
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "not exist in remote storage");
					}
					// query in dictionary service
					lookUpInDictionary(keyword);
				} else {
					// check more to decide
					// filter to find the right one
					for (TrelloCard w : existedWordCardList) {
						if (w.name.equals(keyword)) {
							// got the right word card
							// show with user first
//							sendMessageToUI(MSG_SHOW_RESULT_WORDCARD, w);
							showFloatDictCard(w);

							if (w.closed.equals("true")) {
								// the existed word card has be closed,
								// re-open it.
								if (VelloConfig.DEBUG_SWITCH) {
									Log.d(TAG, "re-start existed word card.");
								}
								reStartWordCard(w.id);
							} else {
								if (w.due.equals("null")) {
									// the existed word card has not be
									// initialized, initialize it. this is the
									// double check
									if (VelloConfig.DEBUG_SWITCH) {
										Log.d(TAG, "initialize existed word card.");
									}
									initializeWordCard(w.id);
								} else {
									// the existed word is in review
									// process, do
									// nothing at present.
									if (VelloConfig.DEBUG_SWITCH) {
										Log.d(TAG, "the existed word is in review.");
									}
								}
							}
							return;
						}
					}
					// not exist in remote storage
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "not exist in remote storage");
					}
					// query in dictionary service
					lookUpInDictionary(keyword);
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_ADD_WORDCARD:
			    String keywordInAddWordCard = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
			    String wsResult = request.getString(VelloRequestFactory.PARAM_EXTRA_DICTIONARY_WS_RESULT);
				TrelloCard addedWordCard = resultData.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (addedWordCard != null) {
					initializeWordCard(addedWordCard.id);
				} else {
					// add failed, add again
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "retry addWordCard...");
					}
				    addWordCard(keywordInAddWordCard, wsResult);
				}

				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "addWordCard end.");
				}

				return;

			case VelloRequestFactory.REQUEST_TYPE_RESTART_WORDCARD:
				TrelloCard restartedWordCard = resultData.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (restartedWordCard != null) {
					// restarted
					// do nothing at present.
				} else {
					// restart failed
					// do nothing at present.
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "reStartzeWordCard end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_INITIALIZE_WORDCARD:
				TrelloCard initializedWordCard = resultData.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (initializedWordCard != null) {
					showFloatDictCard(initializedWordCard);
				} else {
					// initialized failed
					// do nothing at present
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "initializeWordCard end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_GET_OPEN_TRELLO_CARD_LIST:
				int startId = request.getInt(VelloRequestFactory.PARAM_EXTRA_SERVICE_START_ID);
				boolean force = request.getBoolean(VelloRequestFactory.PARAM_EXTRA_FORCE_GET_OPEN_TRELLO_CARD);
				ArrayList<TrelloCard> OpenTrelloCardList = resultData.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_CARD_LIST);

				if (OpenTrelloCardList.size() > 0) {
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "has open trello card, with startId = " + startId + ", force = " + force);
					}
					final ContentResolver resolver = getContentResolver();
					if (!force) {
						// query and backup all local items that syncInNext=true
						// or merge
						// locally later
						ProviderCriteria criteria = new ProviderCriteria();
						criteria.addNe(DbWordCard.Columns.DATE_LAST_OPERATION, "");
						Cursor c = resolver.query(DbWordCard.CONTENT_URI,
								DbWordCard.PROJECTION,
								criteria.getWhereClause(),
								criteria.getWhereParams(),
								criteria.getOrderClause());
						if (c != null) {
							while (c.moveToNext()) {
								DirtyCard dc = new DirtyCard(c);
								mDirtyCards.put(dc.id, dc);
							}
							c.close();
						}
					}

					if (force || !(mDirtyCards.size() > 0)) {
						// no dirty, commit to local DB directly
						if (VelloConfig.DEBUG_SWITCH) {
							Log.d(TAG, "no dirty card, commit to local DB");
						}
						try {
							resolver.delete(DbWordCard.CONTENT_URI, null, null);

							ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
							for (TrelloCard trelloCard : OpenTrelloCardList) {
								operationList.add(ContentProviderOperation
										.newInsert(DbWordCard.CONTENT_URI)
										.withValues(trelloCard.toContentValues())
										.build());
							}
							resolver.applyBatch(VelloProvider.AUTHORITY, operationList);

							// show notification
							Intent intent = new Intent(getApplicationContext(), MainActivity.class);
							PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

							ProviderCriteria cri = new ProviderCriteria();
							cri.addSortOrder(DbWordCard.Columns.DUE, true);
							Calendar rightNow = Calendar.getInstance();

							SimpleDateFormat fo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
							long rightNowUnixTime = rightNow.getTimeInMillis();
							long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
							String now = fo.format(new Date(rightNowUnixTimeGMT));
							cri.addEq(DbWordCard.Columns.MARKDELETED, "false");
							cri.addLt(DbWordCard.Columns.DUE, now, true);
							Cursor cur = getContentResolver().query(
									DbWordCard.CONTENT_URI,
									DbWordCard.PROJECTION,
									cri.getWhereClause(),
									cri.getWhereParams(),
									cri.getOrderClause());
							if (cur != null) {
								int num = cur.getCount();
								if (num > 0) {
									Resources res = getApplicationContext().getResources();
									String stringContentTitle = res.getQuantityString(R.plurals.notif_content_title, num, num);
									String stringContentText = res.getString(R.string.notif_content_text);
									Notification noti = new NotificationCompat.Builder(getApplicationContext())
											.setContentTitle(stringContentTitle)
											.setContentText(stringContentText)
											.setSmallIcon(R.drawable.ic_launcher)
											.setContentIntent(pIntent)
											.build();


									// Hide the notification after its selected
									noti.flags |= Notification.FLAG_AUTO_CANCEL;

									mNM.notify(0, noti);
									sendMessageToUI(VelloService.MSG_STATUS_SYNC_END, null);
								} else {
									// no word need recalling
									sendMessageToUI(MSG_STATUS_SYNC_BLANK, null);
								}
								cur.close();
							}

							if (VelloConfig.DEBUG_SWITCH) {
								Log.d(TAG, "...stop command---#" + startId);
							}

							stopSelf(startId);
						} catch (RemoteException e) {
							e.printStackTrace();
						} catch (OperationApplicationException e) {
							e.printStackTrace();
						}
					}

					if (mDirtyCards.size() > 0 && !force) {
						if (VelloConfig.DEBUG_SWITCH) {
							Log.d(TAG, "dirty card found: " + mDirtyCards.size());
						}
						// maybe need merging
						for (TrelloCard tCard : OpenTrelloCardList) {
							
							if (mDirtyCards.containsKey(tCard.id)) {
								// need merging
								mMergeCards.put(tCard.id, tCard);
							}
						}

						if (VelloConfig.DEBUG_SWITCH) {
							Log.d(TAG, "merge cards num=" + mMergeCards.size());
						}

						for (TrelloCard tCard : mMergeCards.values()) {
							if (VelloConfig.DEBUG_SWITCH) {
								Log.d(TAG, "to merge card --- " + tCard.id);
							}
							DirtyCard dCard = mDirtyCards.get(tCard.id);
							if (dCard.markDeleted.equals("true")) {
								// delete remote Trello card
								deleteRemoteTrelloCard(tCard, startId);
							} else if (dCard.dateLastActivity.equals(tCard.dateLastActivity)) {
								// remote has no commit
								// commit local due, closed, listId to
								// Trello
								updateRemoteTrelloCard(upgradeTrelloCard(tCard, dCard, false), startId);
							} else {
								// remote has commit
								// update remote data based on local data
								updateRemoteTrelloCard(upgradeTrelloCard(tCard, dCard, true), startId);
							}
						}
					}
				} else {
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "has no open trello card");
					}
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_ARCHIVE_WORDCARD:
				TrelloCard closedWordCard = resultData
						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				String idCard = request
						.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_CARD_ID);
				if (closedWordCard != null
						&& closedWordCard.closed.equals("true")) {
					// success, do nothing
				} else {
					// retry
					archiveWordCard(idCard);
				}
				return;
				
			case VelloRequestFactory.REQUEST_TYPE_CREATE_WEBHOOK:
				String hookId = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_WEBHOOK_ID);
				if (hookId != null) {
					// hook created, save it
					AccountUtils.setVocabularyBoardWebHookId(getApplicationContext(), hookId);
					sendMessageToUI(MSG_STATUS_WEBHOOK_CREATED, null);
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "webhook created.");
					}
					
				} else {
					// to create again
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "retry --- createWebHook");
					}
					createWebHook();
				}
				return;
				
			case VelloRequestFactory.REQUEST_TYPE_DELETE_WEBHOOK:
				boolean hasWebhookDeleted = resultData.getBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_MODEL_DELETED);
				if (hasWebhookDeleted) {
					// request success
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "deleteWebHook success");
					}
					sendMessageToUI(VelloService.MSG_STATUS_WEBHOOK_DELETED, null);
				} else {
					// request fail, try again
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "retry --- deleteWebHook");
					}
					deleteWebHook();
				}
				return;
				
			case VelloRequestFactory.REQUEST_TYPE_CHECK_TRELLO_CONNECTION:
				boolean hasTrelloConnection = resultData.getBoolean(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_CONNECTION);
				if (hasTrelloConnection) {
					// valid connection
					sendMessageToUI(VelloService.MSG_VALID_TRELLO_CONNECTION, null);
				} else {
					// invalid connection
					sendMessageToUI(VelloService.MSG_INVALID_TRELLO_CONNECTION, null);
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "CheckTrelloConnection end...");
				}
				return;
			case VelloRequestFactory.REQUEST_TYPE_READ_TRELLO_ACCOUNT_INFO:
				String username = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_ACCOUNT_USERNAME);
				String token = request.getString(VelloRequestFactory.PARAM_EXTRA_TRELLO_ACCESS_TOKEN);
				if (username != null && !username.isEmpty()) {
					sendMessageToUI(VelloService.MSG_RETURN_TRELLO_USERNAME, username);
				} else {
					// failed, retry
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "retry --- readTrelloAccountInfo");
					}
					readTrelloAccountInfo(token);
				}
				return;
				
			case VelloRequestFactory.REQUEST_TYPE_DELETE_REMOTE_TRELLO_CARD:
				boolean deleted = resultData.getBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_MODEL_DELETED);
				TrelloCard deletingCard = (TrelloCard) request.getParcelable(VelloRequestFactory.PARAM_EXTRA_TRELLO_CARD);
				int startIdTriggerDelete = request.getInt(VelloRequestFactory.PARAM_EXTRA_SERVICE_START_ID);
				if (deleted) {
					mMergeCards.remove(deletingCard.id);
				} else {
					// retry
					deleteRemoteTrelloCard(deletingCard, startIdTriggerDelete);
				}
				
				if (mMergeCards.isEmpty()) {
					// merge ok, go clean full sync
					getOpenTrelloCardList(startIdTriggerDelete, true);
				}
				
			case VelloRequestFactory.REQUEST_TYPE_UPDATE_REMOTE_TRELLO_CARD:
				boolean updated = resultData.getBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_TRELLO_CARD_UPDATED);
				TrelloCard updatingCard = (TrelloCard) request.getParcelable(VelloRequestFactory.PARAM_EXTRA_TRELLO_CARD);
				int startIdTriggerUpdate = request.getInt(VelloRequestFactory.PARAM_EXTRA_SERVICE_START_ID);
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "updateRemoteTrelloCard returned, with updated=" + updated);
				}

				if (updated) {
					mMergeCards.remove(updatingCard.id);
				} else {
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "retry updateRemoteTrelloCard");
					}
					updateRemoteTrelloCard(updatingCard, startIdTriggerUpdate);
				}
				
				if (mMergeCards.isEmpty()) {
					// merge ok, go clean full sync
					getOpenTrelloCardList(startIdTriggerUpdate, true);
				}
				
			default:
				return;
			}
		}
	}

	@Override
	public void onRequestConnectionError(Request request, int statusCode) {
		if (mRequestList.contains(request)) {
			if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
				if (request.getRequestType() == VelloRequestFactory.REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD) {
					String boardId = request.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_BOARD_ID);
					AccountUtils.setVocabularyBoardId(getApplicationContext(), boardId);
					Log.d(TAG, "get a board without ownership, skip configuration! vocabulary board id = " + boardId);

					// continue to check vocabulary list if not well formed
					checkVocabularyLists();
				} else if (request.getRequestType() == VelloRequestFactory.REQUEST_TYPE_GET_OPEN_TRELLO_CARD_LIST) {
					// force logout and re-login
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "invalid token");
					}
					AccountUtils.signOut(getApplicationContext());
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					startActivity(intent);
				}
				
			} else if (statusCode == -1) {
				if (request.getRequestType() == VelloRequestFactory.REQUEST_TYPE_CHECK_TRELLO_CONNECTION) {
					sendMessageToUI(VelloService.MSG_INVALID_TRELLO_CONNECTION, null);
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "CheckTrelloConnection end...");
					}
				} else {
					sendMessageToUI(VelloService.MSG_STATUS_CONNECTION_TIMEOUT, null);
				}
			}
			Log.d(TAG, "type=" + request.getRequestType() + "; status code=" + statusCode);
			mRequestList.remove(request);
		}
	}

	@Override
	public void onRequestDataError(Request request) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "onRequestDataError --- " + "type=" + request.getRequestType());
		}
		if (mRequestList.contains(request)) {
			mRequestList.remove(request);
		}
	}

	@Override
	public void onRequestCustomError(Request request, Bundle resultData) {
		// Never called.

	}

	@Override
	public void connectionErrorDialogCancel(Request request) {
	}

	@Override
	public void connectionErrorDialogRetry(Request request) {
		mRequestManager.execute(request, this);
		mRequestList.add(request);
	}
	
	/**
	 * merge wordcard when remote wordcard is NOT the same state
	 * @param wordCard remote wordcard
	 * @param dirtyWordCard local changed wordcard
	 * @return new wordcard merged
	 */
	@SuppressLint("SimpleDateFormat")
	private TrelloCard upgradeTrelloCard(TrelloCard trelloCard, DirtyCard dirtyCard, boolean forcePush) {
		if (forcePush) {
			int localPositionInLists = AccountUtils.getVocabularyListPosition(getApplicationContext(), dirtyCard.idList);
			int remotePostionInLists = AccountUtils.getVocabularyListPosition(getApplicationContext(), trelloCard.idList);
			int delta = remotePostionInLists - localPositionInLists;
			if (delta > 0) {
				int compensation = delta + 1;
				if (remotePostionInLists + compensation > VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
					trelloCard.closed = "true";
				} else {
					int newPostionInLists = remotePostionInLists + compensation;
					trelloCard.idList = AccountUtils.getVocabularyListId(getApplicationContext(), newPostionInLists);
					Calendar rightNow = Calendar.getInstance();
					long rightNowUnixTime = rightNow.getTimeInMillis();
					long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
					long deltaTime = VelloConfig.VOCABULARY_LIST_DUE_DELTA[newPostionInLists];
					long newDueUnixTime = rightNowUnixTimeGMT + deltaTime;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					Date newDueDate = new Date(newDueUnixTime);
					String stringNewDueDate = format.format(newDueDate);
					trelloCard.due = stringNewDueDate;
				}
			}
		} else {
			trelloCard.idList = dirtyCard.idList;
			trelloCard.due = dirtyCard.due;
			trelloCard.closed = dirtyCard.closed;
		}
		
		return trelloCard;
	}
	
	private void showNotification() {
        Notification notif = new Notification(R.drawable.ic_stat_vaa, "VAA clipboard monitor is started", System.currentTimeMillis());
        notif.flags |= (Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        notif.setLatestEventInfo(this, getText(R.string.notif_clip_monitor_service), "Tap here to enter VAA UI", contentIntent);
        // Use layout id because it's unique
        mNM.notify(R.string.notif_clip_monitor_service, notif);
    }
	
	/**
     * Monitor task: monitor new text clips in global system clipboard
     */
    private class MonitorTask extends Thread {

        private volatile boolean mKeepRunning = false;
        
        public MonitorTask() {
            super("ClipboardMonitor");
        }

        public boolean isRunning() {
        	return mKeepRunning;
        }

        /** Cancel task */
        public void cancel() {
			mNM.cancel(R.string.notif_clip_monitor_service);
            mKeepRunning = false;
            interrupt();
        }

        @Override
        public void run() {
			showNotification();
            mKeepRunning = true;
            while (true) {
				if (mCM.hasText()) {
					String clipText = mCM.getText().toString();
					if (!clipText.equals(mLastFakeClipText)) {
						FakingTask();
					}
				}

                try {
                    Thread.sleep(mPrefs.getInt(KEY_MONITOR_INTERVAL, DEF_MONITOR_INTERVAL));
                } catch (InterruptedException ignored) {
                }
                if (!mKeepRunning) {
                    break;
                }
            }
        }
        
		private void FakingTask() {
			try {
				String queryClipText = mCM.getText().toString();
				String fakedClipText = queryClipText + " ";
				mCM.setText(fakedClipText);
				Message msg = Message.obtain(null, MSG_TRIGGER_QUERY_WORD, fakedClipText);
				mMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    }

	@Override
	public void onPrimaryClipChanged() {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		String pasteData = "";
		ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
		pasteData = item.getText().toString();
		Toast.makeText(getApplicationContext(), "clipboard changed --- pasteData=" + pasteData, Toast.LENGTH_SHORT).show();
	}
}
