package com.kalavakuri.generatestocksdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;

public class HistoricalRSIStockCalculator {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final String NSE_HISTORY_URL = "https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=&segmentLink=3&symbolCount=2&series=ALL&dateRange=3month&fromDate=&toDate=&dataType=PRICEVOLUMEDELIVERABLE";

	public static void main(String[] args) throws Exception {

		String nseSymbol = "";
		String stockName = "";

		Response responseStock = Jsoup.connect(MONEY_CONTROL_STOCK_URL + "LGB").ignoreContentType(true).userAgent(
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

		for (int j = 0; j < eachRow.length - 14; j++) {
			int i = j;
			int size = i + 14;
			int k = i;
			String finalDate = "";
			pointsGain.clear();
			pointsLost.clear();
			for (; i <= size; i++) {

				String[] split = eachRow[i].split(",");

				finalDate = split[2].replace("\"", "").trim();
				double currentValue = Double.parseDouble(split[8].replace("\"", "").trim());
				if (i != k) {
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

			System.out.println(finalDate + "\t" + finalCalculation);
		}
	}
}