package com.kalavakuri.generatestocksdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

public class NseVolumesAverage {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static String NIRMAL_BANG_DELIVERY_URL = "https://www.nirmalbang.com/Ajaxpages/companyprofile/CompanyDeliverableVol.aspx?Option=NSE&FinCode=&fmonth=&fyear=&lmonth=&lyear=&PageSize=1000";
	private static final StringBuilder dataToStore = new StringBuilder();

	static {

		Calendar cal = Calendar.getInstance();
		String toYear = new SimpleDateFormat("YYYY").format(cal.getTime());
		String toMonth = new SimpleDateFormat("MMM").format(cal.getTime()).toUpperCase();
		cal.add(Calendar.MONTH, -1);
		String fromYear = new SimpleDateFormat("YYYY").format(cal.getTime()).toUpperCase();
		String fromMonth = new SimpleDateFormat("MMM").format(cal.getTime()).toUpperCase();

		NIRMAL_BANG_DELIVERY_URL = NIRMAL_BANG_DELIVERY_URL.replace("fmonth=", "fmonth=" + fromMonth);
		NIRMAL_BANG_DELIVERY_URL = NIRMAL_BANG_DELIVERY_URL.replace("lmonth=", "lmonth=" + toMonth);
		NIRMAL_BANG_DELIVERY_URL = NIRMAL_BANG_DELIVERY_URL.replace("fyear=", "fyear=" + fromYear);
		NIRMAL_BANG_DELIVERY_URL = NIRMAL_BANG_DELIVERY_URL.replace("lyear=", "lyear=" + toYear);
	}

	public static void main(String[] args) throws Exception {

		for (StockVO stockVO : getMoneyControlAndNirmalBangSymbols()) {

			String stockName = "";

			Response responseStock = Jsoup.connect(MONEY_CONTROL_STOCK_URL + stockVO.getMoneyControlSymbol())
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36")
					.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0)
					.execute();

			Document docStock = responseStock.parse();

			Gson gsonStock = new Gson();
			Map<?, ?> stockDetails = gsonStock.fromJson(docStock.text(), Map.class);
			Map<?, ?> stockData = (Map<?, ?>) stockDetails.get("data");

			stockName = (String) stockData.get("SC_FULLNM");

			stockVO.setName(stockName);

			// System.out.println(stockName);

			Response responseStockHistory = Jsoup
					.connect(NIRMAL_BANG_DELIVERY_URL.replace("FinCode=", "FinCode=" + stockVO.getNirmalBangSymbol()))
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36")
					.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0)
					.execute();

			Document docStockHistory = responseStockHistory.parse();

			Gson gsonStockHistory = new Gson();
			List<Map<String, String>> stockHistoryDetails = gsonStockHistory.fromJson(docStockHistory.text(),
					List.class);
			if (stockHistoryDetails.size() == 0) {
				System.out.println("Failed: " + stockName + " " + stockVO.getMoneyControlSymbol());
				continue;
			}

			List<Double> volumes = new ArrayList<Double>();
			List<Double> deliveryVolumesPer = new ArrayList<Double>();

			for (Map<String, String> history : stockHistoryDetails.subList(0, 20)) {

				volumes.add(Double.parseDouble(history.get("DVVolume")));
				deliveryVolumesPer.add(Double.parseDouble(history.get("DVDLVolumeper")));
			}

			double tradedQuantityAvg = volumes.stream().mapToDouble(v -> v).average().getAsDouble();
			double deliveryToTradedQuantityAvg = deliveryVolumesPer.stream().mapToDouble(v -> v).average()
					.getAsDouble();

			if (dataToStore.length() == 0) {

				dataToStore.append(stockVO.getName() + "#" + tradedQuantityAvg + " " + deliveryToTradedQuantityAvg);
			} else {

				dataToStore
						.append("\n" + stockVO.getName() + "#" + tradedQuantityAvg + " " + deliveryToTradedQuantityAvg);
			}
		}

		StocksDataUtil.writeData("NseVolumesAverage.txt", dataToStore.toString());
	}

	private static List<StockVO> getMoneyControlAndNirmalBangSymbols() {

		List<StockVO> stocks = new ArrayList<>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "#knagamma1");
			preparedStatement = connection.prepareStatement(
					"SELECT MONEY_CONTROL_SYMBOL, NIRMALBANG_SYMBOL FROM MKT_MONEYCONTROL_NIRMALBANG");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				StockVO stock = new StockVO();
				stock.setMoneyControlSymbol(resultSet.getString("MONEY_CONTROL_SYMBOL"));
				stock.setNirmalBangSymbol(resultSet.getString("NIRMALBANG_SYMBOL"));
				stocks.add(stock);
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

		return stocks;

	}
}