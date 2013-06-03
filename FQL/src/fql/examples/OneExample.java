package fql.examples;

import fql.examples.Example;

public class OneExample extends Example {

	@Override
	public String getName() {
		return "One";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = 
	"schema C = { arr : A -> A ;  }" +
	"\n" + 
	"\nmapping F : C -> C = {" + 
	"\n A -> A" +
	"\n;" +
	"\n arr -> A.arr" +
	"\n}" + 
	"\n" + 
	"\ninstance I : C = {" +
	"\n A = {(x,x),(y,y)};" +
	"\n arr = {(x,y),(y,x)}" +
	"}\n";
}
