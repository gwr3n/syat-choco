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
