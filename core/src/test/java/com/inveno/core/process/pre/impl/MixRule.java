package com.inveno.core.process.pre.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class MixRule implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int rule_id = 0;
	
	List<List<Integer>> source = new ArrayList<List<Integer>>();
	
	List<Double>  ratio = new ArrayList<Double>();
	
	List<Integer>  range = new ArrayList<Integer>();

	public int getRule_id() {
		return rule_id;
	}
	
	public void setRule_id(int rule_id) {
		this.rule_id = rule_id;
	}

	public List<List<Integer>> getSource() {
		return source;
	}

	public void setSource(List<List<Integer>> source) {
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
	
}