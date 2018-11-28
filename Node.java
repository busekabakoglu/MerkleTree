package project;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import util.HashGeneration;

public class Node {
	private String data;
	private Node leftChild;
	private Node rightChild;
	private String fileAddress;
	
	public Node() {
		this.leftChild=null;
		this.rightChild=null;
		this.data = "";
		this.fileAddress="";
	}
	public void setFileAddress(String fileAddress) {
		this.fileAddress = fileAddress;
	}
	public String getData() {
		return this.data;
	}
	
	public Node getLeft() {
		return this.leftChild;
	}
	
	public Node getRight() {
		return this.rightChild;
	}
	
	public void setLeft(Node node) {
		this.leftChild=node;
	}
	
	public void setRight(Node node) {
		this.rightChild=node;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getFileAddress() {
		return this.fileAddress;
	}
	
	
}
