package com.donzbox.file.action;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ServULogRankingOutput {

	private static final String [] H_LEVEL = {"Ⅰ","Ⅱ","Ⅲ","Ⅳ","Ⅴ","Ⅵ","Ⅶ","Ⅷ","Ⅸ","Ⅹ"};
	private static final String [] L_LEVEL = {"ⅰ","ⅱ","ⅲ","ⅳ","ⅴ","ⅵ","ⅶ","ⅷ","ⅸ","ⅹ"};

	private final String NAMEDB_ROOT       = "D:\\My Data\\WWW\\Ftp Factory\\Serv-U\\Users\\DonzBox.com";
	private final String BLACKLIST_ROOT    = "D:\\My Data\\MY FTP\\FTP ROOT BLACKLIST";
	private final String DENYIP_IN_ROOT    = "D:\\My Data\\MY FTP\\importDenyIP_01)inUser.";
	private final String DENYIP_OUT_ROOT   = "D:\\My Data\\MY FTP\\importDenyIP_02)outUser.";
	private final String DENYIP_INOUT_ROOT = "D:\\My Data\\MY FTP\\importDenyIP_03)inoutUser.";
	private final String DENYIP_BLACK_ROOT = "D:\\My Data\\MY FTP\\importDenyIP_04)blackUser.";
	private final String NATION_ROOT       = new File("").getAbsolutePath() + File.separator + "DiskSpace" + File.separator + "properties" + File.separator + "nationCode.properties";

	private final Map<String, String> map = new HashMap<>();
	private static final String UP = "UP";
	private static final String DN = "DN";
	private static final String PO = "PO";
	private static final String D1 = "㎯";
	private static final String REPORT_KIND_UPK = "D21)  업로드왕 ";
	private static final String REPORT_KIND_UPG = "D21)  업로드神 ";
	private static final String REPORT_KIND_DNK = "D31)  다운왕 ";
	private static final String REPORT_KIND_DNG = "D31)  다운神 ";
	private static final String REPORT_KIND_PO = "D41)  인기자료 ";
	private static final String REPORT_ILLEGAL = "D69)  ★ 불법접근 ";
	private static final String REPORT_KIND_UP_MORE = "\\D20)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣[업 더보기]\\";
	private static final String REPORT_KIND_DN_MORE = "\\D30)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣[다운 더보기]\\";
	private static final String REPORT_KIND_PO_MORE = "\\D40)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣[인기 더보기]\\";
	private static final int UPDN_TOTAL_RANKING = 5;
	private static final int PO_TOTAL_RANKING = 1000;
	private static final int DENY_BLACKLIST_LIMIT = 500;
	private String notice_root;
	private String report_date;
	private ServULogAnalyzer sla;
	private final DecimalFormat dft  = new DecimalFormat("#,##0.00");
	private final DecimalFormat dfg  = new DecimalFormat("#,##0.0");
	private final DecimalFormat dfr  = new DecimalFormat("00");
	private final DecimalFormat df1  = new DecimalFormat("00000000");
	//	private DecimalFormat df   = new DecimalFormat("#,##0.0G");
	private boolean theEndOfTheYearChk    = false;
	private final List<String> upLoader         = new ArrayList<>();
	private final List<String> denyIP_inUser    = new ArrayList<>();
	private final List<String> denyIP_outUser   = new ArrayList<>();
	private final List<String> denyIP_inoutUser = new ArrayList<>();
	private final List<String> denyIP_krjpUser  = new ArrayList<>();

	private final DiskSpaceOutput dso  = new DiskSpaceOutput();

	public ServULogRankingOutput() {
	}

	public ServULogRankingOutput(final String notice_root, final String [] args) {
		this.notice_root = notice_root;
		try {
			// ID를 이름으로 미리 변환
			final Map<String, String> mapIDtoName = makeHashIDtoName();

			// 랭킹 기초 데이터 추출
			final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMM");
			final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");
			final Calendar cal = Calendar.getInstance();
			final Date date = cal.getTime();
			final DateFormat df2 = new SimpleDateFormat("M");
			// 링킹 초기화
			if (args.length > 0) {
				report_date = args[0];
			} else {
				if ("12".equals(df2.format(cal.getTime()))) {
					report_date = sdf2.format(date);
					theEndOfTheYearChk = true;
				} else {
					report_date = sdf1.format(date);
				}
			}
			sla = new ServULogAnalyzer(report_date, mapIDtoName);

			System.out.println("\n### 랭킹갱신");
			getRanking();

			final List<String> illegal = sla.illegalLog();
			System.out.println("\n### 불법 IP : 대문갱신");
			denyIP_outUser.add("\"IP\",\"Description\",\"Allow\"");
			getIllegalLog(illegal);

			System.out.println("\n### Serv-U용 importDenyIP 출력");
			makeFileDenyIpList (DENYIP_IN_ROOT   , denyIP_inUser);
			makeFileDenyIpList (DENYIP_OUT_ROOT  , denyIP_outUser);
			makeFileDenyIpList (DENYIP_INOUT_ROOT, denyIP_inoutUser);
			makeFileDenyIpList (DENYIP_BLACK_ROOT, denyIP_krjpUser);
			mergeFileDenyIpList(DENYIP_IN_ROOT);
			mergeFileDenyIpList(DENYIP_OUT_ROOT);
			mergeFileDenyIpList(DENYIP_INOUT_ROOT);
			mergeFileDenyIpList(DENYIP_BLACK_ROOT);

			System.out.println("\n### 불법 IP : 대문 -> BLACKLIST 복사");
			copyRoot2BlackList();

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> makeHashIDtoName() {
		return makeHashIDtoName(true);
	}

	// boolean true  : 이름 중간에 X표시
	// boolean false : FULL NAME
	public Map<String, String> makeHashIDtoName(final boolean b) {
		String strRL    = null;
		String strID    = "";
		String strNM    = "";
		String fileName = "";
		File   listFile = null;
		final File [] listFileArray = new File(NAMEDB_ROOT).listFiles();
		for (int i=0 ; i<listFileArray.length ; i++) {
			listFile = listFileArray[i];
			fileName = listFile.getName();
			if (fileName.lastIndexOf(".Archive") > -1 && fileName.lastIndexOf(".Backup") == -1) {
				try {
					//BufferedReader in = new BufferedReader(new FileReader(listFile.getPath())); // 한글깨짐 그래서 아래로 인코딩(txt 파일처럼 ansi 로 인코딩된경우에만)
					final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(listFile.getPath()), "UTF-8"));

					while ((strRL = in.readLine()) != null) {
						if ("LoginID".equals(strRL)) {
							in.readLine();
							in.readLine();
							in.readLine();
							strID = in.readLine();
						}
						if ("FullName".equals(strRL)) {
							in.readLine();
							in.readLine();
							in.readLine();
							strNM = in.readLine();
							break;
						}
					}
					in.close();
					if (!"".equals(strNM)) {
						strID = strID.toUpperCase();
						//				    	System.out.println("[" + (cnt++) + "] map.put(" + strID + ", " + strNM + ");");
						// 이름의 성에 X 표시 여부
						strNM = b?strNM.substring(0, 1) + "ｘ" + (strNM.length()==3?strNM.substring(2, 3):""):strNM;
						map.put(strID, strNM);
						strID = "";
						strNM = "";
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		return map;
	}

	public String id2name(String id, final String strDN, final String strUP, final String flag, final String rankNick) {
		String name = id;

		for (int i=0 ; i<upLoader.size() ; i++) {
			if(id.equals(upLoader.get(i))) {
				String star = "";for (int s=0 ; s<5-i ; s++) {
					star=star+"★";
				}
				if ("0.00".equals(strUP)) {
					return name;
				}
				// full id 가리기
				id = id.substring(0, id.length()-1) + "＊";
				if ("Best5".equals(flag)) {
					name = "(" + m2g(strDN, true) + ")  " + star + " " + rankNick + id;
				} else if ("All".equals(flag)) {
					name = "(" + m2g(strDN, true) + ")  " + rankNick + (i+1) + ".UP " + star + " " + id + " " + star + " " + "UP." + (i+1);
				}
				break;
				//			} else {
				//				name = (String) map.get(id);
				//				if (name==null) name = (id.substring(0, id.length()-1) + "＊") + " (인증필요)";
				//				float fltUP = Float.parseFloat(m2g(strUP, false));
				////				float fltDN = Float.parseFloat(m2g(strDN));
				//				// full id 가리기
				//				if (fltUP < 0.6f) {
				//					name = "(" + m2g(strDN, true) + ")  " + rankNick + name + ("0.00".equals(strUP)?"":", (업로드 " + m2g(strUP, true)+")") + "";
				//				} else {
				//					name = "(" + m2g(strDN, true) + ")  " + rankNick + id   + ("0.00".equals(strUP)?"":", (업로드 " + m2g(strUP, true)+")") + "";
				//				}
			}
		}
		// 속에 있는 로직 밖으로 빼냈음 (uploader가 없는 경우고 다운로드왕만 있는 경우 용량 처리가 되지 않기 때문에)
		name = map.get(id);
		if (name==null) {
			name = (id.substring(0, id.length()-1) + "＊") + " (인증필요)";
		}
		final float fltUP = Float.parseFloat(m2g(strUP, false));
		//			float fltDN = Float.parseFloat(m2g(strDN));
		// full id 가리기
		if (fltUP < 0.6f) {
			name = "(" + m2g(strDN, true) + ")  " + rankNick + name + ("0.00".equals(strUP)?"":", (업로드 " + m2g(strUP, true)+")") + "";
		} else {
			name = "(" + m2g(strDN, true) + ")  " + rankNick + id   + ("0.00".equals(strUP)?"":", (업로드 " + m2g(strUP, true)+")") + "";
		}
		return name;
	}
	public void getIllegalLog(final List<String> illegal, final String blacklist_root) {
		this.notice_root = blacklist_root;
		getIllegalLog(illegal);
	}
	public void getIllegalLog(final List<String> illegal) {

		// 블랙리스트 D69)의 가변 디렉토리 삭제
		File file = new File(BLACKLIST_ROOT);
		File [] listFileArray = file.listFiles();
		File listFile;
		System.out.println("\t01. 블랙리스트 불법IP 디렉토리 삭제");
		for (int j=0 ; j<listFileArray.length ; j++) {
			listFile = listFileArray[j];
			if (listFile.getName().contains("D69)")) {
				// 01. 하위디렉토리 지우고
				deleteSubDirectory(listFile.getAbsolutePath());
				// 02. 상위대릭토리 지우기
				listFile.delete();
			}
		}

		// 본체 D69) 가변 디렉토리 삭제
		file = new File(notice_root);
		listFileArray = file.listFiles();
		System.out.println("\t02. 본체 불법IP 디렉토리 삭제");
		for (int j=0 ; j<listFileArray.length ; j++) {
			listFile = listFileArray[j];
			if (listFile.getName().contains("D69)")) {
				// 01. 하위디렉토리 지우고
				deleteSubDirectory(listFile.getAbsolutePath());
				// 02. 상위대릭토리 지우기
				listFile.delete();
			}
		}

		// D69)의 가변 디렉토리 생성
		System.out.print("\t03. 불법IP목록 생성\n\t");
		final String iIllegaInfo = "\\" + REPORT_ILLEGAL + "IP：" + illegal.get(0) + "건에 대한 리스트 보기 ★\\";
		setDirectoryName(iIllegaInfo, REPORT_ILLEGAL);
		String txt="", country="", ip="", num="";
		for (int i=1 ; i<illegal.size() ; i++) {

			//			System.out.println(illegal.get(i));
			System.out.print(String.format("%5s", i));
			if (i%30 == 0) {
				System.out.print("\n\t");
			}

			txt = illegal.get(i);
			if (txt.contains("] ") && txt.contains("：") && txt.contains(D1)) {
				country = txt.substring(txt.indexOf(") [") +2, txt.indexOf(") [") +7);
				ip = txt.substring(txt.indexOf("] ") +1, txt.indexOf("："));
				ip = ip.trim();
				num = txt.substring(txt.indexOf("）") +1, txt.indexOf("←") -1);
				num = num.trim();
				// illegal.get(i).split(D1)[0] : 줄임 불법ID
				// illegal.get(i).split(D1)[1] : FULL 불법ID
				// illegal.get(i).split(D1)[2] : 최초,최후 접속시간
				setDirectoryName(iIllegaInfo + illegal.get(i).split(D1)[0] + D1 + country + D1 + ip + D1 + num + D1 + illegal.get(i).split(D1)[1] + D1 + illegal.get(i).split(D1)[2], REPORT_ILLEGAL);
			} else {
				setDirectoryName(iIllegaInfo + illegal.get(i)                                                                                                                       , REPORT_ILLEGAL);
			}
		}
		System.out.println();
	}

	public void getRanking() {

		String rankInfo = "";

		String [] ct = null;
		String [] fct = null;
		try {
			deleteSubDirectory(notice_root);
			deleteSubDirectory(notice_root + REPORT_KIND_DN_MORE);
			deleteSubDirectory(notice_root + REPORT_KIND_UP_MORE);
			deleteSubDirectory(notice_root + REPORT_KIND_PO_MORE);
		} catch(final Exception e) {
		}
		ct = getContext(UP);
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			fct = getFilterContextUPDN(ct[j]);
			upLoader.add(fct[0]);
			if (j==UPDN_TOTAL_RANKING-1) {
				break;
			}
		}

		// fct[0]:name, fct[1]:dn, fct[2]:up
		// 다운로더 TOP 화면
		System.out.println("\t01. 다운로더 갱신");
		ct = getContext(DN);
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			fct = getFilterContextUPDN(ct[j]);
			if ("0.00".equals(fct[1])) {
				continue;
			}
			rankInfo = "\\" + getKingOrGod(REPORT_KIND_DNK, j) + (j+1) + "위.  " + id2name(fct[0], fct[1], fct[2], "Best5", getRankNick(fct[1]));
			// 다운로드 1위는 다운신으로 명명
			if (!theEndOfTheYearChk && j == 0) {
				rankInfo = "\\" + REPORT_KIND_DNK + (j+1) + "위.  " + id2name(fct[0], fct[1], fct[2], "Best5", "다운신 ");
			}
			setDirectoryName(rankInfo, REPORT_KIND_DNK);
			if (j==UPDN_TOTAL_RANKING-1) {
				break;
			}
		}
		// 더보기
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			fct = getFilterContextUPDN(ct[j]);

			if ("0.00".equals(fct[1])) {
				continue;
			}
			rankInfo = REPORT_KIND_DN_MORE + getKingOrGod(REPORT_KIND_DNK, j).substring(6) + dfr.format((j+1)) + "위.  " + id2name(fct[0], fct[1], fct[2], "All", getRankNick(fct[1]));
			//  다운로드 1위는 다운신으로 명명
			if (!theEndOfTheYearChk && j == 0) {
				rankInfo = REPORT_KIND_DN_MORE + REPORT_KIND_DNK.substring(6) + dfr.format((j+1)) + "위.  " + id2name(fct[0], fct[1], fct[2], "All", "다운신 ");
			}
			setDirectoryName(rankInfo, REPORT_KIND_DNK);
		}

		// fct[0]:name, fct[1]:dn, fct[2]:up
		// 업로더 TOP 화면
		System.out.println("\t02. 업로더 갱신");
		ct = getContext(UP);
		String id ="";
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			fct = getFilterContextUPDN(ct[j]);
			if ("0.00".equals(fct[1])) {
				continue;
			}
			String star = "";for (int s=0 ; s<5-j ; s++) {
				star=star+"★";
			}
			// full id 가리기
			id = fct[0]; id = id.substring(0, id.length()-1) + "＊";
			rankInfo = "\\" + getKingOrGod(REPORT_KIND_UPK, j) + (j+1) + "위.  (" + m2g(fct[1], true) + ") " + star + " " + id;
			setDirectoryName(rankInfo, REPORT_KIND_UPK);
			if (j==UPDN_TOTAL_RANKING-1) {
				break;
			}
		}
		// 더보기
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			fct = getFilterContextUPDN(ct[j]);
			if ("0.00".equals(fct[1])) {
				continue;
			}
			String star = "";if (j<5) { for (int s=0 ; s<5-j ; s++) {
				star=star+"★";
			}}
			if (j==5) { star="☆";}
			if (6<=j && j<=15) {
				star=H_LEVEL[j-6]+".";
			}
			if (16<=j && j<=25) {
				star=L_LEVEL[j-16]+".";
			}
			// full id 가리기
			id = fct[0]; id = id.substring(0, id.length()-1) + "＊";
			rankInfo = REPORT_KIND_UP_MORE + getKingOrGod(REPORT_KIND_UPK, j).substring(6) + dfr.format((j+1)) + "위.  (" + m2g(fct[1], true) + ") " + star + " " + id;
			setDirectoryName(rankInfo, REPORT_KIND_UPK);
		}

		// 인기파일
		System.out.println("\t03. 인기파일 갱신");
		ct = getContext(PO);
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			if (!(ct[j].toLowerCase().contains("[y]") || ct[j].contains("adult"))) {
				rankInfo = "\\" + REPORT_KIND_PO + (j+1) + "위.  " + getFilterContextPO(ct[j]);
				setDirectoryName(rankInfo, REPORT_KIND_PO);
			}
			if (j==UPDN_TOTAL_RANKING*2-1) {
				break;
			}
		}
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			if (!(ct[j].toLowerCase().contains("[y]") || ct[j].contains("adult"))) {
				rankInfo = REPORT_KIND_PO_MORE + REPORT_KIND_PO.substring(6) + dfr.format((j+1)) + "위.  " + getFilterContextPO(ct[j]);
				setDirectoryName(rankInfo, REPORT_KIND_PO);
			}
		}
	}

	// 연말 총결산때 1~3위는 "신"이라는 명칭 부여, 1월~11월까지는 "왕"이라는 명칭 부여
	public String getKingOrGod(String type, final int rank) {
		if (0 <= rank && rank <3) {
			if (REPORT_KIND_UPK.equals(type)) {
				type = theEndOfTheYearChk?REPORT_KIND_UPG:REPORT_KIND_UPK;
			} else if (REPORT_KIND_DNK.equals(type)) {
				type = theEndOfTheYearChk?REPORT_KIND_DNG:REPORT_KIND_DNK;
			}
		}
		return type;
	}

	public String getRankNick(final String dn) {
		String result = "";
		final float dnSize = Float.parseFloat(dn)/1024F;

		if (!theEndOfTheYearChk) {
			if (dnSize > 120.00F) {
				result = "대통령 ";
			} else if (dnSize > 100.00F) {
				result = "대장  ";
			} else if (dnSize > 90.00F) {
				result = "중장 ";
			} else if (dnSize > 80.00F) {
				result = "소장 ";
			} else if (dnSize > 70.00F) {
				result = "대령 ";
			} else if (dnSize > 60.00F) {
				result = "중령 ";
			} else if (dnSize > 50.00F) {
				result = "소령 ";
			} else if (dnSize > 40.00F) {
				result = "원사 ";
			} else if (dnSize > 35.00F) {
				result = "상사 ";
			} else if (dnSize > 30.00F) {
				result = "중사 ";
			} else if (dnSize > 25.00F) {
				result = "하사 ";
			} else if (dnSize > 20.00F) {
				result = "병장 ";
			} else if (dnSize > 15.00F) {
				result = "상병 ";
			} else if (dnSize > 10.00F) {
				result = "일병 ";
			} else if (dnSize > 5.00F) {
				result = "이병 ";
			} else if (dnSize > 1.00F) {
				result = "훈병 ";
			} else {
				result = "천민 ";
			}
		} else {
			// 매년 12월 결산용 계급
			result = " ";
		}
		return result;
	}

	public String m2g(final String strMegaByte, final boolean flag) {
		float fltMegaByte = Float.parseFloat(strMegaByte);
		fltMegaByte = fltMegaByte/1024f;
		if (dfg.format(fltMegaByte).equals("0.0")) {
			return flag?"0.1G":"0.1";
		} else {
			if ((dfg.format(fltMegaByte)).length() < 7) {
				return flag?dfg.format(fltMegaByte)+"G":dfg.format(fltMegaByte);
			} else {
				return flag?g2t(fltMegaByte)+" T":g2t(fltMegaByte);
			}
		}
	}

	public String g2t(float fltGigaByte) {
		fltGigaByte = fltGigaByte/1024f;
		return dft.format(fltGigaByte);
	}

	public void setDirectoryName(String info, final String kind) {
		try {
			new File(notice_root);
			if (REPORT_KIND_DNK.equals(kind) ||
					REPORT_KIND_UPK.equals(kind) ||
					REPORT_KIND_PO.equals(kind)) {
				final String path = notice_root + info;
				makeDir(path.trim());
				/* java ver 1.7 over
				Path newDir = FileSystems.getDefault().getPath(path.trim());
				Files.createDirectory(newDir);		// 디릭토리 마지막에 공백이 드가면 에러나므로 trim 처리했음
				 */
			}
			if (REPORT_ILLEGAL.equals(kind)) {
				// ip 정보를 기반으로 whois 검색하여 접근지 소재 파악
				if (info.contains("회원포함")) {
					makeWhoisFile(info);
				} else {
					// 디렉토리 생성
					info = notice_root + (info.contains(D1)?info.split(D1)[0]:info);
					makeDir(info.trim());
					/* java ver 1.7 over
					Path newDir = FileSystems.getDefault().getPath(info.trim());
					Files.createDirectory(newDir);		// 디릭토리 마지막에 공백이 드가면 에러나므로 trim 처리했음
					 */
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteSubDirectory(final String directoryPath) {
		String fileName = "";
		final File[] listFile = new File(directoryPath).listFiles();
		try {
			if (listFile.length > 0) {
				for (int i = 0; i < listFile.length; i++) {
					if (listFile[i].isFile()) {
						//						System.out.println("\t파일 삭제 : " + listFile[i].getAbsolutePath());
						listFile[i].delete();
					} else {
						deleteSubDirectory(listFile[i].getPath());
					}
					fileName = listFile[i].getName();
					if (fileName.contains(REPORT_KIND_UPK)
							|| fileName.contains(REPORT_KIND_UPG)
							|| fileName.contains(REPORT_KIND_DNK)
							|| fileName.contains(REPORT_KIND_DNG)
							|| fileName.contains(REPORT_KIND_PO)
							|| fileName.contains("다운왕 ")
							|| fileName.contains("다운神 ")
							|| fileName.contains("업로드왕 ")
							|| fileName.contains("업로드神 ")
							|| fileName.contains("인기자료 ")
							|| fileName.contains("）")
							) {
						//						System.out.println("\t폴더 삭제 : " + listFile[i].getAbsolutePath());
						listFile[i].delete();
					}
				}
			}
		} catch (final Exception e) {
			System.err.println(System.err);
		}
	}

	public String [] getContext(final String type) {

		final List<String> list = new ArrayList<>();
		final String [] rangking = new String[PO_TOTAL_RANKING];
		try {
			if (DN.equals(type)) {
				list.addAll(sla.sentLog());
				for (int i=0 ; i<list.size() ; i++) {
					rangking[i] = list.get(i);
				}
			}
			if (UP.equals(type)) {
				list.clear();
				list.addAll(sla.receivedLog());
				for (int i=0 ; i<list.size() ; i++) {
					rangking[i] = list.get(i);
				}
			}
			if (PO.equals(type)) {
				list.clear();
				list.addAll(sla.popularLog());
				int intPO = PO_TOTAL_RANKING;
				if (PO_TOTAL_RANKING > list.size()) {
					intPO = list.size();
				}
				for (int i=0 ; i<intPO ; i++) {
					rangking[i] = list.get(i);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return rangking;
	}

	public String [] getFilterContextUPDN(final String context) {
		final String [] returnValue = new String[3];
		final String [] contextArray = context.split(D1);
		int cnt = 0;
		for (final String tmp : contextArray) {
			if (tmp.length()!=0 ) {
				if(cnt==0) {
					returnValue[0] = tmp;
				}
				if(cnt==1) {
					returnValue[1] = tmp;
				}
				if(cnt==2) {
					returnValue[2] = tmp;
				}
				cnt++;
			}
		}
		return returnValue;
	}

	public String getFilterContextPO(String context) {
		final String [] contextArray = context.split("\\\\");
		context = contextArray[contextArray.length-1];
		context = context.split(D1)[0];
		return context;
	}

	public void makeWhoisFile(final String info) {
		// info : 디렉토리 + D1 + 접속횟수 + D1 + 접속IP + D1 + 접속IDs + D1 + 접속기간 + D1 + 회원포함여부
		String countryEN = "";
		String countryKO = "";
		String address = "";
		String context = "";
		String path    = notice_root + info.split(D1)[0];
		path    = dso.changeToDirChar(path);
		String num     = info.split(D1)[1];
		final String ip      = info.split(D1)[2];
		final String ids     = info.split(D1)[3];
		final String time    = info.split(D1)[4];
		final String members = info.split(D1)[5];
		final String time1   = time.substring( 0,  4) + "/" + time.substring( 4,  6) + "/" + time.substring( 6,  8) + " "
				+ time.substring( 8, 10) + ":" + time.substring(10, 12) + ":" + time.substring(12, 14) + "～\r\n"
				+ time.substring(14, 18) + "/" + time.substring(18, 20) + "/" + time.substring(20, 22) + " "
				+ time.substring(22, 24) + ":" + time.substring(24, 26) + ":" + time.substring(26, 28);
		final String time2   = time.substring(18, 20) + "월" + time.substring(20, 22) + "일 "
				+ time.substring(22, 24) + "시" + time.substring(24, 26) + "분";
		final String path1 = path.substring(0, path.indexOf("）") +1);
		final String path2 = path.substring(path.indexOf("）") +1 , path.lastIndexOf("："));
		final String path3 = path.substring(path.lastIndexOf("："), path.length());
		String path5 = "";

		final String nationInfo = getNationByApnic(ip);
		countryEN = nationInfo.split(D1)[0];
		countryKO = nationInfo.split(D1)[1];
		address = nationInfo.split(D1)[2];
		context = nationInfo.split(D1)[3];

		// IDs의 중복제거
		String nVal="", ids1="", ids2="";
		final Map<String, String> map = makeHashIDtoName(false);
		List<String> idsList = Arrays.asList(ids.split(","));
		final TreeSet<String> distinctVerifi = new TreeSet<>(idsList);
		idsList = new ArrayList<>(distinctVerifi);
		for (int i=0 ; i<idsList.size() ; i++) {
			nVal = map.get(idsList.get(i).toUpperCase());
			if (nVal != null) {
				ids1 = ids1 + idsList.get(i) + "(" + nVal + "),";
			}
			ids2 = ids2 + idsList.get(i) + ",";
		}
		if (ids1.length() > 0) {
			ids1 = "[" + ids1.substring(0, ids1.length() -1) + "] ";
		}
		if (ids2.length() > 0) {
			ids2 =       ids2.substring(0, ids2.length() -1);
		}

		// 본문 작성
		context = "■ 국가 식별 : " + countryKO + " - " + ip + "\r\n\r\n"
				+ "■ 불법 접근 기간\r\n" + time1 + "\r\n\r\n"
				+ "■ 불법 접근에 이용한 ID 목록\r\n1) LIST : " + ids + "\r\n1) DIST : " + ids2 + "\r\n\r\n"
				+ context;

		// APNIC 와 KRNIC에도 country 정보가 없으면 디렉토리 국가 추가, 접속지 정보 파일 생성을 영문으로 생성
		if (countryKO.length() == 0) {
			countryKO = countryEN;
		}
		// IP에 해당하는 국가정보 취득 후 디렉토리 이름변경(국가코드 및 나라명 추가)
		path5 = path1 + time2 + path2 + " - " + countryKO + path3;
		try {
			// 디렉토리 생성
			makeDir(path5.trim());
			/* java ver 1.7 over
			Path newDir = FileSystems.getDefault().getPath(path5.trim());
			Files.createDirectory(newDir);		// 디릭토리 마지막에 공백이 드가면 에러나므로 trim 처리했음
			 */
		} catch (final Exception e) {
			e.printStackTrace();
		}

		// APNIC 정보를 파일로 생성
		try {
			final BufferedWriter out = new BufferedWriter(new FileWriter(path5 + "\\" + ip + ".txt"));
			out.write(context);
			out.newLine();
			out.close();
		}  catch (final Exception e) {
			e.printStackTrace();
		}

		// denyIP 리포트 작성용 데이터 - 회원이 포함되지 않은 아이피 차단
		num = num.replaceAll(",","").trim();
		if ("N".equals(members)) {
			final int intNum = Integer.parseInt(num);
			// 대한민국,일본의 접속시 DENY_BLACKLIST_LIMIT 건 이상만 접근금지 IP로 지정
			if (("KR".equals(countryEN) || "JP".equals(countryEN)) && intNum >= DENY_BLACKLIST_LIMIT) {
				denyIP_krjpUser.add("\"" + ip + "\",\"" + "[" + countryEN + "," + num.trim() + "] " + address + "\",\"0\"");
				// 대한민국,일본 이외에는 무조건 막음
			} else if (!("KR".equals(countryEN) || "JP".equals(countryEN))) {
				denyIP_outUser.add("\"" + ip + "\",\"" + "[" + countryEN + "," + num.trim() + "] " + address + "\",\"0\"");
			}
		}
		if ("Y".equals(members)) {
			denyIP_inUser.add("\"" + ip + "\",\"" + "[" + countryEN + "," + num.trim() + "] " + ids1 + address + "\",\"0\"");
		}
		denyIP_inoutUser.add("\"" + ip + "\",\"" + "[" + countryEN + "," + num.trim() + "] " + address + "\",\"0\"");
	}

	private int reConnCnt = 1;
	public String getNationByApnic(final String ip) {
		String address   ="";
		String countryEN = "";
		String countryKO = "";
		String context   = "http://wq.apnic.net/apnic-bin/whois.pl?searchtext=" + ip;
		try {
			final Document doc = Jsoup.connect(context).get();
			context = doc.text();
			context = context.replaceAll(":        ", " : ");
			context = context.replaceAll("%", "\r\n■");
			context = context.replaceAll("\n", "\r\n");
			context = context.substring(0, context.indexOf("Bold: ") -2);

			for (String row : context.split("\r\n")) {
				// denyIP를 관리하기 위한 정보 획득
				if (row.replaceAll(" ",  "").toLowerCase().contains("address:")) {
					address = address + (row.split(":")[1]).trim() + ",";
				}
				if (row.replaceAll(" ",  "").toLowerCase().contains("city:")) {
					address = address + (row.split(":")[1]).trim() + ",";
				}
				// APNIC 에서 IP에 해당하는 국가정보 획득
				if (row.replaceAll(" ",  "").toLowerCase().contains("country:")) {
					row = row.replaceAll(" ",  "").toLowerCase();
					row = row.substring(row.indexOf("country:"), row.length());
					countryEN = (row.split(":")[1]).trim();
				}
			}
			if (address.length() > 0) {
				address = address.substring(0, address.length() -1);
			}

			// KRNIC에서 국가정보 획득 (APNIC에 없을 경우)
			if (countryEN.length() == 0) {
				countryEN = sla.getNation(ip);
			}
			if (countryEN.length() > 0) {
				countryEN = countryEN.toUpperCase();
				countryKO = getProperties(countryEN);
			}

		} catch (final Exception e) {
			getNationBydbIp(ip);
		}
		return countryEN + D1 + countryKO + D1 + address + D1 + context;
	}

	public String getNationBydbIp(final String ip) {
		String address   ="";
		String countryEN = "";
		String countryKO = "";
		String context   = "http://api.db-ip.com/v2/free/" + ip;
		try {
			final Document doc = Jsoup.connect(context)
					.header("content-type", "application/json;charset=UTF-8")
					.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
					.header("accept-encoding", "gzip, deflate, br")
					.header("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36")
					.ignoreContentType(true).get();

			final JSONParser jpr = new JSONParser();
			final JSONObject temp = (JSONObject) jpr.parse(doc.text());
			countryEN = (String) temp.get("countryCode");
			countryKO = getProperties(countryEN);
			address = (String) temp.get("countryName") + " " + (String) temp.get("stateProv") + " " + (String) temp.get("city");
			context = (String) temp.get("ipAddress");

		} catch (final Exception e) {
			//e.printStackTrace();
			System.out.println("\n정보획득 실패로 재시도 " + reConnCnt + "회 : " + ip);
			if (reConnCnt <= 3) {
				reConnCnt++;
				getNationByApnic(ip);
			}
			reConnCnt = 1;
		}
		return countryEN + D1 + countryKO + D1 + address + D1 + context;
	}

	public String getProperties(final String nation) {
		String val = "";
		BufferedReader br = null;
		try {
			final Properties properties = new Properties();
			br = new BufferedReader(new InputStreamReader(new FileInputStream(NATION_ROOT),"EUC-KR"));
			properties.load(br);

			/* 전체검색
			Enumeration<Object> key = properties.keys();
			String s = "";
			while (key.hasMoreElements()) {
				s = properties.getProperty((String) key.nextElement());
				System.out.println(s);
			} */
			//  135\D69)  ★ 불법접근 IP：213,771건에 대한 리스트 보기 ★\0135）     123회 ← 회원포함(　)    210.13.73.29：auto,bin,db2inst1,mm…㎯(　)    210.13.73.29：auto,bin,db2inst1,
			val = properties.getProperty(nation);
			val = val.trim();
			val = val.length()==0?val:val.split(":")[1];
		} catch (final IOException ioe) {
			System.out.println("★ 프로퍼티에 없는 국가 : " + nation);
			ioe.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (final IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return val;
	}

	/**
	 * 자바로 utf-8 포맷의 파일을 수정하는 도중 한글 깨짐 현상이 발생
	 * 내용을 읽을 때
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * FileInputStream fis=new FileInputStream(filename);
	 * InputStreamReader isr=new InputStreamReader(fis,"UTF-8");
	 * BufferedReader br=new BufferedReader(isr);
	 *
	 * 파일에 내용을 쓸 때에는
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * FileOutputStream fos = new FileOutputStream(savefilename);
	 * OutputStreamWriter osw=new OutputStreamWriter(fos,"UTF-8");
	 * BufferedWriter bw=new BufferedWriter(osw);
	 */
	public void makeFileDenyIpList(final String writePath, final List<String> list) {
		String str ="";
		final Iterator<String> i = list.iterator();	// 반복자(Iterator)에 리스트 등록
		while (i.hasNext()) {					// 반복자에 다음이 있는 동안에
			str = str + i.next() + "\r\n";
		}
		try {
			final FileOutputStream fos   = new FileOutputStream(writePath + report_date + ".txt");
			final OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			final BufferedWriter bw      = new BufferedWriter(osw);
			bw.write(str);
			//			bw.newLine();
			bw.close();
			osw.close();
			fos.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void mergeFileDenyIpList(String writePath) {
		String strRL    = null;
		String str1="", str2="", str3="", str4="";
		String fileName = "";
		File   listFile = null;
		final File [] listFileArray = new File(writePath.substring(0, writePath.lastIndexOf("\\"))).listFiles();
		List<String> denyIpList1 = new ArrayList<>();
		for (int i=0 ; i<listFileArray.length ; i++) {
			listFile = listFileArray[i];
			fileName = listFile.getName();
			str1 = writePath.substring(writePath.lastIndexOf("\\") +1, writePath.length());
			if (fileName.indexOf(str1) > -1) {
				try {
					//BufferedReader in = new BufferedReader(new FileReader(listFile.getPath())); // 한글깨짐 그래서 아래로 인코딩(txt 파일처럼 ansi 로 인코딩된경우에만)
					final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(listFile.getPath()), "UTF-8"));
					while ((strRL = in.readLine()) != null) {
						if (strRL.indexOf("\"Description\"") > -1) {
							continue;
						}
						if (strRL.length() > 0) {
							denyIpList1.add(strRL);
						}
					}
					in.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		// ID 중복제거
		final TreeSet<String> distinctVerifi = new TreeSet<>(denyIpList1);
		denyIpList1 = new ArrayList<>(distinctVerifi);
		// 공격 횟수가 많은 순으로 정렬
		final List<String> denyIpList2 = new ArrayList<>();
		for (int i=0 ; i<denyIpList1.size() ; i++) {
			// "1.240.111.238","[KR,134] Jung-gu SK NamsanGreen Bldg,Namdaemunno 5(o)-ga, Seoul","0"
			str3 = denyIpList1.get(i);
			str4 = str3.split("\\,")[2];
			str4 = str4.replaceAll(",","");
			str4 = str4.split("\\]")[0];
			str4 = df1.format(Long.parseLong(str4));
			str3 = str4 + "," + str3;
			denyIpList2.add(str3);
		}
		Collections.sort(denyIpList2, Collections.reverseOrder());
		final Iterator<String> iterator = denyIpList2.iterator();
		while(iterator.hasNext()) {
			str3 = iterator.next();
			str2 = str2 + str3.substring(str3.indexOf(",") +1, str3.length()) + "\r\n";
		}
		str2 = "\"IP\",\"Description\",\"Allow\"\r\n" + str2;
		writePath = writePath.replaceAll("User.", ".");
		try {
			final BufferedWriter out = new BufferedWriter(new FileWriter(writePath + report_date.subSequence(0, 4) + ".txt"));
			out.write(str2);
			out.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		System.out.println("\t" + report_date.subSequence(0, 4) + "년 불법 IP : " + str1 + " 출력 : " + denyIpList1.size() + "건");
	}

	public void fileCopy(final File source, final File target) throws IOException {
		// 디렉토리인 경우
		if (source.isDirectory()) {
			// 복사될 Directory가 없으면 만듭니다.
			if (!target.exists()) {
				target.mkdir();
			}
			final String[] subDir = source.list();
			for (int i = 0; i < subDir.length; i++) {
				fileCopy(new File(source, subDir[i]), new File(target, subDir[i]));
			}
		} else {
			// 파일인 경우
			final InputStream in = new FileInputStream(source);
			final OutputStream out = new FileOutputStream(target);

			// Copy the bits from instream to outstream
			final byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public void copyRoot2BlackList() throws IOException {

		final File file = new File(notice_root);
		final File [] listFileArray = file.listFiles();
		File listFile = null;
		for (int j=0 ; j<listFileArray.length ; j++) {
			listFile = listFileArray[j];
			if (listFile.getName().contains("D69)")) {
				break;
			}
		}
		if (listFile == null) {
			System.out.println("\tBLACKLIST에 카피되지 않았습니다");
		} else {
			String lastPath = listFile.getAbsolutePath();
			lastPath = lastPath.substring(lastPath.lastIndexOf("\\") +1, lastPath.length());
			fileCopy(listFile, new File(BLACKLIST_ROOT + "\\" + lastPath));
		}
	}

	public boolean makeDir(final String path) {
		boolean b = true;
		try {
			final File file = new File(path);
			file.mkdir();
		} catch (final Exception e) {
			b = false;
		}
		return b;
	}
	/*
	// http://pusgochu.blog.me/10096214835
	public void sendMail() {
		// email 서버 DNS 또는 IP
		String server = "E-MAIL 서버 주소";
		Properties properties = new Properties();
		properties.put("mail.smtp.host", server);

		try {
			Session s = Session.getDefaultInstance(properties, null);
			Message message = new MimeMessage(s);

			String sender = "보내는 사람 이메일 주소";
			String subject = MimeUtility.encodeText("제목", "UTF-8", "B");
			String content = "본문";

			// 받는 사람은 다음과 같은 형식으로 작성하라.
			// 예) XXX@naver.com, YYY@naver.com, ZZZ@naver.com
			String mailAddress = "받는사람 주소";
			ArrayList<String> receiver = new ArrayList<String>();
			StringTokenizer stMailAddress = new StringTokenizer(mailAddress, ",", false);

			while (stMailAddress.hasMoreTokens()) {
				receiver.add(stMailAddress.nextToken());
			}

			// 보내는 사람 이름이란 메일 주소가 아닌 자신의 이름을 넣는 부분이다.
			// 네이버 같은 경우 "보내는 사람 이름"이 제목 옆에 있다.
			Address senderAddress = new InternetAddress(sender, MimeUtility.encodeText("보내는사람 이름", "UTF-8", "B"));
			Address[] receiverAddress = new Address[receiver.size()];

			for (int i = 0; i < receiver.size(); i++) {
				receiverAddress[i] = new InternetAddress(receiver.get(i));
			}

			message.setHeader("content-type", "text/html;charset=UTF-8");
			message.setFrom(senderAddress);
			message.addRecipients(Message.RecipientType.TO, receiverAddress);
			message.setSubject(subject);
			message.setContent(content, "text/html;charset=UTF-8");
			message.setSentDate(new java.util.Date());

			Transport.send(message);
		} catch (Exception e) {
			System.out.println("Error : send Email method");
			e.printStackTrace();
		}
	}
	 */
}
