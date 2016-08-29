package org.syad.statistics.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.syat.statistics.PearsonChiSquaredTest;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.PoissonDist;

public class PearsonChiSquaredTestTest {
	double[] randomSample;
	EmpiricalDist emp;
	PoissonDist poisson20;
	PearsonChiSquaredTest chiSqst;

	@Before
	public void setUp() throws Exception {
		double[] randomSample = {18, 22, 12, 24, 26, 17, 19, 29, 26, 22, 22, 16, 27, 14, 23, 16, 21, 
				17, 11, 19, 17, 16, 15, 21, 10, 27, 17, 15, 18, 23, 18, 15, 26, 22, 
				21, 20, 21, 19, 17, 18, 24, 31, 7, 14, 23, 13, 20, 23, 23, 19, 25, 
				23, 26, 19, 15, 18, 17, 24, 16, 15, 22, 16, 27, 21, 24, 16, 25, 21, 
				20, 27, 16, 16, 13, 24, 18, 26, 22, 18, 24, 30, 22, 21, 15, 19, 25, 
				22, 26, 15, 26, 14, 21, 15, 16, 25, 29, 17, 18, 19, 19, 17, 23, 19, 
				21, 29, 24, 17, 21, 25, 20, 14, 20, 21, 18, 23, 15, 18, 17, 28, 25, 
				25, 17, 20, 19, 26, 14, 15, 31, 16, 19, 24, 23, 19, 14, 19, 28, 24, 
				17, 23, 27, 18, 17, 13, 21, 13, 19, 24, 18, 20, 18, 23, 21, 21, 11, 
				14, 24, 16, 20, 22, 23, 21, 20, 18, 17, 23, 15, 22, 14, 17, 27, 20, 
				21, 19, 16, 18, 22, 16, 18, 17, 15, 16, 19, 21, 17, 18, 10, 18, 20, 
				18, 25, 25, 19, 27, 23, 21, 24, 27, 24, 12, 20, 17};
		this.randomSample = randomSample;
		this.emp = new EmpiricalDist(this.randomSample);
		this.poisson20 = new PoissonDist(20);
		this.chiSqst = new PearsonChiSquaredTest(this.emp, this.poisson20, 0.95, 0, 1);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void getChiSqQuantile(){
		//System.out.println("ChiSq 0.95-quantile: "+this.chiSqst.getChiSqQuantile());
		assertEquals("ChiSq 0.95-quantile: "+this.chiSqst.getChiSqQuantile(),36.415, this.chiSqst.getChiSqQuantile(),0.001);
	}

	@Test
	public void test() {
		assertEquals(true, this.chiSqst.test());
	}
	
	@Test
	public void chiSqStatistics(){
		//System.out.println("ChiSq statistics: "+this.chiSqst.chiSqStatistics());
		assertEquals("ChiSq statistics: "+this.chiSqst.chiSqStatistics(),14.922, this.chiSqst.chiSqStatistics(),0.001);
	}

	@Test
	public void pValue(){
		//System.out.println("ChiSq pValue: "+this.chiSqst.pValue());
	    assertEquals("ChiSq pValue: "+this.chiSqst.pValue(), 0.923, this.chiSqst.pValue(), 0.001);
	}
}
