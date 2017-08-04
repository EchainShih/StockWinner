package com.echain.stockwinner.logic;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.echain.stockwinner.data.ExRDInfo;

public class ExRDFetcher implements ExecutableTask {
	private static final String URL_BASE = "http://fund.bot.com.tw/z/ze/zeb/zeb.djhtm";
	
	private LinkedHashMap<String, ExRDInfo> mExRDMap;
	SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
	private long mLookDayDiff = 7;

	@Override
	public void execute() {
		mExRDMap = new LinkedHashMap<String, ExRDInfo>();
		try {
			Document doc = Jsoup.connect(URL_BASE).get();
			Elements trNodes = doc.getElementsByClass("t01").get(0).getElementsByTag("tr");
			for (int i = 3; i < trNodes.size(); i++) {
				Elements tdNodes = trNodes.get(i).getElementsByTag("td");
				String date = tdNodes.get(1).text();
				long dayDiff = getDateDiff(date);
				if (dayDiff < 0)
					continue;
				if (mLookDayDiff >= 0 && getDateDiff(date) > mLookDayDiff)
					break;
				
				ExRDInfo info = new ExRDInfo();
				info.date = date;
				String stock = tdNodes.get(0).toString();
				int idx = stock.indexOf("ASR");
				if (idx >= 0)
					idx += 3;
				else
					idx = stock.indexOf("AS") + 2;
				int idx2 = stock.indexOf("','");
				int idx3 = stock.indexOf("');");

				//info.stockName = stock.substring(idx2 + 3, idx3);
				info.stockId = stock.substring(idx, idx + 4);
				info.xd = getValue(tdNodes.get(4).text());
				info.xr = getValue(tdNodes.get(9).text());
				mExRDMap.put(info.stockId, info);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LinkedHashMap<String, ExRDInfo> getExRDList() {
		return mExRDMap;
	}
	
	private long getDateDiff(String date) {
		long dateDiff = 0;
		//String yyy = date.substring(0, date.indexOf("/"));
		//String yyyy = String.valueOf(Integer.valueOf(yyy) + 1911);
		//String newDate = date.replace(yyy, yyyy);
		try {
			Calendar c = Calendar.getInstance(Locale.TAIWAN);
			c.set(Calendar.HOUR, 0);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			Date d1 = c.getTime();
			Date d2 = mDateFormat.parse(date);
			dateDiff = (d2.getTime() - d1.getTime()) / (24 * 60 * 60 * 1000);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateDiff;
	}
	
	private float getValue(String s) {
		float v = 0.0f;
		try {
			v = Float.valueOf(s);
		} catch (Exception e) {
		}
		return v;
	}
}
