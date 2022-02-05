package com.donzbox.file.action;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


/*
 * ---------------------------------
 *        This is main class
 * ---------------------------------
 */
public class DiskSpaceOutput  {

	private static String FTP_ROOT = "D:\\My Data\\MY FTP\\FTP ROOT\\";
	private static final String DRIVE_D = "D82)  [Ｄ：]";
	private static final String DRIVE_E = "D83)  [Ｅ：]";
	private static final String DRIVE_F = "D84)  [Ｆ：]";
	private static final String DRIVE_G = "D85)  [Ｇ：]";
	private static final String DRIVE_H = "D86)  [Ｈ：]";
	private static final String DRIVE_I = "D87)  [Ｉ：]";
	private static final String DRIVE_J = "D88)  [Ｊ：]";
  /*범례	
    +-------+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+
    |Size(T)|  9.99  |  3.00  |  2.00  |  1.50  |  1.00  |  0.50  |  0.32  |  0.30  |  0.12  |  0.08  |
    +-------+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+
    | Float | 0.0013 | 0.0045 | 0.0050 | 0.0064 | 0.0080 | 0.0130 | 0.0140 | 0.0150 | 0.0300 | 0.0350 |
    +-------+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+*/
	private static float ctrlFloat_A = 0.0300f; // 0.30 T
	private static float ctrlFloat_D = 0.0300f; // 0.12 T
	private static float ctrlFloat_E = 0.0050f; // 2.00 T
	private static float ctrlFloat_F = 0.0050f; // 2.00 T
	private static float ctrlFloat_G = 0.0080f; // 1.00 T
	private static float ctrlFloat_H = 0.0300f; // 0.12 T
	private static float ctrlFloat_I = 0.0140f; // 0.32 T
	private static float ctrlFloat_J = 0.0150f; // 0.30 T
	private static final String START = "START";
	private static final String END = "END";
	private static final String NOT_EXIST_HDD = " ⇒  하드디스크 인식불가 (ㅠ_ㅠ)";
	
	private float ctrlFloat;
	private DecimalFormat df1 = new DecimalFormat("0");
//  private DecimalFormat df2 = new DecimalFormat("#,###G");
	private DecimalFormat df2 = new DecimalFormat("####");
	private static DiskSpace ds = new DiskSpace();
//	private static String FTP_ROOT_BLACKLIST = "D:\\My Data\\MY FTP\\FTP ROOT BLACKLIST\\";
	private boolean theEndOfTheYearChk = false;
	
	private int    diskSizeTotal_l    = 0;	// _l : 문자길이
	private int    diskSizeFree_l     = 0;
	private int    diskSizeTotalTop_l = 0;
	private int    diskSizeFreeTop_l  = 0;

	// 메소드의 수행시간 구하기
	public long startTime;
	public long endTime;
	
	private String tmpStr;
	
