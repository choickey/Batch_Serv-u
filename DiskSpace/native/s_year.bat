@echo off

echo ::
set todayYear=%date:~0,4%
echo :: ������ ��� ���� :: %todayYear%
d:
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log"
echo :: ���� ������ �ϳ��� ���Ϸ� ���� ����
del "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%.log"
copy %todayYear%*.log "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%.log"
echo :: ���� ������ �ϳ��� ���Ϸ� ���� ���� :: %todayYear%.log
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"
echo :: 
echo :: �ٿ�ε�� ����Ʈ ��� :: %todayYear%dn.txt
del "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%dn.txt"
sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%.log" > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%dn.txt"
echo :: �ٿ�ε�� ����Ʈ ��� �Ϸ�
echo :: 
echo :: ���ε�� ����Ʈ ��� :: %todayYear%up.txt
del "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%up.txt"
sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%.log" -u > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%up.txt"
echo :: ���ε�� ����Ʈ ��� �Ϸ�
echo :: 
echo :: �α����� ����Ʈ ��� :: %todayYear%po.txt
del "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%po.txt"
sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%.log" -D 50 > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%po.txt"
echo :: �α����� ����Ʈ ��� �Ϸ�
echo :: 

copy "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%po.txt" "E:\z2      [  MOVIE  ]\130.  [  ���ο�ȭ  ]\%todayYear%po.txt"
echo :: �α����� �ߵ������� ī�� :: "E:\z2      [  MOVIE  ]\130.  [  ���ο�ȭ  ]\%todayYear%po.txt"

rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%dn.txt"
rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%up.txt"
rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%po.txt"

set todayYear=
echo :: ���� ����� :: todayYear=

cd "D:\My Data\WWW\App Source\DiskSpace\classes"

echo :: DiskSpaceOutput ����
java com.donzbox.file.action.DiskSpaceOutput

cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"