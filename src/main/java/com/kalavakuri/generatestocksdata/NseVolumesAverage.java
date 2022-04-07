package com.kalavakuri.generatestocksdata;

import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

public class NseVolumesAverage {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static StringBuilder dataToStore = new StringBuilder();

	public static void main(String[] args) throws Exception {

		for (StockVO stockVO : StocksDataUtil.getMoneyControlSymbols()) {

			String nseSymbol = "";
			String stockName = "";

			Response responseStock = Jsoup.connect(MONEY_CONTROL_STOCK_URL + stockVO.getMoneyControlSymbol())
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36")
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

			System.out.println(stockName);

			double tradedQuantityAvg = Double.parseDouble((String) stockData.get("DVolAvg20"));
			double deliveryToTradedQuantityAvg = Double.parseDouble((String) stockData.get("AvgDelVolPer_20day"));

			if (dataToStore.length() == 0) {

				dataToStore.append(stockVO.getName() + "#" + tradedQuantityAvg + " " + deliveryToTradedQuantityAvg);
			} else {

				dataToStore
						.append("\n" + stockVO.getName() + "#" + tradedQuantityAvg + " " + deliveryToTradedQuantityAvg);
			}
		}

		StocksDataUtil.writeData("NseVolumesAverage.txt", dataToStore.toString());
	}
}