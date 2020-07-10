package com.inveno.core.process.pre.impl;

import java.util.ArrayList;
import java.util.List;

import com.inveno.thrift.ResponParam;

public class PreProcessImplTest extends PreProcessImpl{
	
	
	
	public static void main(String[] args) {
		
		PreProcessImpl pre = new PreProcessImpl();
		
		 List<ResponParam> result = new ArrayList<ResponParam>();
		 for (int i = 0; i <10; i++) {
			 ResponParam re = new ResponParam();
			 re.setInfoid("20160422");
			 re.setStrategy("8");
			 result.add(re);
		}
		 
 	}

}
