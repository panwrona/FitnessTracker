package com.panwrona.wear;

import android.app.Activity;
import android.content.IntentSender;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.activeandroid.ActiveAndroid;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.panwrona.wear.data.model.Training;
import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity
	implements SensorEventListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private SensorManager mSensorManager;
	private Sensor mHeartRateSensor;

	private TextView mTextView;

	private Button mStartBtn, mStopBtn;
	private List<Training> trainings = new ArrayList<>();
	private long timeStart = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
		mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
			stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
				@Override
				public void onLayoutInflated(WatchViewStub stub) {
					mTextView = (TextView) stub.findViewById(R.id.text);
					mStartBtn = (Button) stub.findViewById(R.id.start);
					mStopBtn = (Button) stub.findViewById(R.id.stop);
					mStartBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							registerListener();
						}
					});
					mStopBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							unregisterListener();
						}
					});
				}
			});
	}

	private void registerListener() {
		mSensorManager.registerListener(this, this.mHeartRateSensor,
			SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void unregisterListener() {
		mSensorManager.unregisterListener(this, this.mHeartRateSensor);
		timeStart = 0;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.d(TAG, "Time: "
			+ event.timestamp
			+ ", Accuracy: "
			+ event.accuracy
			+ ", Value: "
			+ event.values[0]);
		mTextView.setText("Time: "
			+ event.timestamp
			+ ", Accuracy: "
			+ event.accuracy
			+ ", Value: "
			+ event.values[0]);

		if(timeStart == 0) {
			timeStart = event.timestamp;
		}
		trainings.add(new Training(event.accuracy, timeStart, event.timestamp, event.values[0]));
		timeStart = event.timestamp;
		if(trainings.size() == 50) {
			ActiveAndroid.beginTransaction();
			try {
				for (int i = 0; i < trainings.size(); i++) {
					trainings.get(i).save();
				}
				ActiveAndroid.setTransactionSuccessful();
			} finally {
					ActiveAndroid.endTransaction();
				}
			trainings.clear();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	//implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
	//MessageApi.MessageListener {
	//private static final String TAG = MainActivity.class.getSimpleName();
	//private final String LOG_TAG = MainActivity.class.getSimpleName();
	//
	////API Variables
	//private GoogleApiClient mClient;
	//private GoogleApiClient mFitnessClient;
	//private boolean mResolvingError = false;
	//private static final int REQUEST_RESOLVE_ERROR = 1001;
	//public static final String START_ACTIVITY_PATH = "/start/MainActivity";
	//public static final String CONNECT_FITNESS = "/connect/fitness";
	////EndAPI
	//
	//OnDataPointListener mListener;
	//
	//private TextView mTextView;
	//private Button mSendMsg;
	//
	//@Override
	//protected void onCreate(Bundle savedInstanceState) {
	//	super.onCreate(savedInstanceState);
	//	setContentView(R.layout.activity_main);
	//
	//	mClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
	//		.addApi(Wearable.API)
	//		.addOnConnectionFailedListener(this)
	//		.build();
	//
	//	final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
	//	stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
	//		@Override
	//		public void onLayoutInflated(WatchViewStub stub) {
	//			mTextView = (TextView) stub.findViewById(R.id.text);
	//			mSendMsg = (Button) stub.findViewById(R.id.fitnessID);
	//			mSendMsg.setOnClickListener(new View.OnClickListener() {
	//				@Override
	//				public void onClick(View v) {
	//					new ConnectFitnessTask().execute();
	//				}
	//			});
	//		}
	//	});
	//}
	//
	//@Override
	//public void onConnected(Bundle bundle) {
	//	Log.d(LOG_TAG, "Connected");
	//	//Can now use WearableAPI
	//	Wearable.MessageApi.addListener(mClient, this);
	//}
	//
	//@Override
	//public void onConnectionSuspended(int i) {
	//	Log.d(LOG_TAG, "Connection suspended");
	//}
	//
	//@Override
	//public void onConnectionFailed(ConnectionResult result) {
	//	Log.d(LOG_TAG, "Connect Failed");
	//
	//	if (mResolvingError) {
	//		//currently resolving an error
	//		return;
	//	} else if (result.hasResolution()) {
	//		try {
	//			mResolvingError = true;
	//			result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
	//		} catch (IntentSender.SendIntentException e) {
	//			//Error with resolution intent. Try again
	//			mClient.connect();
	//		}
	//	} else {
	//		//no resolution
	//		//display Error dialog
	//		GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
	//			REQUEST_RESOLVE_ERROR);
	//	}
	//}
	//
	//@Override
	//protected void onStart() {
	//	super.onStart();
	//	if (!mResolvingError) {
	//		mClient.connect();
	//	}
	//}
	//
	//@Override
	//protected void onStop() {
	//	super.onStop();
	//	Wearable.MessageApi.removeListener(mClient, this);
	//}
	//
	//@Override
	//public void onMessageReceived(MessageEvent messageEvent) {
	//	if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
	//		updateText();
	//		mFitnessClient = new GoogleApiClient.Builder(this).addApi(Fitness.SENSORS_API)
	//			.addScope(Fitness.SCOPE_ACTIVITY_READ)
	//			.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
	//				@Override
	//				public void onConnected(Bundle bundle) {
	//					Log.d(LOG_TAG, "Connected to fitness API");
	//				}
	//
	//				@Override
	//				public void onConnectionSuspended(int i) {
	//
	//				}
	//			})
	//			.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
	//				@Override
	//				public void onConnectionFailed(ConnectionResult connectionResult) {
	//					Log.d(LOG_TAG, "Connection failed: " + connectionResult.getErrorCode());
	//				}
	//			})
	//			.build();
	//		mFitnessClient.connect();
	//	}
	//}
	//
	//private void initFitnessApi() {
	//	Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
	//		// At least one datatype must be specified.
	//		.setDataTypes(DataType.TYPE_HEART_RATE_BPM)
	//			// Can specify whether data type is raw or derived.
	//		.setDataSourceTypes(DataSource.TYPE_RAW).build())
	//		.setResultCallback(new ResultCallback<DataSourcesResult>() {
	//			@Override
	//			public void onResult(DataSourcesResult dataSourcesResult) {
	//				mTextView.setText("Result: " + dataSourcesResult.getStatus().toString());
	//				for (DataSource dataSource : dataSourcesResult.getDataSources()) {
	//					Log.d(TAG, "Data source found: " + dataSource.toString());
	//					Log.d(TAG, "Data Source type: " + dataSource.getDataType().getName());
	//
	//					//Let's register a listener to receive Activity data!
	//					if (dataSource.getDataType().equals(DataType.TYPE_HEART_RATE_BPM)
	//						&& mListener == null) {
	//						Log.d(TAG, "Data source for LOCATION_SAMPLE found!  Registering.");
	//						registerFitnessDataListener(dataSource, DataType.TYPE_HEART_RATE_BPM);
	//					}
	//				}
	//			}
	//		});
	//}
	//
	//private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
	//	// [START register_data_listener]
	//	mListener = new OnDataPointListener() {
	//		@Override
	//		public void onDataPoint(DataPoint dataPoint) {
	//			for (Field field : dataPoint.getDataType().getFields()) {
	//				Value val = dataPoint.getValue(field);
	//				Log.d(TAG, "Detected DataPoint field: " + field.getName());
	//				Log.d(TAG, "Detected DataPoint value: " + val);
	//			}
	//		}
	//	};
	//
	//	Fitness.SensorsApi.add(
	//		mClient,
	//		new SensorRequest.Builder()
	//			.setDataSource(dataSource) // Optional but recommended for custom data sets.
	//			.setDataType(dataType) // Can't be omitted.
	//			.setSamplingRate(10, TimeUnit.SECONDS)
	//			.build(),
	//		mListener)
	//		.setResultCallback(new ResultCallback<Status>() {
	//			@Override
	//			public void onResult(Status status) {
	//				if (status.isSuccess()) {
	//					Log.d(TAG, "Listener registered!");
	//				} else {
	//					Log.d(TAG, "Listener not registered.");
	//				}
	//			}
	//		});
	//}
	//
	//private void updateText() {
	//	runOnUiThread(new Runnable() {
	//		@Override
	//		public void run() {
	//			Toast.makeText(MainActivity.this, "Msg received", Toast.LENGTH_SHORT).show();
	//		}
	//	});
	//}
	//
	////Send Msgs
	//private void sendHandheldFitnessPrompt(String nodeId) {
	//	Wearable.MessageApi.sendMessage(mClient, nodeId, CONNECT_FITNESS, new byte[0])
	//		.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
	//			@Override
	//			public void onResult(MessageApi.SendMessageResult sendMessageResult) {
	//				if (!sendMessageResult.getStatus().isSuccess()) {
	//					Log.d(LOG_TAG,
	//						"Failed to send msg, status code: " + sendMessageResult.getStatus()
	//							.getStatusCode());
	//				}
	//			}
	//		});
	//}
	//
	//private Collection<String> getNodes() {
	//	HashSet<String> results = new HashSet<String>();
	//	NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mClient).await();
	//	for (Node node : nodes.getNodes()) {
	//		results.add(node.getId());
	//	}
	//	return results;
	//}
	//
	//// Send Prompt to Handheld to start FitnessAPI
	//private class ConnectFitnessTask extends AsyncTask<Void, Void, Void> {
	//
	//	@Override
	//	protected Void doInBackground(Void... params) {
	//		Collection<String> nodes = getNodes();
	//		for (String n : nodes) {
	//			sendHandheldFitnessPrompt(n);
	//		}
	//		return null;
	//	}
	//}

}
