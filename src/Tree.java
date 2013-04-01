import java.util.*;

public class Tree<T>{
	private Node<T> root;
	
	public Tree(T input){
		root = new Node<T>();
		root.data = input;
		root.children = new ArrayList<Node<T>>();
	}
	
	public Node<T> getRoot(){
		return root;
	}
	
	public void erase(){
		root.children.clear();
	}
	
	public class Node<T>{
		private T data;
		private Node<T> parent;
		private ArrayList<Node<T>> children;
		
		public T getData(){
			return data;
		}
		
		public ArrayList<Node<T>> getChildren(){
			return children;
		}
		
		public void add(T input){
			Node<T> newNode = new Node<T>();
			newNode.data = input;
			newNode.children = new ArrayList<Node<T>>();
			children.add(newNode);
		}
	}
}