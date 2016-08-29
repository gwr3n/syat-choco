package org.chocosolver.samples.statistical.kolmogorovsmirnov;

import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.solver.variables.statistical.distributions.UniformDistVar;
import org.chocosolver.util.tools.ArrayUtils;

import umontreal.iro.lecuyer.randvar.UniformIntGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

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
        		populationX[i] = VariableFactory.bounded("sample "+i, dataX[i]-dataX[i]%10, dataX[i]+(9-dataX[i]%10), solver);
        		//populationX[i] = VariableFactory.bounded("sample "+i, dataX[i], dataX[i], solver);
        	else
        		populationX[i] = VariableFactory.bounded("sample "+i, dataX[i], dataX[i], solver);
        }

        uniformUB = VariableFactory.bounded("UB", 1, 150, solver);
        
        solver.post(IntConstraintFactorySt.arithmSt(populationX, new UniformDistVar(uniformUB), "=", 0.95));
    }
    
    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
    	//return ArrayUtils.append(var1,var2);
    	IntVar[] var3 = new IntVar[var1.length+var2.length];
    	System.arraycopy(var1, 0, var3, 0, var1.length);
    	System.arraycopy(var2, 0, var3, var1.length, var2.length);
    	return var3;
    }

    @Override
    public void configureSearch() {
        AbstractStrategy<IntVar> strat = IntStrategyFactory.domOverWDeg(mergeArrays(populationX,new IntVar[]{uniformUB}),2211);
        // trick : top-down maximization
        solver.set(strat);
    }

    @Override
    public void solve() {
    	StringBuilder st = new StringBuilder();
    	boolean solution = solver.findSolution();
    	do{
    		if(solution) {
    			for(int i = 0; i < populationX.length; i++){
    				//st.append(populationX[i].getValue()+", ");
    				populationXUB[i] = Math.max(populationXUB[i], populationX[i].getValue());
    				populationXLB[i] = Math.min(populationXLB[i], populationX[i].getValue());
    			}
    			upperBound = Math.max(upperBound, uniformUB.getValue());
    			lowerBound = Math.min(lowerBound, uniformUB.getValue());
    		}else{
    			st.append("No solution!");
    		}
    	}while(solution = solver.nextSolution());
    	LoggerFactory.getLogger("bench").info(st.toString());
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
		new IncompleteGermanTankProblem().execute(str);
		
		StringBuilder st = new StringBuilder();
		st.append("\nm: "+lowerBound+"\t"+upperBound+"\n");
		for(int i = 0; i < dataX.length; i++){
			st.append(i+": "+populationXLB[i]+"\t"+populationXUB[i]+"\n");
		}
		LoggerFactory.getLogger("bench").info(st.toString());
    }
}

