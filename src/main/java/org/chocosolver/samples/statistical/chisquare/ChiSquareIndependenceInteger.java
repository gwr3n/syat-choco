package org.chocosolver.samples.statistical.chisquare;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.contingency.ContingencyDecompositions;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareIndependence;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.slf4j.LoggerFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public class ChiSquareIndependenceInteger extends AbstractProblem {
   public IntVar[] seriesA;
   public IntVar[] seriesB;
   public IntVar[][] binVariables;
   IntVar[] marginalsH; 
   IntVar[] marginalsV; 
   public RealVar chiSqstatisticVariable;
   
   int[][] valuesA;
   int[][] valuesB;
   int[] binCounts;
   int[][] binBounds;
   
   double[] chiSqStatistic;
   
   double precision = 1.e-4;
   
   public ChiSquareIndependenceInteger(int[][] valuesA,
                                       int[][] valuesB, 
                                       int[] binCounts,
                                       int[][] binBounds,
                                       double[] chiSqStatistic){
      this.valuesA = valuesA.clone();
      this.valuesB = valuesB.clone();
      this.binCounts = binCounts.clone();
      this.binBounds = binBounds.clone();
      this.chiSqStatistic = chiSqStatistic.clone();
   }
   
   @Override
   public void createSolver() {
       solver = new Solver("ChiSquare");
   }
   
   @Override
   public void buildModel() {
      seriesA = new IntVar[this.valuesA.length];
      for(int i = 0; i < this.valuesA.length; i++)
         seriesA[i] = VariableFactory.enumerated("Value A"+(i+1), valuesA[i], solver);
      
      seriesB = new IntVar[this.valuesB.length];
      for(int i = 0; i < this.valuesB.length; i++)
         seriesB[i] = VariableFactory.enumerated("Value B"+(i+1), valuesB[i], solver);
      
      binVariables = new IntVar[this.binBounds[0].length - 1][this.binBounds[1].length - 1];
      for(int i = 0; i < this.binVariables.length; i++){
         for(int j = 0; j < this.binVariables[0].length; j++){
            binVariables[i][j] = VariableFactory.bounded("Bin "+(i+1)+","+(j+1), this.binCounts[0], this.binCounts[1], solver);
         }
      }
      
      marginalsH = VariableFactory.boundedArray("Marginals H", binVariables.length, 0, seriesA.length, solver);
      
      marginalsV = VariableFactory.boundedArray("Marginals V", binVariables[0].length, 0, seriesA.length, solver);
      
      ContingencyDecompositions.decomposition(seriesA, seriesB, binVariables, binBounds, marginalsH, marginalsV);
      
      chiSqstatisticVariable = VF.real("chiSqStatistics", chiSqStatistic[0], chiSqStatistic[1], precision, solver);
      
      ChiSquareIndependence.decomposition("chiSqConstraint", seriesA.length, binVariables, marginalsH, marginalsV, chiSqstatisticVariable, precision);
   }
   
   @Override
   public void configureSearch() {
      IntVar[] flattenedBins = new IntVar[binVariables.length*binVariables[0].length];
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables[0].length; j++){
            flattenedBins[binVariables[0].length*i + j] = binVariables[i][j]; 
         }
      }
      
      solver.set(
            IntStrategyFactory.activity(seriesA,1234),
            IntStrategyFactory.activity(seriesB,1234),
            //IntStrategyFactory.activity(flattenedBins,1234),
            //IntStrategyFactory.activity(marginalsH,1234),
            //IntStrategyFactory.activity(marginalsV,1234),
            new RealStrategy(new RealVar[]{chiSqstatisticVariable}, new Cyclic(), new RealDomainMiddle())
            );
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     boolean solution = solver.findSolution();
     //do{
        st.append("---\n");
        if(solution) {
           /*for(int i = 0; i < valueVariables.length; i++){
              st.append("("+valueVariables[i].getLB()+", "+valueVariables[i].getUB()+"), ");
           }
           st.append("\n");
           for(int i = 0; i < binVariables.length; i++){
              st.append(binVariables[i].getValue()+", ");
           }
           st.append("\n");
           st.append(meanVariable.getLB()+" "+meanVariable.getUB());
           st.append("\n");
           st.append(stdVariable.getLB()+" "+stdVariable.getUB());
           st.append("\n");
           st.append(chiSqstatisticVariable.getLB()+" "+chiSqstatisticVariable.getUB());
           st.append("\n");*/
           
           //assertTrue(chiSqstatisticVariable.getLB() <= 0);
           //assertTrue(chiSqstatisticVariable.getUB() >= 0);
           
           st.append(chiSqstatisticVariable.getLB()+" "+chiSqstatisticVariable.getUB());
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     LoggerFactory.getLogger("bench").info(st.toString());
   }
   
   @Override
   public void prettyOut() {
       
   }
   
   public static void main(String[] args){
      String[] str={"-log","SOLUTION"};
      
      int[][] valuesA = {{1},{1,2,3},{3},{1},{1,2,3}};
      int[][] valuesB = {{2},{3},{1},{2},{1}};
      int[] binCounts = {0,valuesA.length};
      int[][] binBounds = {{1,2,3,4},{1,2,3,4}};
      double confidence = 0.95;
      double chiSqUB = new ChiSquareDist((binBounds[0].length-1)*(binBounds[1].length-1)).inverseF(confidence);
      
      double[] chiSqStatistic = {0,chiSqUB};
      
      ChiSquareIndependenceInteger cs = new ChiSquareIndependenceInteger(valuesA, valuesB, binCounts, binBounds, chiSqStatistic);
      cs.execute(str);
   }
}
