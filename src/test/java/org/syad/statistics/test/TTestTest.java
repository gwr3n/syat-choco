package org.syad.statistics.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.syat.statistics.TTest;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.UniformDist;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.rng.MRG31k3p;

public class TTestTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void frequencytestE1NeqTM() throws Exception {
		MRG31k3p lfsr = new MRG31k3p();
		int[] seed = {1,2,3,4,5,6};
		lfsr.setSeed(seed);
		UniformGen rngUnif = new UniformGen(lfsr, 0, 1);
		int samples = 50;
		int replications = 10000;
		double successFrequency = 0; 
		for(int i = 0; i < replications; i++){
			double[] randomSample = new double[samples];
			rngUnif.nextArrayOfDouble(randomSample, 0, samples);
			EmpiricalDist emp = new EmpiricalDist(randomSample);
			UniformDist unif01 = new UniformDist(0,1);
			TTest tTest = new TTest(emp, unif01.getMean(), 0.95);
			if(tTest.testE1NeqTM()) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("K-S success frequency: "+(successFrequency/replications),0.95, (successFrequency/replications),0.01);
	}
	
	@Test
	public void frequencyTMGeqE1() throws Exception {
		MRG31k3p lfsr = new MRG31k3p();
		int[] seed = {1,2,3,4,5,6};
		lfsr.setSeed(seed);
		UniformGen rngUnif = new UniformGen(lfsr, 0, 1);
		int samples = 50;
		int replications = 10000;
		double successFrequency = 0; 
		for(int i = 0; i < replications; i++){
			double[] randomSample = new double[samples];
			rngUnif.nextArrayOfDouble(randomSample, 0, samples);
			EmpiricalDist emp = new EmpiricalDist(randomSample);
			UniformDist unif01 = new UniformDist(0,1);
			TTest tTest = new TTest(emp, unif01.getMean(), 0.95);
			if(tTest.testTMGeqE1(0.95)) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("K-S success frequency: "+(successFrequency/replications),0.95, (successFrequency/replications),0.01);
	}
	
	@Test
	public void frequencyE1GeqTM() throws Exception {
		MRG31k3p lfsr = new MRG31k3p();
		int[] seed = {1,2,3,4,5,6};
		lfsr.setSeed(seed);
		UniformGen rngUnif = new UniformGen(lfsr, 0, 1);
		int samples = 50;
		int replications = 10000;
		double successFrequency = 0; 
		for(int i = 0; i < replications; i++){
			double[] randomSample = new double[samples];
			rngUnif.nextArrayOfDouble(randomSample, 0, samples);
			EmpiricalDist emp = new EmpiricalDist(randomSample);
			UniformDist unif01 = new UniformDist(0,1);
			TTest tTest = new TTest(emp, unif01.getMean(), 0.95);
			if(tTest.testE1GeqTM(0.95)) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("K-S success frequency: "+(successFrequency/replications),0.95, (successFrequency/replications),0.01);
	}

}
