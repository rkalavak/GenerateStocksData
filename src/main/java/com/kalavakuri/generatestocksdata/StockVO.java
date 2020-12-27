package com.kalavakuri.generatestocksdata;

import java.io.Serializable;

public class StockVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String moneyControlSymbol;
	private String nseId;
	private String name;
	private double salesGrowthPercentage;
	private double netProfitGrowthPercentage;

	public String getMoneyControlSymbol() {
		return moneyControlSymbol;
	}

	public void setMoneyControlSymbol(String moneyControlSymbol) {
		this.moneyControlSymbol = moneyControlSymbol;
	}

	public String getNseId() {
		return nseId;
	}

	public void setNseId(String nseId) {
		this.nseId = nseId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getSalesGrowthPercentage() {
		return salesGrowthPercentage;
	}

	public void setSalesGrowthPercentage(double salesGrowthPercentage) {
		this.salesGrowthPercentage = salesGrowthPercentage;
	}

	public double getNetProfitGrowthPercentage() {
		return netProfitGrowthPercentage;
	}

	public void setNetProfitGrowthPercentage(double netProfitGrowthPercentage) {
		this.netProfitGrowthPercentage = netProfitGrowthPercentage;
	}
}
