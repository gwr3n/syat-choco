package org.chocosolver.solver.constraints.nary.bincounts;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BincountsTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void testInstance1() {
      String[] str={"-log","SILENT"};
      int[][] values = {{1},{2},{3}};
      int[][] binCounts = {{0,3},{0,3}};
      int[] binBounds = {1,3,4};
      
      Bincounts bc = new Bincounts(values, binCounts, binBounds);
      try{
         bc.execute(str);
      }catch (AssertionError e) {}
      int[][] expectedFilteredDomains = new int[][]{{1},{2},{3}};
      int[][] expectedFilteredBins = new int[][]{{2},{1}};
      assertTrue("Bincounts: ", bc.checkFilteredDomains(expectedFilteredDomains, expectedFilteredBins));
   }
   
   @Test
   public void testInstance2() {
      String[] str={"-log","SILENT"};
      int[][] values = {{3,4},{1,2,4},{2,3,4}};
      int[][] binCounts = {{1,3},{0,1}};
      int[] binBounds = {1,3,5};
      
      Bincounts bc = new Bincounts(values, binCounts, binBounds);
      try{
         bc.execute(str);
      }catch (AssertionError e) {}
      int[][] expectedFilteredDomains = new int[][]{{3,4},{1,2},{2}};
      int[][] expectedFilteredBins = new int[][]{{2},{1}};
      assertTrue("Bincounts: ", bc.checkFilteredDomains(expectedFilteredDomains, expectedFilteredBins));
   }
   
   class Bincounts extends AbstractProblem {
      public IntVar[] valueVariables;
      public IntVar[] binVariables;
      
      int[][] binCounts;
      int[][] values;
      int[] binBounds;
      
      public Bincounts(int[][] values,
                       int[][] binCounts, 
                       int[] binBounds){
         this.values = values.clone();
         this.binCounts = binCounts.clone();
         this.binBounds = binBounds.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("Bincounts");
      }
      
      @Override
      public void buildModel() {
         //setUp();
         valueVariables = new IntVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.enumerated("Value "+(i+1), values[i], solver);
         
         binVariables = new IntVar[this.binCounts.length];
         for(int i = 0; i < this.binCounts.length; i++)
            binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
         
         IntConstraintFactorySt.bincountsDecomposition(valueVariables, binVariables, binBounds, BincountsDecompositionType.Agkun2016_2_EQ);      
      }
      
      private IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
         IntVar[] var3 = new IntVar[var1.length+var2.length];
         System.arraycopy(var1, 0, var3, 0, var1.length);
         System.arraycopy(var2, 0, var3, var1.length, var2.length);
         return var3;
       }
      
      @Override
      public void configureSearch() {
        AbstractStrategy<IntVar> strat = IntStrategyFactory.activity(mergeArrays(valueVariables,binVariables),1234);
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
      }
      
      public boolean checkFilteredDomains(int[][] values, int[][] binCounts){
         boolean condition = true;
         
         {
            int[][] filteredValues = new int[values.length][];
            for(int v = 0; v < filteredValues.length; v++){
               int c = 0;
               int[] domain = new int[valueVariables[v].getDomainSize()]; 
               DisposableValueIterator vit = valueVariables[v].getValueIterator(true);
               while(vit.hasNext()){
                  domain[c++] = vit.next();
               }
               vit.dispose();
               filteredValues[v] = domain;
            }
            
            for(int v = 0; v < values.length; v++){
               if(!Arrays.equals(values[v], filteredValues[v]))
                  condition = false;
            }
         }
         
         {
            int[][] filteredValues = new int[binCounts.length][];
            for(int v = 0; v < filteredValues.length; v++){
               int c = 0;
               int[] domain = new int[binVariables[v].getDomainSize()]; 
               DisposableValueIterator vit = binVariables[v].getValueIterator(true);
               while(vit.hasNext()){
                  domain[c++] = vit.next();
               }
               vit.dispose();
               filteredValues[v] = domain;
            }
            
            for(int v = 0; v < binCounts.length; v++){
               if(!Arrays.equals(binCounts[v], filteredValues[v]))
                  condition = false;
            }
         }
         
         return condition;
      }
      
      @Override
      public void prettyOut() {
         StringBuilder st = new StringBuilder();
         st.append("\n");
         for(int i = 0; i < valueVariables.length; i++){
            st.append(valueVariables[i].toString()+", ");
         }
         st.append("\n");
         for(int i = 0; i < binVariables.length; i++){
            st.append(binVariables[i].toString()+", ");
         }
         System.out.println(st.toString());
      }
   }
}
