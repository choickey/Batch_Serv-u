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

	private static final String [] H_LEVEL = {"��","��","��","��","��","��","��","��","��","��"};
	private static final String [] L_LEVEL = {"��","��","��","��","��","��","��","��","��","��"};

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
	private static final String D1 = "��";
	private static final String REPORT_KIND_UPK = "D21)  ���ε�� ";
	private static final String REPORT_KIND_UPG = "D21)  ���ε��� ";
	private static final String REPORT_KIND_DNK = "D31)  �ٿ�� ";
	private static final String REPORT_KIND_DNG = "D31)  �ٿ��� ";
	private static final String REPORT_KIND_PO = "D41)  �α��ڷ� ";
	private static final String REPORT_ILLEGAL = "D69)  �� �ҹ����� ";
	private static final String REPORT_KIND_UP_MORE = "\\D20)  ������������������������������������[�� ������]\\";
	private static final String REPORT_KIND_DN_MORE = "\\D30)  ������������������������������������[�ٿ� ������]\\";
	private static final String REPORT_KIND_PO_MORE = "\\D40)  ������������������������������������[�α� ������]\\";
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
			// ID�� �̸����� �̸� ��ȯ
			final Map<String, String> mapIDtoName = makeHashIDtoName();

			// ��ŷ ���� ������ ����
			final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMM");
			final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");
			final Calendar cal = Calendar.getInstance();
			final Date date = cal.getTime();
			final DateFormat df2 = new SimpleDateFormat("M");
			// ��ŷ �ʱ�ȭ
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

			System.out.println("\n### ��ŷ����");
			getRanking();

			final List<String> illegal = sla.illegalLog();
			System.out.println("\n### �ҹ� IP : �빮����");
			denyIP_outUser.add("\"IP\",\"Description\",\"Allow\"");
			getIllegalLog(illegal);

			System.out.println("\n### Serv-U�� importDenyIP ���");
			makeFileDenyIpList (DENYIP_IN_ROOT   , denyIP_inUser);
			makeFileDenyIpList (DENYIP_OUT_ROOT  , denyIP_outUser);
			makeFileDenyIpList (DENYIP_INOUT_ROOT, denyIP_inoutUser);
			makeFileDenyIpList (DENYIP_BLACK_ROOT, denyIP_krjpUser);
			mergeFileDenyIpList(DENYIP_IN_ROOT);
			mergeFileDenyIpList(DENYIP_OUT_ROOT);
			mergeFileDenyIpList(DENYIP_INOUT_ROOT);
			mergeFileDenyIpList(DENYIP_BLACK_ROOT);

			System.out.println("\n### �ҹ� IP : �빮 -> BLACKLIST ����");
			copyRoot2BlackList();

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> makeHashIDtoName() {
		return makeHashIDtoName(true);
	}

	// boolean true  : �̸� �߰��� Xǥ��
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
					//BufferedReader in = new BufferedReader(new FileReader(listFile.getPath())); // �ѱ۱��� �׷��� �Ʒ��� ���ڵ�(txt ����ó�� ansi �� ���ڵ��Ȱ�쿡��)
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
						// �̸��� ���� X ǥ�� ����
						strNM = b?strNM.substring(0, 1) + "��" + (strNM.length()==3?strNM.substring(2, 3):""):strNM;
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
					star=star+"��";
				}
				if ("0.00".equals(strUP)) {
					return name;
				}
				// full id ������
				id = id.substring(0, id.length()-1) + "��";
				if ("Best5".equals(flag)) {
					name = "(" + m2g(strDN, true) + ")  " + star + " " + rankNick + id;
				} else if ("All".equals(flag)) {
					name = "(" + m2g(strDN, true) + ")  " + rankNick + (i+1) + ".UP " + star + " " + id + " " + star + " " + "UP." + (i+1);
				}
				break;
				//			} else {
				//				name = (String) map.get(id);
				//				if (name==null) name = (id.substring(0, id.length()-1) + "��") + " (�����ʿ�)";
				//				float fltUP = Float.parseFloat(m2g(strUP, false));
				////				float fltDN = Float.parseFloat(m2g(strDN));
				//				// full id ������
				//				if (fltUP < 0.6f) {
				//					name = "(" + m2g(strDN, true) + ")  " + rankNick + name + ("0.00".equals(strUP)?"":", (���ε� " + m2g(strUP, true)+")") + "";
				//				} else {
				//					name = "(" + m2g(strDN, true) + ")  " + rankNick + id   + ("0.00".equals(strUP)?"":", (���ε� " + m2g(strUP, true)+")") + "";
				//				}
			}
		}
		// �ӿ� �ִ� ���� ������ ������ (uploader�� ���� ���� �ٿ�ε�ո� �ִ� ��� �뷮 ó���� ���� �ʱ� ������)
		name = map.get(id);
		if (name==null) {
			name = (id.substring(0, id.length()-1) + "��") + " (�����ʿ�)";
		}
		final float fltUP = Float.parseFloat(m2g(strUP, false));
		//			float fltDN = Float.parseFloat(m2g(strDN));
		// full id ������
		if (fltUP < 0.6f) {
			name = "(" + m2g(strDN, true) + ")  " + rankNick + name + ("0.00".equals(strUP)?"":", (���ε� " + m2g(strUP, true)+")") + "";
		} else {
			name = "(" + m2g(strDN, true) + ")  " + rankNick + id   + ("0.00".equals(strUP)?"":", (���ε� " + m2g(strUP, true)+")") + "";
		}
		return name;
	}
	public void getIllegalLog(final List<String> illegal, final String blacklist_root) {
		this.notice_root = blacklist_root;
		getIllegalLog(illegal);
	}
	public void getIllegalLog(final List<String> illegal) {

		// ������Ʈ D69)�� ���� ���丮 ����
		File file = new File(BLACKLIST_ROOT);
		File [] listFileArray = file.listFiles();
		File listFile;
		System.out.println("\t01. ������Ʈ �ҹ�IP ���丮 ����");
		for (int j=0 ; j<listFileArray.length ; j++) {
			listFile = listFileArray[j];
			if (listFile.getName().contains("D69)")) {
				// 01. �������丮 �����
				deleteSubDirectory(listFile.getAbsolutePath());
				// 02. �����븯�丮 �����
				listFile.delete();
			}
		}

		// ��ü D69) ���� ���丮 ����
		file = new File(notice_root);
		listFileArray = file.listFiles();
		System.out.println("\t02. ��ü �ҹ�IP ���丮 ����");
		for (int j=0 ; j<listFileArray.length ; j++) {
			listFile = listFileArray[j];
			if (listFile.getName().contains("D69)")) {
				// 01. �������丮 �����
				deleteSubDirectory(listFile.getAbsolutePath());
				// 02. �����븯�丮 �����
				listFile.delete();
			}
		}

		// D69)�� ���� ���丮 ����
		System.out.print("\t03. �ҹ�IP��� ����\n\t");
		final String iIllegaInfo = "\\" + REPORT_ILLEGAL + "IP��" + illegal.get(0) + "�ǿ� ���� ����Ʈ ���� ��\\";
		setDirectoryName(iIllegaInfo, REPORT_ILLEGAL);
		String txt="", country="", ip="", num="";
		for (int i=1 ; i<illegal.size() ; i++) {

			//			System.out.println(illegal.get(i));
			System.out.print(String.format("%5s", i));
			if (i%30 == 0) {
				System.out.print("\n\t");
			}

			txt = illegal.get(i);
			if (txt.contains("] ") && txt.contains("��") && txt.contains(D1)) {
				country = txt.substring(txt.indexOf(") [") +2, txt.indexOf(") [") +7);
				ip = txt.substring(txt.indexOf("] ") +1, txt.indexOf("��"));
				ip = ip.trim();
				num = txt.substring(txt.indexOf("��") +1, txt.indexOf("��") -1);
				num = num.trim();
				// illegal.get(i).split(D1)[0] : ���� �ҹ�ID
				// illegal.get(i).split(D1)[1] : FULL �ҹ�ID
				// illegal.get(i).split(D1)[2] : ����,���� ���ӽð�
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
		// �ٿ�δ� TOP ȭ��
		System.out.println("\t01. �ٿ�δ� ����");
		ct = getContext(DN);
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			fct = getFilterContextUPDN(ct[j]);
			if ("0.00".equals(fct[1])) {
				continue;
			}
			rankInfo = "\\" + getKingOrGod(REPORT_KIND_DNK, j) + (j+1) + "��.  " + id2name(fct[0], fct[1], fct[2], "Best5", getRankNick(fct[1]));
			// �ٿ�ε� 1���� �ٿ������ ���
			if (!theEndOfTheYearChk && j == 0) {
				rankInfo = "\\" + REPORT_KIND_DNK + (j+1) + "��.  " + id2name(fct[0], fct[1], fct[2], "Best5", "�ٿ�� ");
			}
			setDirectoryName(rankInfo, REPORT_KIND_DNK);
			if (j==UPDN_TOTAL_RANKING-1) {
				break;
			}
		}
		// ������
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			fct = getFilterContextUPDN(ct[j]);

			if ("0.00".equals(fct[1])) {
				continue;
			}
			rankInfo = REPORT_KIND_DN_MORE + getKingOrGod(REPORT_KIND_DNK, j).substring(6) + dfr.format((j+1)) + "��.  " + id2name(fct[0], fct[1], fct[2], "All", getRankNick(fct[1]));
			//  �ٿ�ε� 1���� �ٿ������ ���
			if (!theEndOfTheYearChk && j == 0) {
				rankInfo = REPORT_KIND_DN_MORE + REPORT_KIND_DNK.substring(6) + dfr.format((j+1)) + "��.  " + id2name(fct[0], fct[1], fct[2], "All", "�ٿ�� ");
			}
			setDirectoryName(rankInfo, REPORT_KIND_DNK);
		}

		// fct[0]:name, fct[1]:dn, fct[2]:up
		// ���δ� TOP ȭ��
		System.out.println("\t02. ���δ� ����");
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
				star=star+"��";
			}
			// full id ������
			id = fct[0]; id = id.substring(0, id.length()-1) + "��";
			rankInfo = "\\" + getKingOrGod(REPORT_KIND_UPK, j) + (j+1) + "��.  (" + m2g(fct[1], true) + ") " + star + " " + id;
			setDirectoryName(rankInfo, REPORT_KIND_UPK);
			if (j==UPDN_TOTAL_RANKING-1) {
				break;
			}
		}
		// ������
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			fct = getFilterContextUPDN(ct[j]);
			if ("0.00".equals(fct[1])) {
				continue;
			}
			String star = "";if (j<5) { for (int s=0 ; s<5-j ; s++) {
				star=star+"��";
			}}
			if (j==5) { star="��";}
			if (6<=j && j<=15) {
				star=H_LEVEL[j-6]+".";
			}
			if (16<=j && j<=25) {
				star=L_LEVEL[j-16]+".";
			}
			// full id ������
			id = fct[0]; id = id.substring(0, id.length()-1) + "��";
			rankInfo = REPORT_KIND_UP_MORE + getKingOrGod(REPORT_KIND_UPK, j).substring(6) + dfr.format((j+1)) + "��.  (" + m2g(fct[1], true) + ") " + star + " " + id;
			setDirectoryName(rankInfo, REPORT_KIND_UPK);
		}

		// �α�����
		System.out.println("\t03. �α����� ����");
		ct = getContext(PO);
		for (int j=0 ; j<ct.length ; j++) {
			if (ct[j] == null) {
				break;
			}
			if (!(ct[j].toLowerCase().contains("[y]") || ct[j].contains("adult"))) {
				rankInfo = "\\" + REPORT_KIND_PO + (j+1) + "��.  " + getFilterContextPO(ct[j]);
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
				rankInfo = REPORT_KIND_PO_MORE + REPORT_KIND_PO.substring(6) + dfr.format((j+1)) + "��.  " + getFilterContextPO(ct[j]);
				setDirectoryName(rankInfo, REPORT_KIND_PO);
			}
		}
	}

	// ���� �Ѱ�궧 1~3���� "��"�̶�� ��Ī �ο�, 1��~11�������� "��"�̶�� ��Ī �ο�
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
				result = "����� ";
			} else if (dnSize > 100.00F) {
				result = "����  ";
			} else if (dnSize > 90.00F) {
				result = "���� ";
			} else if (dnSize > 80.00F) {
				result = "���� ";
			} else if (dnSize > 70.00F) {
				result = "��� ";
			} else if (dnSize > 60.00F) {
				result = "�߷� ";
			} else if (dnSize > 50.00F) {
				result = "�ҷ� ";
			} else if (dnSize > 40.00F) {
				result = "���� ";
			} else if (dnSize > 35.00F) {
				result = "��� ";
			} else if (dnSize > 30.00F) {
				result = "�߻� ";
			} else if (dnSize > 25.00F) {
				result = "�ϻ� ";
			} else if (dnSize > 20.00F) {
				result = "���� ";
			} else if (dnSize > 15.00F) {
				result = "�� ";
			} else if (dnSize > 10.00F) {
				result = "�Ϻ� ";
			} else if (dnSize > 5.00F) {
				result = "�̺� ";
			} else if (dnSize > 1.00F) {
				result = "�ƺ� ";
			} else {
				result = "õ�� ";
			}
		} else {
			// �ų� 12�� ���� ���
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
				Files.createDirectory(newDir);		// ���丮 �������� ������ �尡�� �������Ƿ� trim ó������
				 */
			}
			if (REPORT_ILLEGAL.equals(kind)) {
				// ip ������ ������� whois �˻��Ͽ� ������ ���� �ľ�
				if (info.contains("ȸ������")) {
					makeWhoisFile(info);
				} else {
					// ���丮 ����
					info = notice_root + (info.contains(D1)?info.split(D1)[0]:info);
					makeDir(info.trim());
					/* java ver 1.7 over
					Path newDir = FileSystems.getDefault().getPath(info.trim());
					Files.createDirectory(newDir);		// ���丮 �������� ������ �尡�� �������Ƿ� trim ó������
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
						//						System.out.println("\t���� ���� : " + listFile[i].getAbsolutePath());
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
							|| fileName.contains("�ٿ�� ")
							|| fileName.contains("�ٿ��� ")
							|| fileName.contains("���ε�� ")
							|| fileName.contains("���ε��� ")
							|| fileName.contains("�α��ڷ� ")
							|| fileName.contains("��")
							) {
						//						System.out.println("\t���� ���� : " + listFile[i].getAbsolutePath());
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
		// info : ���丮 + D1 + ����Ƚ�� + D1 + ����IP + D1 + ����IDs + D1 + ���ӱⰣ + D1 + ȸ�����Կ���
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
				+ time.substring( 8, 10) + ":" + time.substring(10, 12) + ":" + time.substring(12, 14) + "��\r\n"
				+ time.substring(14, 18) + "/" + time.substring(18, 20) + "/" + time.substring(20, 22) + " "
				+ time.substring(22, 24) + ":" + time.substring(24, 26) + ":" + time.substring(26, 28);
		final String time2   = time.substring(18, 20) + "��" + time.substring(20, 22) + "�� "
				+ time.substring(22, 24) + "��" + time.substring(24, 26) + "��";
		final String path1 = path.substring(0, path.indexOf("��") +1);
		final String path2 = path.substring(path.indexOf("��") +1 , path.lastIndexOf("��"));
		final String path3 = path.substring(path.lastIndexOf("��"), path.length());
		String path5 = "";

		final String nationInfo = getNationByApnic(ip);
		countryEN = nationInfo.split(D1)[0];
		countryKO = nationInfo.split(D1)[1];
		address = nationInfo.split(D1)[2];
		context = nationInfo.split(D1)[3];

		// IDs�� �ߺ�����
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

		// ���� �ۼ�
		context = "�� ���� �ĺ� : " + countryKO + " - " + ip + "\r\n\r\n"
				+ "�� �ҹ� ���� �Ⱓ\r\n" + time1 + "\r\n\r\n"
				+ "�� �ҹ� ���ٿ� �̿��� ID ���\r\n1) LIST : " + ids + "\r\n1) DIST : " + ids2 + "\r\n\r\n"
				+ context;

		// APNIC �� KRNIC���� country ������ ������ ���丮 ���� �߰�, ������ ���� ���� ������ �������� ����
		if (countryKO.length() == 0) {
			countryKO = countryEN;
		}
		// IP�� �ش��ϴ� �������� ��� �� ���丮 �̸�����(�����ڵ� �� ����� �߰�)
		path5 = path1 + time2 + path2 + " - " + countryKO + path3;
		try {
			// ���丮 ����
			makeDir(path5.trim());
			/* java ver 1.7 over
			Path newDir = FileSystems.getDefault().getPath(path5.trim());
			Files.createDirectory(newDir);		// ���丮 �������� ������ �尡�� �������Ƿ� trim ó������
			 */
		} catch (final Exception e) {
			e.printStackTrace();
		}

		// APNIC ������ ���Ϸ� ����
		try {
			final BufferedWriter out = new BufferedWriter(new FileWriter(path5 + "\\" + ip + ".txt"));
			out.write(context);
			out.newLine();
			out.close();
		}  catch (final Exception e) {
			e.printStackTrace();
		}

		// denyIP ����Ʈ �ۼ��� ������ - ȸ���� ���Ե��� ���� ������ ����
		num = num.replaceAll(",","").trim();
		if ("N".equals(members)) {
			final int intNum = Integer.parseInt(num);
			// ���ѹα�,�Ϻ��� ���ӽ� DENY_BLACKLIST_LIMIT �� �̻� ���ٱ��� IP�� ����
			if (("KR".equals(countryEN) || "JP".equals(countryEN)) && intNum >= DENY_BLACKLIST_LIMIT) {
				denyIP_krjpUser.add("\"" + ip + "\",\"" + "[" + countryEN + "," + num.trim() + "] " + address + "\",\"0\"");
				// ���ѹα�,�Ϻ� �̿ܿ��� ������ ����
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
			context = context.replaceAll("%", "\r\n��");
			context = context.replaceAll("\n", "\r\n");
			context = context.substring(0, context.indexOf("Bold: ") -2);

			for (String row : context.split("\r\n")) {
				// denyIP�� �����ϱ� ���� ���� ȹ��
				if (row.replaceAll(" ",  "").toLowerCase().contains("address:")) {
					address = address + (row.split(":")[1]).trim() + ",";
				}
				if (row.replaceAll(" ",  "").toLowerCase().contains("city:")) {
					address = address + (row.split(":")[1]).trim() + ",";
				}
				// APNIC ���� IP�� �ش��ϴ� �������� ȹ��
				if (row.replaceAll(" ",  "").toLowerCase().contains("country:")) {
					row = row.replaceAll(" ",  "").toLowerCase();
					row = row.substring(row.indexOf("country:"), row.length());
					countryEN = (row.split(":")[1]).trim();
				}
			}
			if (address.length() > 0) {
				address = address.substring(0, address.length() -1);
			}

			// KRNIC���� �������� ȹ�� (APNIC�� ���� ���)
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
			System.out.println("\n����ȹ�� ���з� ��õ� " + reConnCnt + "ȸ : " + ip);
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

			/* ��ü�˻�
			Enumeration<Object> key = properties.keys();
			String s = "";
			while (key.hasMoreElements()) {
				s = properties.getProperty((String) key.nextElement());
				System.out.println(s);
			} */
			//  135\D69)  �� �ҹ����� IP��213,771�ǿ� ���� ����Ʈ ���� ��\0135��     123ȸ �� ȸ������(��)    210.13.73.29��auto,bin,db2inst1,mm����(��)    210.13.73.29��auto,bin,db2inst1,
			val = properties.getProperty(nation);
			val = val.trim();
			val = val.length()==0?val:val.split(":")[1];
		} catch (final IOException ioe) {
			System.out.println("�� ������Ƽ�� ���� ���� : " + nation);
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
	 * �ڹٷ� utf-8 ������ ������ �����ϴ� ���� �ѱ� ���� ������ �߻�
	 * ������ ���� ��
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * FileInputStream fis=new FileInputStream(filename);
	 * InputStreamReader isr=new InputStreamReader(fis,"UTF-8");
	 * BufferedReader br=new BufferedReader(isr);
	 *
	 * ���Ͽ� ������ �� ������
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * FileOutputStream fos = new FileOutputStream(savefilename);
	 * OutputStreamWriter osw=new OutputStreamWriter(fos,"UTF-8");
	 * BufferedWriter bw=new BufferedWriter(osw);
	 */
	public void makeFileDenyIpList(final String writePath, final List<String> list) {
		String str ="";
		final Iterator<String> i = list.iterator();	// �ݺ���(Iterator)�� ����Ʈ ���
		while (i.hasNext()) {					// �ݺ��ڿ� ������ �ִ� ���ȿ�
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
					//BufferedReader in = new BufferedReader(new FileReader(listFile.getPath())); // �ѱ۱��� �׷��� �Ʒ��� ���ڵ�(txt ����ó�� ansi �� ���ڵ��Ȱ�쿡��)
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
		// ID �ߺ�����
		final TreeSet<String> distinctVerifi = new TreeSet<>(denyIpList1);
		denyIpList1 = new ArrayList<>(distinctVerifi);
		// ���� Ƚ���� ���� ������ ����
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
		System.out.println("\t" + report_date.subSequence(0, 4) + "�� �ҹ� IP : " + str1 + " ��� : " + denyIpList1.size() + "��");
	}

	public void fileCopy(final File source, final File target) throws IOException {
		// ���丮�� ���
		if (source.isDirectory()) {
			// ����� Directory�� ������ ����ϴ�.
			if (!target.exists()) {
				target.mkdir();
			}
			final String[] subDir = source.list();
			for (int i = 0; i < subDir.length; i++) {
				fileCopy(new File(source, subDir[i]), new File(target, subDir[i]));
			}
		} else {
			// ������ ���
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
			System.out.println("\tBLACKLIST�� ī�ǵ��� �ʾҽ��ϴ�");
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
		// email ���� DNS �Ǵ� IP
		String server = "E-MAIL ���� �ּ�";
		Properties properties = new Properties();
		properties.put("mail.smtp.host", server);

		try {
			Session s = Session.getDefaultInstance(properties, null);
			Message message = new MimeMessage(s);

			String sender = "������ ��� �̸��� �ּ�";
			String subject = MimeUtility.encodeText("����", "UTF-8", "B");
			String content = "����";

			// �޴� ����� ������ ���� �������� �ۼ��϶�.
			// ��) XXX@naver.com, YYY@naver.com, ZZZ@naver.com
			String mailAddress = "�޴»�� �ּ�";
			ArrayList<String> receiver = new ArrayList<String>();
			StringTokenizer stMailAddress = new StringTokenizer(mailAddress, ",", false);

			while (stMailAddress.hasMoreTokens()) {
				receiver.add(stMailAddress.nextToken());
			}

			// ������ ��� �̸��̶� ���� �ּҰ� �ƴ� �ڽ��� �̸��� �ִ� �κ��̴�.
			// ���̹� ���� ��� "������ ��� �̸�"�� ���� ���� �ִ�.
			Address senderAddress = new InternetAddress(sender, MimeUtility.encodeText("�����»�� �̸�", "UTF-8", "B"));
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
