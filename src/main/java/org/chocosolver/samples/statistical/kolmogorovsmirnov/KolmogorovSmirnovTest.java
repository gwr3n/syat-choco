package org.chocosolver.samples.statistical.kolmogorovsmirnov;

import org.slf4j.LoggerFactory;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.NormalDist;

public class KolmogorovSmirnovTest extends AbstractProblem {

    // input data
    int[] dataX = {8, 14, 6, 12, 12, 9, 10, 9, 10, 5}; //Poisson[10]
	              //{9, 3, 7, 8, 8, 5, 8, 5, 3, 6}; //Poisson[7]

    // variables
    public IntVar[] populationX;

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
        
        Distribution dist = new NormalDist(10,Math.sqrt(10));
        
        solver.post(IntConstraintFactorySt.arithmSt(populationX, dist, "!=", 0.95));
    }

    @Override
    public void configureSearch() {
        AbstractStrategy<IntVar> strat = IntStrategyFactory.domOverWDeg(populationX,2211);
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
    	//String[] str={"-log","SILENT"};
        new KolmogorovSmirnovTest().execute(args);
    }
}

