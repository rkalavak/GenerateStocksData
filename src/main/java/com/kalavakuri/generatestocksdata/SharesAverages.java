package com.kalavakuri.generatestocksdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

public class SharesAverages {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final String MONEY_CONTROL_HISTORY_URL = "https://www.moneycontrol.com/mc/widget/basicchart/get_chart_value?classic=true&sc_did=&dur=1yr";
	private static final Map<String, List<MissedDatesVO>> missedDatesVOsGlobal = new HashMap<>();
	private static StringBuilder dataToStore = new StringBuilder();

	static {

		try {

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

			// COFORGE LIMITED
			List<MissedDatesVO> missedDates = new ArrayList<>();

			MissedDatesVO missedDatesVO = new MissedDatesVO();
			missedDatesVO.setDate(simpleDateFormat.parse("2020-08-18"));
			missedDatesVO.setStockPrice(2011.50);
			missedDates.add(missedDatesVO);

			missedDatesVO = new MissedDatesVO();
			missedDatesVO.setDate(simpleDateFormat.parse("2020-08-19"));
			missedDatesVO.setStockPrice(2017.45);
			missedDates.add(missedDatesVO);

			missedDatesVOsGlobal.put("COFORGE LIMITED", missedDates);

			// Tata Steel
			missedDates = new ArrayList<>();
			missedDatesVO = new MissedDatesVO();
			missedDatesVO.setDate(simpleDateFormat.parse("2020-08-06"));
			missedDatesVO.setStockPrice(400.45);
			missedDates.add(missedDatesVO);

			missedDatesVOsGlobal.put("Tata Steel", missedDates);

			// TATA Consumer Products
			missedDates = new ArrayList<>();
			missedDatesVO = new MissedDatesVO();
			missedDatesVO.setDate(simpleDateFormat.parse("2020-02-27"));
			missedDatesVO.setStockPrice(363.30);
			missedDates.add(missedDatesVO);

			missedDatesVO = new MissedDatesVO();
			missedDatesVO.setDate(simpleDateFormat.parse("2020-02-28"));
			missedDatesVO.setStockPrice(346.15);
			missedDates.add(missedDatesVO);

			missedDatesVO = new MissedDatesVO();
			missedDatesVO.setDate(simpleDateFormat.parse("2020-03-02"));
			missedDatesVO.setStockPrice(338.80);
			missedDates.add(missedDatesVO);

			missedDatesVOsGlobal.put("TATA Consumer Products", missedDates);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void execute() throws Exception {

		for (StockVO stockVO : getStockAnalysisStocks()) {

			String moneyControlHistorySymbol = "";
			String stockName = "";
			double todaysPrice = 0.00;

			Response responseStock = Jsoup.connect(MONEY_CONTROL_STOCK_URL + stockVO.getMoneyControlSymbol())
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
					.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0)
					.execute();

			Document docStock = responseStock.parse();

			Gson gsonStock = new Gson();
			Map<?, ?> stockDetails = gsonStock.fromJson(docStock.text(), Map.class);
			Map<?, ?> stockData = (Map<?, ?>) stockDetails.get("data");

			moneyControlHistorySymbol = (String) stockData.get("DISPID");
			stockName = (String) stockData.get("SC_FULLNM");
			todaysPrice = Double.parseDouble((String) stockData.get("pricecurrent"));

			stockVO.setName(stockName);

			Response historyResponse = Jsoup
					.connect(MONEY_CONTROL_HISTORY_URL.replace("sc_did=", "sc_did=" + moneyControlHistorySymbol))
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
					.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0)
					.execute();

			Document historyDoc = historyResponse.parse();

			Gson historyGson = new Gson();
			Map<?, ?> historyDetails = historyGson.fromJson(historyDoc.text(), Map.class);
			ArrayList<MissedDatesVO> missedDatesVOs = processDatesWithMissedDates(
					(ArrayList<?>) historyDetails.get("g1"), stockName);

			int historySize = missedDatesVOs.size() - 2;
			double todaysPriceSum = todaysPrice;
			double dayAvg3 = 0.00;
			boolean dayAvg3Calculated = false;
			double dayAvg5 = 0.00;
			boolean dayAvg5Calculated = false;
			double dayAvg10 = 0.00;
			boolean dayAvg10Calculated = false;
			double dayAvg20 = 0.00;
			boolean dayAvg20Calculated = false;
			double dayAvg30 = 0.00;
			boolean dayAvg30Calculated = false;
			double dayAvg50 = 0.00;
			boolean dayAvg50Calculated = false;
			double dayAvg100 = 0.00;
			boolean dayAvg100Calculated = false;
			double dayAvg150 = 0.00;
			boolean dayAvg150Calculated = false;
			double dayAvg200 = 0.00;
			boolean dayAvg200Calculated = false;

			for (int i = historySize; i >= 0; i--) {

				if (i > historySize - 2) {
					MissedDatesVO missedDatesVO = missedDatesVOs.get(i);
					todaysPriceSum = todaysPriceSum + missedDatesVO.getStockPrice();
					continue;
				} else if (!dayAvg3Calculated) {
					dayAvg3 = todaysPriceSum / 3;
					dayAvg3Calculated = true;
				}

				if (i > historySize - 4) {
					MissedDatesVO missedDatesVO = missedDatesVOs.get(i);
					todaysPriceSum = todaysPriceSum + missedDatesVO.getStockPrice();
					continue;
				} else if (!dayAvg5Calculated) {
					dayAvg5 = todaysPriceSum / 5;
					dayAvg5Calculated = true;
				}

				if (i > historySize - 9) {
					MissedDatesVO missedDatesVO = missedDatesVOs.get(i);
					todaysPriceSum = todaysPriceSum + missedDatesVO.getStockPrice();
					continue;
				} else if (!dayAvg10Calculated) {
					dayAvg10 = todaysPriceSum / 10;
					dayAvg10Calculated = true;
				}

				if (i > historySize - 19) {
					MissedDatesVO missedDatesVO = missedDatesVOs.get(i);
					todaysPriceSum = todaysPriceSum + missedDatesVO.getStockPrice();
					continue;
				} else if (!dayAvg20Calculated) {
					dayAvg20 = todaysPriceSum / 20;
					dayAvg20Calculated = true;
				}

				if (i > historySize - 29) {
					MissedDatesVO missedDatesVO = missedDatesVOs.get(i);
					todaysPriceSum = todaysPriceSum + missedDatesVO.getStockPrice();
					continue;
				} else if (!dayAvg30Calculated) {
					dayAvg30 = todaysPriceSum / 30;
					dayAvg30Calculated = true;
				}

				if (i > historySize - 49) {
					MissedDatesVO missedDatesVO = missedDatesVOs.get(i);
					todaysPriceSum = todaysPriceSum + missedDatesVO.getStockPrice();
					continue;
				} else if (!dayAvg50Calculated) {
					dayAvg50 = todaysPriceSum / 50;
					dayAvg50Calculated = true;
				}

				if (i > historySize - 99) {
					MissedDatesVO missedDatesVO = missedDatesVOs.get(i);
					todaysPriceSum = todaysPriceSum + missedDatesVO.getStockPrice();
					continue;
				} else if (!dayAvg100Calculated) {
					dayAvg100 = todaysPriceSum / 100;
					dayAvg100Calculated = true;
				}

				if (i > historySize - 149) {
					MissedDatesVO missedDatesVO = missedDatesVOs.get(i);
					todaysPriceSum = todaysPriceSum + missedDatesVO.getStockPrice();
					continue;
				} else if (!dayAvg150Calculated) {
					dayAvg150 = todaysPriceSum / 150;
					dayAvg150Calculated = true;
				}

				if (i > historySize - 199) {
					MissedDatesVO missedDatesVO = missedDatesVOs.get(i);
					todaysPriceSum = todaysPriceSum + missedDatesVO.getStockPrice();
					continue;
				} else if (!dayAvg200Calculated) {
					dayAvg200 = todaysPriceSum / 200;
					dayAvg200Calculated = true;
					break;
				}
			}

			if (dataToStore.length() == 0) {

				dataToStore.append(stockVO.getName() + "    " + StocksDataUtil.format(dayAvg3) + " (3)    "
						+ StocksDataUtil.format(dayAvg5) + " (5)    " + StocksDataUtil.format(dayAvg10) + " (10)    "
						+ StocksDataUtil.format(dayAvg20) + " (20)    " + StocksDataUtil.format(dayAvg30) + " (30)    "
						+ StocksDataUtil.format(dayAvg50) + " (50)    " + StocksDataUtil.format(dayAvg100)
						+ " (100)    " + StocksDataUtil.format(dayAvg150) + " (150)    "
						+ StocksDataUtil.format(dayAvg200) + " (200)");
			} else {

				dataToStore.append("\n" + stockVO.getName() + "    " + StocksDataUtil.format(dayAvg3) + " (3)    "
						+ StocksDataUtil.format(dayAvg5) + " (5)    " + StocksDataUtil.format(dayAvg10) + " (10)    "
						+ StocksDataUtil.format(dayAvg20) + " (20)    " + StocksDataUtil.format(dayAvg30) + " (30)    "
						+ StocksDataUtil.format(dayAvg50) + " (50)    " + StocksDataUtil.format(dayAvg100)
						+ " (100)    " + StocksDataUtil.format(dayAvg150) + " (150)    "
						+ StocksDataUtil.format(dayAvg200) + " (200)");
			}
		}
		StocksDataUtil.writeData("SharesAverages.txt", dataToStore.toString());
	}

