package org.syad.statistics.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.syat.statistics.BinomialProportion;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.randvar.BinomialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class BinomialProportionTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void testClopperPearson() {
      double confidence = 0.90;
      double p = 0.5;
      int replications = 10000;
      int sampleSize = 500;
      
      double coverageProbabilityCP = 0;
      MRG32k3a rng = new MRG32k3a();
      BinomialGen binomial = new BinomialGen(rng, 1, p);
      for(int i = 0; i < replications; i++){
         double[] variates = new double[sampleSize];
         binomial.nextArrayOfDouble(variates, 0, sampleSize);
         EmpiricalDist empDist = new EmpiricalDist(variates);
         BinomialProportion cp = new BinomialProportion(empDist);
         
         double[] intervalCP = cp.computeClopperPearsonCI(confidence);
         if(intervalCP[0] <= p && p <= intervalCP[1]){
            coverageProbabilityCP++;
         }
      }
      coverageProbabilityCP/=replications;
      assertEquals("ClopperPearsonCI: "+coverageProbabilityCP,0.9, coverageProbabilityCP,0.001);
   }

   @Test
   public void testAgrestiCoull() {
      double confidence = 0.90;
      double p = 0.5;
      int replications = 10000;
      int sampleSize = 500;
      
      double coverageProbabilityAC = 0;
      MRG32k3a rng = new MRG32k3a();
      BinomialGen binomial = new BinomialGen(rng, 1, p);
      for(int i = 0; i < replications; i++){
         double[] variates = new double[sampleSize];
         binomial.nextArrayOfDouble(variates, 0, sampleSize);
         EmpiricalDist empDist = new EmpiricalDist(variates);
         BinomialProportion cp = new BinomialProportion(empDist);
         
         double[] intervalAC = cp.computeAgrestiCoullCI(confidence);
         if(intervalAC[0] <= p && p <= intervalAC[1]){
            coverageProbabilityAC++;
         }
      }
      coverageProbabilityAC/=replications;
      assertEquals("AgrestiCoullCI: "+coverageProbabilityAC,0.9, coverageProbabilityAC,0.001);
   }
}
