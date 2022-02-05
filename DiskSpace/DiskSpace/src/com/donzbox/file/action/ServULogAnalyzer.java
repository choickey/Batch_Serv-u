package com.donzbox.file.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * ------------------------------------------------------------------------------------------------------------------
 * IP접속 정보 알아내기
 * ------------------------------------------------------------------------------------------------------------------
 * [해외망]
 * http://wq.apnic.net/apnic-bin/whois.pl?searchtext=" + ip;
 * http://hexillion.com/samples/WhoisXML/?query=183.60.204.158
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 * [국내망]
 * http://whois.kisa.or.kr/openapi/whois.jsp?query=[도메인이름, IP주소, AS번호]&key=2014011617403609763399&answer=xml
 * 예제) http://whois.kisa.or.kr/openapi/whois.jsp?query=211.129.87.205&key=2014011617403609763399&answer=json
 * 예제) http://whois.kisa.or.kr/openapi/whois.jsp?query=1.240.111.238&key=2014011617403609763399&answer=json
 * ------------------------------------------------------------------------------------------------------------------
 */
public class ServULogAnalyzer {

	// 로그 루트 경로
	private static String logPath = "D:\\My Data\\WWW\\Ftp Factory\\ftp.DonzBox.com_Result\\copy_log\\";
	public static final String D1 = "㎯";
	public static final int LIMIT = 30;	// 불법 접근 시도 횟수 제한
	private static String args; 
	
	private static final String START = "START";
	private static final String END = "END";
	private static String sysYear = "0000\\";
	private static String sysMonth = "00\\";

	private Map<String, String> mapIDtoName = new HashMap<String, String>();
	private Map<String, List<String>> logHash = new HashMap<String, List<String>>();
	private List<String> logList = new ArrayList<String>();
	private List<String> popList = new ArrayList<String>();
	private DecimalFormat df1 = new DecimalFormat("0000");
	private DecimalFormat df2 = new DecimalFormat("#,###");
	private DecimalFormat df3  = new DecimalFormat("###0.00");
	private NumberFormat nf = NumberFormat.getNumberInstance();
	private boolean isMonth;

	// 메소드의 수행시간 구하기
	private long startTime;
	private long endTime;

	public static void main(String[] arg) {

		args = arg[0];
		// 읽을 파일
		if (arg.length == 0) {
			System.out.println("파일을 선택해 주세요.");
			return;
		}
	}
	
	public ServULogAnalyzer() {
		// 초기화
		init(args);
		
		// Sent 다운로그 추출
		sentLog();
		
		// Received 업로그 추출
		receivedLog();
		
		// Popular 인기파일 추출
		popularLog();

		// 불법 사용자 추출
		illegalLog();

		// 로그파일 출력
//		writeLog();
	}

	public ServULogAnalyzer(String fileName, Map<String, String> mapIDtoName) {
		// ID값을 NAME으로 변경
		this.mapIDtoName = mapIDtoName;
		init(fileName);
	}
	
	public void init(String fileName) {
		// 읽을 파일
		args = fileName + ".log";
		sysYear = fileName.substring(0, 4) + "\\";
		isMonth = (fileName.length() == 6)?true:false;
		sysMonth = isMonth?fileName.substring(4, 6) + "\\":sysMonth;
		// 로그파일 읽기
		long time1 = System.currentTimeMillis();
		System.out.println("\n### 파일읽기 : " + logPath + sysYear + args);
		readLog();
		long time2 = System.currentTimeMillis();
		System.out.println ("### File Reading Time : " + ( time2 - time1 ) / 1000.0 + " S");
	}

	public List<String> sentLog() {
		List<String> logVal = upDownRank("Sent");
		List<String> logTmp = upDownRank("Received");
		return rtnLog(logVal, logTmp);
	}

	public List<String> receivedLog() {
		List<String> logVal = upDownRank("Received");
		List<String> logTmp = upDownRank("Sent");
		return rtnLog(logVal, logTmp);
	}
	
