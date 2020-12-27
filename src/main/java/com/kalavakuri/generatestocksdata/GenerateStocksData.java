package com.kalavakuri.generatestocksdata;

import java.util.Scanner;

public class GenerateStocksData {

	public static void main(String[] args) throws Exception {

		Scanner scanner = new Scanner(System.in);
		String response = null;

		try {

			System.out.println("\n" + "     Updation Started.");
			AllGoodQuartersStocks.execute();
			System.out.println("\n" + "     AllGoodQuartersStocks Updated.");
			CurrentQuarterGoodStocks.execute();
			System.out.println("\n" + "     CurrentQuarterGoodStocks Updated.");
			CurrentQuarterGoodStocksWithGrowthPercentage.execute();
			System.out.println("\n" + "     CurrentQuarterGoodStocksWithGrowthPercentage Updated.");
			MissingDates.execute();
			System.out.println("\n" + "     MissingDates Check Completed.");
			MissingDatesNse.execute();
			System.out.println("\n" + "     MissingDatesNse Check Completed.");
			SharesAverages.execute();
			System.out.println("\n" + "     SharesAverages Updated.");
			SharesAveragesWithoutCurrentPrice.execute();
			System.out.println("\n" + "     SharesAveragesWithoutCurrentPrice Updated.");
			GoodShares.execute();
			System.out.println("\n" + "     GoodShares Updated.");
			GoodSharesNse.execute();
			System.out.println("\n" + "     GoodSharesNse Updated.");
			System.out.println("\n" + "     Updation Completed, Do You Want To Update Breakout.");
			response = scanner.nextLine();

			if ("y".equalsIgnoreCase(response)) {

				BreakoutUpdate.execute();
				System.out.println("\n" + "     Breakout Updated, Press Any Key To Exit.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}
}