package com.kalavakuri.generatestocksdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

public class MissingDates {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final String MONEY_CONTROL_HISTORY_URL = "https://www.moneycontrol.com/mc/widget/basicchart/get_chart_value?classic=true&sc_did=&dur=1yr";

	public static void execute() throws Exception {

		System.out.print("\n" + "     ");

		Set<String> originalDays = getOriginalDays();

		for (StockVO stockVO : getStockAnalysisStocks()) {

			Set<String> tradedDays = getTradedDays(stockVO);

			for (String day : originalDays) {

				if (!tradedDays.contains(day)) {

					System.out.print("\n" + "     " + stockVO.getName() + " " + day + " Missing");
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static Set<String> getOriginalDays() {

		Set<String> days = new HashSet<>();
		String formatedDate = null;

		Set<String> holidays = new HashSet<>();
		holidays.add("2019-12-25");
		holidays.add("2020-02-21");
		holidays.add("2020-03-10");
		holidays.add("2020-04-02");
		holidays.add("2020-04-06");
		holidays.add("2020-04-10");
		holidays.add("2020-04-14");
		holidays.add("2020-05-01");
		holidays.add("2020-05-25");
		holidays.add("2020-10-02");
		holidays.add("2020-11-16");
		holidays.add("2020-11-30");
		holidays.add("2020-12-25");

		// Last Year 2019 List

		holidays.add("2019-03-04");
		holidays.add("2019-03-21");
		holidays.add("2019-04-17");
		holidays.add("2019-04-19");
		holidays.add("2019-04-29");
		holidays.add("2019-05-01");
		holidays.add("2019-06-05");
		holidays.add("2019-08-12");
		holidays.add("2019-08-15");
		holidays.add("2019-09-02");
		holidays.add("2019-09-10");
		holidays.add("2019-10-02");
		holidays.add("2019-10-08");
		holidays.add("2019-10-21");
		holidays.add("2019-10-28");
		holidays.add("2019-11-12");
		holidays.add("2019-12-25");

		// 2 Years Back

		holidays.add("2018-12-25");

		Set<String> muhuratTrading = new HashSet<>();
		muhuratTrading.add("2020-11-14");
		muhuratTrading.add("2019-10-27");

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date todayDate = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);

		Date oldDate = calendar.getTime();
		Date calculatedDate = null;
		formatedDate = dateFormat.format(oldDate);
		String formatedTodayDate = dateFormat.format(todayDate);

		if ((oldDate.getDay() != 0 && oldDate.getDay() != 6 && !holidays.contains(formatedDate))
				|| (muhuratTrading.contains(formatedDate))) {
			days.add(dateFormat.format(oldDate));
		}

		do {

			Calendar calendarLocal = Calendar.getInstance();
			calendarLocal.setTime(oldDate);
			calendarLocal.add(Calendar.DATE, 1);

			calculatedDate = calendarLocal.getTime();

			formatedDate = dateFormat.format(calculatedDate);

			if ((calculatedDate.getDay() != 0 && calculatedDate.getDay() != 6)
					|| (muhuratTrading.contains(formatedDate))) {

				if ((!holidays.contains(formatedDate) && !formatedTodayDate.equals(formatedDate))
						|| (muhuratTrading.contains(formatedDate))) {
					days.add(formatedDate);
				}
			}

			oldDate = calculatedDate;

		} while (todayDate.compareTo(oldDate) != 0);

		return days;
	}

	public static Set<String> getTradedDays(StockVO stockVO) throws Exception {

		Set<String> days = new HashSet<>();
		String moneyControlHistorySymbol = "";
		String stockName = "";

		Response responseStock = Jsoup.connect(MONEY_CONTROL_STOCK_URL + stockVO.getMoneyControlSymbol())
				.ignoreContentType(true)
				.userAgent(
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
				.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0).execute();

		Document docStock = responseStock.parse();

		Gson gsonStock = new Gson();
		Map<?, ?> stockDetails = gsonStock.fromJson(docStock.text(), Map.class);
		Map<?, ?> stockData = (Map<?, ?>) stockDetails.get("data");

		moneyControlHistorySymbol = (String) stockData.get("DISPID");
		stockName = (String) stockData.get("SC_FULLNM");

		stockVO.setName(stockName);

		Response historyResponse = Jsoup
				.connect(MONEY_CONTROL_HISTORY_URL.replace("sc_did=", "sc_did=" + moneyControlHistorySymbol))
				.ignoreContentType(true)
				.userAgent(
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
				.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0).execute();

		Document historyDoc = historyResponse.parse();

		Gson historyGson = new Gson();
		Map<?, ?> historyDetails = historyGson.fromJson(historyDoc.text(), Map.class);
		ArrayList<?> historyData = (ArrayList<?>) historyDetails.get("g1");

		int historySize = historyData.size() - 1;

		for (int i = historySize; i >= 0; i--) {

			Map<?, ?> map = (Map<?, ?>) historyData.get(i);
			days.add(((String) map.get("date")));
		}

		return days;
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
}