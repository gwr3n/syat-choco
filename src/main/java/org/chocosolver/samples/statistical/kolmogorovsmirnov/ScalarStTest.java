package org.chocosolver.samples.statistical.kolmogorovsmirnov;

import java.io.IOException;

import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

public class ScalarStTest extends AbstractProblem {

    /*@Option(name = "-d", aliases = "--data", usage = "Knapsack data ID.", required = false)
    Data data = Data.k20;

    @Option(name = "-n", usage = "Restricted to n objects.", required = false)
    int n = 13;*/

    // input data
    int[][] data = {{100,1083,35,505,122,405,4458,737},
    		{129,1616,52,510,144,705,3611,640},
    		{100,1929,128,504,153,270,3788,739},
    		{92,2432,53,455,115,353,3877,842},
    		{95,1729,138,567,259,363,3957,627},
    		{89,3744,112,461,324,496,4494,794}};
    int maxStocks = 5;

    // variables
    public IntVar[] objects;

    public void setUp() {
        // read data
    }

    @Override
    public void createSolver() {
        solver = new Solver("Knapsack");
    }

    @Override
    public void buildModel() {
        setUp();
        int nos = data[0].length-1;
        // occurrence of each item
        objects = new IntVar[nos];
        for (int i = 0; i < nos; i++) {
            objects[i] = VariableFactory.bounded("o_" + (i + 1), 0, maxStocks, solver);
        }
        
        // objective variable
        //power = VariableFactory.bounded("power", 0, 9999, solver);

        int[][] securityPrices = new int[data.length][data[0].length-1];
        int[] capital = new int[data.length];
        
        for(int i = 0; i < data.length; i++){
        	for(int j = 0; j < securityPrices[i].length; j++){
        		securityPrices[i][j] = data[i][j];
        	}
        	capital[i] = data[i][data[i].length-1];
        }
        solver.post(IntConstraintFactorySt.scalarSt(objects, securityPrices, capital, 0.85));      
    }

    @Override
    public void configureSearch() {
        AbstractStrategy strat = IntStrategyFactory.domOverWDeg(objects,2211);
        // trick : top-down maximization
        solver.set(strat);
    }

    @Override
    public void solve() {
        StringBuilder st = new StringBuilder();
    	boolean solution = solver.findSolution();
    	do{
    		if(solution) {
    			st.append("\tItem: Count\n");
    	        String[] headings = {"AAPL","AMZN","CSCO","HPQ","EBAY","FB","GOOG"};
    	        for (int i = 0; i < objects.length; i++) {
    	            st.append(String.format("\t"+headings[i]+": %d\n", objects[i].getValue()));
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
    	String[] str={"-log","SILENT"};
        new ScalarStTest().execute(str);
    }
}
