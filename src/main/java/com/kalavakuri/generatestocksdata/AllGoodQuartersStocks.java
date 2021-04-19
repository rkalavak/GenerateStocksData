package com.kalavakuri.generatestocksdata;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

public class AllGoodQuartersStocks {

	private static String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static String STOCK_SCREENER_CONSOLIDATED_URL = "https://www.screener.in/company//consolidated/";
	private static String STOCK_SCREENER_STANDALONE_URL = "https://www.screener.in/company//";
	private static StringBuilder dataToStore = new StringBuilder();

	public static void execute() throws IOException {

		fetchQuarterlyGoodStocks();
		StocksDataUtil.writeData("AllGoodQuartersStocks.txt", dataToStore.toString());
	}

	private static void fetchQuarterlyGoodStocks() throws IOException {

		List<StockVO> stocks = getMoneyControlSymbols();

		for (StockVO stockVO : stocks) {

			String NSESymbol = "";
			String stockName = "";

			Response response = Jsoup.connect(MONEY_CONTROL_STOCK_URL + stockVO.getMoneyControlSymbol())
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
					.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0)
					.execute();

			Document doc = response.parse();

			Gson gson = new Gson();
			Map<?, ?> stockDetails = gson.fromJson(doc.text(), Map.class);
			Map<?, ?> stockData = (Map<?, ?>) stockDetails.get("data");

			try {

				NSESymbol = (String) stockData.get("NSEID");
				stockName = (String) stockData.get("SC_FULLNM");

				stockVO.setNseId(NSESymbol);
				stockVO.setName(stockName);

			} catch (Exception e) {
				e.printStackTrace();
			}

			fetchQuarterlyResults(stockVO);
		}
	}

	private static List<StockVO> getMoneyControlSymbols() {

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
			}
		}

		return stocks;
	}

	private static void fetchQuarterlyResults(StockVO stockVO) throws IOException {

		String quarter = null;
		boolean isGoodStock = false;

		Response response = Jsoup
				.connect(STOCK_SCREENER_CONSOLIDATED_URL.replace("company/", "company/" + stockVO.getNseId()))
				.ignoreContentType(true)
				.userAgent(
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
				.timeout(90 * 1000).followRedirects(true).maxBodySize(0).execute();

		Document doc = response.parse();

		Element quarterlyDivision = doc.getElementById("quarters");
		Element table = quarterlyDivision.getElementsByTag("table").get(0);
		Element tableHead = table.getElementsByTag("thead").get(0);
		Element tableHeadRow = tableHead.getElementsByTag("tr").get(0);
		Elements tableHeadRowData = tableHeadRow.getElementsByTag("th");

		String tableHeadText = tableHeadRowData.text();

		if (tableHead.text().trim().equals("") || !tableHeadText.contains("Jun 2019")
				|| !tableHeadText.contains("Sep 2019") || !tableHeadText.contains("Dec 2019")
				|| !tableHeadText.contains("Mar 2020") || !tableHeadText.contains("Jun 2020")
				|| !tableHeadText.contains("Sep 2020") || !tableHeadText.contains("Dec 2020")) {

			response = Jsoup.connect(STOCK_SCREENER_STANDALONE_URL.replace("company/", "company/" + stockVO.getNseId()))
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
					.timeout(90 * 1000).followRedirects(true).maxBodySize(0).execute();

			doc = response.parse();

			quarterlyDivision = doc.getElementById("quarters");
			table = quarterlyDivision.getElementsByTag("table").get(0);
			tableHead = table.getElementsByTag("thead").get(0);

			tableHeadRow = tableHead.getElementsByTag("tr").get(0);
			tableHeadRowData = tableHeadRow.getElementsByTag("th");

		}

		tableHeadText = tableHeadRowData.text();

		if (!tableHeadText.contains("Jun 2019") || !tableHeadText.contains("Sep 2019")
				|| !tableHeadText.contains("Dec 2019") || !tableHeadText.contains("Mar 2020")
				|| !tableHeadText.contains("Jun 2020") || !tableHeadText.contains("Sep 2020")
				|| !tableHeadText.contains("Dec 2020")) {

			System.out.println(stockVO.getName() + " " + stockVO.getMoneyControlSymbol() + " " + stockVO.getNseId());
		}

		if (tableHeadText.contains("Mar 2021")) {

			quarter = "Q4";

			isGoodStock = isGoodStock(table, 1);

			if (isGoodStock) {

				isGoodStock = isGoodStock(table, 2);

				if (isGoodStock) {

					isGoodStock = isGoodStock(table, 3);

					if (isGoodStock) {

						isGoodStock = isGoodStock(table, 4);
					}
				}
			}

		} else if (tableHeadText.contains("Dec 2020")) {

			quarter = "Q3";

			isGoodStock = isGoodStock(table, 1);

			if (isGoodStock) {

				isGoodStock = isGoodStock(table, 2);

				if (isGoodStock) {

					isGoodStock = isGoodStock(table, 3);
				}
			}

		} else if (tableHeadText.contains("Sep 2020")) {

			quarter = "Q2";

			isGoodStock = isGoodStock(table, 1);

			if (isGoodStock) {

				isGoodStock = isGoodStock(table, 2);
			}

		} else if (tableHeadText.contains("Jun 2020")) {

			quarter = "Q1";

			isGoodStock = isGoodStock(table, 1);
		}

		if (isGoodStock) {

			if (dataToStore.length() == 0) {

				dataToStore.append(stockVO.getName() + "   " + quarter);
			} else {

				dataToStore.append("\n" + stockVO.getName() + "   " + quarter);
			}
		}
	}

	private static boolean isGoodStock(Element table, int number) {

		boolean isGood = false;

		Element tableBody = table.getElementsByTag("tbody").get(0);

		Element salesRow = tableBody.getElementsByTag("tr").get(0);

		Elements salesRowData = salesRow.getElementsByTag("td");

		int salesRowDataSize = salesRowData.size() - number;

		double currentQuarterSales = Double.parseDouble(salesRowData.get(salesRowDataSize).text().replace(",", ""));
		double pastQuarterSales = Double.parseDouble(salesRowData.get(salesRowDataSize - 4).text().replace(",", ""));

		if (currentQuarterSales > pastQuarterSales) {

			isGood = true;
		}

		if (isGood) {

			Element netProfitRow = tableBody.getElementsByTag("tr").get(9);
			Elements netProfitRowData = netProfitRow.getElementsByTag("td");

			int netProfitRowDataSize = netProfitRowData.size() - number;

			double currentQuarterProfits = Double
					.parseDouble(netProfitRowData.get(netProfitRowDataSize).text().replace(",", "").replace(",", ""));
			double pastQuarterProfits = Double
					.parseDouble(netProfitRowData.get(netProfitRowDataSize - 4).text().replace(",", ""));

			if (currentQuarterProfits > pastQuarterProfits) {

				isGood = true;

			} else {

				isGood = false;
			}
		}

		return isGood;
	}
}