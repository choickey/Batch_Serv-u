@echo off

cls
echo ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
set todayYear=%date:~0,4%
set todayMonth=%date:~0,4%%date:~5,2%
echo :: ������ ��� ���� :: %todayMonth%
d:
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_LOG"
echo :: ���� ������ �ϳ��� ���Ϸ� ���� ����
copy %todayMonth%*.txt "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayMonth%.log"
echo :: ���� ������ �ϳ��� ���Ϸ� ���� ���� :: %todayMonth%.log
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"

sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayMonth%.log" > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%dn.txt"
echo :: �ٿ�ε�� ����Ʈ ��� :: %todayMonth%dn.txt

sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayMonth%.log" -u > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%up.txt"
echo :: ���ε�� ����Ʈ ��� :: %todayMonth%up.txt

sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayMonth%.log" -D 100 > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%po.txt"
echo :: �α����� ����Ʈ ��� :: %todayMonth%po.txt

copy "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%po.txt" "E:\z2      [  MOVIE  ]\130.  [  ���ο�ȭ  ]\%todayMonth%po.txt"
echo :: �α����� �ߵ������� ī�� :: "E:\z2      [  MOVIE  ]\130.  [  ���ο�ȭ  ]\%todayMonth%po.txt"

rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%dn.txt"
rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%up.txt"
rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%po.txt"

set todayMonth=
echo :: ���� ����� :: todayMonth=

cd "D:\My Data\WWW\App Source\DiskSpace\classes"

echo :: DiskSpaceOutput ����
java com.donzbox.file.action.DiskSpaceOutput

rem cd D:\WWW\FTP ROOT
rem del .htaccess*
rem echo :: .htaccess ����� :: del D:\WWW\FTP ROOT\.htaccess*

cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"

echo "%date:~5,2%"

if %date:~5,2% == 12 (
   echo ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   echo ::
   echo :: todayYear�� ��� ����
   s_year.bat
)