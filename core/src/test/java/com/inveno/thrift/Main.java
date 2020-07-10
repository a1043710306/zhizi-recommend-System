package com.inveno.thrift;


import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) throws Exception {

		System.out.println("1");

		List<Object> caches = new ArrayList<Object>();
		for (int i = 0; i < 7; i++) {
			caches.add(new byte[1024 * 1024 * 3]);
			Thread.sleep(1000);
		}
		caches.clear();
		for (int i = 0; i < 2; i++) {
			caches.add(new byte[1024 * 1024 * 3]);
			Thread.sleep(1000);
		}

		Thread.sleep(1111);
	}
}
