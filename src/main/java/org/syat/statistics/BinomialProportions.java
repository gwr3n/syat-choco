package org.syat.statistics;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.randvar.BinomialGen;
import umontreal.iro.lecuyer.probdist.BetaDist;
import umontreal.iro.lecuyer.probdist.BinomialDist;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.NormalDist;

public class BinomialProportions {
	
	EmpiricalDist emp;
	double frequency;
	int N;
	
	public BinomialProportions(EmpiricalDist emp){
		this.emp = emp;
		this.frequency = emp.getMean();
		this.N = emp.getN();
	}
	
	public BinomialProportions(double frequency, int N){
		this.frequency = frequency;
		this.N = N;
	}
	
	public double[] computeAgrestiCoullCI(double confidence){
		
		double alpha = Math.round(N*frequency) + 2;
		double beta =  N + 4;
		
		NormalDist norm = new NormalDist(0,1);
	  	
		double[] interval = new double[2];
	  	interval[0] = alpha/beta-norm.inverseF(1-(1-confidence)/2.0)*Math.sqrt((alpha/beta)*(1-alpha/beta)/beta); 	/*LB*/
	  	interval[1] = alpha/beta+norm.inverseF(1-(1-confidence)/2.0)*Math.sqrt((alpha/beta)*(1-alpha/beta)/beta); 	/*UB*/
	  	
		return interval;
	}
	
	public double[] computeClopperPearsonCI(double confidence){
		int alpha = (int) Math.round(N*frequency);
		int beta =  N - (int) Math.round(N*frequency);
		
		BetaDist ub = null;
		if(beta > 0)
			ub = new BetaDist(alpha+1, beta);
		
		BetaDist lb = null;
		if(alpha > 0)
	  		lb = new BetaDist(alpha, beta+1);
	  	
		double[] interval = new double[2];
	  	interval[0] = lb != null ? lb.inverseF((1-confidence)/2.0) : 0; 	/*LB*/
	  	interval[1] = ub != null ? ub.inverseF(1-(1-confidence)/2.0) : 1; 	/*UB*/
	  	
		return interval;
	}
	
	public static void main(String args[]){
		double confidence = 0.90;
		double p = 0.5;
		int replications = 10000;
		int sampleSize = 500;
		
		double coverageProbabilityCP = 0;
		double coverageProbabilityAC = 0;
		MRG32k3a rng = new MRG32k3a();
		BinomialGen binomial = new BinomialGen(rng, 1, p);
		for(int i = 0; i < replications; i++){
			double[] variates = new double[sampleSize];
			binomial.nextArrayOfDouble(variates, 0, sampleSize);
			EmpiricalDist empDist = new EmpiricalDist(variates);
			BinomialProportions cp = new BinomialProportions(empDist);
			
			double[] intervalCP = cp.computeClopperPearsonCI(confidence);
			if(intervalCP[0] <= p && p <= intervalCP[1]){
				coverageProbabilityCP++;
			}
			
			double[] intervalAC = cp.computeAgrestiCoullCI(confidence);
			if(intervalAC[0] <= p && p <= intervalAC[1]){
				coverageProbabilityAC++;
			}
		}
		System.out.println("CP: "+coverageProbabilityCP/replications);
		System.out.println("AC: "+coverageProbabilityAC/replications);
	}
}
