package com.panwrona.wear;

import android.app.Application;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.facebook.stetho.Stetho;
import com.panwrona.wear.data.model.Training;

public class FitnessTrackerApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Configuration.Builder configurationBuilder = new Configuration.Builder(this);
		configurationBuilder.addModelClass(Training.class);
		ActiveAndroid.initialize(configurationBuilder.create());
		Stetho.initializeWithDefaults(this);

	}
}
