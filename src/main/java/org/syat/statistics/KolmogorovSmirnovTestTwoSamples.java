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
import umontreal.iro.lecuyer.probdist.KolmogorovSmirnovDist;

public class KolmogorovSmirnovTestTwoSamples {
	EmpiricalDist emp1;
	EmpiricalDist emp2;
	double confidence;
	KolmogorovSmirnovDist ksDist;
	
	/*
	 * http://www.math.nsysu.edu.tw/~lomn/homepage/class/92/kstest/kolmogorov.pdf
	 * http://www.itl.nist.gov/div898/handbook/eda/section3/eda35g.htm
	 * http://ocw.mit.edu/courses/mathematics/18-443-statistics-for-applications-fall-2006/lecture-notes/lecture14.pdf
	 * */
	public KolmogorovSmirnovTestTwoSamples(EmpiricalDist emp1, EmpiricalDist emp2, double confidence){
		this.emp1 = emp1;
		this.emp2 = emp2;
		this.confidence = confidence;
		this.ksDist = new KolmogorovSmirnovDist((emp1.getN()*emp2.getN())/(emp1.getN()+emp2.getN()));
	}
	
	public double getKSQuantile(){
		return this.ksDist.inverseF(this.confidence);
	}
	
	public boolean testE2NeqE1(){
		return 1.0-confidence >= this.pValueE2NeqE1() ? false : true;
	}
	
	public boolean testE1GeqE2(){
		return (1.0-confidence)*2 >= this.pValueE1GeqE2() ? false : true;
	}
	
	public boolean testE2GeqE1(){
		return (1.0-confidence)*2 >= this.pValueE2GeqE1() ? false : true;
	}
	
	private boolean constantDistributions(){
		if(emp1.getXinf() == emp2.getXinf() && emp1.getXinf() == emp2.getXinf() && emp1.getXinf() == emp2.getXsup())
			return true;
		else
			return false;
	}
	
	public double KSstatisticsTwoTailed(){
		double supDiscrepancy = 0;
		if(constantDistributions()) return supDiscrepancy;
		for(int i = 0; i < emp1.getN(); i++){
			double observation = emp1.getObs(i);
			double discrepancy = 
			Math.max(
					Math.abs(emp1.cdf(observation)-emp2.cdf(observation)),
					i == 0 ? Math.abs(0-emp2.cdf(observation)) : Math.abs(emp1.cdf(emp1.getObs(i-1))-emp2.cdf(observation))
					);
			if(discrepancy>supDiscrepancy){
				supDiscrepancy = discrepancy;
			}
		}
		for(int i = 0; i < emp2.getN(); i++){
			double observation = emp2.getObs(i);
			double discrepancy = 
			Math.max(
					Math.abs(emp2.cdf(observation)-emp1.cdf(observation)),
					i == 0 ? Math.abs(0-emp1.cdf(observation)) : Math.abs(emp2.cdf(emp2.getObs(i-1))-emp1.cdf(observation))
					);
			if(discrepancy>supDiscrepancy){
				supDiscrepancy = discrepancy;
			}
		}
		return supDiscrepancy;
	}
	
	/*
	 * Emp 1 > Emp 2
	 */
	public double KSstatisticsSingleTailedE1GeqE2(){
		double supDiscrepancy = 0;
		if(constantDistributions()) return supDiscrepancy;
		for(int i = 0; i < emp1.getN(); i++){
			double observation = emp1.getObs(i);
			double discrepancy = 
			Math.max(
					(emp1.cdf(observation)-emp2.cdf(observation)),
					i == 0 ? (0-emp2.cdf(observation)) : (emp1.cdf(emp1.getObs(i-1))-emp2.cdf(observation))
					);
			if(discrepancy>supDiscrepancy){
				supDiscrepancy = discrepancy;
			}
		}
		for(int i = 0; i < emp2.getN(); i++){
			double observation = emp2.getObs(i);
			double discrepancy = 
			Math.max(
					(emp1.cdf(observation)-emp2.cdf(observation)),
					i == 0 ? (0-emp2.cdf(observation)) : (emp1.cdf(observation)-emp2.cdf(emp2.getObs(i-1)))
					);
			if(discrepancy>supDiscrepancy){
				supDiscrepancy = discrepancy;
			}
		}
		return supDiscrepancy;
	}
	
	/*
	 * Emp 2 > Emp 1
	 */
	public double KSstatisticsSingleTailedE2GeqE1(){
		double supDiscrepancy = 0;
		if(constantDistributions()) return supDiscrepancy;
		for(int i = 0; i < emp1.getN(); i++){
			double observation = emp1.getObs(i);
			double discrepancy = 
			Math.max(
					(emp2.cdf(observation)-emp1.cdf(observation)),
					i == 0 ? (emp2.cdf(observation)-0) : (emp2.cdf(observation)-emp1.cdf(emp1.getObs(i-1)))
					);
			if(discrepancy>supDiscrepancy){
				supDiscrepancy = discrepancy;
			}
		}
		for(int i = 0; i < emp2.getN(); i++){
			double observation = emp2.getObs(i);
			double discrepancy = 
			Math.max(
					(emp2.cdf(observation)-emp1.cdf(observation)),
					i == 0 ? (emp2.cdf(observation)-0) : (emp2.cdf(emp2.getObs(i-1))-emp1.cdf(observation))
					);
			if(discrepancy>supDiscrepancy){
				supDiscrepancy = discrepancy;
			}
		}
		return supDiscrepancy;
	}
	
	public double pValueE2NeqE1(){
		double supDiscrepancy = this.KSstatisticsTwoTailed();
		return 1.0-this.ksDist.cdf(supDiscrepancy);
	}
	
	public double pValueE1GeqE2(){
		double supDiscrepancy = this.KSstatisticsSingleTailedE1GeqE2();
		return 1.0-this.ksDist.cdf(supDiscrepancy);
	}
	
	public double pValueE2GeqE1(){
		double supDiscrepancy = this.KSstatisticsSingleTailedE2GeqE1();
		return 1.0-this.ksDist.cdf(supDiscrepancy);
	}
}
