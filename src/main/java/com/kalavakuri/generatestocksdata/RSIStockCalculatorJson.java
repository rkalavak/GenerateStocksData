package com.kalavakuri.generatestocksdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

public class RSIStockCalculatorJson {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final String NSE_HISTORY_URL = "https://www.nseindia.com/api/historical/cm/equity?symbol=";
	private static final String REFERRER_URL = "https://www.nseindia.com/get-quotes/equity?symbol=";
	private static final StringBuilder dataToStore = new StringBuilder();

	public static void main(String[] args) throws Exception {

		Map<String, Double> indexRSI = new HashMap<>();
		Map<String, String> cookies = getCookies();

		for (StockVO stockVO : StocksDataUtil.getMoneyControlSymbols()) {

			String nseSymbol = "";
			String stockName = "";

			Response responseStock = Jsoup.connect(MONEY_CONTROL_STOCK_URL + stockVO.getMoneyControlSymbol())
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36")
					.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0)
					.execute();

			Document docStock = responseStock.parse();

			Gson gsonStock = new Gson();
			Map<?, ?> stockDetails = gsonStock.fromJson(docStock.text(), Map.class);
			Map<?, ?> stockData = (Map<?, ?>) stockDetails.get("data");

			nseSymbol = (String) stockData.get("NSEID");
			nseSymbol = nseSymbol.contains("&") ? nseSymbol.replace("&", "%26") : nseSymbol;
			stockName = (String) stockData.get("SC_FULLNM");

			stockVO.setName(stockName);

			System.out.print(stockName);

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
			int count = 0;

			for (Object historyData : historyDatas) {

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

				count++;

				if (count == 14) {
					break;
				}
			}

			double pointsGainAverage = pointsGain.stream().mapToDouble(v -> v).average().getAsDouble();
			double pointsLostAverage = pointsLost.stream().mapToDouble(v -> v).average().getAsDouble();
			double finalCalculation = StocksDataUtil
					.format(100 - (100 / (1 + (pointsGainAverage / pointsLostAverage))));

			indexRSI.put(stockName, finalCalculation);
			System.out.print("\t" + ((Map<?, ?>) historyDatas.get(0)).get("CH_TIMESTAMP") + "\n");
		}
		Map<String, Double> result = indexRSI.entrySet().stream()
				.sorted(Map.Entry.<String, Double>comparingByValue().reversed()).collect(Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

		result.forEach((k, v) -> {

			if (dataToStore.length() == 0) {

				dataToStore.append(k + "\t" + v);
			} else {

				dataToStore.append("\n" + k + "\t" + v);
			}
		});

		StocksDataUtil.writeData("RSI.txt", dataToStore.toString());
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