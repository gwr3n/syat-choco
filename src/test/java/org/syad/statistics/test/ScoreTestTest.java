package org.syad.statistics.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.syat.statistics.ScoreTest;

import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvarmulti.MultinormalCholeskyGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class ScoreTestTest {
   
   static double[][] generateObservations(MRG32k3a rng, double[] mu, double[][] sigma, int nbObservations){
      NormalGen gen = new NormalGen(rng);
      MultinormalCholeskyGen dist = new MultinormalCholeskyGen(gen, mu, sigma);
      double[][] observations = new double[nbObservations][mu.length];
      dist.nextArrayOfPoints(observations, 0, nbObservations);
      return observations;
   }

   @Before
   public void setUp() throws Exception {
      
   }

   @After
   public void tearDown() throws Exception {
      Thread.sleep(1000);
      System.gc();
   }

   @Test
   public void testKnownSigma() {
      
      double[] mu = {1, 1, 1};
      double[][] sigma = new double[][]{
         { 1.0, 0.1, 0.2 },
         { 0.1, 1.0, 0.1 },
         { 0.2, 0.1, 1.0 }
      };
      
      int M = 50;
      
      MRG32k3a rng = new MRG32k3a();
      rng.setSeed(new long[]{1,2,3,4,5,6});
      
      double R = 1000;
      int counter = 0;
      for(int i = 0; i < R; i++){
      
         double[][] observations = generateObservations(rng, new double[]{1,1,1}, sigma, M);
      
         ScoreTest test = new ScoreTest(mu, sigma, observations);
      
         if(test.scoreTest(0.05))
            counter++;
      }
      
      assertTrue("Success frequency: "+(counter/R), counter/R >= 0.94);
   }

   //@Test
   public void testUnknownSigma() {
      
      double[] mu = {1, 1, 1};
      double[][] sigma = new double[][]{
         { 1.0, 0.1, 0.2 },
         { 0.1, 1.0, 0.1 },
         { 0.2, 0.1, 1.0 }
      };
      
      int M = 500;
      
      MRG32k3a rng = new MRG32k3a();
      rng.setSeed(new long[]{1,2,3,4,5,6});
      
      double R = 10000;
      int counter = 0;
      for(int i = 0; i < R; i++){
      
         double[][] observations = generateObservations(rng, new double[]{1,1,1}, sigma, M);
      
         ScoreTest test = new ScoreTest(mu, observations);
      
         if(test.scoreTest(0.05))
            counter++;
      }
      
      assertTrue("Success frequency: "+(counter/R), counter/R >= 0.94);
   }
}
