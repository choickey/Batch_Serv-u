@echo off

REM �۾������췯 - DonzBox Title���żӼ� - ����
REM
REM ���α׷� ��ũ��Ʈ
REM "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat\HiddenStart_4.2\hstart64.exe"
REM
REM �μ��߰�
REM /NOCONSOLE "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat\sustat.bat"

cls
echo ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
set todayYear=%date:~0,4%
set todayMonth=%date:~0,4%%date:~5,2%
set yadongDir=I:\D95)  [�ɣ�]2      [  ADULT  ]
echo :: 1~11���� ������ ��� ���� :: %todayMonth%
d:

IF NOT EXIST "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%" (
  ECHO ^|   ���丮 ���� :: D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%
  MKDIR "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%"
)
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_LOG"
echo :: ���� ������ �ϳ��� ���Ϸ� ���� ����
copy %todayMonth%*.txt "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%\%todayMonth%.log"
echo :: ���� ������ �ϳ��� ���Ϸ� ���� :: %todayMonth%.log
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"

set todayMonth=
echo :: ���� ����� :: todayMonth=

if NOT %date:~5,2% == 12 (
	cd "D:\My Data\WWW\App Source\batch\bin"
	echo :: DiskSpaceOutput ����
	cd "D:\My Data\WWW\App Source\batch\DiskSpace"
	"C:\Program Files\Java\jdk1.8.0_181\bin\java" -jar target\DiskSpace.jar
)

cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"

if %date:~5,2% == 12 (
   echo ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   echo ::
   echo :: %todayYear%�� ��� ����

   echo ":: ���ϻ��� => D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%\%todayYear%.log"
   cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%"
   del "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%\%todayYear%.log"

   echo :: ���� ������ �ϳ��� ���Ϸ� ���� ����
   copy %todayYear%*.log "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%\%todayYear%.log"
   echo :: ���� ������ �ϳ��� ���Ϸ� ���� ���� :: %todayYear%.log
   
   set todayYear=
   echo :: ���� ����� :: todayYear=
   
   MKDIR "D:\My Data\WWW\App Source\batch\bin\DiskSpace"
   copy "D:\My Data\WWW\App Source\batch\DiskSpace\DiskSpace.dll" "D:\My Data\WWW\App Source\batch\bin\DiskSpace\DiskSpace.dll"
   cd "D:\My Data\WWW\App Source\batch\bin"
   echo :: DiskSpaceOutput ����
   cd "D:\My Data\WWW\App Source\batch\DiskSpace"
   "C:\Program Files\Java\jdk1.8.0_181\bin\java" -Xms8192m -Xmx8192m -jar target\DiskSpace.jar
   
   cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"
      
REM "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat\s_year.bat"
)