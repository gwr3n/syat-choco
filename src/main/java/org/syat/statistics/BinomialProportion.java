/*
 * syat-choco: a Choco extension for Declarative Statistics.
 * 
 * MIT License
 * 
 * Copyright (c) 2016 Roberto Rossi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.syat.statistics;

import umontreal.iro.lecuyer.probdist.BetaDist;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.NormalDist;

public class BinomialProportion {
	
	EmpiricalDist emp;
	double frequency;
	int N;
	
	public BinomialProportion(EmpiricalDist emp){
		this.emp = emp;
		this.frequency = emp.getMean();
		this.N = emp.getN();
	}
	
	public BinomialProportion(double frequency, int N){
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
}
