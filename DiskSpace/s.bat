@echo off

cls
echo ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
set todayYear=%date:~0,4%
set todayMonth=%date:~0,4%%date:~5,2%
echo :: 오늘의 년월 셋팅 :: %todayMonth%
d:
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_LOG"
echo :: 여러 파일을 하나의 파일로 만듬 시작
copy %todayMonth%*.txt "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayMonth%.log"
echo :: 여러 파일을 하나의 파일로 만듬 종료 :: %todayMonth%.log
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"

sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayMonth%.log" > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%dn.txt"
echo :: 다운로드왕 리포트 출력 :: %todayMonth%dn.txt

sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayMonth%.log" -u > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%up.txt"
echo :: 업로드왕 리포트 출력 :: %todayMonth%up.txt

sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayMonth%.log" -D 100 > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%po.txt"
echo :: 인기파일 리포트 출력 :: %todayMonth%po.txt

copy "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%po.txt" "E:\z2      [  MOVIE  ]\130.  [  성인영화  ]\%todayMonth%po.txt"
echo :: 인기파일 야동폴더에 카피 :: "E:\z2      [  MOVIE  ]\130.  [  성인영화  ]\%todayMonth%po.txt"

rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%dn.txt"
rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%up.txt"
rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\%todayMonth%po.txt"

set todayMonth=
echo :: 변수 지우기 :: todayMonth=

cd "D:\My Data\WWW\App Source\DiskSpace\classes"

echo :: DiskSpaceOutput 실행
java com.donzbox.file.action.DiskSpaceOutput

rem cd D:\WWW\FTP ROOT
rem del .htaccess*
rem echo :: .htaccess 지우기 :: del D:\WWW\FTP ROOT\.htaccess*

cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"

echo "%date:~5,2%"

if %date:~5,2% == 12 (
   echo ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   echo ::
   echo :: todayYear년 결산 실행
   s_year.bat
)