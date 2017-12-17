package org.chocosolver.samples.statistical.ttest;

import umontreal.iro.lecuyer.probdist.StudentDist;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.t.tStatistic;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

public class TTest extends AbstractProblem {

    // input data
	int[] data = {8, 14, 6, 12, 12, 9, 10, 9, 10, 5}; //Poisson[10]

    // variables
    public IntVar[] population;
    public IntVar mean;
    
    double precision = 1.e-4;
    
    double alpha = 0.1;

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
        mean = VariableFactory.bounded("mean ", 0, 20, solver);

        //solver.post(IntConstraintFactory.arithmSt(population, "=", "MEAN", 0.95, mean));
        //solver.post(IntConstraintFactorySt.arithmSt(population, mean, "=", "MEAN", 0.95));
        
        StudentDist tDist = new StudentDist(populationSize - 1);
        
        RealVar t = VariableFactory.real("tStatistic", tDist.inverseF(alpha/2), tDist.inverseF(1-alpha/2), precision, solver);
        
        tStatistic.decompose(population, VariableFactory.real(mean, precision), t, precision);
    }

    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
    	IntVar[] var3 = new IntVar[var1.length+var2.length];
    	System.arraycopy(var1, 0, var3, 0, var1.length);
    	System.arraycopy(var2, 0, var3, var1.length, var2.length);
    	return var3;
    }
    
    @Override
    public void configureSearch() {
    	AbstractStrategy<IntVar> stratPop = IntStrategyFactory.domOverWDeg(population,2211);
    	AbstractStrategy<IntVar> stratMean = IntStrategyFactory.domOverWDeg(new IntVar[]{mean},2211);
        // trick : top-down maximization
        solver.set(stratPop,stratMean);
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
    			st.append(mean.getValue()+", ");
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
        new TTest().execute(str);
    }
}
