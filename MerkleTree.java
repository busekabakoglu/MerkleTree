package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

import util.HashGeneration;

public class MerkleTree{
	private String thisPath;
	private List<String> addressList = new ArrayList<String>();
	private Node root;
	private Queue<Node> nodes = new LinkedList<Node>();

	public MerkleTree(String path){
		this.thisPath = path;
		try {
			this.addressList = readFile(path);
			this.nodes = addLeafNodesToQ();
			add();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
		
	}

	public MerkleTree() {
		this.root = null;
	}
	
	public List<String> readFile(String path) throws FileNotFoundException {
		File f = new File(path);
		if(f.exists()) {
			Scanner input = new Scanner(f);
			while(input.hasNext()) {
				addressList.add(input.nextLine());
			}
		}
		return addressList;
	}

	public Node constructTree(String path) throws NoSuchAlgorithmException, IOException {
		Queue<String> q = new LinkedList<String>();
		File file = new File(path);
		Scanner input = new Scanner(file);
		while(input.hasNext()) {
			q.add(input.nextLine());
		}
		MerkleTree newTree = new MerkleTree(this.thisPath);
		Queue<Node> temp1 = new LinkedList<Node>();
		Queue<Node> temp2 = new LinkedList<Node>();
		temp1.add(newTree.root);
		while(!q.isEmpty()) {
			while(!temp1.isEmpty()) {
				Node node = temp1.remove();
				if(node.getLeft()!=null) {
					temp2.add(node.getLeft());
				}
				if(node.getRight()!=null) {
					temp2.add(node.getRight());
				}
				node.setData(q.remove());
			}
			temp1.addAll(temp2);
			temp2.clear();
		}
		return newTree.root;

	}

	public ArrayList<Stack<String>> findCorruptChunks(String path){
		ArrayList<Stack<String>> list = new ArrayList<Stack<String>>();
		if(checkAuthenticity(path)==false) {
			MerkleTree checkingTree = new MerkleTree();
			try {
				checkingTree.root = constructTree(path);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Stack<String> s = new Stack<String>();
			list.add(s);
			list.get(0).push(this.root.getData());		
			return findCorruptChunks(list,this.root,checkingTree.root,s );
		}
		else {
			return list;
		}
	}

	public  ArrayList<Stack<String>> findCorruptChunks(ArrayList<Stack<String>> list, Node thisRoot,Node otherRoot,Stack<String> s){
		if(thisRoot.getLeft() == null && thisRoot.getRight() == null) {
			return list;
		}
		else {
			if(thisRoot.getRight()!=null && !thisRoot.getLeft().getData().equals(otherRoot.getLeft().getData()) 
					&& thisRoot.getRight().getData().equals(otherRoot.getRight().getData())) {
				s.push(thisRoot.getLeft().getData());
				findCorruptChunks(list,thisRoot.getLeft(),otherRoot.getLeft(),s);
			}
			else if(thisRoot.getRight()!=null && !thisRoot.getRight().getData().equals(otherRoot.getRight().getData())
					&& thisRoot.getLeft().getData().equals(otherRoot.getLeft().getData())) {
				s.push(thisRoot.getRight().getData());
				findCorruptChunks(list, thisRoot.getRight(), otherRoot.getRight(), s);
			}
			else {
				Stack<String> newStack = new Stack<String>();
				list.add(newStack);
				newStack.addAll(s);
				if(thisRoot.getLeft()!=null) {
					s.push(thisRoot.getLeft().getData());
				}
				if(thisRoot.getRight()!=null) {
					newStack.push(thisRoot.getRight().getData());
				}
				if(thisRoot.getLeft()!=null) {
				findCorruptChunks(list, thisRoot.getLeft(), otherRoot.getLeft(), s);
				}
				if(thisRoot.getRight()!=null) {
				findCorruptChunks(list, thisRoot.getRight(), otherRoot.getRight(), newStack);
				}
			}

		}
		return list;
	}

	public boolean checkAuthenticity(String path){
		File file = new File(path);
		Scanner input=null;
		try {
			input = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		List<String> untrustedHashes = new ArrayList<String>();
		while(input.hasNext()) {
			untrustedHashes.add(input.nextLine());
		}

		if(root.getData().equals(untrustedHashes.get(0))){
			return true;
		}
		else return false;
	}

	public Queue<Node> addLeafNodesToQ() throws NoSuchAlgorithmException, IOException{
		for(int i = 0; i<addressList.size() ; i++) {
			Node node = new Node();
			node.setFileAddress(addressList.get(i));
			node.setData(HashGeneration.generateSHA256(createTheFile(addressList.get(i))));
			this.nodes.add(node);
		}
		return nodes;

	}
	
	public void add() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		Queue<Node> copyNodes = new LinkedList<Node>(this.nodes);
		add(copyNodes);
	}
	public void add(Queue<Node> copyNodes) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		if(copyNodes.size()==1) {
			root = copyNodes.poll();
		}
		else {
			Queue<Node> temp = new LinkedList<Node>();
			while(!copyNodes.isEmpty()) {
				Node newNode = new Node();
				String s1 = copyNodes.peek().getData();
				String s2 = "";
				newNode.setLeft(copyNodes.poll());
				if(!copyNodes.isEmpty()) {
					s2 = copyNodes.peek().getData();
					newNode.setRight(copyNodes.poll());
				}
				newNode.setData(HashGeneration.generateSHA256(s1+s2));
				temp.add(newNode);
			}
			add(temp);
		}
	}

	public Node getRoot() {
		return this.root;
	}
	

	public File createTheFile(String path) {
		File file = new File(path);
		return file;
	}

	public ArrayList<Node> findTheWrongFiles(ArrayList<String> wrongHashes) {
		ArrayList<Node> list = new ArrayList<Node>();
		Node tempNode;
		int size = this.nodes.size();
		for(int a = 0 ; a<size ; a++) {
			tempNode = this.nodes.remove();
			for(int i = 0; i<wrongHashes.size(); i++) {
				if(tempNode.getData().equals(wrongHashes.get(i))) {
					list.add(tempNode);
				}
				this.nodes.add(tempNode);
			}
		}
		return list;
	}
	public void changeWrongNodes(ArrayList<Node> wrongNodes, int x) throws MalformedURLException, IOException {
		String lastTwo = "";
		String temp = "";
		for(Node n : wrongNodes) {
			lastTwo = n.getFileAddress().substring(n.getFileAddress().length()-2);
			File f = new File("secondaryPart/data/"+(x+3)+"alt.txt");
			Scanner input  = new Scanner(f);			
			while(input.hasNext()) {
				temp = input.nextLine();
				if(temp.substring(temp.length()-2).equals(lastTwo)){
					Files.copy(new URL(temp).openStream(), Paths.get("secondaryPart/data/split/"+(x+3)+"/"+lastTwo), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}
}
