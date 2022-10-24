package com.kalavakuri.generatestocksdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;

public class RSIStockCalculator {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final String NSE_HISTORY_URL = "https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=&segmentLink=3&symbolCount=2&series=ALL&dateRange=1month&fromDate=&toDate=&dataType=PRICEVOLUMEDELIVERABLE";

	public static void main(String[] args) throws Exception {

		Map<String, Double> indexRSI = new HashMap<>();

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

			List<Double> pointsGain = new ArrayList<>();
			List<Double> pointsLost = new ArrayList<>();
			double previousValue = 0.00;
			String currentDate = "";
			int startIndex = eachRow.length-15;

			for (int i = startIndex; i < eachRow.length; i++) {

				String[] split = eachRow[i].split(",");

				double currentValue = Double.parseDouble(split[8].replace("\"", "").trim());
				currentDate = split[2].replace("\"", "").trim();
				if (i != startIndex) {
					if ((currentValue - previousValue) >= 0) {
						pointsGain.add(currentValue - previousValue);
					} else {
						pointsGain.add(0.00);
					}

					if ((previousValue - currentValue) >= 0) {
						pointsLost.add(previousValue - currentValue);
					} else {
						pointsLost.add(0.00);
					}
				}
				previousValue = currentValue;
			}
			double pointsGainAverage = pointsGain.stream().mapToDouble(v -> v).average().getAsDouble();
			double pointsLostAverage = pointsLost.stream().mapToDouble(v -> v).average().getAsDouble();
			double finalCalculation = StocksDataUtil
					.format(100 - (100 / (1 + (pointsGainAverage / pointsLostAverage))));

			indexRSI.put(stockName, finalCalculation);
			System.out.print("\t" + currentDate + "\n");
		}
		Map<String, Double> result = indexRSI.entrySet().stream()
				.sorted(Map.Entry.<String, Double>comparingByValue().reversed()).collect(Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

		result.forEach((k, v) -> System.out.println(k + "\t" + v));
	}
}