	private static List<StockVO> getStockAnalysisStocks() {

		List<StockVO> stocks = new ArrayList<>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "#knagamma1");
			preparedStatement = connection.prepareStatement("SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_AUTO UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_BANK UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_COMMODITIES UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_CPSE UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_ENERGY UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_FINANCIAL_SERVICES UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_FMCG UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_INDIA_CONSUMPTION UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_INFRASTRUCTURE UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_IT UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_MEDIA UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_METAL UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_MNC UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_PHARMA UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_PRIVATE_BANK UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_PSE UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_PSU_BANK UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_REALTY UNION \r\n"
					+ "SELECT MONEY_CONTROL_SYMBOL FROM MKT_NIFTY_SERVICES_SECTOR");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				StockVO stock = new StockVO();
				stock.setMoneyControlSymbol(resultSet.getString("MONEY_CONTROL_SYMBOL"));
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

	private static ArrayList<MissedDatesVO> processDatesWithMissedDates(ArrayList<?> historyData, String stockName)
			throws Exception {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		ArrayList<MissedDatesVO> missedDatesVOs = new ArrayList<>();

		for (Object object : historyData) {

			Map<?, ?> map = (Map<?, ?>) object;

			MissedDatesVO missedDatesVO = new MissedDatesVO();

			double stockPrice = Double.parseDouble((String) map.get("close"));
			String date = (String) map.get("date");

			missedDatesVO.setStockPrice(stockPrice);
			missedDatesVO.setDate(simpleDateFormat.parse(date));

			missedDatesVOs.add(missedDatesVO);
		}

		if (missedDatesVOsGlobal.containsKey(stockName)) {
			missedDatesVOs.addAll(missedDatesVOsGlobal.get(stockName));
		}

		missedDatesVOs.sort(Comparator.comparing(MissedDatesVO::getDate));

		return missedDatesVOs;
	}
}