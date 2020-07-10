package com.inveno.core.process.post.process;

public class Integer_highestOneBit {

	public static void main(String[] args) throws Exception {
		
		int i = Integer.parseInt("10011", 2);
		System.out.println(i);
		int highestOneBit = Integer.highestOneBit(i);
		System.out.println("highestOneBit : " + highestOneBit);
		
		System.out.println(Math.log(highestOneBit)/Math.log(2));
		
		System.out.println(Integer.toHexString(highestOneBit));
		
		
	}
	
}
