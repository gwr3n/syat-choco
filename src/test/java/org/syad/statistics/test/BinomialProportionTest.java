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
