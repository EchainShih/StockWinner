package com.echain.stockwinner.logic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.content.Context;
import android.util.Log;

public class StockNamesFetcher {
	private static final String TAG = "StockNamesFetcher";
	
	private static final String URL_BASE = "http://isin.twse.com.tw/isin/class_main.jsp?";
	private static final String URL_REQUESTS[] = {
		"market=1&issuetype=1",  // Public, Stock
		"market=1&issuetype=I",  // Public, ETF
		"market=1&issuetype=J",  // Public, TDR
		"market=2&issuetype=4",  // OTC, Stock
		"market=2&issuetype=1",  // OTC, Stock 2
		"market=2&issuetype=3",  // OTC, ETF
		"market=2&issuetype=J",  // OTC, TDR
	};
	private static final String FILE_NAME = "stock_names";
	
	private Context mContext;
	private ArrayList<String> mStockNames = new ArrayList<String>();
	private String[] mStockNamesArray = null;
	private HashMap<String, String> mIdNameMap = new HashMap<String, String>();
	
	public StockNamesFetcher(Context context) {
		mContext = context;
	}
	
	public String getNameById(String stockId) {
		return mIdNameMap.get(stockId);
	}
	
	public String getIdByName(String name) {
		for (Entry<String, String> entry : mIdNameMap.entrySet()) {
			if (entry.getValue().equals(name))
				return entry.getKey();
		}
		return null;
	}
	
	public String[] getArray() {
		return mStockNamesArray;
	}
	
	public boolean refetch() {
		mContext.deleteFile(FILE_NAME);
		mIdNameMap.clear();
		mStockNames.clear();
		return fetch();
	}

	public boolean fetch() {
		if (!loadFromFile()) {
			for (String request : URL_REQUESTS) {
				grabbing(request);
			}
			writeToFile();
		}
		mStockNamesArray = new String[mStockNames.size()];
		mStockNames.toArray(mStockNamesArray);
		mStockNames.clear();
		return true;
	}
	
	private void grabbing(String request) {
		try {
			Document doc = Jsoup.connect(URL_BASE + request).get();
			Elements trNodes = doc.getElementsByTag("tr");
			for (int i = 1; i < trNodes.size(); i++) {
				Elements tdNodes = trNodes.get(i).children();
				if (tdNodes.size() >= 4) {
					mIdNameMap.put(tdNodes.get(2).text(), tdNodes.get(3).text());
					mStockNames.add(tdNodes.get(2).text() + " " + tdNodes.get(3).text());
				} else {
					Log.w(TAG, "Size less then 4");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean loadFromFile() {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		boolean result = false;
		
		try {
			fis = mContext.openFileInput(FILE_NAME);
			isr = new InputStreamReader(fis);
			reader = new BufferedReader(isr);
			String line;
			while ((line = reader.readLine()) != null) {
				String[] args = line.split(" ");
				mIdNameMap.put(args[0], args[1]);
				mStockNames.add(line);
			}
			result = mStockNames.size() != 0;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (isr != null)
					isr.close();
				if (fis != null)
					fis.close();
			} catch (IOException e) {
			}
		}
		return result;
	}
	
	private boolean writeToFile() {
		FileOutputStream fos = null;
		boolean result = false;
		
		try {
			fos = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
			for (String stockName : mStockNames) {
				fos.write(stockName.getBytes());
				fos.write("\n".getBytes());
			}
			result = true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