	public DiskSpaceOutput() {
		try {
			redrawDirectory();
			getDiskSpace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getDiskSpace() throws Exception {
		
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine        js  = sem.getEngineByName("javascript");
		
		List<String> driverList = new ArrayList<String>();
//		for (int i='D' ; i<='J' ; i++) {
//			String s = (String)js.eval("DRIVE_" + Character.toString((char)i));
//			System.out.println(s);
//			driverList.add((String)js.eval("DRIVE_" + Character.toString((char)i)));
//		}
		driverList.add(DRIVE_D);
		driverList.add(DRIVE_E);
		driverList.add(DRIVE_F);
		driverList.add(DRIVE_G);
		driverList.add(DRIVE_H);
		driverList.add(DRIVE_I);
		driverList.add(DRIVE_J);

		File [] listFileArray = new File(FTP_ROOT).listFiles();
		File   listFile;
		String fileName           = "";
		String diskSize           = "";
		int    diskSizeTotalTop_i = 0;	// _i : 숫자
		int    diskSizeTotal_i    = 0;
		int    diskSizeEmpty_i    = 0;
		int    diskSizeUsed_i     = 0;
		int    diskSizeFree_i     = 0;
		String diskSizeTotal_s    = "";	// _s : 스트링형 숫자	
		String diskSizeUsed_s     = "";
		String diskSizeFree_s     = "";
		float  diskSizeTotal_f    = 0.0f;
		float  diskSizeUsed_f     = 0.0f;
		float  diskSizeFree_f     = 0.0f;
		String driverName         = "";

		// 용량 그래프를 만들기 전에, 실제 용량을 간략한 수치로 산정하여, 기준이 될 최대 네모칸 사이즈 계산
		for (int i=0 ; i<driverList.size() ; i++) {
			driverName = (String)driverList.get(i);
			// ex) "D82)  [Ｄ：]" -> "D:"
			tmpStr = change2To1ByteChar(driverName);
			tmpStr = tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf("]"));
			if (!new File(tmpStr).exists()) continue;
			
			if (DRIVE_D.equals(driverName)) {
				ctrlFloat = ctrlFloat_D;
			} else if (DRIVE_E.equals(driverName)) {
				ctrlFloat = ctrlFloat_E;
			} else if (DRIVE_F.equals(driverName)) {
				ctrlFloat = ctrlFloat_F;
			} else if (DRIVE_G.equals(driverName)) {
				ctrlFloat = ctrlFloat_G;
			} else if (DRIVE_H.equals(driverName)) {
				ctrlFloat = ctrlFloat_H;
			} else if (DRIVE_I.equals(driverName)) {
				ctrlFloat = ctrlFloat_I;
			} else if (DRIVE_J.equals(driverName)) {
				ctrlFloat = ctrlFloat_J;
			} else {
				ctrlFloat = ctrlFloat_A;
			}
			diskSize   = ds.getDiskSpace(tmpStr);

			diskSizeTotal_s = diskSize.split("\\|")[0];
			diskSizeFree_s  = diskSize.split("\\|")[1];
			diskSizeTotal_l = diskSizeTotal_s.length();
			diskSizeFree_l  = diskSizeFree_s.length();
			diskSizeTotal_i = Integer.parseInt(diskSizeTotal_s);
			diskSizeFree_i  = Integer.parseInt(diskSizeFree_s);
			diskSizeUsed_i  = diskSizeTotal_i - diskSizeFree_i;
			diskSizeUsed_s  = String.valueOf(diskSizeUsed_i);
			
			diskSizeUsed_f  = Float.parseFloat(diskSizeUsed_s);
			diskSizeFree_f  = Float.parseFloat(diskSizeFree_s);

			// 실제 용량을 간략 용량으로 변환
			diskSizeUsed_s  = df1.format(diskSizeUsed_f*ctrlFloat);
			diskSizeFree_s  = df1.format(diskSizeFree_f*ctrlFloat);
			diskSizeUsed_i  = Integer.parseInt(diskSizeUsed_s);
			diskSizeFree_i  = Integer.parseInt(diskSizeFree_s);
			
			// 모든 드라이브 중에 최대 용량을 diskSizeTotalTop_i에 입력(그래프 길에 조율용)
			diskSizeTotal_i = diskSizeUsed_i + diskSizeFree_i;
			if (diskSizeTotal_i > diskSizeTotalTop_i) diskSizeTotalTop_i = diskSizeTotal_i;
			// 모든 드라이브의 용량 중, 최대 length 추출(숫자길에 조율용)
			if (diskSizeTotal_l > diskSizeTotalTop_l) diskSizeTotalTop_l = diskSizeTotal_l;
			// 모든 드라이브의 남은용량 중, 최대 length 추출(숫자길에 조율용)
			if (diskSizeFree_l  > diskSizeFreeTop_l)  diskSizeFreeTop_l  = diskSizeFree_l;
		}
		
		// 용량 그래프 그리기 종합
		for (int i=0 ; i<driverList.size() ; i++) {
			driverName = (String)driverList.get(i);
			// ex) "D82)  [Ｄ：]" -> "D:"
			tmpStr = change2To1ByteChar(driverName);
			tmpStr = tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf("]"));

			// "하드디스크 인식불가" 디렉토리 타이틀 삭제
			if (!new File(tmpStr).exists()) {
				for (int j=0 ; j<listFileArray.length ; j++) {
					listFile = listFileArray[j];
					fileName = listFile.getName();
					if (fileName.contains(driverName)) listFile.delete();
				}
				continue;
			}
			
			if (DRIVE_D.equals(driverName)) {
				ctrlFloat = ctrlFloat_D;
			} else if (DRIVE_E.equals(driverName)) {
				ctrlFloat = ctrlFloat_E;
			} else if (DRIVE_F.equals(driverName)) {
				ctrlFloat = ctrlFloat_F;
			} else if (DRIVE_G.equals(driverName)) {
				ctrlFloat = ctrlFloat_G;
			} else if (DRIVE_H.equals(driverName)) {
				ctrlFloat = ctrlFloat_H;
			} else if (DRIVE_I.equals(driverName)) {
				ctrlFloat = ctrlFloat_I;
			} else if (DRIVE_J.equals(driverName)) {
				ctrlFloat = ctrlFloat_J;
			} else {
				ctrlFloat = ctrlFloat_A;
			}
			// 실제 디스크의 용량 정보 가져오기 (Native Java)
			diskSize   = ds.getDiskSpace(tmpStr);

			diskSizeTotal_s = diskSize.split("\\|")[0];
			diskSizeFree_s  = diskSize.split("\\|")[1];
			diskSizeTotal_i = Integer.parseInt(diskSizeTotal_s);
			diskSizeFree_i  = Integer.parseInt(diskSizeFree_s);
			diskSizeUsed_i  = diskSizeTotal_i - diskSizeFree_i;
			diskSizeUsed_s  = String.valueOf(diskSizeUsed_i);
			
			diskSizeTotal_f = Float.parseFloat(diskSizeTotal_s);
			diskSizeUsed_f  = Float.parseFloat(diskSizeUsed_s);
			diskSizeFree_f  = Float.parseFloat(diskSizeFree_s);
						
			for (int j=0 ; j<listFileArray.length ; j++) {
				String diskSizeUsed_t  = "";
				String diskSizeFree_t  = "";
				String diskSizeEmpty_t = "";
				
				listFile = listFileArray[j];
				fileName = listFile.getName();
				diskSizeUsed_s  = df1.format(diskSizeUsed_f*ctrlFloat);
				diskSizeFree_s  = df1.format(diskSizeFree_f*ctrlFloat);
				diskSizeUsed_i  = Integer.parseInt(diskSizeUsed_s);
				diskSizeFree_i  = Integer.parseInt(diskSizeFree_s);
				diskSizeTotal_i = diskSizeUsed_i + diskSizeFree_i;
				diskSizeEmpty_i = diskSizeTotalTop_i - diskSizeTotal_i;
				for (int k=0 ; k<diskSizeUsed_i  ; k++) diskSizeUsed_t = diskSizeUsed_t + "■";
				for (int k=0 ; k<diskSizeFree_i  ; k++) diskSizeFree_t = diskSizeFree_t + "□";
				for (int k=0 ; k<diskSizeEmpty_i ; k++) diskSizeEmpty_t = diskSizeEmpty_t + "　";
				
				diskSizeTotal_s = df2.format(diskSizeTotal_f);
				diskSizeFree_s  = df2.format(diskSizeFree_f);
				String diskPrint = " 全" + change1To2ByteSpaceChar(diskSizeTotal_s, "total") + " " + diskSizeUsed_t + diskSizeFree_t + diskSizeEmpty_t + change1To2ByteSpaceChar(diskSizeFree_s, "free") + "空";
				if (fileName.contains(DRIVE_D) && driverName.equals(DRIVE_D)) {
//					System.out.println(diskSizeTotal_f*ctrlFloat + " = " + diskSizeUsed_f*ctrlFloat + " + " + diskSizeFree_f*ctrlFloat);
					fileName = FTP_ROOT + DRIVE_D + diskPrint;
					listFile.renameTo(new File(fileName));
				}
				if (fileName.contains(DRIVE_E) && driverName.equals(DRIVE_E)) {
//					System.out.println(diskSizeTotal_f*ctrlFloat + " = " + diskSizeUsed_f*ctrlFloat + " + " + diskSizeFree_f*ctrlFloat);
					fileName = FTP_ROOT + DRIVE_E + diskPrint;
					listFile.renameTo(new File(fileName));
				}
				if (fileName.contains(DRIVE_F) && driverName.equals(DRIVE_F)) {
//					System.out.println(diskSizeTotal_f*ctrlFloat + " = " + diskSizeUsed_f*ctrlFloat + " + " + diskSizeFree_f*ctrlFloat);
					fileName = FTP_ROOT + DRIVE_F + diskPrint;
					listFile.renameTo(new File(fileName));
				}
				if (fileName.contains(DRIVE_G) && driverName.equals(DRIVE_G)) {
//					System.out.println(diskSizeTotal_f*ctrlFloat + " = " + diskSizeUsed_f*ctrlFloat + " + " + diskSizeFree_f*ctrlFloat);
					fileName = FTP_ROOT + DRIVE_G + diskPrint;
					listFile.renameTo(new File(fileName));
				}
				if (fileName.contains(DRIVE_H) && driverName.equals(DRIVE_H)) {
//					System.out.println(diskSizeTotal_f*ctrlFloat + " = " + diskSizeUsed_f*ctrlFloat + " + " + diskSizeFree_f*ctrlFloat);
					fileName = FTP_ROOT + DRIVE_H + diskPrint;
					listFile.renameTo(new File(fileName));
				}
				if (fileName.contains(DRIVE_I) && driverName.equals(DRIVE_I)) {
//					System.out.println(diskSizeTotal_f*ctrlFloat + " = " + diskSizeUsed_f*ctrlFloat + " + " + diskSizeFree_f*ctrlFloat);
					fileName = FTP_ROOT + DRIVE_I + diskPrint;
					listFile.renameTo(new File(fileName));
				}
				if (fileName.contains(DRIVE_J) && driverName.equals(DRIVE_J)) {
//					System.out.println(diskSizeTotal_f*ctrlFloat + " = " + diskSizeUsed_f*ctrlFloat + " + " + diskSizeFree_f*ctrlFloat);
					fileName = FTP_ROOT + DRIVE_J + diskPrint;
					listFile.renameTo(new File(fileName));
				}
				if (fileName.contains("D01)")) {
					listFile.renameTo(new File(FTP_ROOT + "D01)  " + getTitle()));
				}
				if (fileName.contains("D02)")) {
					listFile.renameTo(new File(FTP_ROOT + "D02)  " + (theEndOfTheYearChk?"　【 12월은 업로드神! 다운神! 선발대회 】":"")));
				}
				if (fileName.contains("D97)")) {
					listFile.renameTo(new File(FTP_ROOT + "D97)  " + getTitle()));
				}
			}
		}
	}
	// 디렉토리에 사용할 수 없는 문자 치환
	//                              ＼          ／         ：          ＊    ？   ＂  ＜  ＞  ┃
	private String [] dir1Byte = {/*"\\\\",*/	"/"		/*,":"*/	,"\\*","\\?","\"","\\<","\\>","\\|"}; 
	private String [] dir2Byte = {/*"＼",*/		"／"	/*,"："*/	,"＊","？","＂","＜","＞","┃"};
	public String changeToDirChar(String s) {
		for (int i=0 ; i<dir1Byte.length ; i++) s = s.replaceAll(dir1Byte[i], dir2Byte[i]);
		return s;
	}

