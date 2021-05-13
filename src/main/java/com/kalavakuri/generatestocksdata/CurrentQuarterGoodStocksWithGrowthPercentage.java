package com.kalavakuri.generatestocksdata;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

public class CurrentQuarterGoodStocksWithGrowthPercentage {

	private static String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static StringBuilder dataToStore = new StringBuilder();

	public static void execute() throws IOException {

		fetchQuarterlyGoodStocks();
		StocksDataUtil.writeData("CurrentQuarterGoodStocksWithGrowthPercentage.txt", dataToStore.toString());
	}

	private static List<StockVO> fetchQuarterlyGoodStocks() throws IOException {

		List<StockVO> stocks = StocksDataUtil.getMoneyControlSymbols();

		for (StockVO stockVO : stocks) {

			String NSESymbol = "";
			String stockName = "";

			Response response = Jsoup.connect(MONEY_CONTROL_STOCK_URL + stockVO.getMoneyControlSymbol())
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
					.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0)
					.execute();

			Document doc = response.parse();

			Gson gson = new Gson();
			Map<?, ?> stockDetails = gson.fromJson(doc.text(), Map.class);
			Map<?, ?> stockData = (Map<?, ?>) stockDetails.get("data");

			try {

				NSESymbol = (String) stockData.get("NSEID");
				stockName = (String) stockData.get("SC_FULLNM");

				stockVO.setNseId(NSESymbol);
				stockVO.setName(stockName);

			} catch (Exception e) {
				e.printStackTrace();
			}

			fetchQuarterlyResults(stockVO);
		}

		return stocks;
	}

	private static void fetchQuarterlyResults(StockVO stockVO) throws IOException {

		Document doc = StocksDataUtil.getStockResponse(stockVO.getName());

		Element quarterlyDivision = doc.getElementById("quarters");
		Element table = quarterlyDivision.getElementsByTag("table").get(0);
		Element tableHead = table.getElementsByTag("thead").get(0);
		Element tableHeadRow = tableHead.getElementsByTag("tr").get(0);
		Elements tableHeadRowData = tableHeadRow.getElementsByTag("th");

		String tableHeadText = tableHeadRowData.text();

		if (isQ1Good(table, stockVO)) {

			if (dataToStore.length() == 0) {

				dataToStore.append(stockVO.getName() + "   " + stockVO.getSalesGrowthPercentage() + "   "
						+ stockVO.getNetProfitGrowthPercentage() + "   "
						+ (tableHeadText.contains("Mar 2021") ? "Q4"
								: (tableHeadText.contains("Dec 2020") ? "Q3"
										: (tableHeadText.contains("Sep 2020") ? "Q2" : "Q1"))));
			} else {

				dataToStore.append("\n" + stockVO.getName() + "   " + stockVO.getSalesGrowthPercentage() + "   "
						+ stockVO.getNetProfitGrowthPercentage() + "   "
						+ (tableHeadText.contains("Mar 2021") ? "Q4"
								: (tableHeadText.contains("Dec 2020") ? "Q3"
										: (tableHeadText.contains("Sep 2020") ? "Q2" : "Q1"))));
			}
		}
	}

	private static boolean isQ1Good(Element table, StockVO stockVO) {

		boolean isGood = false;
		double salesGrowthPercentage;
		double netProfitGrowthPercentage;

		Element tableBody = table.getElementsByTag("tbody").get(0);

		Element salesRow = tableBody.getElementsByTag("tr").get(0);

		Elements salesRowData = salesRow.getElementsByTag("td");

		int salesRowDataSize = salesRowData.size() - 1;

		double currentQuarterSales = Double.parseDouble(salesRowData.get(salesRowDataSize).text().replace(",", ""));
		double pastQuarterSales = Double.parseDouble(salesRowData.get(salesRowDataSize - 4).text().replace(",", ""));

		if (currentQuarterSales > pastQuarterSales) {

			isGood = true;
		}

		if (isGood) {

			Element netProfitRow = tableBody.getElementsByTag("tr").get(9);
			Elements netProfitRowData = netProfitRow.getElementsByTag("td");

			int netProfitRowDataSize = netProfitRowData.size() - 1;

			double currentQuarterProfits = Double
					.parseDouble(netProfitRowData.get(netProfitRowDataSize).text().replace(",", "").replace(",", ""));
			double pastQuarterProfits = Double
					.parseDouble(netProfitRowData.get(netProfitRowDataSize - 4).text().replace(",", ""));

			if (currentQuarterProfits > pastQuarterProfits) {

				isGood = true;

				salesGrowthPercentage = getGrowthPercentage(currentQuarterSales, pastQuarterSales);
				netProfitGrowthPercentage = getGrowthPercentage(currentQuarterProfits, pastQuarterProfits);

				stockVO.setSalesGrowthPercentage(salesGrowthPercentage);
				stockVO.setNetProfitGrowthPercentage(netProfitGrowthPercentage);

			} else {

				isGood = false;
			}
		}

		return isGood;
	}

	private static double getGrowthPercentage(double latestValue, double pastValue) {

		double growthPercentage = ((latestValue / pastValue) * 100) - 100;
		growthPercentage = StocksDataUtil.format(growthPercentage);

		return growthPercentage;
	}
}