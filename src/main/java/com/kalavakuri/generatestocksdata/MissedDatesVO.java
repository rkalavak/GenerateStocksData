package com.kalavakuri.generatestocksdata;

import java.io.Serializable;
import java.util.Date;

public class MissedDatesVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private double stockPrice;
	private Date date;

	public double getStockPrice() {
		return stockPrice;
	}

	public void setStockPrice(double stockPrice) {
		this.stockPrice = stockPrice;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
