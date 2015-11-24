package com.panwrona.wear.data.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Training")
public class Training extends Model{

	@Column(name = "Accuracy")
	public int accuracy;

	@Column(name = "TimeStart")
	public long timeStart;

	@Column(name = "TimeStop")
	public long timeStop;

	@Column(name = "Value")
	public float value;

	public Training(int accuracy, long timeStart, long timeStop, float value) {
		this.accuracy = accuracy;
		this.timeStart = timeStart;
		this.timeStop = timeStop;
		this.value = value;
	}
}
