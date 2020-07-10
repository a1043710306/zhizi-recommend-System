package com.inveno.core.process.cms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MixedRule implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int rule_id = 0;
	
	List<List<String>> source = new ArrayList<List<String>>();
	
	List<Double>  ratio = new ArrayList<Double>();
	
	List<Integer>  range = new ArrayList<Integer>();

	public int getRule_id() {
		return rule_id;
	}
	
	public void setRule_id(int rule_id) {
		this.rule_id = rule_id;
	}

	public List<List<String>> getSource() {
		return source;
	}

	public void setSource(List<List<String>> source) {
		this.source = source;
	}

	public List<Double> getRatio() {
		return ratio;
	}
	
	public void setRatio(List<Double> ratio) {
		this.ratio = ratio;
	}

	public List<Integer> getRange() {
		return range;
	}

	public void setRange(List<Integer> range) {
		this.range = range;
	}
	
	@Override
	public String toString() {
 		return "range :" + this.getRange() +" ,ratio:"+ this.getRatio() + ",source:"+ this.source +",rule_id:"+this.getRule_id();
	}
	
}