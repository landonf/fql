package fql.examples;

public class TransformExample extends Example {

	@Override
	public String getName() {
		return "Transforms";
	}

	@Override
	public String getText() {
		return s;
	}

	String s =

	"schema s = {"

	+ "\n	nodes a, b; "

	+ "\n	attributes att : a -> string; "

	+ "\n	arrows f : b -> a; "

	+ "\n	equations;"

	+ "\n}"

	+ "\n"

	+ "\ninstance i = {"

	+ "\n	nodes a -> {1,2,3}, b -> {4}; "

	+ "\n     attributes att -> {(1,1),(2,2),(3,3)}; "

	+ "\n     arrows f -> {(4,1)};"

	+ "\n} : s"

	+ "\n              "

	+ "\ninstance j = {"

	+ "\n	nodes a -> {a,b}, b -> {}; "

	+ "\n 	attributes att -> {(a,a),(b,b)}; "

	+ "\n 	arrows f -> {};"

	+ "\n } : s"

	+ "\n"

	+ "\ntransform g = id i"

	+ "\n"

	+ "\ninstance ij0 = (i * j)"

	+ "\n"

	+ "\ntransform ij0fst = ij0.fst"

	+ "\n"

	+ "\ntransform ij0snd = ij0.snd"

	+ "\n"

	+ "\ntransform ij00 = ij0.(ij0fst * ij0snd)"

	+ "\n"

	+ "\ninstance ij = (i + j)"

	+ "\n"

	+ "\ntransform ijinl = ij.inl"

	+ "\n"

	+ "\ntransform ijinr = ij.inr"

	+ "\n"

	+ "\ntransform ij0X = ij.(ijinl + ijinr)"

	+ "\n"

	+ "\ninstance II = void s"

	+ "\n"

	+ "\ntransform t1 = II.void j"

	+ "\n"

	+ "\nschema t = {nodes a; attributes; arrows; equations;}"

	+ "\n"

	+ "\ninstance tx = unit t"

	+ "\n"

	+ "\ninstance tj = {nodes a -> {a,b,c}; attributes; arrows;} : t"

	+ "\n"

	+ "\ntransform JJ = tx.unit tj"

	;

	;

}
