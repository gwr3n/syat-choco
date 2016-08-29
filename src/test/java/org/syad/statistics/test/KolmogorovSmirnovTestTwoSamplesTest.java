package org.syad.statistics.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.syat.statistics.KolmogorovSmirnovTestTwoSamples;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.rng.MRG31k3p;

public class KolmogorovSmirnovTestTwoSamplesTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void frequencyE1GeqE2() throws Exception {
		MRG31k3p lfsr = new MRG31k3p();
		int[] seed = {5,2,3,4,5,6};
		lfsr.setSeed(seed);
		UniformGen rngUnif1 = new UniformGen(lfsr, 0, 1);
		UniformGen rngUnif2 = new UniformGen(lfsr, 0, 1);
		int samples = 500;
		int replications = 20000;
		double successFrequency = 0; 
		for(int i = 0; i < replications; i++){
			double[] randomSample1 = new double[samples];
			rngUnif1.nextArrayOfDouble(randomSample1, 0, samples);
			EmpiricalDist emp1 = new EmpiricalDist(randomSample1);
			double[] randomSample2 = new double[samples];
			rngUnif2.nextArrayOfDouble(randomSample2, 0, samples);
			EmpiricalDist emp2 = new EmpiricalDist(randomSample2);
			KolmogorovSmirnovTestTwoSamples kst = new KolmogorovSmirnovTestTwoSamples(emp1, emp2, 0.9);
			if(kst.testE1GeqE2()) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("K-S success frequency: "+(successFrequency/replications),0.90, (successFrequency/replications),0.01);
	}

	@Test
	public void frequencyE2GeqE1() throws Exception {
		MRG31k3p lfsr = new MRG31k3p();
		int[] seed = {5,2,3,4,5,6};
		lfsr.setSeed(seed);
		UniformGen rngUnif1 = new UniformGen(lfsr, 0, 1);
		UniformGen rngUnif2 = new UniformGen(lfsr, 0, 1);
		int samples = 500;
		int replications = 20000;
		double successFrequency = 0; 
		for(int i = 0; i < replications; i++){
			double[] randomSample1 = new double[samples];
			rngUnif1.nextArrayOfDouble(randomSample1, 0, samples);
			EmpiricalDist emp1 = new EmpiricalDist(randomSample1);
			double[] randomSample2 = new double[samples];
			rngUnif2.nextArrayOfDouble(randomSample2, 0, samples);
			EmpiricalDist emp2 = new EmpiricalDist(randomSample2);
			KolmogorovSmirnovTestTwoSamples kst = new KolmogorovSmirnovTestTwoSamples(emp1, emp2, 0.9);
			if(kst.testE2GeqE1()) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("K-S success frequency: "+(successFrequency/replications),0.90, (successFrequency/replications),0.01);
	}
	
	@Test
	public void frequencyE2NeqE1() throws Exception {
		MRG31k3p lfsr = new MRG31k3p();
		int[] seed = {5,2,3,4,5,6};
		lfsr.setSeed(seed);
		UniformGen rngUnif1 = new UniformGen(lfsr, 0, 1);
		UniformGen rngUnif2 = new UniformGen(lfsr, 0, 1);
		int samples = 500;
		int replications = 20000;
		double successFrequency = 0; 
		for(int i = 0; i < replications; i++){
			double[] randomSample1 = new double[samples];
			rngUnif1.nextArrayOfDouble(randomSample1, 0, samples);
			EmpiricalDist emp1 = new EmpiricalDist(randomSample1);
			double[] randomSample2 = new double[samples];
			rngUnif2.nextArrayOfDouble(randomSample2, 0, samples);
			EmpiricalDist emp2 = new EmpiricalDist(randomSample2);
			KolmogorovSmirnovTestTwoSamples kst = new KolmogorovSmirnovTestTwoSamples(emp1, emp2, 0.9);
			if(kst.testE2NeqE1()) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("K-S success frequency: "+(successFrequency/replications),0.90, (successFrequency/replications),0.01);
	}
}
