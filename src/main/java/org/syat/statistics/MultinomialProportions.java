package org.syat.statistics;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.randvarmulti.MultinomialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class MultinomialProportions {
	
	double[][] observations;
	double[] frequencies;
	int N;
	
	public MultinomialProportions(double[][] observations){
		this.observations = observations;
		N = observations.length;
		frequencies = new double[observations[0].length];
		for(int k = 0; k < observations.length; k++){
			for(int j = 0; j < observations[k].length; j++){
				if(observations[k][j] == 1) frequencies[j] += 1;
			}
		}
		
		for(int j = 0; j < observations[0].length; j++){
			frequencies[j] /= observations.length;
		}
	}
	
	public MultinomialProportions(double[] frequencies, int N){
		this.frequencies = frequencies;
		this.N = N;
	}
	
	public double[] getFrequencies(){
		return this.frequencies;
	}
	
	public double[][] getObservations(){
		return this.observations;
	}
	
	/* http://www.jstor.org/stable/1266673?seq=1 */
	public double[][] computeQuesenberryHurstCI(double confidence){
		double[][] intervals = new double[frequencies.length][2];
		ChiSquareDist chiSq = new ChiSquareDist(frequencies.length-1);
		double A = chiSq.inverseF(confidence);
		
		double[] n = new double[frequencies.length];
		
		for(int i = 0; i < frequencies.length; i++){
			n[i] = frequencies[i]*N;
		}
		
		for(int i = 0; i < frequencies.length; i++){
			intervals[i][0] = (A + 2*n[i] - Math.sqrt(A*(A+4*n[i]*(N-n[i])/N)))/(2*(N+A));
			intervals[i][1] = (A + 2*n[i] + Math.sqrt(A*(A+4*n[i]*(N-n[i])/N)))/(2*(N+A));
		}
		return intervals;
	}
	
	public double[][] computeGoodmanCI(double confidence){
		double[][] intervals = new double[frequencies.length][2];
		ChiSquareDist chiSq = new ChiSquareDist(1);
		double A = chiSq.inverseF(1-(1-confidence)/frequencies.length);
		
		double[] n = new double[frequencies.length];
		
		for(int i = 0; i < frequencies.length; i++){
			n[i] = frequencies[i]*N;
		}
		
		for(int i = 0; i < frequencies.length; i++){
			intervals[i][0] = (A + 2*n[i] - Math.sqrt(A*(A+4*n[i]*(N-n[i])/N)))/(2*(N+A));
			intervals[i][1] = (A + 2*n[i] + Math.sqrt(A*(A+4*n[i]*(N-n[i])/N)))/(2*(N+A));
		}
		return intervals;
	}
	
	public static void main(String args[]){
		double confidence = 0.90;
		double[] p = {0.3,0.3,0.4}; 
		int replications = 10000;
		int sampleSize = 10;
		
		double coverageProbability = 0;
		MRG32k3a rng = new MRG32k3a();
		UniformGen gen1 = new UniformGen(rng);
		MultinomialGen binomial = new MultinomialGen(gen1, p, 1);
		for(int i = 0; i < replications; i++){
			double[][] variates = new double[sampleSize][p.length];
			binomial.nextArrayOfPoints(variates, 0, sampleSize);
			
			MultinomialProportions mp = new MultinomialProportions(variates);
			
			double[][] intervals = mp.computeGoodmanCI(confidence);
			boolean covered = true;
			for(int j = 0; j < p.length; j++){
				if(intervals[j][0] >= p[j] || p[j] >= intervals[j][1]){
					covered = false;
				}
			}
			if(covered) coverageProbability++;
		}
		
		System.out.println("CP: "+coverageProbability/replications);
	}
}
