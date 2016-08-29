package org.syat.statistics;

import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.KolmogorovSmirnovDist;

public class KolmogorovSmirnovTest {
	EmpiricalDist emp;
	Distribution dist;
	double confidence;
	KolmogorovSmirnovDist ksDist;
	
	/*
	 * http://www.math.nsysu.edu.tw/~lomn/homepage/class/92/kstest/kolmogorov.pdf
	 * http://www.itl.nist.gov/div898/handbook/eda/section3/eda35g.htm
	 * 
	 * http://ocw.mit.edu/courses/mathematics/18-443-statistics-for-applications-fall-2006/lecture-notes/lecture14.pdf
	 * http://books.google.co.uk/books?id=bmwhcJqq01cC&dq=single+tailed+kolmogorov+smirnov&source=gbs_navlinks_s
	 * */
	public KolmogorovSmirnovTest(EmpiricalDist emp, Distribution dist, double confidence) throws NullPointerException{
		if(!(dist instanceof ContinuousDistribution)) 
			throw new NullPointerException("Theoretical distribution should not be discrete");
		this.emp = emp;
		this.dist = dist;
		this.confidence = confidence;
		this.ksDist = new KolmogorovSmirnovDist(emp.getN());
	}
	
	public double getKSQuantile(){
		return this.ksDist.inverseF(this.confidence);
	}
	
	public boolean testE1NeqD1(){
		return 1.0-confidence >= this.pValueE1NeqD1() ? false : true;
	}
	
	public boolean testE1GeqD1(){
		return (1.0-confidence)*2 >= this.pValueE1GeqD1() ? false : true;
	}
	
	public boolean testD1GeqE1(){
		return (1.0-confidence)*2 >= this.pValueD1GeqE1() ? false : true;
	}
	
	public double KSstatisticsTwoTailed(){
		double supDiscrepancy = 0;
		for(int i = 0; i < emp.getN(); i++){
			double observation = emp.getObs(i);
			double discrepancy = 
					Math.max(
					Math.abs(emp.cdf(observation)-dist.cdf(observation)),
					i == 0 ? Math.abs(0-dist.cdf(observation)) : Math.abs(emp.cdf(emp.getObs(i-1))-dist.cdf(observation))
					);
			if(discrepancy>supDiscrepancy){
				supDiscrepancy = discrepancy;
			}
		}
		return supDiscrepancy;
	}
	
	public double KSstatisticsSingleTailedE1GeqD1(){
		double supDiscrepancy = 0;
		for(int i = 0; i < emp.getN(); i++){
			double observation = emp.getObs(i);
			double discrepancy = 
					Math.max(
					(emp.cdf(observation)-dist.cdf(observation)),
					i == 0 ? (0-dist.cdf(observation)) : (emp.cdf(emp.getObs(i-1))-dist.cdf(observation))
					);
			if(discrepancy>supDiscrepancy){
				supDiscrepancy = discrepancy;
			}
		}
		return supDiscrepancy;
	}
	
	public double KSstatisticsSingleTailedD1GeqE1(){
		double supDiscrepancy = 0;
		for(int i = 0; i < emp.getN(); i++){
			double observation = emp.getObs(i);
			double discrepancy = 
					Math.max(
					(dist.cdf(observation)-emp.cdf(observation)),
					i == 0 ? (dist.cdf(observation)-0) : (dist.cdf(observation)-emp.cdf(emp.getObs(i-1)))
					);
			if(discrepancy>supDiscrepancy){
				supDiscrepancy = discrepancy;
			}
		}
		return supDiscrepancy;
	}
	
	public double pValueE1NeqD1(){
		double supDiscrepancy = this.KSstatisticsTwoTailed();
		return 1.0-this.ksDist.cdf(supDiscrepancy);
	}
	
	public double pValueE1GeqD1(){
		double supDiscrepancy = this.KSstatisticsSingleTailedE1GeqD1();
		return 1.0-this.ksDist.cdf(supDiscrepancy);
	}
	
	public double pValueD1GeqE1(){
		double supDiscrepancy = this.KSstatisticsSingleTailedD1GeqE1();
		return 1.0-this.ksDist.cdf(supDiscrepancy);
	}
}
