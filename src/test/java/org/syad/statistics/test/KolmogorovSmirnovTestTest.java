package org.syad.statistics.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.syat.statistics.KolmogorovSmirnovTest;

import umontreal.iro.lecuyer.gof.GofStat;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.UniformDist;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.rng.MRG31k3p;

public class KolmogorovSmirnovTestTest {
	double[] randomSample;
	EmpiricalDist emp;
	UniformDist unif01;
	KolmogorovSmirnovTest kst;
	
	@Before
	public void setUp() throws Exception {
		double[] randomSample = {0.621,0.503,0.203,0.477,0.710,0.581,0.329,0.480,0.554,0.382};
		this.randomSample = randomSample;
		this.emp = new EmpiricalDist(this.randomSample);
		this.unif01 = new UniformDist(0,1);
		this.kst = new KolmogorovSmirnovTest(this.emp, this.unif01, 0.95);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		assertEquals(true, kst.testE1NeqD1());
	}
	
	@Test
	public void getKSQuantile(){
		//System.out.println("K-S 0.95-quantile: "+this.kst.getKSQuantile());
		assertEquals("K-S 0.95-quantile: "+this.kst.getKSQuantile(),0.409, this.kst.getKSQuantile(),0.001);
	}
	
	@Test
	public void KSstatisticsTwoTailed() {
		//System.out.println("K-S statistics: "+kst.KSstatisticsTwoTailed());
		assertEquals("K-S statistics: "+kst.KSstatisticsTwoTailed(),0.290, kst.KSstatisticsTwoTailed(),0.001);
	}
	
	@Test
	public void pValue() {
		//System.out.println("K-S pValue: "+kst.pValue());
		assertTrue("K-S pValue: "+kst.pValueE1NeqD1(),kst.pValueE1NeqD1() > 0.2);
	}
	
	@Test
	public void sanityCheck(){
		double[] randomSample = new double[this.randomSample.length];
		System.arraycopy(this.randomSample, 0, randomSample, 0, this.randomSample.length);
		Arrays.sort(randomSample);
		double[] stats = GofStat.kolmogorovSmirnov(randomSample);
		assertEquals("K-S statistics: "+stats[2],0.290, stats[2],0.001);
	}
	
	@Test
	public void frequencyE1NeqD1() throws Exception {
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
			KolmogorovSmirnovTest kst = new KolmogorovSmirnovTest(emp, unif01, 0.95);
			if(kst.testE1NeqD1()) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("K-S success frequency: "+(successFrequency/replications),0.95, (successFrequency/replications),0.01);
	}
	
	@Test
	public void frequencyE1GeqD1() throws Exception {
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
			KolmogorovSmirnovTest kst = new KolmogorovSmirnovTest(emp, unif01, 0.95);
			if(kst.testE1GeqD1()) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("K-S success frequency: "+(successFrequency/replications),0.95, (successFrequency/replications),0.01);
	}
	
	@Test
	public void frequencyD1GeqE1() throws Exception {
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
			KolmogorovSmirnovTest kst = new KolmogorovSmirnovTest(emp, unif01, 0.95);
			if(kst.testD1GeqE1()) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("K-S success frequency: "+(successFrequency/replications),0.95, (successFrequency/replications),0.01);
	}
}
