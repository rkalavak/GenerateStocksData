package com.kalavakuri.generatestocksdata;

import java.util.Arrays;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;

public class NseVolumesAverageFromNSE {

	private static final String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static final String NSE_HISTORY_URL = "https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=&segmentLink=3&symbolCount=2&series=ALL&dateRange=12month&fromDate=&toDate=&dataType=PRICEVOLUMEDELIVERABLE";
	private static StringBuilder dataToStore = new StringBuilder();

	public static void main(String[] args) throws Exception {

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

			int historySize = (eachRow.length) - 1;

			double tradedQuantitySum = 0.00;
			double deliveryToTradedQuantitySum = 0.00;
			double tradedQuantityAvg = 0.00;
			double deliveryToTradedQuantityAvg = 0.00;

			for (int i = historySize; i >= 1; i--) {

				String[] split = eachRow[i].split(",");
				double tradedQuantity = Double.parseDouble(split[10].replace("\"", "").trim());
				double deliveryToTradedQuantity = Double.parseDouble(split[14].replace("\"", "").trim());

				if (i > historySize - 20) {
					deliveryToTradedQuantitySum = deliveryToTradedQuantitySum + deliveryToTradedQuantity;
					tradedQuantitySum = tradedQuantitySum + tradedQuantity;
					continue;
				} else {
					tradedQuantityAvg = StocksDataUtil.format(tradedQuantitySum / 20.00);
					deliveryToTradedQuantityAvg = StocksDataUtil.format(deliveryToTradedQuantitySum / 20.00);
					break;
				}
			}

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