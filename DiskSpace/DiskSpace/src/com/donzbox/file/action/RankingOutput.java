package com.donzbox.file.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingOutput {

	private static final String [] H_LEVEL = {"Ⅰ","Ⅱ","Ⅲ","Ⅳ","Ⅴ","Ⅵ","Ⅶ","Ⅷ","Ⅸ","Ⅹ"};
	private static final String [] L_LEVEL = {"ⅰ","ⅱ","ⅲ","ⅳ","ⅴ","ⅵ","ⅶ","ⅷ","ⅸ","ⅹ"}; 
	
	private String REPORT_ROOT = "D:\\My Data\\WWW\\Ftp Factory\\ftp.DonzBox.com_Result\\log_result";
	private String NAMEDB_ROOT = "D:\\My Data\\WWW\\Ftp Factory\\Serv-U\\Users\\DonzBox";
	private Map<String, String> map = new HashMap<String, String>();
	private static final String REPORT_KIND_UPK = "D21)  업로드왕 ";
	private static final String REPORT_KIND_UPG = "D21)  업로드神 ";
	private static final String REPORT_KIND_DNK = "D31)  다운왕 ";
	private static final String REPORT_KIND_DNG = "D31)  다운神 ";
	private static final String REPORT_KIND_PO = "D41)  인기자료 ";
	private static final String REPORT_KIND_UP_MORE = "\\D20)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣[업 더보기]\\";
	private static final String REPORT_KIND_DN_MORE = "\\D30)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣[다운 더보기]\\";
	private static final String REPORT_KIND_PO_MORE = "\\D40)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣[인기 더보기]\\";
	private static final int UPDN_TOTAL_RANKING = 5;
	private static final int PO_TOTAL_RANKING = 100;
	private String notice_root;
	private DecimalFormat dft  = new DecimalFormat("#,##0.00");
	private DecimalFormat dfg  = new DecimalFormat("#,##0.0");
	private DecimalFormat dfr = new DecimalFormat("00");
//	private DecimalFormat df  = new DecimalFormat("#,##0.0G");
	private List<String> upLoader = new ArrayList<String>();
	private boolean theEndOfTheYearChk = false;
	
	public void makeHashIDtoName() {
		int cnt = 1;
		String strRL    = null;
		String strID    = "";
		String strNM    = "";
		String fileName = "";
		File   listFile = null;
		File [] listFileArray = new File(NAMEDB_ROOT).listFiles();
		for (int i=0 ; i<listFileArray.length ; i++) {
			listFile = listFileArray[i];
			fileName = listFile.getName();
			if (fileName.indexOf(".Archive") > -1 && fileName.indexOf(".Backup") == -1) {
				try {
				    //BufferedReader in = new BufferedReader(new FileReader(listFile.getPath())); // 한글깨짐 그래서 아래로 인코딩(txt 파일처럼 ansi 로 인코딩된경우에만) 
				    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(listFile.getPath()), "UTF-8"));
				    
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
					    map.put(strID, strNM);
					    strID = "";
					    strNM = "";
				    }
				} catch (IOException e) {
				    e.printStackTrace();
				}
			}
		}
	}	
	
	public String id2name(String id, String strDN, String strUP, String flag, String rankNick) {
		String name = id;
		for (int i=0 ; i<upLoader.size() ; i++) {
			if(id.equals(upLoader.get(i))) {
				String star = "";for (int s=0 ; s<5-i ; s++) star=star+"★";
				if ("0.00".equals(strUP)) return name;
				if ("Best5".equals(flag)) {
					name = "(" + m2g(strDN, true) + ")  " + star + " " + rankNick + id;
				} else if ("All".equals(flag)) {
					name = "(" + m2g(strDN, true) + ")  " + rankNick + (i+1) + ".UP " + star + " " + id + " " + star + " " + "UP." + (i+1);
				}
				break;
			} else {
				name = (String) map.get(id);
				if (name==null) name = id + " (인증필요)";
				float fltUP = Float.parseFloat(m2g(strUP, false));
//				float fltDN = Float.parseFloat(m2g(strDN));
				if (fltUP < 0.6f) {
					name = "(" + m2g(strDN, true) + ")  " + rankNick + name + ("0.00".equals(strUP)?"":", (업로드 " + m2g(strUP, true)+"G)") + "";
				} else {
					name = "(" + m2g(strDN, true) + ")  " + rankNick + id   + ("0.00".equals(strUP)?"":", (업로드 " + m2g(strUP, true)+"G)") + "";
				}
			}
		}
		return name;
	}
	
	public RankingOutput(String notice_root) {
		this.notice_root = notice_root;
		try {
			makeHashIDtoName();
			getRanking();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getRanking() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		String sysDate = sdf.format(date);

		DateFormat df1 = new SimpleDateFormat("yyyy");
		DateFormat df2 = new SimpleDateFormat("M");
		if ("12".equals(df2.format(cal.getTime()))) {
			REPORT_ROOT = "D:\\My Data\\WWW\\Ftp Factory\\ftp.DonzBox.com_Result\\log_result\\each_year";
			sysDate = df1.format(date);
			theEndOfTheYearChk = true;
		}
sysDate = "201201";

		File file = new File(REPORT_ROOT);
		File [] listFileArray = file.listFiles();
		File listFile;
		String fileName = "";
		String rankInfo = "";
		
		String [] ct = null;
		String [] fct = null;

		try {
			deleteRankDirectory(notice_root);
			deleteRankDirectory(notice_root + REPORT_KIND_DN_MORE);
			deleteRankDirectory(notice_root + REPORT_KIND_UP_MORE);
			deleteRankDirectory(notice_root + REPORT_KIND_PO_MORE);
		} catch(Exception e) {
		}
		for (int i=0 ; i<listFileArray.length ; i++) {
			listFile = listFileArray[i];
			fileName = listFile.getName();
			
			ct = getContext(listFile);
			if ((sysDate+"up.txt").equals(fileName)) {
				for (int j=0 ; j<ct.length ; j++) {
					if (ct[j] == null) break;
					fct = getFilterContextUPDN(ct[j]);
					upLoader.add(fct[0]);
					if (j==UPDN_TOTAL_RANKING-1) break;
				}
			}
		}
			
		for (int i=0 ; i<listFileArray.length ; i++) {
			listFile = listFileArray[i];
			fileName = listFile.getName();
			ct = getContext(listFile);
			
			// fct[0]:name, fct[1]:dn, fct[2]:up
			if ((sysDate+"dn.txt").equals(fileName)) {
				// TOP 화면
				for (int j=0 ; j<ct.length ; j++) {
					if (ct[j] == null) break;
					fct = getFilterContextUPDN(ct[j]);
					if ("0.00".equals(fct[1])) continue;
					rankInfo = "\\" + getKingOrGod(REPORT_KIND_DNK, j) + (j+1) + "위.  " + id2name(fct[0], fct[1], fct[2], "Best5", getRankNick(fct[1]));
					// 다운로드 1위는 다운신으로 명명
					if (!theEndOfTheYearChk && j == 0) {
						rankInfo = "\\" + REPORT_KIND_DNK + (j+1) + "위.  " + id2name(fct[0], fct[1], fct[2], "Best5", "다운신 ");
					}
					setDirectoryName(rankInfo, REPORT_KIND_DNK);
					if (j==UPDN_TOTAL_RANKING-1) break;
				}
				// 더보기
				for (int j=0 ; j<ct.length ; j++) {
					if (ct[j] == null) break;
					fct = getFilterContextUPDN(ct[j]);
					if ("0.00".equals(fct[1])) continue;
					rankInfo = REPORT_KIND_DN_MORE + getKingOrGod(REPORT_KIND_DNK, j).substring(6) + dfr.format((j+1)) + "위.  " + id2name(fct[0], fct[1], fct[2], "All", getRankNick(fct[1]));
					//  다운로드 1위는 다운신으로 명명
					if (!theEndOfTheYearChk && j == 0) {
						rankInfo = REPORT_KIND_DN_MORE + REPORT_KIND_DNK.substring(6) + dfr.format((j+1)) + "위.  " + id2name(fct[0], fct[1], fct[2], "All", "다운신 ");
					}
					setDirectoryName(rankInfo, REPORT_KIND_DNK);
				}
			}
			if ((sysDate+"up.txt").equals(fileName)) {
				// top 화면
				for (int j=0 ; j<ct.length ; j++) {
					if (ct[j] == null) break;
					fct = getFilterContextUPDN(ct[j]);
					if ("0.00".equals(fct[2])) continue;
					String star = "";for (int s=0 ; s<5-j ; s++) star=star+"★";
					rankInfo = "\\" + getKingOrGod(REPORT_KIND_UPK, j) + (j+1) + "위.  (" + m2g(fct[2], true) + ") " + star + " " + fct[0];
					setDirectoryName(rankInfo, REPORT_KIND_UPK);
					if (j==UPDN_TOTAL_RANKING-1) break;
				}
				// 더보기
				for (int j=0 ; j<ct.length ; j++) {
					if (ct[j] == null) break;
					fct = getFilterContextUPDN(ct[j]);
					if ("0.00".equals(fct[2])) continue;
					String star = "";if (j<5) { for (int s=0 ; s<5-j ; s++) star=star+"★";}
					if (j==5) { star="☆";}
					if (6<=j && j<=15) star=H_LEVEL[j-6]+".";
					if (16<=j && j<=25) star=L_LEVEL[j-16]+".";
					rankInfo = REPORT_KIND_UP_MORE + getKingOrGod(REPORT_KIND_UPK, j).substring(6) + dfr.format((j+1)) + "위.  (" + m2g(fct[2], true) + ") " + star + " " + fct[0];
					setDirectoryName(rankInfo, REPORT_KIND_UPK);
				}
			}
			if ((sysDate+"po.txt").equals(fileName)) {
				//
				for (int j=0 ; j<ct.length ; j++) {
					if (!(ct[j].toLowerCase().contains("[y]") || ct[j].contains("adult"))) {
						if (ct[j] == null) break;
						rankInfo = "\\" + REPORT_KIND_PO + (j+1) + "위.  " + getFilterContextPO(ct[j]);
						setDirectoryName(rankInfo, REPORT_KIND_PO);
						if (j==8) break;
					}
				}
				//
				for (int j=0 ; j<ct.length ; j++) {
					if (!(ct[j].toLowerCase().contains("[y]") || ct[j].contains("adult"))) {
						if (ct[j] == null) break;
						rankInfo = REPORT_KIND_PO_MORE + REPORT_KIND_PO.substring(6) + dfr.format((j+1)) + "위.  " + getFilterContextPO(ct[j]);
						setDirectoryName(rankInfo, REPORT_KIND_PO);
					}
				}
			}
		}
	}
	
	// 연말 총결산때 1~3위는 "신"이라는 명칭 부여, 1월~11월까지는 "왕"이라는 명칭 부여
	public String getKingOrGod(String type, int rank) {
		if (0 <= rank && rank <3) {
			if (REPORT_KIND_UPK.equals(type)) {
				type = theEndOfTheYearChk?REPORT_KIND_UPG:REPORT_KIND_UPK;
			} else if (REPORT_KIND_DNK.equals(type)) {
				type = theEndOfTheYearChk?REPORT_KIND_DNG:REPORT_KIND_DNK;
			}
		}
		return type;
	}
	
	public String getRankNick(String dn) {
		String result = "";
		float dnSize = Float.parseFloat(dn)/1024F;
		
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
			int lvl = (int)dnSize/5;
			result = " ";
		}
		return result;
	}
	
	public String m2g(String strMegaByte, boolean flag) {
		float fltMegaByte = Float.parseFloat(strMegaByte);
		fltMegaByte = fltMegaByte/1024f;
		if (dfg.format(fltMegaByte).equals("0.0"))
			return flag?"0.1G":"0.1";
		else {
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
	
	public void setDirectoryName(String rankInfo, String kind) {
		File file = new File(notice_root);
		if (REPORT_KIND_DNK.equals(kind)) {
			File tmpFile = new File(file + rankInfo);
			tmpFile.mkdirs();
		}
		if (REPORT_KIND_UPK.equals(kind)) {
			File tmpFile = new File(file + rankInfo);
			tmpFile.mkdirs();
		}
		if (REPORT_KIND_PO.equals(kind)) {
			File tmpFile = new File(file + rankInfo);
			tmpFile.mkdirs();
		}
	}
	
	public void deleteRankDirectory(String directoryPath) {
		File file = new File(directoryPath);
		File [] listFileArray = file.listFiles();
		String fileName = "";
		File listFile;
		
		for (int j=0 ; j<listFileArray.length ; j++) {
			listFile = listFileArray[j];
			fileName = listFile.getName();
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
			) {
				listFile.delete();
			}
		}
	}
	
	public String [] getContext(File listFile) {
		int arrayCnt = 0;
		int contextCnt = 1;
		String [] rangking = new String[PO_TOTAL_RANKING];
		try {
			BufferedReader in = new BufferedReader(new FileReader(listFile));
			String oneLineContents;
			while ((oneLineContents = in.readLine()) != null) {
				if (oneLineContents.contains(" " + (contextCnt) + ":")) {
					if (!(oneLineContents.contains("[Y]") || oneLineContents.contains("[y]") || oneLineContents.contains("adult"))) {
						if (listFile.getName().contains("dn.txt")) {
							rangking[arrayCnt] = oneLineContents;
						}
						if (listFile.getName().contains("up.txt")) {
							rangking[arrayCnt] = oneLineContents;
						}
						if (listFile.getName().contains("po.txt")) {
							rangking[arrayCnt] = oneLineContents;
						}
						arrayCnt++;	
					}
					contextCnt++;
				}
				if (arrayCnt == PO_TOTAL_RANKING) break;
			}
			in.close();
		} catch (IOException e) {}
		return rangking;
	}
	
	public String [] getFilterContextUPDN(String context) {
		String [] returnValue = new String[3];
		String [] contextArray = context.split(" ");
		int cnt = 0;
		for (String tmp : contextArray) {
			if (tmp.length()!=0 ) {
				if(cnt==1) {
					returnValue[0] = tmp;
					System.out.print("[" + tmp + "] ");
				}
				if(cnt==4) {
					returnValue[1] = tmp;
					System.out.print("[" + tmp + "] ");
				}
				if(cnt==7) {
					returnValue[2] = tmp;
					System.out.println("[" + tmp + "]");
				}
				cnt++;
			}
		}
		return returnValue;
	}
	
	public String getFilterContextPO(String context) {
		String [] contextArray = context.split("\\\\");
		context = contextArray[contextArray.length-1];
		return context;
	}
}
