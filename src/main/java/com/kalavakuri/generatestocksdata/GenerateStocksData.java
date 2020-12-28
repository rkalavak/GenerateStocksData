package com.kalavakuri.generatestocksdata;

import java.util.Scanner;

public class GenerateStocksData {

	public static void main(String[] args) throws Exception {

		Scanner scanner = new Scanner(System.in);
		String response = null;

		try {

			System.out.print("\n\n" + "     Updation Started.");
			AllGoodQuartersStocks.execute();
			System.out.print("\n\n" + "     AllGoodQuartersStocks Updated.");
			CurrentQuarterGoodStocks.execute();
			System.out.print("\n\n" + "     CurrentQuarterGoodStocks Updated.");
			CurrentQuarterGoodStocksWithGrowthPercentage.execute();
			System.out.print("\n\n" + "     CurrentQuarterGoodStocksWithGrowthPercentage Updated.");
			MissingDates.execute();
			System.out.print("\n\n" + "     MissingDates Check Completed.");
			MissingDatesNse.execute();
			System.out.print("\n\n" + "     MissingDatesNse Check Completed.");
			SharesAverages.execute();
			System.out.print("\n\n" + "     SharesAverages Updated.");
			SharesAveragesWithoutCurrentPrice.execute();
			System.out.print("\n\n" + "     SharesAveragesWithoutCurrentPrice Updated.");
			GoodShares.execute();
			System.out.print("\n\n" + "     GoodShares Updated.");
			GoodSharesNse.execute();
			System.out.print("\n\n" + "     GoodSharesNse Updated.");
			System.out.print("\n\n" + "     Do You Want To Update Breakout: ");
			response = scanner.nextLine();

			if ("y".equalsIgnoreCase(response)) {

				BreakoutUpdate.execute();
				System.out.print("\n" + "     Breakout Updated");
				System.out.print("\n\n" + "     All Updated, Press Any Key To Exit...");
				scanner.nextLine();
			} else {
				System.out.print("\n" + "     All Updated, Press Any Key To Exit...");
				scanner.nextLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}
}