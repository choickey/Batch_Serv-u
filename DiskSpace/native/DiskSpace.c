#include <jni.h>
#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include "DiskSpace.h"

typedef BOOL (WINAPI *PGETDISKFREESPACEEX)(LPCSTR, PULARGE_INTEGER, PULARGE_INTEGER, PULARGE_INTEGER);

JNIEXPORT jstring JNICALL Java_com_donzbox_file_action_DiskSpace_diskspace(JNIEnv* env, jobject obj, jstring msg) {

	const char *str = (*env)->GetStringUTFChars(env, msg, 0);
	double temp;
	char s1[33 + 1];
	char s2[33 + 1];

	LPCSTR pszDrive = str;
	PGETDISKFREESPACEEX pGetDiskFreeSpaceEx;
	__int64 i64FreeBytesToCaller, i64TotalBytes, i64FreeBytes, i64ReturnResult;
	DWORD dwSectPerClust, dwBytesPerSect, dwFreeClusters, dwTotalClusters;
	BOOL fResult;
	pGetDiskFreeSpaceEx = (PGETDISKFREESPACEEX) GetProcAddress(GetModuleHandle("kernel32.dll"),	"GetDiskFreeSpaceExA");

	if (pGetDiskFreeSpaceEx) {
		fResult = pGetDiskFreeSpaceEx (pszDrive, (PULARGE_INTEGER)&i64FreeBytesToCaller, (PULARGE_INTEGER)&i64TotalBytes, (PULARGE_INTEGER)&i64FreeBytes);
		if(fResult) {
//			printf("Total free bytes = %I64d\n", i64FreeBytes);
			i64ReturnResult = i64FreeBytes;
		}
	} else {
		fResult = GetDiskFreeSpaceA (pszDrive, &dwSectPerClust, &dwBytesPerSect, &dwFreeClusters, &dwTotalClusters);
		if(fResult) {
//			printf("Total free bytes = %I64d\n", dwFreeClusters*dwSectPerClust*dwBytesPerSect);
			i64ReturnResult = dwFreeClusters*dwSectPerClust*dwBytesPerSect;
		}
	}
	temp = i64TotalBytes / 1024 / 1024 / 1024;
	ltoa(temp, s1, 10);
	temp = i64ReturnResult / 1024 / 1024 / 1024;
	ltoa(temp, s2, 10);

	(*env)->ReleaseStringUTFChars(env, msg, str);
	return (*env)->NewStringUTF(env, strcat(strcat(s1,"|"),s2));
}