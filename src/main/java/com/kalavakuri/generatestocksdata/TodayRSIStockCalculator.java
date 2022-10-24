package com.kalavakuri.generatestocksdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

public class TodayRSIStockCalculator {

	private static final String FILE_BASE_URL = "C:\\Personal\\Stock Analysis\\";
	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final Map<String, List<Double>> RSISums = new HashMap<>();

	static {

		File file = new File(FILE_BASE_URL.concat("RSISums.txt"));

		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.exit(0);
		}

		String line;
		try {
			while ((line = bufferedReader.readLine()) != null) {

				String[] stockAndAverages = line.split("@");
				String stockName = stockAndAverages[0];
				double pointsGain = Double.parseDouble(stockAndAverages[1]);
				double pointsLost = Double.parseDouble(stockAndAverages[2]);

				List<Double> pointsGainAndLostList = new ArrayList<>();
				pointsGainAndLostList.add(pointsGain);
				pointsGainAndLostList.add(pointsLost);

				RSISums.put(stockName, pointsGainAndLostList);
			}
		} catch (IOException e) {
			System.exit(0);
		}
	}

	public static void main(String[] args) throws Exception {

		Map<String, Double> indexRSI = new HashMap<>();

		for (StockVO stockVO : StocksDataUtil.getMoneyControlSymbols()) {

			String stockName = "";
			double todaysPrice = 0.00;
			double pricePrevClose = 0.00;
			double pointsGain = 0.00;
			double pointsLost = 0.00;
			double pointsGainAverage = 0.00;
			double pointsLostAverage = 0.00;

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

			stockName = (String) stockData.get("SC_FULLNM");
			todaysPrice = Double.parseDouble((String) stockData.get("pricecurrent"));
			pricePrevClose = Double.parseDouble((String) stockData.get("priceprevclose"));

			if ((todaysPrice - pricePrevClose) >= 0) {
				pointsGain = todaysPrice - pricePrevClose;
			}

			if ((pricePrevClose - todaysPrice) >= 0) {
				pointsLost = pricePrevClose - todaysPrice;
			}
			List<Double> pointsGainAndLostList = RSISums.get(stockName);
			pointsGain = pointsGain + pointsGainAndLostList.get(0);
			pointsLost = pointsLost + pointsGainAndLostList.get(1);
			pointsGainAverage = pointsGain / 14;
			pointsLostAverage = pointsLost / 14;
			double finalCalculation = StocksDataUtil
					.format(100 - (100 / (1 + (pointsGainAverage / pointsLostAverage))));
			indexRSI.put(stockName, finalCalculation);

		}
		Map<String, Double> result = indexRSI.entrySet().stream()
				.sorted(Map.Entry.<String, Double>comparingByValue().reversed()).collect(Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

		result.forEach((k, v) -> System.out.println(k + "\t" + v));
	}
}