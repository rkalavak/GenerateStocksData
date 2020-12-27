package com.kalavakuri.generatestocksdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StocksDataUtil {

	private static NumberFormat formatter = new DecimalFormat("#0.00");
	private static final String FILE_BASE_URL = "C:\\Personal\\Stock Analysis\\";

	public static double format(double value) {
		return Double.parseDouble(formatter.format(value));
	}

	public static void writeData(String fileName, String data) throws IOException {

		File file = new File(FILE_BASE_URL.concat(fileName));
		FileWriter fileReader = new FileWriter(file);
		BufferedWriter bufferedReader = new BufferedWriter(fileReader);

		bufferedReader.write(data);

		fileReader.flush();
		bufferedReader.flush();
		fileReader.close();
		bufferedReader.close();
	}
}
