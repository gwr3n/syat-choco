package org.syat.statistics;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.StudentDist;

public class TTest {
	EmpiricalDist emp;
	double confidence;
	StudentDist tDist;
	double targetMean;
	
	public TTest(EmpiricalDist emp, double targetMean, double confidence){
		this.emp = emp;
		this.confidence = confidence;
		this.tDist = new StudentDist(emp.getN()-1);
		this.targetMean = targetMean;
	}
	
	public double getTQuantile(){
		return this.tDist.inverseF(this.confidence);
	}
	
	public boolean testE1NeqTM(){
		if(testTMGeqE1(1-(1-this.confidence)/2) && testE1GeqTM(1-(1-this.confidence)/2))
			return true;
		else
			return false;
	}
	
	public boolean testTMGeqE1(double confidence){
		double sampleMean = this.emp.getSampleMean();
		double sampleStandardDeviation = this.emp.getSampleStandardDeviation();
		if(sampleMean-this.tDist.inverseF(confidence)*sampleStandardDeviation/Math.sqrt(this.emp.getN())>=this.targetMean)
			return false;
		else
			return true;
	}
	
	/*public double getConfidenceIntervalLB(double confidence){
		double sampleMean = this.emp.getSampleMean();
		double sampleStandardDeviation = this.emp.getSampleStandardDeviation();
		return sampleMean-this.tDist.inverseF(confidence)*sampleStandardDeviation/Math.sqrt(this.emp.getN());
	}*/
	
	public boolean testE1GeqTM(double confidence){
		double sampleMean = this.emp.getSampleMean();
		double sampleStandardDeviation = this.emp.getSampleStandardDeviation();
		if(sampleMean+this.tDist.inverseF(confidence)*sampleStandardDeviation/Math.sqrt(this.emp.getN())<=this.targetMean)
			return false;
		else
			return true;
	}
	
	/*public double getConfidenceIntervalUB(double confidence){
		double sampleMean = this.emp.getSampleMean();
		double sampleStandardDeviation = this.emp.getSampleStandardDeviation();
		return sampleMean+this.tDist.inverseF(confidence)*sampleStandardDeviation/Math.sqrt(this.emp.getN());
	}*/
}
