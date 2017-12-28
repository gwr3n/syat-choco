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
