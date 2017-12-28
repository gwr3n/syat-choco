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

package org.syad.statistics.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.syat.statistics.TTestTwoSamples;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.rng.MRG31k3p;

public class TTestTwoSamplesTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testE1GeqE2() throws Exception {
		double[] samplesX = {9, 3, 7, 8, 8, 5, 8, 5, 3, 6}; //Poisson[7]
		//double[] samplesX = {8, 14, 6, 12, 12, 9, 10, 9, 10, 5};  //Poisson[10]
	    double[] samplesY = {9, 10, 9, 6, 11, 8, 10, 11, 14, 11}; //Poisson[10]
		EmpiricalDist empX = new EmpiricalDist(samplesX);
		EmpiricalDist empY = new EmpiricalDist(samplesY);
		TTestTwoSamples tTest = new TTestTwoSamples(empX, empY, 0.95);
		assertFalse(tTest.testE1GeqE2(0.95));
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
			TTestTwoSamples tTest = new TTestTwoSamples(emp1, emp2, 0.9);
			if(tTest.testE1GeqE2(0.9)) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("t Test success frequency: "+(successFrequency/replications),0.90, (successFrequency/replications),0.01);
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
			TTestTwoSamples tTest = new TTestTwoSamples(emp1, emp2, 0.9);
			if(tTest.testE2GeqE1(0.9)) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("t Test success frequency: "+(successFrequency/replications),0.90, (successFrequency/replications),0.01);
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
			TTestTwoSamples tTest = new TTestTwoSamples(emp1, emp2, 0.9);
			if(tTest.testE2NeqE1()) successFrequency++;
		}
		//System.out.println((successFrequency/replications));
		assertEquals("t Test success frequency: "+(successFrequency/replications),0.90, (successFrequency/replications),0.01);
	}
}
