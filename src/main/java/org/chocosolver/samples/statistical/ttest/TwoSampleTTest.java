package org.chocosolver.samples.statistical.ttest;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.t.tStatistic;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

import umontreal.iro.lecuyer.probdist.StudentDist;

public class TwoSampleTTest extends AbstractProblem {

    // input data
    int[] dataX = {8, 14, 6, 12, 12, 9, 10, 9, 10, 5}; //Poisson[10]
	              //{9, 3, 7, 8, 8, 5, 8, 5, 3, 6}; //Poisson[7]
    int[] dataY = {9, 10, 9, 6, 11, 8, 10, 11, 14, 11}; //Poisson[10]

    // variables
    public IntVar[] populationX;
    public IntVar[] populationY;
    
    double precision = 1.e-4;
    
    double alpha = 0.1;

    public void setUp() {
        // read data
    }

    @Override
    public void createSolver() {
        solver = new Solver("TwoSampleTTest");
    }

    @Override
    public void buildModel() {
        setUp();
        int populationXSize = dataX.length;
        populationX = new IntVar[populationXSize];
        for(int i = 0; i < populationXSize; i++)
        	populationX[i] = VariableFactory.bounded("sample "+i, dataX[i], dataX[i], solver);

        int populationYSize = dataY.length;
        populationY = new IntVar[populationYSize];
        for(int i = 0; i < populationYSize; i++)
        	populationY[i] = VariableFactory.bounded("sample "+i, dataY[i], dataY[i], solver);
        
        StudentDist tDist = new StudentDist(populationXSize + populationYSize - 2);
        
        RealVar t = VariableFactory.real("tStatistic", tDist.inverseF(alpha/2), tDist.inverseF(1-alpha/2), precision, solver);
        
        //solver.post(IntConstraintFactorySt.arithmSt(populationX, populationY, "!=", "MEAN", 0.95));
        tStatistic.decompose("tStatistic_cons", populationX, populationY, t, precision);
    }
    
    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
    	IntVar[] var3 = new IntVar[var1.length+var2.length];
    	System.arraycopy(var1, 0, var3, 0, var1.length);
    	System.arraycopy(var2, 0, var3, var1.length, var2.length);
    	return var3;
    }

    @Override
    public void configureSearch() {
        AbstractStrategy<IntVar> strat = IntStrategyFactory.domOverWDeg(mergeArrays(populationX,populationY),2211);
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
    				st.append(populationX[i].getValue()+", ");
    			}
    			st.append("\n");
    			for(int i = 0; i < populationY.length; i++){
    				st.append(populationY[i].getValue()+", ");
    			}
    		}else{
    			st.append("No solution!");
    		}
    	}while(solution = solver.nextSolution());
    	System.out.println(st.toString());
    }

    @Override
    public void prettyOut() {
        
    }

    public static void main(String[] args) {
    	String[] str={"-log","SILENT"};
        new TwoSampleTTest().execute(str);
    }
}

