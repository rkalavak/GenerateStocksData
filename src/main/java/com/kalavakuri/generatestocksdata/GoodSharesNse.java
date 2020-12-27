package com.kalavakuri.generatestocksdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;

public class GoodSharesNse {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final String NSE_HISTORY_URL = "https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=&segmentLink=3&symbolCount=2&series=ALL&dateRange=12month&fromDate=&toDate=&dataType=PRICEVOLUMEDELIVERABLE";
	private static StringBuilder dataToStore = new StringBuilder();

	public static void execute() throws Exception {

		List<StockVO> stockVOs = new ArrayList<>();

		for (StockVO stockVO : getStockAnalysisStocks()) {

			String nseSymbol = "";
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

			nseSymbol = (String) stockData.get("NSEID");
			nseSymbol = nseSymbol.contains("&") ? nseSymbol.replace("&", "%26") : nseSymbol;
			stockName = (String) stockData.get("SC_FULLNM");
			todaysPrice = Double.parseDouble((String) stockData.get("pricecurrent"));

			stockVO.setName(stockName);

			Response historyResponse = Jsoup.connect(NSE_HISTORY_URL.replace("symbol=", "symbol=" + nseSymbol))
					.ignoreContentType(true)
					.referrer("https://www1.nseindia.com/products/content/equities/equities/eq_security.htm")
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
					.timeout(90 * 1000).followRedirects(true).maxBodySize(0).execute();

			Document historyDoc = historyResponse.parse();

			Element elementById = historyDoc.getElementById("csvContentDiv");

			String fullData = elementById.text();
			String[] eachRow = fullData.split(":");

			eachRow = Arrays.stream(eachRow).filter(s -> (s.contains("EQ") || s.contains("BE"))).toArray(String[]::new);

			int historySize = (eachRow.length) - 2;

			double todaysPriceSum = todaysPrice;
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

			for (int i = historySize; i >= 1; i--) {

				String[] split = eachRow[i].split(",");
				double price = Double.parseDouble(split[8].replace("\"", "").trim());

				if (i > historySize - 4) {
					todaysPriceSum = todaysPriceSum + price;
					continue;
				} else if (!dayAvg5Calculated) {
					dayAvg5 = todaysPriceSum / 5;
					dayAvg5Calculated = true;
				}

				if (i > historySize - 9) {
					todaysPriceSum = todaysPriceSum + price;
					continue;
				} else if (!dayAvg10Calculated) {
					dayAvg10 = todaysPriceSum / 10;
					dayAvg10Calculated = true;
				}

				if (i > historySize - 19) {
					todaysPriceSum = todaysPriceSum + price;
					continue;
				} else if (!dayAvg20Calculated) {
					dayAvg20 = todaysPriceSum / 20;
					dayAvg20Calculated = true;
				}

				if (i > historySize - 29) {
					todaysPriceSum = todaysPriceSum + price;
					continue;
				} else if (!dayAvg30Calculated) {
					dayAvg30 = todaysPriceSum / 30;
					dayAvg30Calculated = true;
				}

				if (i > historySize - 49) {
					todaysPriceSum = todaysPriceSum + price;
					continue;
				} else if (!dayAvg50Calculated) {
					dayAvg50 = todaysPriceSum / 50;
					dayAvg50Calculated = true;
				}

				if (i > historySize - 99) {
					todaysPriceSum = todaysPriceSum + price;
					continue;
				} else if (!dayAvg100Calculated) {
					dayAvg100 = todaysPriceSum / 100;
					dayAvg100Calculated = true;
				}

				if (i > historySize - 149) {
					todaysPriceSum = todaysPriceSum + price;
					continue;
				} else if (!dayAvg150Calculated) {
					dayAvg150 = todaysPriceSum / 150;
					dayAvg150Calculated = true;
				}

				if (i > historySize - 199) {
					todaysPriceSum = todaysPriceSum + price;
					continue;
				} else if (!dayAvg200Calculated) {
					dayAvg200 = todaysPriceSum / 200;
					dayAvg200Calculated = true;
					break;
				}
			}
			if (todaysPrice >= dayAvg5 && todaysPrice >= dayAvg10 && todaysPrice >= dayAvg20 && todaysPrice >= dayAvg30
					&& todaysPrice >= dayAvg50 && todaysPrice >= dayAvg100 && todaysPrice >= dayAvg150
					&& todaysPrice >= dayAvg200 && dayAvg5Calculated && dayAvg10Calculated && dayAvg20Calculated
					&& dayAvg30Calculated && dayAvg50Calculated && dayAvg100Calculated && dayAvg150Calculated
					&& dayAvg200Calculated) {

				stockVOs.add(stockVO);
			}
		}
		stockVOs.sort(Comparator.comparing(StockVO::getName));
		stockVOs.forEach(stockVO -> {
			if (dataToStore.length() == 0) {

				dataToStore.append(stockVO.getName());
			} else {

				dataToStore.append("\n" + stockVO.getName());
			}
		});
		StocksDataUtil.writeData("GoodSharesNse.txt", dataToStore.toString());
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
}