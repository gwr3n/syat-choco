package org.syat.statistics;

import umontreal.iro.lecuyer.probdist.DiscreteDistribution;
import umontreal.iro.lecuyer.probdist.DiscreteDistributionInt;
import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public class PearsonChiSquaredTest {
	
	EmpiricalDist emp;
	Distribution dist;
	double confidence;
	double binSize;
	int nbBins;
	ChiSquareDist chiSqDist;
	
	/*
	 * http://courses.wcupa.edu/rbove/Berenson/10th%20ed%20CD-ROM%20topics/section12_5.pdf
	 * http://www.math.nsysu.edu.tw/~lomn/homepage/class/92/kstest/kolmogorov.pdf
	 * http://www.itl.nist.gov/div898/handbook/eda/section3/eda35g.htm
	 * */
	public PearsonChiSquaredTest(EmpiricalDist emp, Distribution dist, double confidence, int estimatedParameters, double binSize) throws Exception{
		if(!(dist instanceof DiscreteDistribution) && !(dist instanceof DiscreteDistributionInt)) 
			throw new Exception("Distribution is not discrete");
		this.emp = emp;
		this.dist = dist;
		this.confidence = confidence;
		this.binSize = binSize;
		this.nbBins = (int)Math.round((emp.getXsup()-emp.getXinf()+binSize)/binSize);
		this.chiSqDist = new ChiSquareDist(this.nbBins-estimatedParameters-1);
	}
	
	public double getChiSqQuantile(){
		return this.chiSqDist.inverseF(this.confidence);
	}
	
	public boolean test(){
		return 1.0-confidence >= this.pValue() ? false : true;
	}
	
	public double chiSqStatistics(){		
		double chiSqStatistics = 0;
		for(double i = emp.getXinf(); i <= emp.getXsup(); i+=binSize){
			if(i==emp.getXinf()){
				double observed = (emp.cdf(i)-emp.cdf(i-binSize))*emp.getN();
				double predicted = (dist.cdf(i))*emp.getN();
				chiSqStatistics += Math.pow(observed-predicted,2)/predicted;
			}else if(i==emp.getXsup()){
				double observed = (emp.cdf(i)-emp.cdf(i-binSize))*emp.getN();
				double predicted = (1.0-dist.cdf(i-binSize))*emp.getN();
				chiSqStatistics += Math.pow(observed-predicted,2)/predicted;
			}else{
				double observed = (emp.cdf(i)-emp.cdf(i-binSize))*emp.getN();
				double predicted = (dist.cdf(i)-dist.cdf(i-binSize))*emp.getN();
				chiSqStatistics += Math.pow(observed-predicted,2)/predicted;
			}
		}
		return chiSqStatistics;
	}
	
	public double pValue(){
		double chiSqStatistics = this.chiSqStatistics();
		return 1.0-this.chiSqDist.cdf(chiSqStatistics);
	}
}
