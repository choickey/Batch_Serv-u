package com.donzbox.file.action;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


/**
 * ------------------------------------------------------------------------------------------------------------------
 * IP접속 정보 알아내기
 * ------------------------------------------------------------------------------------------------------------------
 * [해외망]
 * http://wq.apnic.net/apnic-bin/whois.pl
 * http://wq.apnic.net/apnic-bin/whois.pl?searchtext=183.60.204.158
 * http://hexillion.com/samples/WhoisXML/?query=183.60.204.158
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 * [국내망]
 * http://whois.kisa.or.kr/openapi/whois.jsp?query=[도메인이름, IP주소, AS번호]&key=2014011617403609763399&answer=xml
 * 예제) http://whois.kisa.or.kr/openapi/whois.jsp?key=2014011617403609763399&answer=json&query=211.129.87.205
 * 예제) http://whois.kisa.or.kr/openapi/whois.jsp?key=2014011617403609763399&answer=xml&query=1.240.111.238
 * ------------------------------------------------------------------------------------------------------------------
 */
public class FinderXmlParser {

	public static void main(final String [] args) {
		final FinderXmlParser f = new FinderXmlParser();
		//f.getWhois("211.129.87.205");
		f.getNationBydbIp("191.28.61.18");
	}

	public String getWhois(String addr) {

		String text = "";
		addr = "http://wq.apnic.net/apnic-bin/whois.pl?searchtext=" + addr;
		try {
			final Document doc = Jsoup.connect(addr).get();
			text = doc.text();
			text = text.replaceAll(":        ", " : ");
			text = text.replaceAll("%", "\n■");
			text = text.substring(0, text.indexOf("Bold: ") -2);

			//			BufferedWriter out = new BufferedWriter(new FileWriter("out.txt"));
			//			out.write(text);
			//			out.newLine();

			final int i = 0;
			for (final String str : text.split("\n")) {

				if (str.indexOf("country : ") > -1) {
					text = "[" + str + "]";
					break;
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return text;
	}

	public String getNationBydbIp(final String ip) {
		String address   ="";
		String countryEN = "";
		String context   = "http://api.db-ip.com/v2/free/" + ip;
		Document doc = null;
		try {
			doc = Jsoup.connect(context)
					.header("content-type", "application/json;charset=UTF-8")
					.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
					.header("accept-encoding", "gzip, deflate, br")
					.header("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36")
					.ignoreContentType(true).get();

			final JSONParser jpr = new JSONParser();
			final JSONObject temp = (JSONObject) jpr.parse(doc.text());
			countryEN = (String) temp.get("countryCode");
			address = (String) temp.get("countryName") + " " + (String) temp.get("stateProv") + " " + (String) temp.get("city");
			context = (String) temp.get("ipAddress");
			System.out.println(countryEN + "\r\n" + address + "\r\n" + context);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return doc.text();
	}
}
