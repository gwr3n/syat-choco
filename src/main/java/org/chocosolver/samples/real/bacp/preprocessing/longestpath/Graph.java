package org.chocosolver.samples.real.bacp.preprocessing.longestpath;

public class Graph {
   private int V;
   private int[][] matrix;
   private int[] vertices;
   private boolean[] visited;
   private int[] distances;
   private int[] predecessor;
   private Stack stack;

   public Graph(int V) {
      this.V = V;
      vertices = new int[V];
      visited = new boolean[V];
      predecessor = new int[V];
      distances = new int[V];
      matrix = new int[V][V];
      stack = new Stack(V);
      for (int i = 0; i < V; i++) {
         addVertex(i);
         distances[i] = Integer.MIN_VALUE;
         predecessor[i] = -1;
      }
   }

   private void addVertex(int name) {
      vertices[name] = name;
   }

   public void addEdge(int source, int destination, int weight) {
      matrix[source][destination] = weight;
   }

   public int[] findLongestPath(int source) {
      invokeTopologicalSort();
      distances[source] = 0; // Initialize source with 0
      updateMaxDistanceForAllAdjVertices(); // for all nodes connected,
      // directly or indirectly,
      // with source will have
      // their distances
      // calculated
      //printDistances(source);
      //printPath(source);
      return distances;
   }

   @SuppressWarnings("unused")
   private void printDistances(int source) {
      System.out.println("Distances from source " + source + " are as follows: ");
      for (int to = 0; to < V; to++) {
         int distance = distances[to];
         System.out.print("from " + source + " to " + to + ": ");
         if (distance == Integer.MIN_VALUE) {
            System.out.println(" -Infinity ");
         } else {
            System.out.println(distance + " ");
         }
      }
      System.out.println();
   }

   @SuppressWarnings("unused")
   private void printPath(int source) {
      System.out.println("Path from source " + source + " to other nodes are as follows: ");
      for (int i = 0; i < V; i++) {
         if (distances[i] == Integer.MIN_VALUE) {
            System.out.println("No Path from " + source + " to " + i);
         } else if (i != source) {
            int from = predecessor[i];
            System.out.print("Path from " + source + " to " + i + ": ");
            if (from == source) {
               System.out.print(from + " ");
            }
            while (from != source) {
               System.out.print(from + " ");
               from = predecessor[from];
            }
            System.out.print(i + " ");
            System.out.println();
         }
      }
   }

   private void updateMaxDistanceForAllAdjVertices() {
      while (!stack.isEmpty()) {
         int from = stack.pop();
         if (distances[from] != Integer.MIN_VALUE) {
            for (int adjacent = 0; adjacent < V; adjacent++) {
               if (matrix[from][adjacent] != 0) {
                  if (distances[adjacent] < distances[from] + matrix[from][adjacent]) {
                     predecessor[adjacent] = from;
                     distances[adjacent] = distances[from] + matrix[from][adjacent];
                  }
               }
            }
         }
      }
   }

   private void invokeTopologicalSort() {
      for (int i = 0; i < V; i++) {
         if (!visited[i]) {
            dfs(i);
         }
      }
   }

   private void dfs(int source) {
      visited[source] = true;
      for (int adjacent = 0; adjacent < V; adjacent++) {
         if (matrix[source][adjacent] != 0 && !visited[adjacent]) {
            dfs(adjacent);
         }
      }
      stack.push(source);
   }

}