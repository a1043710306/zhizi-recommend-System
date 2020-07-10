package com.inveno.fallback.calc.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.inveno.fallback.calc.ScoreCalc;

@Component
public class ScoreCalcImpl implements ScoreCalc {

	//公式 s1*gmp+s2*dwelltime
	public Double caclScorce(Double s1,Double s2,Double dwelltime,Double gmp) {
		// TODO Auto-generated method stub
		BigDecimal bs1 = new BigDecimal(s1);
		BigDecimal bs2 = new BigDecimal(s2);
		BigDecimal bDwelltime = new BigDecimal(dwelltime);
		BigDecimal bGmp = new BigDecimal(gmp);
		
		BigDecimal s1GmpValue = bs1.multiply(bGmp);
		BigDecimal s2DwelltimeValue = bs2.multiply(bDwelltime);
		Double result = s1GmpValue.add(s2DwelltimeValue).setScale(6,BigDecimal.ROUND_HALF_UP).doubleValue();
		return result;
	}

}
