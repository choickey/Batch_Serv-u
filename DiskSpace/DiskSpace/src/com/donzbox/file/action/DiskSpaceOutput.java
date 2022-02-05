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
	private static final String DRIVE_D = "D82)  [�ģ�]";
	private static final String DRIVE_E = "D83)  [�ţ�]";
	private static final String DRIVE_F = "D84)  [�ƣ�]";
	private static final String DRIVE_G = "D85)  [�ǣ�]";
	private static final String DRIVE_H = "D86)  [�ȣ�]";
	private static final String DRIVE_I = "D87)  [�ɣ�]";
	private static final String DRIVE_J = "D88)  [�ʣ�]";
  /*����	
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
	private static final String NOT_EXIST_HDD = " ��  �ϵ��ũ �νĺҰ� (��_��)";
	
	private float ctrlFloat;
	private DecimalFormat df1 = new DecimalFormat("0");
//  private DecimalFormat df2 = new DecimalFormat("#,###G");
	private DecimalFormat df2 = new DecimalFormat("####");
	private static DiskSpace ds = new DiskSpace();
//	private static String FTP_ROOT_BLACKLIST = "D:\\My Data\\MY FTP\\FTP ROOT BLACKLIST\\";
	private boolean theEndOfTheYearChk = false;
	
	private int    diskSizeTotal_l    = 0;	// _l : ���ڱ���
	private int    diskSizeFree_l     = 0;
	private int    diskSizeTotalTop_l = 0;
	private int    diskSizeFreeTop_l  = 0;

	// �޼ҵ��� ����ð� ���ϱ�
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
		int    diskSizeTotalTop_i = 0;	// _i : ����
		int    diskSizeTotal_i    = 0;
		int    diskSizeEmpty_i    = 0;
		int    diskSizeUsed_i     = 0;
		int    diskSizeFree_i     = 0;
		String diskSizeTotal_s    = "";	// _s : ��Ʈ���� ����	
		String diskSizeUsed_s     = "";
		String diskSizeFree_s     = "";
		float  diskSizeTotal_f    = 0.0f;
		float  diskSizeUsed_f     = 0.0f;
		float  diskSizeFree_f     = 0.0f;
		String driverName         = "";

		// �뷮 �׷����� ����� ����, ���� �뷮�� ������ ��ġ�� �����Ͽ�, ������ �� �ִ� �׸�ĭ ������ ���
		for (int i=0 ; i<driverList.size() ; i++) {
			driverName = (String)driverList.get(i);
			// ex) "D82)  [�ģ�]" -> "D:"
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

			// ���� �뷮�� ���� �뷮���� ��ȯ
			diskSizeUsed_s  = df1.format(diskSizeUsed_f*ctrlFloat);
			diskSizeFree_s  = df1.format(diskSizeFree_f*ctrlFloat);
			diskSizeUsed_i  = Integer.parseInt(diskSizeUsed_s);
			diskSizeFree_i  = Integer.parseInt(diskSizeFree_s);
			
			// ��� ����̺� �߿� �ִ� �뷮�� diskSizeTotalTop_i�� �Է�(�׷��� �濡 ������)
			diskSizeTotal_i = diskSizeUsed_i + diskSizeFree_i;
			if (diskSizeTotal_i > diskSizeTotalTop_i) diskSizeTotalTop_i = diskSizeTotal_i;
			// ��� ����̺��� �뷮 ��, �ִ� length ����(���ڱ濡 ������)
			if (diskSizeTotal_l > diskSizeTotalTop_l) diskSizeTotalTop_l = diskSizeTotal_l;
			// ��� ����̺��� �����뷮 ��, �ִ� length ����(���ڱ濡 ������)
			if (diskSizeFree_l  > diskSizeFreeTop_l)  diskSizeFreeTop_l  = diskSizeFree_l;
		}
		
		// �뷮 �׷��� �׸��� ����
		for (int i=0 ; i<driverList.size() ; i++) {
			driverName = (String)driverList.get(i);
			// ex) "D82)  [�ģ�]" -> "D:"
			tmpStr = change2To1ByteChar(driverName);
			tmpStr = tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf("]"));

			// "�ϵ��ũ �νĺҰ�" ���丮 Ÿ��Ʋ ����
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
			// ���� ��ũ�� �뷮 ���� �������� (Native Java)
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
				for (int k=0 ; k<diskSizeUsed_i  ; k++) diskSizeUsed_t = diskSizeUsed_t + "��";
				for (int k=0 ; k<diskSizeFree_i  ; k++) diskSizeFree_t = diskSizeFree_t + "��";
				for (int k=0 ; k<diskSizeEmpty_i ; k++) diskSizeEmpty_t = diskSizeEmpty_t + "��";
				
				diskSizeTotal_s = df2.format(diskSizeTotal_f);
				diskSizeFree_s  = df2.format(diskSizeFree_f);
				String diskPrint = " ��" + change1To2ByteSpaceChar(diskSizeTotal_s, "total") + " " + diskSizeUsed_t + diskSizeFree_t + diskSizeEmpty_t + change1To2ByteSpaceChar(diskSizeFree_s, "free") + "��";
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
					listFile.renameTo(new File(FTP_ROOT + "D02)  " + (theEndOfTheYearChk?"���� 12���� ���ε���! �ٿ���! ���ߴ�ȸ ��":"")));
				}
				if (fileName.contains("D97)")) {
					listFile.renameTo(new File(FTP_ROOT + "D97)  " + getTitle()));
				}
			}
		}
	}
	// ���丮�� ����� �� ���� ���� ġȯ
	//                              ��          ��         ��          ��    ��   ��  ��  ��  ��
	private String [] dir1Byte = {/*"\\\\",*/	"/"		/*,":"*/	,"\\*","\\?","\"","\\<","\\>","\\|"}; 
	private String [] dir2Byte = {/*"��",*/		"��"	/*,"��"*/	,"��","��","��","��","��","��"};
	public String changeToDirChar(String s) {
		for (int i=0 ; i<dir1Byte.length ; i++) s = s.replaceAll(dir1Byte[i], dir2Byte[i]);
		return s;
	}

	// ���� ���� ĭ���� ���߱� ���� ���
	//                            ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��  ��
	private String [] str1Byte = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"," ","/",":"}; 
	private String [] str2Byte = {"��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��","��"};
	public String change1To2ByteSpaceChar(String s, String flag) {
		for (int i=0 ; i<str1Byte.length ; i++) s = s.replaceAll(str1Byte[i], str2Byte[i]);
		int sLength = s.length();
		if ("total".equals(flag) && sLength < diskSizeTotalTop_l) {
			for (int i=0 ; i<diskSizeTotalTop_l - sLength ; i++) s = "��" + s;
		} else if ("free".equals(flag) && sLength < diskSizeFreeTop_l) {
			for (int i=0 ; i<diskSizeFreeTop_l - sLength ; i++) s = "��" + s;
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
		DateFormat df = new SimpleDateFormat("   (d��HH��)");
		DateFormat df1 = new SimpleDateFormat("   �ģ����£��  M ��");
		DateFormat df2 = new SimpleDateFormat("M");
		if ("12".equals(df2.format(currentDate.getTime()))) {
			df = new SimpleDateFormat("  (M��d��HH��)");
			df1 = new SimpleDateFormat("   'DonzBox' yyyy �Ѱ��");
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
//System.out.println("���� : " + listFile.getName());
				}
			}
		}
		
		List<String> list = new ArrayList<String>();
		list.add("D00)  ����������������������������������������������");
		list.add("D01)");
		list.add("D02)");
		list.add("D20)  ������������������������������������[�� ������]");
