/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.real.bacp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.samples.real.bacp.preprocessing.longestpath.LongestPath;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsPropagatorType;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.iterators.DisposableValueIterator;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

/**
 * The balanced academic curriculum problem: 
 * <a href="http://www.dcs.st-and.ac.uk/~ianm/CSPLib/prob/prob030/spec.html">
 * csplib problem 30
 * </a>
 * <p/>
 * A curriculum is a set of courses with prerequisites.
 * <p/>
 * Each course must be assigned within a set number of periods.
 * <p/>
 * A course cannot be scheduled before its prerequisites.
 * <p/>
 * Each course confers a number of academic credits (it's "load").
 * <p/>
 * Students have lower and upper bounds on the number of credits
 * they can study for in a given period.
 * <p/>
 * Students have lower and upper bounds on the number of courses
 * they can study for in a given period.
 * <p/>
 * The goal is to assign a period to every course satisfying these
 * criteria, minimising the load for all periods.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/07/12
 */
public class BACP_Chi_Bincounts extends AbstractProblem {
    
    String instance = "BACP/bacp-1"
                      + ".mzn";
   
    public void loadInstance(){
       FileReader fr = null;
       String model = "";
       try {
         ClassLoader classLoader = getClass().getClassLoader();
         File file = new File(classLoader.getResource(instance).getFile()); 
         fr = new FileReader(file);
         char[] buffer = new char[1000];
         while(fr.read(buffer) > -1){
               model += new String(buffer);
               buffer = new char[1000];
         }
         fr.close();
         String[] lines = model.split("\n");
         for(int i = 1; i < lines.length; i++){
            if(!lines[i].startsWith("constraint prerequisite")){
               String[] parts = lines[i].substring(0, lines[i].length()-1).split(" = ");
               if(parts[0].equals("n_courses"))
                  n_courses = Integer.parseInt(parts[1]);
               else if(parts[0].equals("n_periods"))
                  n_periods = Integer.parseInt(parts[1]);
               else if(parts[0].equals("load_per_period_lb"))
                  load_per_period_lb = Integer.parseInt(parts[1]);
               else if(parts[0].equals("load_per_period_ub"))
                  load_per_period_ub = Integer.parseInt(parts[1]);
               else if(parts[0].equals("courses_per_period_lb"))
                  courses_per_period_lb = Integer.parseInt(parts[1]);
               else if(parts[0].equals("courses_per_period_ub"))
                  courses_per_period_ub = Integer.parseInt(parts[1]);
               else if(parts[0].equals("course_load")){
                  String[] load =  parts[1].substring(1, parts[1].length() - 1).split(", ");
                  course_load = new int[load.length];
                  for(int l = 0; l < load.length; l++)
                     course_load[l] = Integer.parseInt(load[l]);
               }
            }
         }
      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
    }
    
    public void loadPrerequisites(){
       FileReader fr = null;
       String model = "";
       try {
         ClassLoader classLoader = getClass().getClassLoader();
         File file = new File(classLoader.getResource(instance).getFile()); 
         fr = new FileReader(file);
         char[] buffer = new char[1000];
         while(fr.read(buffer) > -1){
               model += new String(buffer);
               buffer = new char[1000];
         }
         fr.close();
         String[] lines = model.split("\n");
         for(int i = 1; i < lines.length; i++){
            if(lines[i].startsWith("constraint prerequisite")){
               String[] parts = lines[i].substring(0, lines[i].length()-1).split("prerequisite");
               String prerequisite[] = parts[1].substring(1, parts[1].length()-1).split(", ");
               this.prerequisite(Integer.parseInt(prerequisite[0]), Integer.parseInt(prerequisite[1]));
            }
         }
      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
    }
   
    int n_courses = 50;
    int n_periods = 10;
    int load_per_period_lb = 2;
    int load_per_period_ub = 100;
    int courses_per_period_lb = 2;
    int courses_per_period_ub = 10;
    int[] course_load =
            {6, 3, 5, 3, 7, 8, 1, 9,
                    4, 9, 8, 8, 4, 5,
                    6, 3, 2, 1, 3, 1,
                    1, 2, 6, 7, 6, 10,
                    10, 1, 7, 3, 4, 2, 7,
                    9, 7, 4, 6, 7, 2, 2,
                    5, 9, 9, 10, 4, 6, 4,
                    5, 6, 6};
    
    int[] binBounds = new int[]{0,15,20,30,35,load_per_period_ub+1};
    int[] targetFrequencies = new int[]{1,2,4,2,1};
    
    // Target frequencies cannot be zero! If necessary reduce number of bins.
    //int[] binBounds = new int[]{15,20,30,35};
    //int[] targetFrequencies = new int[]{2,6,2};

    // period course is assigned to
    IntVar[] course_period;
    // whether period i has course j assigned
    BoolVar[][] x;
    // total load for each period
    IntVar[] load;
    IntVar[] binVariables;
    
    RealVar chiSqStatistics;
    RealVar[] allRV;
    
    double precision = 0.01;

    ChiSquareDist chiSqDist;
    double pValue = 0.99;
    
    @Override
    public void createSolver() {
        solver = new Solver("BACP");
    }

    @Override
    public void buildModel() {  
        loadInstance();
       
        // period course is assigned to
        //course_period = VariableFactory.enumeratedArray("c_p", n_courses, 0, n_periods-1, solver);
        course_period = new IntVar[n_courses];
        LongestPath path = new LongestPath();
        int[] distancesLB = path.computeLBs(instance);
        int[] distancesUB = path.computeUBs(instance);
        for(int i = 0; i < course_period.length; i++){
           course_period[i] = VariableFactory.enumerated("c_p"+i, distancesLB[i+1]-1, distancesUB[i+1]-1, solver);
        }
        
        // whether period i has a course j assigned
        x = VariableFactory.boolMatrix("X", n_periods, n_courses, solver);
        // total load for each period
        load = VariableFactory.enumeratedArray("load", n_periods, load_per_period_lb, load_per_period_ub, solver);
        // sum variable
        IntVar[] sum = VariableFactory.integerArray("courses_per_period", n_periods, courses_per_period_lb, courses_per_period_ub, solver);
        // constraints
        for (int i = 0; i < n_periods; i++) {
            // forall(c in courses) (x[p,c] = bool2int(course_period[c] = p)) /\
            for (int j = 0; j < n_courses; j++) {
               try{
                solver.post(
                        LogicalConstraintFactory.ifThenElse_reifiable(x[i][j],
                        IntConstraintFactory.arithm(course_period[j], "=", i),
                        IntConstraintFactory.arithm(course_period[j], "!=", i))
                );
               }catch(NullPointerException e){
                  e.printStackTrace();
               }
            }
            // sum(i in courses) (x[p, i])>=courses_per_period_lb /\
            // sum(i in courses) (x[p, i])<=courses_per_period_ub /\
            solver.post(IntConstraintFactory.sum(x[i], sum[i]));
            //  load[p] = sum(c in courses) (x[p, c]*course_load[c])/\
            solver.post(IntConstraintFactory.scalar(x[i], course_load, load[i]));
        }
        
        int[] values = new int[n_periods];
        for(int i = 0; i < n_periods; i++) values[i] = i;
        
        solver.post(IntConstraintFactory.global_cardinality(course_period, values, sum, true));
        
        solver.post(IntConstraintFactory.bin_packing(course_period, course_load, load, 0));
        
        /*for(int l = 0; l < n_periods; l++){
           IntVar[] reducedLoadArray = new IntVar[n_periods - l];
           System.arraycopy(load, l, reducedLoadArray, 0, n_periods - l);
           IntVar loadVar = VariableFactory.bounded("loadVar_"+l, (n_periods - l)*load_per_period_lb, (n_periods - l)*load_per_period_ub, solver);
           solver.post(IntConstraintFactory.sum(reducedLoadArray, loadVar));
        }*/
        
        binVariables = new IntVar[binBounds.length-1];
        for(int i = 0; i < binBounds.length-1; i++){
           binVariables[i] = VariableFactory.bounded("Bin "+i, 0, n_periods, solver);
        }
        
        solver.post(IntConstraintFactorySt.bincounts(load, binVariables, binBounds, BincountsPropagatorType.EQFast));
        
        this.chiSqDist = new ChiSquareDist(this.binVariables.length-1);
        
        chiSqStatistics = VariableFactory.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-pValue), precision, solver);
        
        RealVar[] realViews = VariableFactory.real(binVariables, precision);
        allRV = new RealVar[realViews.length+1];
        System.arraycopy(realViews, 0, allRV, 0, realViews.length);
        allRV[realViews.length] = chiSqStatistics;
        
        String chiSqExp = "";
        for(int i = 0; i < binVariables.length; i++)
           if(i == binVariables.length - 1)
              chiSqExp += "(({"+i+"}-"+targetFrequencies[i]+")^2)/"+targetFrequencies[i]+"={"+(binVariables.length)+"}";
           else
              chiSqExp += "(({"+i+"}-"+targetFrequencies[i]+")^2)/"+targetFrequencies[i]+"+";
        
        solver.post(new RealConstraint("chiSqTest",
              chiSqExp,
              Ibex.HC4_NEWTON, allRV
              ));

        // prerequisite(a, b) means "course a has prerequisite course b".
        
        loadPrerequisites();
        
        /*prerequisite(3, 1);
        prerequisite(4, 1);
        prerequisite(5, 1);
        prerequisite(6, 1);
        prerequisite(7, 1);
        prerequisite(6, 2);
        prerequisite(8, 2);
        prerequisite(11, 3);
        prerequisite(11, 4);
        prerequisite(16, 4);
        prerequisite(16, 5);
        prerequisite(11, 6);
        prerequisite(14, 6);
        prerequisite(16, 8);
        prerequisite(13, 9);
        prerequisite(14, 9);
        prerequisite(17, 11);
        prerequisite(19, 11);
        prerequisite(17, 12);
        prerequisite(19, 12);
        prerequisite(18, 13);
        prerequisite(17, 14);
        prerequisite(18, 14);
        prerequisite(23, 17);
        prerequisite(21, 19);
        prerequisite(26, 21);
        prerequisite(27, 21);
        prerequisite(30, 22);
        prerequisite(24, 23);
        prerequisite(25, 23);
        prerequisite(27, 23);
        prerequisite(33, 25);
        prerequisite(34, 27);
        prerequisite(35, 27);
        prerequisite(35, 28);
        prerequisite(33, 29);
        prerequisite(34, 29);
        prerequisite(35, 30);
        prerequisite(36, 31);
        prerequisite(38, 31);
        prerequisite(39, 31);
        prerequisite(40, 31);
        prerequisite(43, 31);
        prerequisite(40, 32);
        prerequisite(37, 33);
        prerequisite(38, 33);
        prerequisite(40, 33);
        prerequisite(38, 34);
        prerequisite(41, 34);
        prerequisite(41, 35);
        prerequisite(42, 35);
        prerequisite(44, 36);
        prerequisite(45, 36);
        prerequisite(45, 37);
        prerequisite(44, 40);
        prerequisite(45, 40);
        prerequisite(47, 40);
        prerequisite(44, 41);
        prerequisite(45, 41);
        prerequisite(46, 41);
        prerequisite(46, 42);
        prerequisite(47, 42);
        prerequisite(48, 47);
        prerequisite(44, 43);
        prerequisite(45, 43);
        prerequisite(49, 46);
        prerequisite(50, 47);*/
        
        postSymBreakDominanceConstraints(course_load);
    }
    
