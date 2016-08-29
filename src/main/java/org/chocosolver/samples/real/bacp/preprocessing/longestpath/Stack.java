package org.chocosolver.samples.real.bacp.preprocessing.longestpath;

public class Stack {
   private int maxSize;
   private int[] stack;
   private int top = -1;
   private int size = 0;

   public Stack(int maxSize) {
      this.maxSize = maxSize;
      stack = new int[this.maxSize];
   }

   public void push(int item) {
      stack[++top] = item;
      size++;
   }

   public int pop() {
      int item = stack[top--];
      size--;
      return item;
   }

   public boolean isEmpty() {
      return size == 0;
   }
}