//		list.add("D21)  ���ε��");
		list.add("D30)  ������������������������������������[�ٿ� ������]");
//		list.add("D31)  �ٿ��");
		list.add("D40)  ������������������������������������[�α� ������]");
//		list.add("D41)  �α��ڷ�");
		list.add("D50)  ����������������������������������������������");
		list.add("D51)  ����1 - [HDD 5.47�׶�] SCHUKI��");
		list.add("D52)  ����2 - [HDD 0.65�׶�] AFREET9��");
		list.add("D52)  ����3 - [HDD 1.00�׶�] KODAL3�� & KEYLIN��");
		list.add("D52)  ����4 - [HDD 2.00�׶�] BEDSHI��");
		list.add("D55)  ��� - HAI��, EHE��, Donz");
		list.add("D68)   �ߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣ�");
//		list.add("D69)");
		list.add("D70)  ����������������������������������������������");
		list.add("D80)  ����������������������������������������������");
		list.add("D81)  �ߣߣ�[ Box Size Checker ]�ߣߣߴ������Ⱑ�ߣ�");
		list.add(DRIVE_D + NOT_EXIST_HDD);
		list.add(DRIVE_E + NOT_EXIST_HDD);
		list.add(DRIVE_F + NOT_EXIST_HDD);
		list.add(DRIVE_G + NOT_EXIST_HDD);
		list.add(DRIVE_H + NOT_EXIST_HDD);
		list.add(DRIVE_I + NOT_EXIST_HDD);
		list.add(DRIVE_J + NOT_EXIST_HDD);
		list.add("D90)  ����������������������������������������������");
		list.add("D96)   �ߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣ�");
		list.add("D97)");
		list.add("D98)   ����������������������������������������������");
		File tmpFile;
		for (int i=0 ; i<list.size() ; i++) {
			tmpFile = new File(FTP_ROOT + list.get(i));
			tmpFile.mkdirs();
		}
		list.clear();
//		list.add("D20) ����������������������������������������������");
//		list.add("D51)  ����� FTP�� ���Ͽ� ������ ������ ������");
//		list.add("D52)  �Ǽ����ϵ��� ���ε� �ǰ� �ֽ��ϴ�.");
//		list.add("D53)  �̷� ���� ���� ������� ���ظ� ���� �� �ֽ��ϴ�.");
//		list.add("D54)  ������ ġ�� �� �Ʒ��� �ּҷ� �����ϼ���.");
//		list.add("D55)  ");
//		list.add("D56)            - ������ DonzBox.com@gmail.com -");
//		list.add("D60) �ߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣ�");
//		for (int i=0 ; i<list.size() ; i++) {
//			tmpFile = new File(FTP_ROOT_BLACKLIST + list.get(i));
//			tmpFile.mkdirs();
//		}
	}
	
	// �޼ҵ��� ����ð� ���ϱ�
	public void runningTime(String type) {
		if (START.equals(type)) {
			startTime = System.nanoTime();
		} else if (END.equals(type)) {
			endTime = System.nanoTime();
			long resultTime = endTime - startTime;
			System.out.println("\r\n### ����ð� " + getMillisecond2Time(resultTime));
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
//���Ų�new RankingOutput(FTP_ROOT);
		new ServULogRankingOutput(FTP_ROOT, args);
		
		dso.runningTime(END);
	}
}
