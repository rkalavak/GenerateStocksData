package com.kalavakuri.generatestocksdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RSIIndicesSumsCalculator {

	private static final Map<String, String> indexNamesAndTables = new HashMap<>();
	private static final StringBuilder dataToStore = new StringBuilder();

	static {
		indexNamesAndTables.put("NIFTY 50", "MKT_NIFTY_50_STOCKS");
		indexNamesAndTables.put("NIFTY OIL & GAS", "MKT_OIL_AND_GAS");
		indexNamesAndTables.put("NIFTY REALTY", "MKT_REALTY");
		indexNamesAndTables.put("NIFTY MNC", "MKT_MNC");
		indexNamesAndTables.put("NIFTY PSE", "MKT_PSE");
		indexNamesAndTables.put("NIFTY BANK", "MKT_BANK");
		indexNamesAndTables.put("NIFTY PHARMA", "MKT_PHARMA");
		indexNamesAndTables.put("NIFTY CPSE", "MKT_CPSE");
		indexNamesAndTables.put("NIFTY CONSUMER DURABLES", "MKT_CONSUMER_DURABLES");
		indexNamesAndTables.put("NIFTY MEDIA", "MKT_MEDIA");
		indexNamesAndTables.put("NIFTY METAL", "MKT_METAL");
		indexNamesAndTables.put("NIFTY IT", "MKT_IT");
		indexNamesAndTables.put("NIFTY ENERGY", "MKT_ENERGY");
		indexNamesAndTables.put("NIFTY PRIVATE BANK", "MKT_PRIVATE_BANK");
		indexNamesAndTables.put("NIFTY PSU BANK", "MKT_PSU_BANK");
		indexNamesAndTables.put("NIFTY FINANCIAL SERVICES", "MKT_FINANCIAL_SERVICES");
		indexNamesAndTables.put("NIFTY COMMODITIES", "MKT_COMMODITIES");
		indexNamesAndTables.put("NIFTY SERVICES SECTOR", "MKT_SERVICES_SECTOR");
		indexNamesAndTables.put("NIFTY HEALTHCARE INDEX", "MKT_HEALTHCARE");
		indexNamesAndTables.put("NIFTY INDIA DIGITAL", "MKT_INDIA_DIGITAL");
		indexNamesAndTables.put("NIFTY AUTO", "MKT_AUTO");
		indexNamesAndTables.put("NIFTY INDIA MANUFACTURING", "MKT_INDIA_MANUFACTURING");
		indexNamesAndTables.put("NIFTY INFRASTRUCTURE", "MKT_INFRASTRUCTURE");
		indexNamesAndTables.put("NIFTY FMCG", "MKT_FMCG");
		indexNamesAndTables.put("NIFTY INDIA CONSUMPTION", "MKT_INDIA_CONSUMPTION");
	}

	public static void main(String[] args) throws Exception {

		Map<String, List<Double>> rsiSums = new HashMap<>();

		indexNamesAndTables.forEach((key, value) -> {
			List<Double> indexData = getIndexData(value);
			List<Double> pointsGain = new ArrayList<>();
			List<Double> pointsLost = new ArrayList<>();
			double previousValue = 0.00;
			for (int i = 0; i < indexData.size(); i++) {
				double currentValue = indexData.get(i);
				if (i != 0) {
					if ((currentValue - previousValue) >= 0) {
						pointsGain.add(currentValue - previousValue);
					} else {
						pointsGain.add(0.00);
					}

					if ((previousValue - currentValue) >= 0) {
						pointsLost.add(previousValue - currentValue);
					} else {
						pointsLost.add(0.00);
					}
				}
				previousValue = currentValue;
			}
			double pointsGainSum = pointsGain.stream().mapToDouble(v -> v).sum();
			double pointsLostSum = pointsLost.stream().mapToDouble(v -> v).sum();
			List<Double> sumsList = new ArrayList<>();
			sumsList.add(pointsGainSum);
			sumsList.add(pointsLostSum);

			rsiSums.put(key, sumsList);
		});

		rsiSums.forEach((key, value) -> {

			if (dataToStore.length() == 0) {

				dataToStore.append(key + "@" + value.get(0) + "@" + value.get(1));
			} else {

				dataToStore.append("\n" + key + "@" + value.get(0) + "@" + value.get(1));
			}
		});

		StocksDataUtil.writeData("RSIPreviousTradeIndicesSumsCalculation.txt", dataToStore.toString());

		System.out.println("Success...");
	}

	private static List<Double> getIndexData(String tableName) {

		List<Double> closeValues = new ArrayList<>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "#knagamma1");
			preparedStatement = connection.prepareStatement(
					"SELECT * FROM (SELECT ROW_NUMBER() OVER(ORDER BY TRADE_DATE DESC) ROW_NUMBER, TRADE_DATE, CLOSE_VALUE FROM "
							+ tableName + ") WHERE ROW_NUMBER <= 14 ORDER BY TRADE_DATE");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				closeValues.add(resultSet.getDouble("CLOSE_VALUE"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				if (null != resultSet && !resultSet.isClosed())
					resultSet.close();
				if (null != preparedStatement && !preparedStatement.isClosed())
					preparedStatement.close();
				if (null != connection && !connection.isClosed())
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}

		return closeValues;
	}
}
