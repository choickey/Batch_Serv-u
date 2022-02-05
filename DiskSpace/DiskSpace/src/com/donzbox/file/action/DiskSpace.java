package com.donzbox.file.action;

import java.io.File;

public class DiskSpace {

	public String dllPath1, dllPath2;
	public DiskSpace() {

		dllPath1 = new File("").getAbsolutePath() + File.separator + "DiskSpace" + File.separator + "native" + File.separator + "DiskSpace.dll";
		dllPath2 = new File("").getAbsolutePath() + File.separator + "native" + File.separator + "DiskSpace.dll";
		final File file1 = new File(dllPath1);
		final File file2 = new File(dllPath2);

		if(file1.isFile()) {
			try {
				System.load(dllPath1);
				System.out.println("TYPE A) \"" + dllPath1 + "\" Library is loaded.");
			} catch (final Exception e) {
				System.out.println("TYPE A) \"" + dllPath1 + "\" Library loaded is fail.");
			}
		} else if(file2.isFile()) {
			try {
				System.load(dllPath2);
				System.out.println("TYPE B) \"" + dllPath2 + "\" Library is loaded.");
			} catch (final Exception e) {
				System.out.println("TYPE B) \"" + dllPath2 + "\" Library loaded is fail.");
			}
		} else {
			System.out.println("TYPE C) \"" + dllPath1 + "\" Library no exist.");
		}
	}

	public native String diskspace(String drv);
	public String getDiskSpace(final String drv) throws Exception {
		return diskspace(drv);
	}
}