    /**
     * See https://www.info.ucl.ac.be/~pdupont/pdupont/pdf/BACP_symcon_07.pdf
     * 
     * @param load
     */
    private void postSymBreakDominanceConstraints(int[] load){
       LongestPath path = new LongestPath();
       int[][] prerequisiteMatrix = path.loadPrerequisites(instance);
       for(int i = 0; i < load.length; i++){
          for(int j = i; j < load.length; j++){
             if(i != j && load[i] == load[j]){
                boolean equivalent = true;
                for(int k = 0; k < prerequisiteMatrix[i].length; k++){
                   if(prerequisiteMatrix[i][k] != prerequisiteMatrix[j][k] ||
                         prerequisiteMatrix[k][i] != prerequisiteMatrix[k][j]){
                      equivalent = false;
                   }
                }
                if(equivalent){
                   solver.post(IntConstraintFactory.arithm(course_period[i], "<=", course_period[j]));
                }
             }
          }
       }
    }

    private void prerequisite(int a, int b) {
        solver.post(IntConstraintFactory.arithm(course_period[b - 1], "<", course_period[a - 1]));
    }

    @Override
    public void configureSearch() {   
       //IntVar[] allVars = new IntVar[course_period.length+load.length];
       //System.arraycopy(load, 0, allVars, 0, load.length);
       //System.arraycopy(course_period, 0, allVars, load.length, course_period.length);
       solver.set(
             //new RealStrategy(new RealVar[]{variance}, new Cyclic(), new RealDomainMiddle()),
             IntStrategyFactory.activity(course_period,1234)
             /*IntStrategyFactory.custom(
                   IntStrategyFactory.minDomainSize_var_selector(), 
                   new org.chocosolver.solver.search.strategy.selectors.IntValueSelector(){
                      public int selectValue(IntVar var) {
                         int periodWinner = -1;
                         int minLoad = Integer.MAX_VALUE;
                         DisposableValueIterator vit = var.getValueIterator(true);
                         while(vit.hasNext()){
                             int v = vit.next();
                             if(load[v].getLB() < minLoad){
                                minLoad = load[v].getLB();
                                periodWinner = v;
                             }
                             // operate on value v here
                         }
                         vit.dispose();
                         return periodWinner;
                      };
                   }, 
                   course_period
                   )*/        
       );
    }

