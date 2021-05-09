package com.kalavakuri.generatestocksdata;

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

public class GoodShares {

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

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void execute() throws Exception {

		List<StockVO> stockVOs = new ArrayList<>();

		for (StockVO stockVO : StocksDataUtil.getMoneyControlSymbols()) {

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
		StocksDataUtil.writeData("GoodShares.txt", dataToStore.toString());
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