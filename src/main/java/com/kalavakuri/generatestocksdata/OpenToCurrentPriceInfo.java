package com.kalavakuri.generatestocksdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

public class OpenToCurrentPriceInfo {

	private static final Double INVESTMENT_AMOUNT = Double.valueOf(1900000);
	private static final String NSE_URL = "https://www.nseindia.com/api/quote-equity?symbol=";
	private static final String REFERRER_URL = "https://www.nseindia.com/get-quotes/equity?symbol=";
	private static final String FILE_BASE_URL = "C:\\Users\\NF54BI\\Personal\\ShareMarket\\";
	private static final List<String> stockSymbols = new ArrayList<>();
	private static Map<String, String> cookies = null;

	static {

		File file = new File(FILE_BASE_URL.concat("TodayBestStocks.txt"));

		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.exit(0);
		}

		String line;
		try {
			while ((line = bufferedReader.readLine()) != null) {

				String[] stockRow = line.split(":");
				String stockSymbol = stockRow[0];

				stockSymbols.add(stockSymbol);
			}

		} catch (IOException e) {
			System.exit(0);
		}
	}

	private static final Double EACH_STOCK_AMOUNT = Double.valueOf(INVESTMENT_AMOUNT / stockSymbols.size());

	public static void main(String[] args) throws IOException, InterruptedException {

		getCookies();

		List<Double> openBuyPriceAmount = new ArrayList<>();
		List<Double> lastBuyPriceAmount = new ArrayList<>();
		List<Double> previousBuyPriceAmount = new ArrayList<>();

		for (;;) {
			for (String nseSymbol : stockSymbols) {

				try {
					Response historyResponse = Jsoup.connect(NSE_URL.replace("symbol=", "symbol=" + nseSymbol))
							.ignoreContentType(true).referrer(REFERRER_URL.replace("symbol=", "symbol=" + nseSymbol))
							.userAgent(
									"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
							.timeout(90 * 1000).header("Accept", "application/json").cookies(cookies)
							.followRedirects(true).maxBodySize(0).execute();

					Document docHistory = historyResponse.parse();

					Gson gso = new Gson();
					Map<?, ?> tradeDetails = gso.fromJson(docHistory.text(), Map.class);
					Map<?, ?> priceInfo = (Map<?, ?>) tradeDetails.get("priceInfo");

					double lastPrice = (Double) priceInfo.get("close");
					double previousClose = (Double) priceInfo.get("previousClose");
					double open = (Double) priceInfo.get("open");

					double quantityToBuy = calculateQuantity(open);

					openBuyPriceAmount.add(quantityToBuy * open);
					lastBuyPriceAmount.add(quantityToBuy * lastPrice);
					previousBuyPriceAmount.add(quantityToBuy * previousClose);

				} catch (Exception e) {
					System.out.println("Failed for: " + nseSymbol);
				}
			}

			double totalOpenBuyPriceAmount = openBuyPriceAmount.stream().mapToDouble(Double::doubleValue).sum();
			double totalLastBuyPriceAmount = lastBuyPriceAmount.stream().mapToDouble(Double::doubleValue).sum();
			double totalPreviousBuyPriceAmount = previousBuyPriceAmount.stream().mapToDouble(Double::doubleValue).sum();

			System.out.println(new Date() + " - ("
					+ StocksDataUtil.format((totalLastBuyPriceAmount - totalOpenBuyPriceAmount)) + ")" + " - ("
					+ StocksDataUtil.format((totalLastBuyPriceAmount - totalPreviousBuyPriceAmount)) + ")");

			openBuyPriceAmount.clear();
			lastBuyPriceAmount.clear();
			previousBuyPriceAmount.clear();

			Thread thread = new Thread(new GetCookieThread());
			thread.start();

			Thread.sleep(180000);
		}
	}

	private static void getCookies() throws IOException {

		Response response = null;

		try {

			response = Jsoup.connect("https://www.nseindia.com/").ignoreContentType(true).userAgent(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36")
					.timeout(90 * 1000).followRedirects(true).maxBodySize(0).execute();

		} catch (Exception e) {
			e.printStackTrace();
		}

		cookies = response.cookies();
	}

	private static int calculateQuantity(double price) {

		double quantity = EACH_STOCK_AMOUNT / price;

		return (int) quantity;
	}

	static class GetCookieThread implements Runnable {

		@Override
		public void run() {
			try {
				getCookies();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
