package com.kalavakuri.generatestocksdata;

import java.io.Serializable;

public class HoldIndexDataVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double openValue;
	private double highValue;
	private double lowValue;
	private double closeValue;

	public double getOpenValue() {
		return openValue;
	}

	public void setOpenValue(double openValue) {
		this.openValue = openValue;
	}

	public double getHighValue() {
		return highValue;
	}

	public void setHighValue(double highValue) {
		this.highValue = highValue;
	}

	public double getLowValue() {
		return lowValue;
	}

	public void setLowValue(double lowValue) {
		this.lowValue = lowValue;
	}

	public double getCloseValue() {
		return closeValue;
	}

	public void setCloseValue(double closeValue) {
		this.closeValue = closeValue;
	}
}