	public List<String> illegalLog() {
		String key="", bKey="", val="", time="";
		String tmp00="", tmp01="", tmp02="", tmp03="";
		List<String> tmpList = new ArrayList<String>();
		List<String> logVal = new ArrayList<String>();
		for (int i=0 ; i<logList.size() ; i++) {
			key = logList.get(i);
			logVal.addAll(logHash.get(key));
			for (int j=0 ; j<logVal.size() ; j++) {
				val = logVal.get(j).split(D1)[0];
				time = logVal.get(j).split(D1)[1];
				// IP 추출
				if (val.indexOf("Connected to") > -1) {
					tmp01 = val.split(" ")[2];
					tmp01 = tmp01.replaceAll("\"", "");
				}				
				// ID 추출
				if (val.indexOf("Invalid login") > -1) {
					tmp02 = val.split(" ")[4];
					tmp02 = tmp02.replaceAll("\"", "");
					tmp02 = tmp02.replaceAll(";", "");
				}
				if (tmp01.length() > 0 && tmp02.length() > 0) {
//					System.out.println(tmp01 + D1 + tmp02 + D1 + time);
					tmpList.add(tmp01 + D1 + tmp02 + D1 + time);
				}
			}
			tmp01 = ""; tmp02 = "";
			logVal.clear();
		}
		
		// 추출된 Invalid login credentials의 IP + ID 통합정렬
		Collections.sort(tmpList);
		int cnt = 0;
		List<String> rtnList = new ArrayList<String>();
		Map<String, Integer> rtnHash = new HashMap<String, Integer>();
		for (int i=0 ; i<tmpList.size() ; i++) {
			key = tmpList.get(i);
//			System.out.println("[ ] " + i + " : " + bKey + " = " + key);
			// [A] 첫줄은 b변수 초기화
			if (i == 0) bKey = key;
			
			// [B] 중간줄을 logHash에 넣기
			if (!bKey.equals(key)) {
				rtnList.add(bKey);
				rtnHash.put(bKey, cnt);
//				System.out.println("[B] " + i + " : " + bKey + " = " + key);
				cnt = 0;
			}
			cnt++;
			bKey = key;
			
			// [C] 마지막줄을 logHash에 넣기
			if (i == tmpList.size() -1) {
				rtnList.add(bKey);
				rtnHash.put(bKey, cnt);
//				System.out.println("[C] " + i + " : " + bKey + " = " + key);
			}		
		}

		// 추출된 Invalid login credentials의 IP별 정렬
		cnt = 0;
		long cVal=0, tVal=0;
		String iVal="";
		List<String> id2name = new ArrayList<String>();
		List<Long> invList = new ArrayList<Long>();
		Map<Long, String> invHash = new HashMap<Long, String>();
		
		// 접속시간 정보(최초 부터 최후 까지)
		for (int i=0 ; i<rtnList.size() ; i++) {
			key = rtnList.get(i).split(D1)[0];
			time = rtnList.get(i).split(D1)[2];
			cVal = rtnHash.get(rtnList.get(i));
			
			// [A] 첫줄은 b변수 초기화
			if (i == 0) { bKey=key; }
			
			// [B] 중간줄을 logHash에 넣기
			if (!bKey.equals(key)) {
//																			System.out.println(tVal + " = " + bKey + "：" + iVal.substring(0, iVal.length()-1));
				tVal = Long.parseLong(String.valueOf(tVal) + df1.format(cnt==9999?cnt=0:++cnt));
				invList.add(tVal);
				invHash.put(tVal, iVal.substring(0, iVal.length()-1));
				tVal = 0;
				iVal = "";
			}
			// 시간정보 붙이기
			iVal = iVal + time + ",";
			tVal = tVal + cVal;
			bKey = key;
			
			// [C] 마지막줄을 logHash에 넣기
			if (i == rtnList.size() -1) {
//																			System.out.println(tVal + " = " + bKey + " , " + iVal.substring(0, iVal.length()-1));
				tVal = Long.parseLong(String.valueOf(tVal) + df1.format(cnt==9999?cnt=0:++cnt));
				invList.add(tVal);
				invHash.put(tVal, iVal.substring(0, iVal.length()-1));
			}
		}
		// 접속시간 정보(최초,최후 만)
		Map<Long, String> tHash = new HashMap<Long, String>();
		for (int i=0 ; i<invList.size() ; i++) {
			tVal = invList.get(i);
			iVal = invHash.get(tVal);
			List<String> tList = new ArrayList<String>();
			for (String t : iVal.split(",")) {
				tList.add(t);
			}
			Collections.sort(tList);
			// time 최소값:tList.get(0), 최대값:tList.get(tList.size()-1)
			tHash.put(tVal, tList.get(0) + tList.get(tList.size()-1));
		}

		// 디렉토리 이름 구성
		invList.clear();
		invHash.clear();
		iVal="";
		cnt=0; cVal=0; tVal=0;
		String nVal="";
		boolean bool=false, bBool=false;
		for (int i=0 ; i<rtnList.size() ; i++) {
			try {
				key = rtnList.get(i).split(D1)[0];
				val = rtnList.get(i).split(D1)[1];
				// 털린 id가 name map db에 존재하면 이름으로 변경
				nVal = mapIDtoName.get(val.toUpperCase());
				if (nVal != null) {
					val = val + "＊";
//					val = val + "(" + nVal + ")";
					val = val + "(" +  ""  + ")";	// 정보보안을 위해 그냥 이름은 가려쥬쟈
					id2name.add(val);  // 털린 이름 추출
				}
				cVal = rtnHash.get(rtnList.get(i));
				
				// [A] 첫줄은 b변수 초기화
				if (i == 0) { bKey=key; bBool=bool; }
				
				// [B] 중간줄을 logHash에 넣기
				if (!bKey.equals(key)) {
					tVal = Long.parseLong(String.valueOf(tVal) + df1.format(cnt==9999?cnt=0:++cnt));
					invList.add(tVal);
					// IP -> 국가정보 매핑
//					System.out.println((bBool?"(○)":"(　)") + String.format("%16s", bKey) + "：" + iVal.substring(0, iVal.length()-1) + D1 + tHash.get(tVal));
					invHash.put(tVal, (bBool?"(○)":"(　)") + String.format("%16s", bKey) + "：" + iVal.substring(0, iVal.length()-1) + D1 + tHash.get(tVal));
					tVal = 0;
					iVal = "";
					bool=false;
				}
				// 추출된 불법ID를 디렉토리로 표현하기 위하여 디렉토리에서 사용할 수 없는 기호를 치환
				val = val.trim().replaceAll(":", ";");
				val = val.replaceAll("//", "");
				// a1215535,Alin…형식으로 접붙이기
				iVal = iVal + val + ",";
				tVal = tVal + cVal;
				bKey = key;
				if (nVal != null) bool = true;
				bBool = bool;
				
				// [C] 마지막줄을 logHash에 넣기
				if (i == rtnList.size() -1) {
					tVal = Long.parseLong(String.valueOf(tVal) + df1.format(cnt==9999?cnt=0:++cnt));
					invList.add(tVal);
					// IP -> 국가정보 매핑
//					System.out.println((bBool?"(○)":"(　)") + String.format("%16s", bKey) + "：" + iVal.substring(0, iVal.length()-1) + D1 + tHash.get(tVal));
					invHash.put(tVal, (bBool?"(○)":"(　)") + String.format("%16s", bKey) + "：" + iVal.substring(0, iVal.length()-1) + D1 + tHash.get(tVal));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// 최종 리포트
		String infoDir1="", infoDir2="", infoDir3="", infoIP="", infoIDs="", infoTime="", infoMemb="";
		cnt = 1;
		long totVal = 0;
		long todVal = 0;
		rtnList.clear();
		tmpList.clear();
		Collections.sort(invList, Collections.reverseOrder());
		if (isMonth) {
			tmp00 = df1.format(cnt++) + "）- " + "-[ " + sysMonth.substring(0, 2) + "월 중 공격 IP 리스트：총 " + df2.format(totVal) + "회 ]- - - - - - - - - - - - - - - - -";
		} else {
			tmp00 = df1.format(cnt++) + "）- " + "-[ " + sysYear.substring(0, 4) + "년 공격 IP 리스트：총 " + df2.format(totVal) + "회 ]- - - - - - - - - - - - - - - - -";
		}
		for (int i=0 ; i<invList.size() ; i++) {
			tVal = invList.get(i);
			key = invHash.get(tVal);
			val = String.valueOf(tVal);
			val = val.substring(0, val.length() -4);
			tVal = Long.parseLong(val);
			
			//-----------------------------------------------------------------
			// String.format(param)에서 param이 "%04d" 의 의미 (4자리를 맞춤)
			//- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			// % : 명령의시작 , 0 : 채워질 문자(넣지 않으면 공백으로 대채)
			// 4 : 총 자리수 , d : 십진정수
			//- - - + - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			// String.format("%04d", "30"); 의 결과 "0030"
			//- - - + - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			// String.format("%-20s",key); 의 결과 -20일때는 공백을 오른쪽에 
			//- - - + - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			// 참고 | http://devbible.tistory.com/78
			//------+----------------------------------------------------------
			
			// LIMIT회 보다 많으면 공격IP로 지정함
			if (tVal > LIMIT) {
				key = (key.indexOf("＊(") > -1)?key.replaceAll("\\(\\)", ""):key;
				val = String.format("%6s", comma(val));
				
				// 디렉토리정보 + D1 + 접속횟수 + D1 + 접속IP + D1 + 접속IDs + D1 + 접속기간 + D1 + 회원포함여부
				infoIP   = (key.substring(key.indexOf(")") +1, key.indexOf("："))).trim();
				infoIDs  = key.substring(key.indexOf("：") +1, key.indexOf(D1));
				infoTime = key.split(D1)[1];
				infoMemb = key.contains("(○)")?"Y":"N";

				// infoIDs "＊" 없애기
				String infoIDt = "";
				for (String infoID : infoIDs.split(",")) {
					infoID  = (infoID.indexOf("＊") > -1)?infoID.substring(0, infoID.length()-1):infoID;
					infoIDt = infoIDt + infoID + ",";
				}
				infoIDs = infoIDt.substring(0, infoIDt.length() -1);

				// FULL ID "＊"로 가리기
				infoIDt = "";
				infoDir1 = key.split("：")[0];
				infoDir2 = key.split("：")[1].split(D1)[0];
				infoDir3 = key.split("：")[1].split(D1)[1];
				for (String infoID : infoDir2.split(",")) {
					infoID  = (infoID.indexOf("＊") > -1)?infoID.substring(0, infoID.length()-2) + "＊":infoID;
					infoIDt = infoIDt + infoID + ",";
				}
				key = infoIDt.substring(0, infoIDt.length() -1);
				key = infoDir1 + "：" + key + D1 + infoDir3;
				
				// 글자 짜르기 40자 이내 + "…"
				tmp00 = df1.format(cnt++) + "）  " + val + "회 ← 회원포함" + (key.length()<=40?key.substring(0, key.length()):key.substring(0, 40) + "…") + D1 + val + D1 + infoIP + D1 + infoIDs + D1 + infoTime + D1 + infoMemb;
																															tmpList.add(tmp00);
				// 공격에 이용당한 ID만 필터함
				if (key.indexOf("(○)") > -1) {
					tmp00 = "";
					tmp01 = "";
					tmp02 = key.split("：")[0];	// IP
					tmp03 = key.split("：")[1];
					tmp03 = tmp03.split(D1)[0];	// IDes
					List<String> idList = new ArrayList<String>();
					for (int j=0 ; j<tmp03.split(",").length ; j++) {
						tmp00 = tmp03.split(",")[j];
						if (tmp00.indexOf("＊") > -1) idList.add(tmp00);
					}
					// ID 중복제거
					TreeSet<String> distinctVerifi = new TreeSet<String>(idList);
					idList = new ArrayList<String>(distinctVerifi);
					Collections.sort(idList);
					Iterator<String> iterator = idList.iterator();
					while(iterator.hasNext()) tmp01 = tmp01 + iterator.next() + ",";
					tmp01 = tmp01.substring(0, tmp01.length()-1);	// 컴마떼기
//	  				System.out.println("\t" + val + "회 ←" + tmp02.replaceAll("\\(○\\)" , "") + "：" + tmp01);
					// 디렉토리정보 + D1 + 접속횟수 + D1 + 접속IP + D1 + 접속IDs + D1 + 접속기간 + D1 + 회연포함여부
					rtnList.add(val + "회 ←" + tmp02.replaceAll("\\(○\\)" , "") + "：" + tmp01 + D1 + val + D1 + infoIP + D1 + infoIDs + D1 + infoTime + D1 + infoMemb);
					todVal = todVal + tVal;
				}
			}
			totVal = totVal + tVal;
		}
		tmp00 = df2.format(totVal);																							tmpList.add(0, tmp00);
		if (isMonth) {
			tmp00 = "0001）- " + "-[ " + sysMonth.substring(0, 2) + "월 중 공격 IP 리스트：총 " + df2.format(totVal) + "회 ]- - - - - - - - - - - - - - - - -";
		} else {
			tmp00 = "0001）- " + "-[ " + sysYear.substring(0, 4) + "년 공격 IP 리스트：총 " + df2.format(totVal) + "회 ]- - - - - - - - - - - - - - - - -";
		}																													tmpList.add(1, tmp00);
		// 공격에 이용당한 ID가 있다면 이하의 리포트 출력
		if (todVal > 0) {
			tmp00 = df1.format(cnt++) + "）  ";																				tmpList.add(tmp00);
			if (isMonth) {
				tmp00 = df1.format(cnt++) + "）- " + "-[ " + sysMonth.substring(0, 2) + "월 중 공격에 이용당한 ID만 필터함：총 " + df2.format(todVal) + "회 ]- - - - - -";
			} else {
				tmp00 = df1.format(cnt++) + "）- " + "-[ " + sysYear.substring(0, 4) + "년 중 공격에 이용당한 ID만 필터함：총 " + df2.format(todVal) + "회 ]- - - - - -";
			}
			id2name.clear();																								tmpList.add(tmp00);
			for (int i=0 ; i<rtnList.size() ; i++) {
				tmp00 = df1.format(cnt++) + "）   " + rtnList.get(i);
				tmp00 = tmp00.replaceAll("\\(", ""); tmp00 = tmp00.replaceAll("\\)", "");									tmpList.add(tmp00);
				tmp00 = tmp00.split(D1)[0]; tmp00 = tmp00.split("：")[1];
				// 컴마로 이어진 id분해
				for (String tmpID : tmp00.split(","))																		id2name.add(tmpID);
			}
			tmp00 = df1.format(cnt++) + "）";																				tmpList.add(tmp00);
			TreeSet<String> distinctData = new TreeSet<String>(id2name);
			id2name = new ArrayList<String>(distinctData);
			if (isMonth) {
				tmp00 = df1.format(cnt++) + "）- " + "-[ " + sysMonth.substring(0, 2) + "월 중 공격에 이용당한 " + id2name.size() + "인의 명단 ]- - - - - - - - - - - -";
			} else {
				tmp00 = df1.format(cnt++) + "）- " + "-[ " + sysYear.substring(0, 4) + "년 공격에 이용당한 " + id2name.size() + "인의 명단 ]- - - - - - - - - - - -";
			}
																															tmpList.add(tmp00);
			double dVal = id2name.size();
			tVal = (int)Math.ceil(dVal/6.0f);
			for (int i=1 ; i<=tVal; i++) {
				tmp00 = df1.format(cnt++) + "）  ";
				for (int j=6*i-6 ; j<6*i; j++) {
					if (j < id2name.size()) tmp00 = tmp00 + id2name.get(j) + ", ";
				}
				tmp00 = tmp00.substring(0, tmp00.length()-2);
				tmp00 = tmp00.replaceAll("\\(", ""); tmp00 = tmp00.replaceAll("\\)", "");									tmpList.add(tmp00);
			}
		}
		
		// 콘솔 화면 출력
//		for (int i=0 ; i<tmpList.size() ; i++) System.out.println(tmpList.get(i));
		return tmpList;
	}
	
	public List<String> rtnLog(List<String> logVal, List<String> logTmp) {
		String key="", tKey="";
		long val=0, tVal=0;
		List<String> logRtn = new ArrayList<String>();
		for (int i=0 ; i<logVal.size() ; i++) {
			key = logVal.get(i).split(D1)[0];
			val = Long.parseLong(logVal.get(i).split(D1)[1]);
			for (int j=0 ; j<logTmp.size() ; j++) {
				tKey = logTmp.get(j).split(D1)[0];
				if (key.equals(tKey)) {
					tVal = Long.parseLong(logTmp.get(j).split(D1)[1]);
					break;
				}
			}
			logRtn.add(key.toUpperCase() + D1 + b2m(val) + D1 + b2m(tVal));
			// 아이디 , 업 or 다운 , 다운 or 업
//			System.out.println(key + "\t" + b2m(val) + "\t" + b2m(tVal));
			tVal = 0;
		}
		return logRtn;
	}
	
	public String b2m(float fltByte) {
		fltByte = fltByte/1024f/1024f;
		return df3.format(fltByte);
	}

	
	public List<String> upDownRank(String type) {

		int cnt = 0;
		boolean b01=false, b02=false;
		String key="", bKey="", val="", tmp01="", tmp02="", tmp03="";
		List<String> logVal = new ArrayList<String>();
		List<String> logTot = new ArrayList<String>();

		for (int i=0 ; i<logList.size() ; i++) {
			key = logList.get(i);
			logVal.addAll(logHash.get(key));
			for (int j=0 ; j<logVal.size() ; j++) {
				val = logVal.get(j).split(D1)[0];
				// ID 추출
				if (val.indexOf("logged in") > -1 || val.indexOf("logged out") > -1) {
					b01 = true;
					tmp01 = val.split(" ")[1];
					tmp01 = tmp01.replaceAll("\"", "");
				}
				// UP/DOWN 데이터 추출
				if (val.indexOf(type + " file") > -1) {
					b02 = true;
					tmp02 = val;
					tmp02 = tmp02.substring(tmp02.lastIndexOf("-") +2, tmp02.length() -1);
					tmp02 = tmp02.split(" ")[0];
					tmp02 = tmp02.replaceAll(",", "");
					tmp03 = val.substring(val.indexOf(type + " file") + (type + " file").length() +2, val.lastIndexOf("\""));
					// 인기파일
					if (tmp03 != null && tmp03.length() != 0) {
						if ("Sent".equals(type) && '[' == tmp03.charAt(tmp03.indexOf(" ") +2)) {
							popList.add(tmp03.substring(tmp03.indexOf(" ") +2, tmp03.lastIndexOf("\\")) + D1 + tmp02 + D1 + df1.format(cnt==9999?cnt=0:++cnt));
						}
					}
				}
				if (b01 && b02) {
					b01=false; b02=false;
					// ex) jusin㎯811068000㎯D:\[tvN] 응답하라 1994.E00.131011.mp4
					logTot.add(tmp01 + D1 + tmp02 + D1 + tmp03);
//					System.out.println(tmp01 + D1 + tmp02 + D1 + tmp03);
				}
			}
			logVal.clear();
		}	
		
		// 사용자별 파일size 합산
		Collections.sort(logTot);
		cnt = 0;
		long cVal=0, tVal=0; 
		List<Long> rtnList = new ArrayList<Long>();
		Map<Long, String> rtnHash = new HashMap<Long, String>();
		for (int i=0 ; i<logTot.size() ; i++) {
			tmp01 = logTot.get(i);
			key = tmp01.split(D1)[0];
			val = tmp01.split(D1)[1];
//			System.out.println(key + "," + val);
			try { cVal = Long.parseLong(val); } catch (Exception e) {}
			
			// [A] 첫줄은 b변수 초기화
			if (i == 0) bKey = key;
			
			// [B] 중간줄을 logHash에 넣기
			if (!bKey.equals(key)) {
				tVal = Long.parseLong(String.valueOf(tVal) + df1.format(cnt==9999?cnt=0:++cnt));
				rtnList.add(tVal);
				rtnHash.put(tVal, bKey);
//				System.out.println(tVal + " = " + bKey);
				tVal = 0;
			}
			tVal = tVal + cVal;
			bKey = key;
			
			// [C] 마지막줄을 logHash에 넣기
			if (i == logTot.size() -1) {
//				System.out.println(tVal + " = " + bKey);
				tVal = Long.parseLong(String.valueOf(tVal) + df1.format(cnt==9999?cnt=0:++cnt));
				rtnList.add(tVal);
				rtnHash.put(tVal, bKey);
			}
		}
		
		Collections.sort(rtnList, Collections.reverseOrder());  
		for (int i=0 ; i<rtnList.size() ; i++) {
			tVal = rtnList.get(i);
			key = rtnHash.get(tVal);
			val = String.valueOf(tVal);
			if (val.length() > 3) val = val.substring(0, val.length() -4);
			tVal = Long.parseLong(val);
			logVal.add(key + D1 + tVal);
//			System.out.println(nf.format(tVal/1024/1024) + "MB -> " + key);
		}
		return logVal;
	}

	public List<String> popularLog() {
		String key="", bKey="", val="", tmp01="";
		List<String> logVal = new ArrayList<String>();
		List<String> logTot = new ArrayList<String>();

		// 인기 파일 이름순 정렬
		Collections.sort(popList);
		
		int cnt = 0;
		long cVal=0, tVal=0; 
		Map<Long, String> rtnHash = new HashMap<Long, String>();
		List<Long> rtnList = new ArrayList<Long>();
		for (int i=0 ; i<popList.size() ; i++) {
			tmp01 = popList.get(i);
			key = tmp01.split(D1)[0];
			val = tmp01.split(D1)[1];
			cVal = Long.parseLong(val);
//			System.out.println(key + "\t" + val);
			
			// [A] 첫줄은 b변수 초기화
			if (i == 0) bKey = key;
			
			// [B] 중간줄을 logHash에 넣기
			if (!bKey.equals(key)) {
				tVal = Long.parseLong(String.valueOf(tVal) + df1.format(cnt==9999?cnt=0:++cnt));
				rtnList.add(tVal);
//				System.out.println(tVal + " = " + bKey);
				rtnHash.put(tVal, bKey);
				tVal = 0;
			}
			tVal = tVal + cVal;
			bKey = key;
			
			// [C] 마지막줄을 logHash에 넣기
			if (i == logTot.size() -1) {
//				System.out.println(tVal + " = " + bKey);
				tVal = Long.parseLong(String.valueOf(tVal) + df1.format(cnt==9999?cnt=0:++cnt));
				rtnList.add(tVal);
				rtnHash.put(tVal, bKey);
			}
		}
		
		Collections.sort(rtnList, Collections.reverseOrder());  
		for (int i=0 ; i<rtnList.size() ; i++) {
			tVal = rtnList.get(i);
			key = rtnHash.get(tVal);
			val = df1.format(tVal);
			val = val.substring(0, val.length() -4);
			val = (val.length() == 0)?"0":val;
			tVal = Long.parseLong(val);
			logVal.add(key + D1 + tVal + nf.format(tVal/1024/1024) + "MB");
//			System.out.println(nf.format(tVal/1024/1024) + "MB -> " + key);
		}
		
		return logVal;
	}
	
	public void writeLog() {
		String key="", val="", time="";
		List<String> logVal = new ArrayList<String>();
		for (int i=0 ; i<logList.size() ; i++) {
			key = logList.get(i);
			System.out.println(key);
			logVal.addAll(logHash.get(key));
			for (int j=0 ; j<logVal.size() ; j++) {
				val = logVal.get(j).split(D1)[0];
				time = logVal.get(j).split(D1)[1];
				System.out.println("\t" + time + "\t" + val);
			}
			logVal.clear();
		}	
	}
	
	public void readLog() {
		
		Map<String, String> tmpHash = new HashMap<String, String>();
		List<String> tmpList = new ArrayList<String>();
		try {
			File file = new File(logPath + sysYear + args);
	        FileInputStream fis = new FileInputStream(file);
	        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	        BufferedReader br = new BufferedReader(isr);
	        
	        // 읽은 수를 저장할 곳
	        String row="", key="", bKey="", val="";
	        int cnt = 0;
	        while((row = br.readLine()) != null) {
	        	if (row.length() > 1) {
		        	key = row.split("-")[0];
		    		val = row.substring(row.indexOf("-") +2, row.length());
		    		if ('(' == val.charAt(0)) {
		    			key = val.split(" ")[0] + makeDate(key) + df1.format(cnt==9999?cnt=0:++cnt);
		    			val = val.substring(val.indexOf(")") +2, val.length());
		    			
		    			tmpList.add(key);
		    			tmpHash.put(key,  val);
		    			
/* 테스트를 위한 행수 제한 	c++; if (c == 10000) break; */
		    		}
	        	}
	        }
	        fis.close();
	        
	        // Transaction 별 정열
	        List<String> logVal = new ArrayList<String>();
			Collections.sort(tmpList);
			String time = "";
			for (int i=0 ; i<tmpList.size() ; i++) {
				
				key = tmpList.get(i).split("\\)")[0].replaceAll("\\(", "");
				val = tmpHash.get(tmpList.get(i));
				time = tmpList.get(i).split("\\)")[1].substring(0, 14);
				
				// [A] 첫줄은 b변수 초기화
				if (i == 0) bKey = key;
				
				// [B] 중간줄을 logHash에 넣기
				if (!bKey.equals(key)) {
					logList.add(bKey);
					logHash.put(bKey, logVal);
					logVal = new ArrayList<String>();
				}
				logVal.add(val + D1 + time);	// 로그에 시간정보 추가
				bKey = key;
//				System.out.println(key + " / " + val + D1 + time);
				
				// [C] 마지막줄을 logHash에 넣기
				if (i == tmpList.size() -1) {
					logList.add(bKey);
					logHash.put(bKey, logVal);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("### 읽기실패 : 상기의 파일을 찾을 수 없습니다.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String makeDate(String s) {
		String s2="", s3="";
		Map<String, String> mh = new HashMap<String, String>();
		mh.put("Jan", "01");mh.put("Feb", "02");mh.put("Mar", "03");mh.put("Apr", "04");mh.put("May", "05");mh.put("Jun", "06");
		mh.put("Jul", "07");mh.put("Aug", "08");mh.put("Sep", "09");mh.put("Oct", "10");mh.put("Nov", "11");mh.put("Dec", "12");
		s2 = s.split(" ")[2];s3 = s.split(" ")[3];
		s2 = "20" + s2.substring(5, 7) + mh.get(s2.substring(2, 5)) + s2.substring(0, 2);
		s3 = s3.replaceAll(":", "");
		
		return s2 + s3;
	}
	
	/**
	 * %04d 의 의미 = %:명령의시작 , 0:채워질 문자 , 4:총 자리수 , d:십진정수
	 * 한마디로.. 4자리를 맞춘다는거임..
	 * int i = 30;
	 * String no = String.format("%04d", i);
	 * no 는 0030이 된다.
	 */
	public String getLogWriteTime() {
		Calendar currentDate = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("▶출력 : yyyy년MM월dd일(E) HH시mm분ss초");
		return df.format(currentDate.getTime());
	}

	public String getDateTimeFormat(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(str.substring(0, 4));
		sb.append("/");
		sb.append(str.substring(4, 6));
		sb.append("/");
		sb.append(str.substring(6, 8));
		sb.append(" ");
		sb.append(str.substring(8, 10));
		sb.append(":");
		sb.append(str.substring(10, 12));
		sb.append(":");
		sb.append(str.substring(12, 14));
		return sb.toString();
	}

	public String getSysDate() {
		Calendar currentDate = Calendar.getInstance();
		DateFormat dfy = new SimpleDateFormat("yyyy");
		dfy.format(currentDate.getTime());
		DateFormat dfm = new SimpleDateFormat("MM");
		dfm.format(currentDate.getTime());
		return dfy.format(currentDate.getTime()) + dfm.format(currentDate.getTime());
	}

	public String getTime2Millisecond(long diffTime) {
		long ss = diffTime / 1000000 / 1000;
		long mm = ss / 60;
		long HH = mm / 60;
		String rSs = getTimeString(ss);
		String rMm = getTimeString(mm);
		String rHH = getTimeString(HH);
		return rHH + ":" + rMm + ":" + rSs;
	}

	public String getTimeString(long time) {
		String str;
		while (true) {
			if (time >= 60) {
				time = time - 60;
			} else {
				break;
			}
		}
		str = String.valueOf(time);
		if (str.length() == 1) str = "0" + str;
		return str;
	}

	public void runningTime(String type) {
		if (START.equals(type)) {
			startTime = System.nanoTime();
		} else if (END.equals(type)) {
			endTime = System.nanoTime();
			long resultTime = endTime - startTime;
			System.out.println(" (수행시간 " + getTime2Millisecond(resultTime) + ")");
		}
	}

	public void copy(String originPath, String targetPath) {
		try {
			File file = new File(originPath);
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(targetPath);

			FileChannel fcin =  fis.getChannel();
			FileChannel fcout = fos.getChannel();

			long size = fcin.size();
			fcin.transferTo(0, size, fcout);

			fcout.close();
			fcin.close();
			fos.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public String comma(String str) {
        String temp = reverseString(str);
        String result = "";
 
        for(int i=0 ; i<temp.length() ; i+=3) {
            if(i+3 <temp.length()) {
                result += temp.substring(i, i+3) + ",";
            } else {
                result += temp.substring(i);
            }
        }
        return reverseString(result);
    }
    
    public String getNation(String addr) {
    	String nationRtn = "";
    	try {
	    	addr = "http://whois.kisa.or.kr/openapi/whois.jsp?key=2014011617403609763399&answer=json&query=" + addr;
	    	URL url = new URL(addr);       // read from the URL
	    	Scanner scan = new Scanner(url.openStream());
	    	String jsonText = new String();
	    	while (scan.hasNext()) {
	    		jsonText = scan.nextLine();
				JSONParser jsonParser = new JSONParser();
				FinderJsonKey finder = new FinderJsonKey();
				finder.setMatchKey("countryCode");
				try {
					while (!finder.isEnd()) {
						jsonParser.parse(jsonText, finder, true);
						if (finder.isFound()) {
							finder.setFound(false);
							nationRtn = (String)finder.getValue();
							break;
						}
					}
				} catch (ParseException pe) {
//					pe.printStackTrace();
				}
	    	}
	    	scan.close();       // build a JSON object
//	    	System.out.println(addr + " : " + nationRtn);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return nationRtn;
    }
 
    private String reverseString(String s) {
        return new StringBuffer(s).reverse().toString();
    }	
}
