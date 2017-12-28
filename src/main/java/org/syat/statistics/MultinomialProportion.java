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

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public class MultinomialProportion {
	
	double[][] observations;
	double[] frequencies;
	int N;
	
	public MultinomialProportion(double[][] observations){
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
	
	public MultinomialProportion(double[] frequencies, int N){
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
}
