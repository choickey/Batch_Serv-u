package com.donzbox.file.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EmptyDiskSpace {

	private final static String ROOT_DIRECTORY = "C:\\tmpDonzBox\\";
	public static void main (String [] args) {
		
		List arrayList = new ArrayList();
		
		arrayList.add("Bou����������������������������������������");
		arrayList.add("Bov    1���� �ģ����£��     (�ǽð�2222)");
		arrayList.add("Bow�ߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣ�");
		arrayList.add("Box`1) ��249G  [���������]  20G��");
		arrayList.add("Box`2) ��232G  [��������]  4G��");
		arrayList.add("Box`3) ��279G  [���������]  39G��");
		arrayList.add("Box`4) ��931G  [������������]  104G��");
		arrayList.add("Box`5) ������������������������������������");
		arrayList.add("Box`8) �Ʒ��� ���� �ֿ��� ��ȥ���ϸ����� �մϴ�");
		arrayList.add("Box`9) ��� ���� ��ȥ���� ���� ���");
		arrayList.add("Box`9) �� ��� = IHD2�� + ����");
		arrayList.add("Box`9) �� �ð� = 2009��1��24�� ����� ���� 7��");
		arrayList.add("Box`9) �� ��� = �ſ����� ������");
		arrayList.add("Box`9_");
		arrayList.add("Boy������������������������������������[�� ������]");
		arrayList.add("note 1)  ���ε�� 1��.  RYU (48.7G)");
		arrayList.add("note 1)  ���ε�� 2��.  4BEAT (36.0G)");
		arrayList.add("note 1)  ���ε�� 3��.  HL5YKN (29.7G)");
		arrayList.add("note 1)  ���ε�� 4��.  REMONEID (20.9G)");
		arrayList.add("note 1)  ���ε�� 5��.  HONGSI7812 (10.6G)");
		arrayList.add("note 1����������������������������������[�ٿ� ������]");
		arrayList.add("note 2)  �ٿ�� 1��.  �ֺ��� (355.8G), (��4.5G)");
		arrayList.add("note 2)  �ٿ�� 2��.  �̺��� (181.3G), (��3.7G)");
		arrayList.add("note 2)  �ٿ�� 3��.  HL5YKN (176.2G)");
		arrayList.add("note 2)  �ٿ�� 4��.  ���ι� (131.3G), (���ε� ����)");
		arrayList.add("note 2)  �ٿ�� 5��.  ������ (101.6G), (���ε� ����)");
		arrayList.add("note 2����������������������������������[�α� ������]");
		arrayList.add("note 3)  �α��ڷ� 1��.  [�ϵ�] ü���� - �⹫��Ÿ���");
		arrayList.add("note 3)  �α��ڷ� 2��.  �йи��� ����");
		arrayList.add("note 3)  �α��ڷ� 3��.  [�ѵ�] ���亥 ���̷��� 2008");
		arrayList.add("note 3)  �α��ڷ� 4��.  [��ȭ] [bluray] starwars episode iii - revenge of the sith 2005");
		arrayList.add("note 3)  �α��ڷ� 5��.  [�ѵ�] �ٶ��� ȭ��");
		arrayList.add("note 3)  �α��ڷ� 6��.  [�ѵ�] �ɺ��� ���� - 2009 kbs");
		arrayList.add("note 3)  �α��ڷ� 7��.  [��ȭ] [bluray] ���ǵ巹�̼� - �� �⿬");
		arrayList.add("note 3)  �α��ڷ� 8��.  [�ѵ�] �����ǵ���");
		arrayList.add("note 3)  �α��ڷ� 9��.  [�ϵ�] �Ͱ��ϱ�");
		arrayList.add("note 3��������������������������������������");
		arrayList.add("note 4)  . 0) [������]�� ���µǾ����ϴ�. ��̷� �˾ƺ��� �ڽ��� ��!");
		arrayList.add("note 4)  . 1) [������]�� �Ǹ�Ȯ���� �ȵǴ� ���� ���̵�, �Ǹ�, �н�����, �׸���");
		arrayList.add("note 4)  . 2) �����ڿ��� ���踦 �ؽ�Ʈ ���Ϸ� �����ּ���");
		arrayList.add("note 4)  . 3) ���� ���̵� �������� �е� �Ȱ��� ��û�� �ּ���");
		arrayList.add("note 4)  . 5) ���� ��� �� ���̵��û�� ����� �� ��� ����");
		arrayList.add("note 4)  . 6) 2������ ���̵� �Ѱ��� �Ѱ��� ���Ӹ� ���˴ϴ�");
		arrayList.add("note 4)  . [Blu-Ray] ������ ���涧�� �̷��� �ϼ���");
		arrayList.add("note 4)  . ������ - 4beat , bohemian, freo78 , hiczeros , hongsi7812, luimars , remoneid , ryu");
		arrayList.add("note 4)  . �����ڴԵ��� ���尡ġ ���� ������ ������ �����ּ���");
		arrayList.add("nz������������������������������������������");
		arrayList.add("z      [  DonzBox 1  ]");
		arrayList.add("zz�ߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣߣ�");

		File tmpFile;
		for (int i=0 ; i<arrayList.size() ; i++) {
			tmpFile = new File(ROOT_DIRECTORY + arrayList.get(i));
			tmpFile.mkdirs();
		}
	}
}