	// 보기 좋게 칸수를 맞추기 위한 노력
	//                            ０  １  ２  ３  ４  ５  ６  ７  ８  ９  Ａ  Ｂ  Ｃ  Ｄ  Ｅ  Ｆ  Ｇ  Ｈ  Ｉ  Ｊ  Ｋ  Ｌ  Ｍ  Ｎ  Ｏ  Ｐ  Ｑ  Ｒ  Ｓ  Ｔ  Ｕ  Ｖ  Ｗ  Ｘ  Ｙ  Ｚ  　  ／  ：
	private String [] str1Byte = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"," ","/",":"}; 
	private String [] str2Byte = {"０","１","２","３","４","５","６","７","８","９","Ａ","Ｂ","Ｃ","Ｄ","Ｅ","Ｆ","Ｇ","Ｈ","Ｉ","Ｊ","Ｋ","Ｌ","Ｍ","Ｎ","Ｏ","Ｐ","Ｑ","Ｒ","Ｓ","Ｔ","Ｕ","Ｖ","Ｗ","Ｘ","Ｙ","Ｚ","　","／","："};
	public String change1To2ByteSpaceChar(String s, String flag) {
		for (int i=0 ; i<str1Byte.length ; i++) s = s.replaceAll(str1Byte[i], str2Byte[i]);
		int sLength = s.length();
		if ("total".equals(flag) && sLength < diskSizeTotalTop_l) {
			for (int i=0 ; i<diskSizeTotalTop_l - sLength ; i++) s = "　" + s;
		} else if ("free".equals(flag) && sLength < diskSizeFreeTop_l) {
			for (int i=0 ; i<diskSizeFreeTop_l - sLength ; i++) s = "　" + s;
		}
		return s;
	}
	public String change1To2ByteChar(String s) {
		for (int i=0 ; i<str1Byte.length ; i++) s = s.replaceAll(str1Byte[i], str2Byte[i]);
		return s;
	}
	public String change2To1ByteChar(String s) {
		for (int i=0 ; i<str2Byte.length ; i++) s = s.replaceAll(str2Byte[i], str1Byte[i]);
		return s;
	}
	public String getTitle() {
		Calendar currentDate = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("   (d일HH시)");
		DateFormat df1 = new SimpleDateFormat("   ＤｏｎｚＢｏｘ  M 월");
		DateFormat df2 = new SimpleDateFormat("M");
		if ("12".equals(df2.format(currentDate.getTime()))) {
			df = new SimpleDateFormat("  (M월d일HH시)");
			df1 = new SimpleDateFormat("   'DonzBox' yyyy 총결산");
			theEndOfTheYearChk = true;
		}
		String sysdate = df1.format(currentDate.getTime()) + df.format(currentDate.getTime());
		return sysdate;   
	}
	
