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

	+ "\ntransform h = {"

	+ "\n	nodes a -> {(a,1),(b,2)}, b -> {};"

	+ "\n} : j -> i"

	+ "\n"

	+ "\ntransform f = {"

	+ "\n	nodes a -> {(1,1),(2,1),(3,1)}, b -> {(4,4)};	"

	+ "\n} : i -> i"

	+ "\n"

	+ "\ntransform g = id i"

	+ "\n"

	+ "\ntransform k = (h then f)"

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

	+ "\ninstance I = void s"

	+ "\n"

	+ "\ntransform t1 = I.void j"

	+ "\n"

	+ "\nschema t = {nodes a; attributes; arrows; equations;}"

	+ "\n"

	+ "\ninstance tx = unit t"

	+ "\n"

	+ "\ninstance tj = {nodes a -> {a,b,c}; attributes; arrows;} : t"

	+ "\n"

	+ "\ntransform J = tx.unit tj"

	;

	;

}
