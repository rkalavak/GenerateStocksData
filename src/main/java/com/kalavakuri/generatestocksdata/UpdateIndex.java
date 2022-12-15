package com.kalavakuri.generatestocksdata;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

public class UpdateIndex {

	private static final String ALL_INDICES = "https://www.nseindia.com/api/allIndices";
	private static final Map<String, String> indexNamesAndTables = new HashMap<>();
	private static final Map<String, HoldIndexDataVO> indexValues = new HashMap<>();

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

	public static void main(String[] args) {

		try {

			Response response = Jsoup.connect(ALL_INDICES).ignoreContentType(true)
					.referrer("https://www.nseindia.com/market-data/live-market-indices")
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36")
					.timeout(90 * 1000).header("Accept", "application/json").cookies(getCookies()).followRedirects(true)
					.maxBodySize(0).execute();

			Document doc = response.parse();

			Gson gson = new Gson();
			Map<?, ?> indicesDetails = gson.fromJson(doc.text(), Map.class);
			ArrayList<?> indicesDetailsList = (ArrayList<?>) indicesDetails.get("data");

			for (Object index : indicesDetailsList) {

				Map<?, ?> indexDetails = (Map<?, ?>) index;

				String indexNseName = (String) indexDetails.get("index");

				if (indexNamesAndTables.containsKey(indexNseName)) {

					HoldIndexDataVO holdIndexDataVO = new HoldIndexDataVO();
					holdIndexDataVO.setOpenValue((Double) indexDetails.get("open"));
					holdIndexDataVO.setHighValue((Double) indexDetails.get("high"));
					holdIndexDataVO.setLowValue((Double) indexDetails.get("low"));
					holdIndexDataVO.setCloseValue((Double) indexDetails.get("last"));

					indexValues.put(indexNamesAndTables.get(indexNseName), holdIndexDataVO);
				}
			}

			updateIndexes(indexValues);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static Map<String, String> getCookies() throws IOException {

		Response response = Jsoup.connect("https://www.nseindia.com/market-data/live-market-indices")
				.ignoreContentType(true).referrer("https://www.nseindia.com/")
				.userAgent(
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36")
				.timeout(90 * 1000).followRedirects(true).maxBodySize(0).execute();

		return response.cookies();
	}

	private static void updateIndexes(Map<String, HoldIndexDataVO> indexValues) {

		Connection connection = null;
		Statement statement = null;

		try {

			Class.forName("oracle.jdbc.driver.OracleDriver");
			connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "#knagamma1");
			connection.setAutoCommit(false);
			statement = connection.createStatement();

			for (Map.Entry<String, HoldIndexDataVO> entry : indexValues.entrySet()) {

				HoldIndexDataVO holdIndexDataVO = entry.getValue();
				statement.addBatch("INSERT INTO " + entry.getKey()
						+ "(TRADE_DATE, OPEN_VALUE, HIGH_VALUE, LOW_VALUE, CLOSE_VALUE) VALUES(TO_DATE('15-12-2022'),"
						+ holdIndexDataVO.getOpenValue() + "," + holdIndexDataVO.getHighValue() + ","
						+ holdIndexDataVO.getLowValue() + "," + holdIndexDataVO.getCloseValue() + ")");
			}

			statement.executeBatch();
			connection.commit();

			System.out.println("Commit success...");

		} catch (Exception e) {
			try {
				if (null != connection && !connection.isClosed())
					connection.rollback();
			} catch (SQLException sqlException) {
				sqlException.printStackTrace();
				System.exit(0);
			}
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				if (null != statement && !statement.isClosed())
					statement.close();
				if (null != connection && !connection.isClosed())
					connection.close();
			} catch (SQLException sqlException) {
				sqlException.printStackTrace();
				System.exit(0);
			}
		}
	}
}