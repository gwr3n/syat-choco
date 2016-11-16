package org.syat.statistics;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;

public class UniformDistUB {
	
	EmpiricalDist emp;
	double sup;
	int N;
	
	public UniformDistUB(EmpiricalDist emp){
		this.emp = emp;
		this.sup = emp.getXsup();
		this.N = emp.getN();
	}
	
	public UniformDistUB(double sup, int N){
		this.sup = sup;
		this.N = N;
	}
	
	public double[] computeUBCI(double confidence){
		double[] interval = new double[2];
		interval[0] = this.sup;
		interval[1] = this.sup/Math.pow(1-confidence,1.0/this.N);
		return interval;
	}
}
