package org.syat.statistics;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.StudentDist;

/*
 * http://www.statsdirect.com/help/default.htm#parametric_methods/unpaired_t.htm
 */

public class TTestTwoSamples {
	EmpiricalDist emp1;
	EmpiricalDist emp2;
	double confidence;
	StudentDist tDist;
	
	public TTestTwoSamples(EmpiricalDist emp1, EmpiricalDist emp2, double confidence){
		this.emp1 = emp1;
		this.emp2 = emp2;
		this.confidence = confidence;
		this.tDist = new StudentDist(emp1.getN()+emp2.getN()-2);
	}
	
	public double getTQuantile(){
		return this.tDist.inverseF(this.confidence);
	}
	
	public boolean testE2NeqE1(){
		if(testE2GeqE1(1-(1-this.confidence)/2) && testE1GeqE2(1-(1-this.confidence)/2))
			return true;
		else
			return false;
	}
	
	public boolean testE2GeqE1(double confidence){
		double sampleMean1 = this.emp1.getSampleMean();
		double sampleMean2 = this.emp2.getSampleMean();
		double s1 = this.emp1.getSampleVariance()*this.emp1.getN();
		double s2 = this.emp2.getSampleVariance()*this.emp2.getN();
		double sSq = (s1+s2)/(this.emp1.getN()+this.emp2.getN()-2);
		double populations = (1.0/this.emp1.getN()+1.0/this.emp2.getN());
		if(sampleMean1-sampleMean2-this.tDist.inverseF(confidence)*Math.sqrt(sSq*populations)>=0)
			return false;
		else
			return true;
	}
	
	public boolean testE1GeqE2(double confidence){
		double sampleMean1 = this.emp1.getSampleMean();
		double sampleMean2 = this.emp2.getSampleMean();
		double s1 = this.emp1.getSampleVariance()*this.emp1.getN();
		double s2 = this.emp2.getSampleVariance()*this.emp2.getN();
		double sSq = (s1+s2)/(this.emp1.getN()+this.emp2.getN()-2);
		double populations = (1.0/this.emp1.getN()+1.0/this.emp2.getN());
		if(sampleMean1-sampleMean2+this.tDist.inverseF(confidence)*Math.sqrt(sSq*populations)<=0)
			return false;
		else
			return true;
	}
}
