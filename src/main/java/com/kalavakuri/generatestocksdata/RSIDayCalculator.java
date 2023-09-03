package com.kalavakuri.generatestocksdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

public class RSIDayCalculator {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final List<Double> prices = new ArrayList<>();

	public static void main(String[] args) throws Exception {

		System.out.println(new Date());

		for (;;) {
			Response responseStock = Jsoup.connect(MONEY_CONTROL_STOCK_URL + "IT").ignoreContentType(true).userAgent(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36")
					.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0)
					.execute();

			Document docStock = responseStock.parse();

			Gson gsonStock = new Gson();
			Map<?, ?> stockDetails = gsonStock.fromJson(docStock.text(), Map.class);
			Map<?, ?> stockData = (Map<?, ?>) stockDetails.get("data");

			prices.add(Double.parseDouble((String) stockData.get("pricecurrent")));

			if (prices.size() >= 15) {

				List<Double> stockHistoryDetailsReverse = new ArrayList<>();

				for (int i = prices.size() - 1; i >= 0; i--) {
					stockHistoryDetailsReverse.add(prices.get(i));
				}

				List<Double> pointsGain = new ArrayList<>();
				List<Double> pointsLost = new ArrayList<>();
				int count = 0;

				for (int i = 0; i < stockHistoryDetailsReverse.size(); i++) {

					double closingPrice = stockHistoryDetailsReverse.get(i);
					double prevClosingPrice = stockHistoryDetailsReverse.get(i + 1);

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

				System.out.println(new Date() + " - " + prices.get(prices.size() - 1) + " - " + finalCalculation);
			}

			Thread.sleep(60000);
		}
	}
}