    @Override
    public void solve() {
       solver.getSearchLoop().plugSearchMonitor(new IMonitorSolution() {
          public void onSolution() {
                System.out.println("---");
                System.out.println("Chi^2 statistics (min threshold "+chiSqDist.inverseF(1-pValue)+"): ("+chiSqStatistics.getLB()+", "+chiSqStatistics.getUB()+")");
                System.out.print("Course\t");
                for(int i = 0; i < course_period.length; i++){
                   System.out.print(i+"\t");
                }
                System.out.print("\nPeriod\t");
                for(int i = 0; i < course_period.length; i++){
                   System.out.print(course_period[i].getValue()+"("+course_period[i].getDomainSize()+")\t");
                }
                System.out.print("\nPeriod\t");
                for(int i = 0; i < load.length; i++){
                   System.out.print(i+"\t");
                }
                System.out.print("\nLoad\t");
                for(int i = 0; i < load.length; i++){
                   System.out.print(load[i].getValue()+"\t");
                }
                System.out.print("\nBins\t");
                for(int i = 0; i < binVariables.length; i++){
                   System.out.print(binVariables[i].getValue()+"\t");
                }
                System.out.println();
                System.out.println("---");
             }
          });
       solver.findSolution();
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
       String[] str={"-log","SOLUTION"};
       new BACP_Chi_Bincounts().execute(str);
    }
}
