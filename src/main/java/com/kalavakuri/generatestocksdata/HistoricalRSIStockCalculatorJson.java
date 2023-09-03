package com.kalavakuri.generatestocksdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

public class HistoricalRSIStockCalculatorJson {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final String NSE_HISTORY_URL = "https://www.nseindia.com/api/historical/cm/equity?symbol=&series=[%22EQ%22]&from=01-01-2023&to=04-07-2023";
	private static final String REFERRER_URL = "https://www.nseindia.com/get-quotes/equity?symbol=";

	public static void main(String[] args) throws Exception {

		String nseSymbol = "";
		String stockName = "";
		Map<String, String> cookies = getCookies();

		Response responseStock = Jsoup.connect(MONEY_CONTROL_STOCK_URL + "HZ").ignoreContentType(true).userAgent(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36")
				.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0).execute();

		Document docStock = responseStock.parse();

		Gson gsonStock = new Gson();
		Map<?, ?> stockDetails = gsonStock.fromJson(docStock.text(), Map.class);
		Map<?, ?> stockData = (Map<?, ?>) stockDetails.get("data");

		nseSymbol = (String) stockData.get("NSEID");
		nseSymbol = nseSymbol.contains("&") ? nseSymbol.replace("&", "%26") : nseSymbol;
		stockName = (String) stockData.get("SC_FULLNM");

		System.out.println(stockName);

		Response historyResponse = Jsoup.connect(NSE_HISTORY_URL.replace("symbol=", "symbol=" + nseSymbol))
				.ignoreContentType(true).referrer(REFERRER_URL.replace("symbol=", "symbol=" + nseSymbol))
				.userAgent(
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
				.timeout(90 * 1000).header("Accept", "application/json").cookies(cookies).followRedirects(true)
				.maxBodySize(0).execute();

		Document docHistory = historyResponse.parse();

		Gson gsoHistory = new Gson();
		Map<?, ?> historyDetails = gsoHistory.fromJson(docHistory.text(), Map.class);
		ArrayList<?> historyDatas = (ArrayList<?>) historyDetails.get("data");

		List<Double> pointsGain = new ArrayList<>();
		List<Double> pointsLost = new ArrayList<>();

		for (int i = 0; i < historyDatas.size(); i++) {

			List<?> subList = null;

			try {
				subList = historyDatas.subList(i, i + 14);
			} catch (Exception e) {
				break;
			}

			for (Object historyData : subList) {

				Map<?, ?> data = (Map<?, ?>) historyData;
				double closingPrice = (Double) data.get("CH_CLOSING_PRICE");
				double prevClosingPrice = (Double) data.get("CH_PREVIOUS_CLS_PRICE");

				if ((closingPrice - prevClosingPrice) >= 0) {
					pointsGain.add(closingPrice - prevClosingPrice);
				} else {
					pointsGain.add(0.00);
				}

				if ((prevClosingPrice - closingPrice) >= 0) {
					pointsLost.add(prevClosingPrice - closingPrice);
				} else {
					pointsLost.add(0.00);
				}
			}

			double pointsGainAverage = pointsGain.stream().mapToDouble(v -> v).average().getAsDouble();
			double pointsLostAverage = pointsLost.stream().mapToDouble(v -> v).average().getAsDouble();
			double finalCalculation = StocksDataUtil
					.format(100 - (100 / (1 + (pointsGainAverage / pointsLostAverage))));
			pointsGain.clear();
			pointsLost.clear();

			System.out.println(((Map<?, ?>) subList.get(0)).get("CH_TIMESTAMP") + "\t" + finalCalculation);
		}
	}

	private static Map<String, String> getCookies() throws IOException {

		Response response = null;

		try {

			response = Jsoup.connect("https://www.nseindia.com/").ignoreContentType(true).userAgent(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36")
					.timeout(90 * 1000).followRedirects(true).maxBodySize(0).execute();

		} catch (Exception e) {
			return null;
		}

		return response.cookies();
	}
}