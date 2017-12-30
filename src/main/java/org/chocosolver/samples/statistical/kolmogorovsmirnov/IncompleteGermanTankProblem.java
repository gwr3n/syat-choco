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

package org.chocosolver.samples.statistical.kolmogorovsmirnov;

import java.util.Arrays;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.SyatConstraintFactory;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.distributions.UniformDistVar;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Incomplete German Tank Problem.
 * 
 * @author Roberto Rossi
 * @see <a href="http://dx.doi.org/10.3233/978-1-61499-419-0-777">Statistical Constraints</a>; Rossi et al., 2014.
 */
public class IncompleteGermanTankProblem extends AbstractProblem {

    // input data
    static int[] dataX = {2, 6, 6, 17, 4, 11, 10, 7, 2, 15}; //Uniform[0,20]

    // variables
    public IntVar[] populationX;
    public IntVar uniformUB;

    public void setUp() {
        // read data
    }

    @Override
    public void createSolver() {
        solver = new Solver("VarDistributionTest");
    }

    @Override
    public void buildModel() {
        setUp();
        int populationXSize = dataX.length;
        populationX = new IntVar[populationXSize];
        for(int i = 0; i < populationXSize; i++){
        	if(i == 5)
        		//populationX[i] = VariableFactory.bounded("sample "+i, dataX[i]-dataX[i]%10, dataX[i]+(9-dataX[i]%10), solver);
        		populationX[i] = VariableFactory.bounded("sample "+i, dataX[i], dataX[i], solver);
        	else
        		populationX[i] = VariableFactory.bounded("sample "+i, dataX[i], dataX[i], solver);
        }

        uniformUB = VariableFactory.bounded("UB", 1, 150, solver);
        
        solver.post(SyatConstraintFactory.kolmogorov_smirnov(populationX, new UniformDistVar(uniformUB), "=", 0.9));
    }
    
    @SuppressWarnings("unused")
    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
    	//return ArrayUtils.append(var1,var2);
    	IntVar[] var3 = new IntVar[var1.length+var2.length];
    	System.arraycopy(var1, 0, var3, 0, var1.length);
    	System.arraycopy(var2, 0, var3, var1.length, var2.length);
    	return var3;
    }

    @Override
    public void configureSearch() {
        AbstractStrategy<IntVar> strat = IntStrategyFactory.minDom_LB(populationX);
        // trick : top-down maximization
        solver.set(strat);
    }

    @Override
    public void solve() {
    	try {
         solver.propagate();
      } catch (ContradictionException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
    	
    	for(int i = 0; i < populationX.length; i++){
         //st.append(populationX[i].getValue()+", ");
         populationXUB[i] = populationX[i].getUB();
         populationXLB[i] = populationX[i].getLB();
      }
      upperBound = uniformUB.getUB();
      lowerBound = uniformUB.getLB();
    }

    @Override
    public void prettyOut() {
        
    }
    
    /*
Last figure missing																			
11	12	13	14	15	16	17	18	19	20	21	22	23							
11	12	13	14	15	16	17	18	19	20	21	22	23	24	25					
11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26				
11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26				
11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26				
11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26				
11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26				
11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26				
11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26	27	28		
11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26	27	28	29	30
																			
																			
All data known																			
11	12	13	14	15	16	17	18	19	20	21	22	23	24	25										
    */
    
    public static double upperBound = Double.MIN_VALUE;
    public static double lowerBound = Double.MAX_VALUE;
    
    public static int populationXUB[];
    public static int populationXLB[];

    /*public static void main(String[] args) {
    	String[] str={"-log","SILENT"};
    	int replications = 500;
    	int sampleSize = 10;
    	int UB = 20;
    	
    	double coverageProbability = 0;
    	MRG32k3a rng = new MRG32k3a();
    	//rng.setSeed(new long[]{System.currentTimeMillis() % 10000, System.currentTimeMillis() % 10000, System.currentTimeMillis() % 10000, System.currentTimeMillis() % 10000, System.currentTimeMillis() % 10000, System.currentTimeMillis() % 10000});
		UniformIntGen uniform = new UniformIntGen(rng, 0, UB);
		for(int i = 0; i < replications; i++){
			upperBound = Double.MIN_VALUE;
			lowerBound = Double.MAX_VALUE;
			populationXUB = new int[dataX.length];
		    populationXLB = new int[dataX.length];
			Arrays.fill(populationXUB, 0);
			Arrays.fill(populationXLB, Integer.MAX_VALUE);
			int[] variates = new int[sampleSize];
			uniform.nextArrayOfInt(variates, 0, sampleSize);
			dataX = variates;
			new IncompleteGermanTankProblem().execute(str);
			if(UB >= lowerBound && UB <= upperBound) 
				coverageProbability++;
		}
		System.out.println("Frequency: "+coverageProbability/replications);
    }*/
    
    public static void main(String[] args) {
    	upperBound = Double.MIN_VALUE;
		lowerBound = Double.MAX_VALUE;
		
		populationXUB = new int[dataX.length];
		populationXLB = new int[dataX.length];
		
		Arrays.fill(populationXUB, 0);
		Arrays.fill(populationXLB, Integer.MAX_VALUE);
    	
    	String[] str={"-log","SILENT"};
    	IncompleteGermanTankProblem pb = new IncompleteGermanTankProblem();
    	pb.execute(str);
		
		StringBuilder st = new StringBuilder();
		st.append("\nm: "+lowerBound+"\t"+upperBound+"\n");
		for(int i = 0; i < dataX.length; i++){
			st.append(i+": "+populationXLB[i]+"\t"+populationXUB[i]+"\n");
		}
		System.out.println(st.toString());
    }
}

