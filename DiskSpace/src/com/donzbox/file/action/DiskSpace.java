package com.donzbox.file.action;

import java.io.File;

public class DiskSpace {
	public ClassLoader cl;
	public String dllPath;
	public DiskSpace() {
//		cl = Thread.currentThread().getContextClassLoader();
//		if (cl == null) {
//			cl = ClassLoader.getSystemClassLoader();
//		}
//		dllPath = cl.getResource("").getPath().toString();

		dllPath = new File("").getAbsolutePath() + File.separator + "DiskSpace.dll";
		
		System.out.println(dllPath);
		File file = new File(dllPath);
		if (file.isFile()) {
			try {
				System.load(dllPath);
				System.out.println("TYPE A) \"" + dllPath + "\" Library is loaded.");
			} catch (Exception e) {
				System.out.println("TYPE B) \"" + dllPath + "\" Library loaded is fail.");
			}
		} else {
			System.out.println("TYPE C) \"" + dllPath + "\" Library no exist.");
		}
	}
	public native String diskspace(String drv);
	public String getDiskSpace(String drv) throws Exception {
		return diskspace(drv);
	}
}