	public void redrawDirectory() {
		File file = new File(FTP_ROOT);
		File [] listFileArray = file.listFiles();
		File listFile;
		
		//String patternStr1 = "zz[0-9]|D[0-68][0-9]\\)";
		String patternStr1 = "D[0-68-9][0-9]\\)";
		Pattern pattern = Pattern.compile(patternStr1);
		CharSequence fileName;
		Matcher matcher;
		
		for (int j=0 ; j<listFileArray.length ; j++) {
			listFile = listFileArray[j];
			fileName = listFile.getName();
			if ((listFile.getName()).indexOf("D91) ") <= -1) {
				matcher = pattern.matcher(fileName);
				if (matcher.find()) {
					listFile.delete();
//System.out.println("삭제 : " + listFile.getName());
				}
			}
		}
		
		List<String> list = new ArrayList<String>();
		list.add("D00)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣");
		list.add("D01)");
		list.add("D02)");
		list.add("D20)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣[업 더보기]");
//		list.add("D21)  업로드왕");
		list.add("D30)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣[다운 더보기]");
//		list.add("D31)  다운왕");
		list.add("D40)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣[인기 더보기]");
//		list.add("D41)  인기자료");
		list.add("D50)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣");
		list.add("D51)  기증1 - [HDD 5.47테라] SCHUKI＊");
		list.add("D52)  기증2 - [HDD 0.65테라] AFREET9＊");
		list.add("D52)  기증3 - [HDD 1.00테라] KODAL3＊ & KEYLIN＊");
		list.add("D52)  기증4 - [HDD 2.00테라] BEDSHI＊");
		list.add("D55)  운영자 - HAI＊, EHE＊, Donz");
		list.add("D68)   ＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿");
//		list.add("D69)");
		list.add("D70)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣");
		list.add("D80)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣");
		list.add("D81)  ＿＿＿[ Box Size Checker ]＿＿＿단위：기가＿＿");
		list.add(DRIVE_D + NOT_EXIST_HDD);
		list.add(DRIVE_E + NOT_EXIST_HDD);
		list.add(DRIVE_F + NOT_EXIST_HDD);
		list.add(DRIVE_G + NOT_EXIST_HDD);
		list.add(DRIVE_H + NOT_EXIST_HDD);
		list.add(DRIVE_I + NOT_EXIST_HDD);
		list.add(DRIVE_J + NOT_EXIST_HDD);
		list.add("D90)  ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣");
		list.add("D96)   ＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿");
		list.add("D97)");
		list.add("D98)   ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣");
		File tmpFile;
		for (int i=0 ; i<list.size() ; i++) {
			tmpFile = new File(FTP_ROOT + list.get(i));
			tmpFile.mkdirs();
		}
		list.clear();
//		list.add("D20) ￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣");
//		list.add("D51)  당신의 FTP를 통하여 비정기 지속적 적으로");
//		list.add("D52)  악성파일들이 업로드 되고 있습니다.");
//		list.add("D53)  이로 인해 많은 사람들이 피해를 받을 수 있습니다.");
//		list.add("D54)  완전히 치료 후 아래의 주소로 문의하세요.");
//		list.add("D55)  ");
//		list.add("D56)            - 관리자 DonzBox.com@gmail.com -");
//		list.add("D60) ＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿");
//		for (int i=0 ; i<list.size() ; i++) {
//			tmpFile = new File(FTP_ROOT_BLACKLIST + list.get(i));
//			tmpFile.mkdirs();
//		}
	}
	
	// 메소드의 수행시간 구하기
	public void runningTime(String type) {
		if (START.equals(type)) {
			startTime = System.nanoTime();
		} else if (END.equals(type)) {
			endTime = System.nanoTime();
			long resultTime = endTime - startTime;
			System.out.println("\r\n### 수행시간 " + getMillisecond2Time(resultTime));
		}
	}
	public String getMillisecond2Time(long diffTime) {
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

	public static void main (String [] args) {
		
		DiskSpaceOutput dso = new DiskSpaceOutput();
		
		dso.runningTime(START);
		
		new DiskSpaceOutput();
//과거꺼new RankingOutput(FTP_ROOT);
		new ServULogRankingOutput(FTP_ROOT, args);
		
		dso.runningTime(END);
	}
}
