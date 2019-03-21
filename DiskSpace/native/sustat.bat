@echo off

REM 작업스케쥴러 - DonzBox Title갱신속선 - 동작
REM
REM 프로그램 스크립트
REM "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat\HiddenStart_4.2\hstart64.exe"
REM
REM 인수추가
REM /NOCONSOLE "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat\sustat.bat"

cls
echo ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
set todayYear=%date:~0,4%
set todayMonth=%date:~0,4%%date:~5,2%
set yadongDir=I:\D95)  [Ｉ：]2      [  ADULT  ]
echo :: 1~11월용 오늘의 년월 셋팅 :: %todayMonth%
d:

IF NOT EXIST "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%" (
  ECHO ^|   디렉토리 생성 :: D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%
  MKDIR "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%"
)
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_LOG"
echo :: 여러 파일을 하나의 파일로 만듬 시작
copy %todayMonth%*.txt "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%\%todayMonth%.log"
echo :: 여러 파일을 하나의 파일로 만듬 :: %todayMonth%.log
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"

set todayMonth=
echo :: 변수 지우기 :: todayMonth=

if NOT %date:~5,2% == 12 (
	cd "D:\My Data\WWW\App Source\batch\bin"
	echo :: DiskSpaceOutput 실행
	cd "D:\My Data\WWW\App Source\batch\DiskSpace"
	"C:\Program Files\Java\jdk1.8.0_181\bin\java" -jar target\DiskSpace.jar
)

cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"

if %date:~5,2% == 12 (
   echo ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   echo ::
   echo :: %todayYear%년 결산 실행

   echo ":: 파일삭제 => D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%\%todayYear%.log"
   cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%"
   del "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%\%todayYear%.log"

   echo :: 여러 파일을 하나의 파일로 만듬 시작
   copy %todayYear%*.log "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%\%todayYear%.log"
   echo :: 여러 파일을 하나의 파일로 만듬 종료 :: %todayYear%.log
   
   set todayYear=
   echo :: 변수 지우기 :: todayYear=
   
   MKDIR "D:\My Data\WWW\App Source\batch\bin\DiskSpace"
   copy "D:\My Data\WWW\App Source\batch\DiskSpace\DiskSpace.dll" "D:\My Data\WWW\App Source\batch\bin\DiskSpace\DiskSpace.dll"
   cd "D:\My Data\WWW\App Source\batch\bin"
   echo :: DiskSpaceOutput 실행
   cd "D:\My Data\WWW\App Source\batch\DiskSpace"
   "C:\Program Files\Java\jdk1.8.0_181\bin\java" -Xms8192m -Xmx8192m -jar target\DiskSpace.jar
   
   cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"
      
REM "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat\s_year.bat"
)