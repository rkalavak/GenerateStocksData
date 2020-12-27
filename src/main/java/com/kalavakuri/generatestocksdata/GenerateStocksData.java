package com.kalavakuri.generatestocksdata;

import java.util.Scanner;

public class GenerateStocksData {

	public static void main(String[] args) throws Exception {

		Scanner scanner = new Scanner(System.in);
		String response = "";

		try {
			AllGoodQuartersStocks.execute();
			System.out.print("\n" + "     AllGoodQuartersStocks Insertion Completed, Do You Want To Proceed: ");
			response = scanner.nextLine();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if ("y".equalsIgnoreCase(response)) {

			try {
				CurrentQuarterGoodStocks.execute();
				System.out.print("\n" + "     CurrentQuarterGoodStocks Insertion Completed, Do You Want To Proceed: ");
				response = scanner.nextLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ("y".equalsIgnoreCase(response)) {

			try {
				CurrentQuarterGoodStocksWithGrowthPercentage.execute();
				System.out.print("\n"
						+ "     CurrentQuarterGoodStocksWithGrowthPercentage Insertion Completed, Do You Want To Proceed: ");
				response = scanner.nextLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ("y".equalsIgnoreCase(response)) {

			try {
				MissingDates.execute();
				System.out.print("\n" + "     MissingDates Completed, Do You Want To Proceed: ");
				response = scanner.nextLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ("y".equalsIgnoreCase(response)) {

			try {
				MissingDatesNse.execute();
				System.out.print("\n" + "     MissingDatesNse Completed, Do You Want To Proceed: ");
				response = scanner.nextLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ("y".equalsIgnoreCase(response)) {

			try {
				SharesAverages.execute();
				System.out.print("\n" + "     SharesAverages Insertion Completed, Do You Want To Proceed: ");
				response = scanner.nextLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ("y".equalsIgnoreCase(response)) {

			try {
				SharesAveragesWithoutCurrentPrice.execute();
				System.out.print(
						"\n" + "     SharesAveragesWithoutCurrentPrice Insertion Completed, Do You Want To Proceed: ");
				response = scanner.nextLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ("y".equalsIgnoreCase(response)) {

			try {
				GoodShares.execute();
				System.out.print("\n" + "     GoodShares Insertion Completed, Do You Want To Proceed: ");
				response = scanner.nextLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ("y".equalsIgnoreCase(response)) {

			try {
				GoodSharesNse.execute();
				System.out.print("\n" + "     GoodSharesNse Insertion Completed, Do You Want To Proceed: ");
				response = scanner.nextLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		scanner.close();
	}
}
