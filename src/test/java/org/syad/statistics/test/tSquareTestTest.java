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
import org.syat.statistics.tSquareTest;

import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvarmulti.MultinormalCholeskyGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class tSquareTestTest {
   
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
      
         tSquareTest test = new tSquareTest(mu, sigma, observations);
      
         if(test.tSquareTestBoolean(0.05))
            counter++;
      }
      
      assertTrue("Success frequency: "+(counter/R), counter/R >= 0.94);
   }

   @Test
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
      
         tSquareTest test = new tSquareTest(mu, observations);
      
         if(test.tSquareTestBoolean(0.05))
            counter++;
      }
      
      assertTrue("Success frequency: "+(counter/R), counter/R >= 0.94);
   }
}
