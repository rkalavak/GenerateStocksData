package com.kalavakuri.generatestocksdata;

import java.util.Scanner;

public class GenerateStocksData {

	public static void main(String[] args) throws Exception {

		Scanner scanner = new Scanner(System.in);

		try {
			AllGoodQuartersStocks.execute();
			System.out.print("\n" + "     AllGoodQuartersStocks Insertion Completed, Do You Want To Proceed: ");
			CurrentQuarterGoodStocksWithGrowthPercentage.execute();
			System.out.print("\n"
					+ "     CurrentQuarterGoodStocksWithGrowthPercentage Insertion Completed, Do You Want To Proceed: ");
			MissingDates.execute();
			System.out.print("\n" + "     MissingDates Completed, Do You Want To Proceed: ");
			MissingDatesNse.execute();
			System.out.print("\n" + "     MissingDatesNse Completed, Do You Want To Proceed: ");
			SharesAverages.execute();
			System.out.print("\n" + "     SharesAverages Insertion Completed, Do You Want To Proceed: ");
			SharesAveragesWithoutCurrentPrice.execute();
			System.out.print(
					"\n" + "     SharesAveragesWithoutCurrentPrice Insertion Completed, Do You Want To Proceed: ");
			GoodShares.execute();
			System.out.print("\n" + "     GoodShares Insertion Completed, Do You Want To Proceed: ");
			GoodSharesNse.execute();
			System.out.print("\n" + "     GoodSharesNse Insertion Completed, Do You Want To Proceed: ");
			scanner.nextLine();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}
}