package com.donzbox.file.action;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


/**
 * ------------------------------------------------------------------------------------------------------------------
 * IP���� ���� �˾Ƴ���
 * ------------------------------------------------------------------------------------------------------------------
 * [�ؿܸ�]
 * http://wq.apnic.net/apnic-bin/whois.pl
 * http://wq.apnic.net/apnic-bin/whois.pl?searchtext=183.60.204.158
 * http://hexillion.com/samples/WhoisXML/?query=183.60.204.158
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 * [������]
 * http://whois.kisa.or.kr/openapi/whois.jsp?query=[�������̸�, IP�ּ�, AS��ȣ]&key=2014011617403609763399&answer=xml
 * ����) http://whois.kisa.or.kr/openapi/whois.jsp?key=2014011617403609763399&answer=json&query=211.129.87.205
 * ����) http://whois.kisa.or.kr/openapi/whois.jsp?key=2014011617403609763399&answer=xml&query=1.240.111.238
 * ------------------------------------------------------------------------------------------------------------------
 */
public class FinderXmlParser {

	public static void main(String [] args) {
		FinderXmlParser f = new FinderXmlParser();
		f.getWhois("211.129.87.205");
	}
	
	public String getWhois(String addr) {
		
    	String text = "";
    	addr = "http://wq.apnic.net/apnic-bin/whois.pl?searchtext=" + addr;
		try {
			Document doc = Jsoup.connect(addr).get();
			text = doc.text();
			text = text.replaceAll(":        ", " : ");
			text = text.replaceAll("%", "\n��");
			text = text.substring(0, text.indexOf("Bold: ") -2);

//			BufferedWriter out = new BufferedWriter(new FileWriter("out.txt"));
//			out.write(text);
//			out.newLine();
			
			int i = 0;
			for (String str : text.split("\n")) {
				
				if (str.indexOf("country : ") > -1) {
					text = "[" + str + "]";
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}
}
