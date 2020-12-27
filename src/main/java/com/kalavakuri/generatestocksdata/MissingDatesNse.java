package com.kalavakuri.generatestocksdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;

public class MissingDatesNse {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final String NSE_HISTORY_URL = "https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=&segmentLink=3&symbolCount=2&series=ALL&dateRange=12month&fromDate=&toDate=&dataType=PRICEVOLUMEDELIVERABLE";

	public static void execute() throws Exception {

		System.out.println("");

		Set<String> originalDays = getOriginalDays();

		for (StockVO stockVO : getStockAnalysisStocks()) {

			Set<String> tradedDays = getTradedDays(stockVO);

			for (String day : originalDays) {

				if (!tradedDays.contains(day)) {

					System.out.println("     " + stockVO.getName() + " " + day + " Missing");
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static Set<String> getOriginalDays() {

		Set<String> days = new HashSet<>();
		String formatedDate = null;

		Set<String> holidays = new HashSet<>();
		holidays.add("25-Dec-2019");
		holidays.add("21-Feb-2020");
		holidays.add("10-Mar-2020");
		holidays.add("02-Apr-2020");
		holidays.add("06-Apr-2020");
		holidays.add("10-Apr-2020");
		holidays.add("14-Apr-2020");
		holidays.add("01-May-2020");
		holidays.add("25-May-2020");
		holidays.add("02-Oct-2020");
		holidays.add("16-Nov-2020");
		holidays.add("30-Nov-2020");
		holidays.add("25-Dec-2020");

		Set<String> muhuratTrading = new HashSet<>();
		muhuratTrading.add("14-Nov-2020");

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		Date todayDate = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		// calendar.add(Calendar.DATE, 1);

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
		String nseSymbol = "";
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

		nseSymbol = (String) stockData.get("NSEID");
		nseSymbol = nseSymbol.contains("&") ? nseSymbol.replace("&", "%26") : nseSymbol;
		stockName = (String) stockData.get("SC_FULLNM");

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

		int size = (eachRow.length) - 1;

		for (int i = size; i >= 0; i--) {

			String[] split = eachRow[i].split(",");

			days.add(split[2].replace("\"", "").trim());
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