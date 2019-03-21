@echo off

echo ::
set todayYear=%date:~0,4%
echo :: 오늘의 년월 셋팅 :: %todayYear%
d:
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log"
echo :: 여러 파일을 하나의 파일로 만듬 시작
del "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%.log"
copy %todayYear%*.log "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%.log"
echo :: 여러 파일을 하나의 파일로 만듬 종료 :: %todayYear%.log
cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"
echo :: 
echo :: 다운로드왕 리포트 출력 :: %todayYear%dn.txt
del "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%dn.txt"
sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%.log" > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%dn.txt"
echo :: 다운로드왕 리포트 출력 완료
echo :: 
echo :: 업로드왕 리포트 출력 :: %todayYear%up.txt
del "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%up.txt"
sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%.log" -u > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%up.txt"
echo :: 업로드왕 리포트 출력 완료
echo :: 
echo :: 인기파일 리포트 출력 :: %todayYear%po.txt
del "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%po.txt"
sustat "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\copy_log\%todayYear%.log" -D 50 > "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%po.txt"
echo :: 인기파일 리포트 출력 완료
echo :: 

copy "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%po.txt" "E:\z2      [  MOVIE  ]\130.  [  성인영화  ]\%todayYear%po.txt"
echo :: 인기파일 야동폴더에 카피 :: "E:\z2      [  MOVIE  ]\130.  [  성인영화  ]\%todayYear%po.txt"

rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%dn.txt"
rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%up.txt"
rem type "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\log_result\each_year\%todayYear%po.txt"

set todayYear=
echo :: 변수 지우기 :: todayYear=

cd "D:\My Data\WWW\App Source\DiskSpace\classes"

echo :: DiskSpaceOutput 실행
java com.donzbox.file.action.DiskSpaceOutput

cd "D:\My Data\WWW\Ftp Factory\ftp.DonzBox.com_Result\sustat"