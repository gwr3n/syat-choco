package org.chocosolver.solver.constraints.nary.matrix;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EigendecompositionTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      System.gc();
      Thread.sleep(1000);
   }

   @Test
   public void test() {
      String[] str={"-log","SOLUTION"};
      double[][] A = {
            {1, 0.5, 0.6},
            {0.5, 1, 0.2},
            {0.6, 0.2, 1}
            };
      
      double[][] Q = {
            {-0.660966, -0.499326, -0.560176}, 
            {0.0527814, -0.775573, 0.629048}, 
            {0.748557, -0.386212, -0.538983}};
      
      double[][] Lambda = {
            {1.88623, 0., 0.}, 
            {0., 0.803758, 0.}, 
            {0., 0., 0.310012}};
      
      EigendecompositionReal eigendecompositionTest = new EigendecompositionReal(A, Q, Lambda);
      eigendecompositionTest.execute(str);
   }
   
   class EigendecompositionReal extends AbstractProblem {
      public RealVar[][] matrixA;
      public RealVar[][] matrixQ;
      public RealVar[][] inverseQ;
      public RealVar[][] matrixLambda;
      
      double[][] A;
      double[][] Q;
      double[][] Lambda;
      
      double precision = 0.01;
      
      public EigendecompositionReal(double[][] A, double Q[][], double[][] Lambda) {
         this.A = A;
         this.Q = Q;
         this.Lambda = Lambda;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("Eigendecomposition");
      }
      
      @Override
      public void buildModel() {
         matrixA = new RealVar[this.A.length][this.A[0].length];
         for(int i = 0; i < matrixA.length; i++){
            for(int j = 0; j < matrixA[i].length; j++){
               matrixA[i][j] = VariableFactory.real("MatrixA_"+(i+1)+"_"+(j+1), this.A[i][j]-precision, this.A[i][j]+precision, precision, solver);
            }
         }
         
         matrixQ = new RealVar[this.A.length][this.A[0].length];
         for(int i = 0; i < matrixQ.length; i++){
            for(int j = 0; j < matrixQ[i].length; j++){
               matrixQ[i][j] = VariableFactory.real("MatrixQ_"+(i+1)+"_"+(j+1), -100, 100, precision, solver);
            }
         }
         
         inverseQ = new RealVar[matrixQ.length][matrixQ[0].length];
         
         for(int i = 0; i < inverseQ.length; i++) {
            for(int j = 0; j < inverseQ[i].length; j++){
               //inverseQ[i][j] = VariableFactory.real("Q^{-1}_"+(i+1)+"_"+(j+1), -1, 1, precision, solver);
               inverseQ[i][j] = matrixQ[j][i]; // Inverse = Transpose
            }
         }
         
         //MatrixInversion.decompose("InverseQ", matrixQ, inverseQ);
         
         IntVar zero = VariableFactory.fixed(0, solver);
         RealVar zeroReal = VariableFactory.real(zero, precision);
         
         matrixLambda = new RealVar[this.A.length][this.A[0].length];
         for(int i = 0; i < matrixLambda.length; i++){
            for(int j = 0; j < matrixLambda[i].length; j++){
               if(i!=j)
                  matrixLambda[i][j] = zeroReal;
               else
                  matrixLambda[i][j] = VariableFactory.real("MatrixLambda_"+(i+1)+"_"+(j+1), 0, VariableFactory.MAX_INT_BOUND, precision, solver); // Matrix must be semidefinite positive
            }
         }
         
         Eigendecomposition.decompose("Eigendecomposition", matrixA, inverseQ, matrixLambda, matrixQ, zeroReal, solver);
      }
      
      public RealVar[] flatten(RealVar[][] matrix){
         int n = matrix.length;
         RealVar[] array = new RealVar[n*n];
         for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
               array[i*n+j] = matrix[i][j];
            }
         }
         return array;
      }
      
      @Override
      public void configureSearch() {
         //RealStrategy stratA = new RealStrategy(flatten(matrixA), new Cyclic(), new RealDomainMiddle());
         RealStrategy stratQ = new RealStrategy(flatten(matrixQ), new Cyclic(), new RealDomainMiddle());
         //RealStrategy stratInvQ = new RealStrategy(flatten(inverseQ), new Cyclic(), new RealDomainMiddle());
         RealStrategy stratLambda = new RealStrategy(new RealVar[] {matrixLambda[0][0],matrixLambda[1][1],matrixLambda[2][2]}, new Cyclic(), new RealDomainMiddle());
         solver.set(stratLambda, stratQ);
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           if(solution) {
              for(int i = 0; i < flatten(matrixQ).length; i++){
                 //st.append("("+flatten(matrixQ)[i].getLB()+","+flatten(matrixQ)[i].getUB()+"), ");
                 st.append(flatten(matrixQ)[i].getLB()+"\t");
                 //assertEquals(flatten(matrixC)[i].getLB(), C[i/matrixC[0].length][i%matrixC.length], precision);
              }
              st.append("\n");
              for(int i = 0; i < flatten(matrixLambda).length; i++){
                 //st.append("("+flatten(matrixLambda)[i].getLB()+","+flatten(matrixLambda)[i].getUB()+"), ");
                 st.append(flatten(matrixLambda)[i].getLB()+"\t");
                 //assertEquals(flatten(matrixC)[i].getLB(), C[i/matrixC[0].length][i%matrixC.length], precision);
              }
              assertTrue(st.toString().equals(
                    "-1.1795481431911017\t-0.6187159805837857\t-0.37046905426624155\t-0.05940527538341387\t-0.8491910451491553\t-0.0753638513957249\t-0.3024110065361656\t-0.0010342256076943074\t-0.8411750139720657\t\n" + 
                    "0.6361523437500001\t0.0\t0.0\t0.0\t1.0438865355293816\t0.0\t0.0\t0.0\t1.280195129279382\t"));
           }else{
              st.append("No solution!");
              fail("No solution!");
           }
        //}while(solution = solver.nextSolution());
        System.out.println(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
}
