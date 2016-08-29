package org.chocosolver.samples.statistical.ttest;

import org.slf4j.LoggerFactory;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

public class TTest extends AbstractProblem {

    // input data
	int[] data = {8, 14, 6, 12, 12, 9, 10, 9, 10, 5}; //Poisson[10]

    // variables
    public IntVar[] population;
    public IntVar[] mean;

    public void setUp() {
        // read data
    }

    @Override
    public void createSolver() {
        solver = new Solver("TTest");
    }

    @Override
    public void buildModel() {
        setUp();
        int populationSize = data.length;
        population = new IntVar[populationSize];
        for(int i = 0; i < populationSize; i++)
        	population[i] = VariableFactory.bounded("sample "+i, data[i], data[i], solver);
        mean = new IntVar[1];
        mean[0] = VariableFactory.bounded("mean ", 0, 20, solver);

        //solver.post(IntConstraintFactory.arithmSt(population, "=", "MEAN", 0.95, mean));
        solver.post(IntConstraintFactorySt.arithmSt(population, mean, "=", "MEAN", 0.95));
    }

    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
    	IntVar[] var3 = new IntVar[var1.length+var2.length];
    	System.arraycopy(var1, 0, var3, 0, var1.length);
    	System.arraycopy(var2, 0, var3, var1.length, var2.length);
    	return var3;
    }
    
    @Override
    public void configureSearch() {
    	AbstractStrategy<IntVar> strat = IntStrategyFactory.domOverWDeg(mergeArrays(population,mean),2211);
        // trick : top-down maximization
        solver.set(strat);
    }

    @Override
    public void solve() {
    	StringBuilder st = new StringBuilder();
    	boolean solution = solver.findSolution();
    	do{
    		if(solution) {
    			/*for(int i = 0; i < population.length; i++){
    				st.append(population[i].getValue()+", ");
    			}*/
    			st.append(mean[0].getValue()+", ");
    		}else{
    			st.append("No solution!");
    		}
    	}while(solution = solver.nextSolution());
    	LoggerFactory.getLogger("bench").info(st.toString());
    }

    @Override
    public void prettyOut() {
        
    }

    public static void main(String[] args) {
    	String[] str={"-log","SILENT"};
        new TTest().execute(str);
    }
}
