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
import org.syat.statistics.UniformDistUB;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class UniformDistUBTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCoverageProbability() {
		double confidence = 0.95;
		double UB = 100;
		int replications = 1000;
		int sampleSize = 10;
		
		double coverageProbability = 0;
		MRG32k3a rng = new MRG32k3a();
		UniformGen uniform = new UniformGen(rng, 0, UB);
		for(int i = 0; i < replications; i++){
			double[] variates = new double[sampleSize];
			uniform.nextArrayOfDouble(variates, 0, sampleSize);
			EmpiricalDist empDist = new EmpiricalDist(variates);
			UniformDistUB cp = new UniformDistUB(empDist);
			
			double[] interval = cp.computeUBCI(confidence);
			//System.out.println(interval[0]+"\t"+interval[1]);
			if(interval[0] <= UB && UB <= interval[1]){
				coverageProbability++;
			}
		}
		//System.out.println("Frequency: "+coverageProbability/replications);
		assertEquals("Frequency: "+(coverageProbability/replications),0.95, (coverageProbability/replications),0.01);
	}

}
