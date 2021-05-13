package com.kalavakuri.generatestocksdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;

import oracle.jdbc.internal.OracleTypes;

public class StocksDataUtil {

	private static NumberFormat formatter = new DecimalFormat("#0.00");
	private static final String FILE_BASE_URL = "C:\\Personal\\Stock Analysis\\";
	private static Map<String, Document> stockNameAndResponse = new HashMap<>();

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

	public static List<StockVO> getMoneyControlSymbols() {

		List<StockVO> stocks = new ArrayList<>();

		Connection connection = null;
		CallableStatement callableStatement = null;
		ResultSet resultSet = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "#knagamma1");
			callableStatement = connection.prepareCall("{call SP_FETCH_STOCKS_DETAILS(?,?,?)}");
			callableStatement.setInt(1, 0);
			callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
			callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
			callableStatement.execute();

			resultSet = (ResultSet) callableStatement.getObject(2);

			while (resultSet.next()) {
				StockVO stock = new StockVO();
				stock.setMoneyControlSymbol(resultSet.getString("MONEY_CONTROL_SYMBOL"));
				stocks.add(stock);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != resultSet && !resultSet.isClosed())
					resultSet.close();
				if (null != callableStatement && !callableStatement.isClosed())
					callableStatement.close();
				if (null != connection && !connection.isClosed())
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return stocks;
	}

	public static void putStockNameAndResponse(String stockName, Document document) {

		stockNameAndResponse.put(stockName, document);
	}

	public static Document getStockResponse(String stockName) {

		return stockNameAndResponse.get(stockName);
	}
}