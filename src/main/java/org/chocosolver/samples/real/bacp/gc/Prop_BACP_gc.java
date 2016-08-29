package org.chocosolver.samples.real.bacp.gc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.chocosolver.samples.real.bacp.preprocessing.longestpath.Graph;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;

import gnu.trove.map.hash.THashMap;

public class Prop_BACP_gc extends Propagator<IntVar> {

   Graph g;
   
   //IntVar[] course_period;
   int[][] connectionMatrix;
   int[] distances;
   int n_courses;
   int n_periods;
   
   private int[][] loadPrerequisites(String instance){
      int[][] prerequisiteMatrix = null;
      FileReader fr = null;
      String model = "";
      try {
        fr = new FileReader(new File(instance));
        char[] buffer = new char[1000];
        while(fr.read(buffer) > -1){
              model += new String(buffer);
              buffer = new char[1000];
        }
        fr.close();
        String[] lines = model.split("\n");
        for(int i = 1; i < lines.length; i++){
           if(lines[i].startsWith("n_courses")){
              String[] parts = lines[i].substring(0, lines[i].length()-1).split(" = ");
              n_courses = Integer.parseInt(parts[1]);
              prerequisiteMatrix = new int[n_courses][n_courses];
           } else if(lines[i].startsWith("n_periods")){
              String[] parts = lines[i].substring(0, lines[i].length()-1).split(" = ");
              n_periods = Integer.parseInt(parts[1]);
           } else if(lines[i].startsWith("constraint prerequisite")){
              String[] parts = lines[i].substring(0, lines[i].length()-1).split("prerequisite");
              String prerequisite[] = parts[1].substring(1, parts[1].length()-1).split(", ");
              prerequisiteMatrix[Integer.parseInt(prerequisite[0])-1][Integer.parseInt(prerequisite[1])-1] = 1;
           }
        }
     } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
     } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
     }
      
     return prerequisiteMatrix;
   }
   
   public Prop_BACP_gc(IntVar[] course_period, String instance){
      super(course_period, PropagatorPriority.LINEAR, true);
      //this.course_period = new IntVar[course_period.length];
      //System.arraycopy(course_period, 0, this.course_period, 0, course_period.length);
      this.connectionMatrix = loadPrerequisites(instance);
      this.distances = computeInitialLBs();
   }

   @Override
   public void propagate(int evtmask) throws ContradictionException {
      // TODO Auto-generated method stub
      if (PropagatorEventType.isFullPropagation(evtmask)) {
         updateBounds();
      }
   }
   
   @Override
   public void propagate(int i, int mask) throws ContradictionException {
   // TODO Auto-generated method stub
      if (PropagatorEventType.isFullPropagation(mask)) {
         updateBounds();
      }
   }
   
   private void updateBounds() throws ContradictionException {
      // TODO Auto-generated method stub
      int[] distancesLB = this.computeLBs();
      int[] distancesUB = this.computeUBs();
      for(int k = 0; k < this.vars.length; k++){
         this.vars[k].updateLowerBound(distancesLB[k+1]-1, aCause);
         this.vars[k].updateUpperBound(distancesUB[k+1]-1, aCause);
      }
   }

   private void initialiseOriginalGraph(){
      
      g = new Graph(connectionMatrix.length + 1);
      
      for(int i = 0; i < connectionMatrix.length; i++){
         if(Arrays.stream(connectionMatrix[i]).sum() == 0){
            g.addEdge(0, i+1, 1);
            
         }
         for(int j = 0; j < connectionMatrix.length; j++){
            if(connectionMatrix[i][j] == 1){
               g.addEdge(j+1, i+1, 1);
            }
         }
      }
   }
   
   private int[] computeInitialLBs(){
      initialiseOriginalGraph();
      int source = 0;
      return g.findLongestPath(source);
   }
   
   private void initialiseGraph(){
      
      g = new Graph(connectionMatrix.length + 1);
      
      for(int i = 0; i < connectionMatrix.length; i++){
         if(Arrays.stream(connectionMatrix[i]).sum() == 0){      
            g.addEdge(0, i+1, vars[i].getLB() + 1);
            
         }
         for(int j = 0; j < connectionMatrix.length; j++){
            if(connectionMatrix[i][j] == 1){
               int newDist = (vars[j].getLB() + 1) - this.distances[j+1] + 1; //Incorrect
               g.addEdge(j+1, i+1, 1);
            }
         }
      }
   }
   
   private int[] computeLBs(){
      initialiseGraph();
      int source = 0;
      int[] distances = g.findLongestPath(source);
      return distances;
   }
   
   private int[] computeUBs(){
      int[] dist = new int[n_courses+1];
      for(int i = 1; i < n_courses+1; i++){
         initialiseGraph();
         dist[i] = n_periods - Arrays.stream(g.findLongestPath(i)).max().getAsInt();
      }
      return dist;
   }

   @Override
   public ESat isEntailed() {
      // TODO Auto-generated method stub
      return null;
   }
   
   @Override
   public int getPropagationConditions(int vIdx) {
     return IntEventType.boundAndInst();
   }
   
   @Override
   public String toString() {
       return "not implemented";
   }
   
   @Override
   public void explain(ExplanationEngine xengine, Deduction d, Explanation e) {
       //e.add(xengine.getPropagatorActivation(this));
     throw new SolverException("Constraint duplication not implemented");
   }
   
   @Override
   public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
       throw new SolverException("Constraint duplication not implemented");
   }
}
