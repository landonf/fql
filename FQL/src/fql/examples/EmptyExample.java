package fql.examples;

public class EmptyExample extends Example {
	
	@Override
	public String getName() {
		return "All Syntax";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = 
			"//illustrates all possible FQL declarations"
					+ "\nenum color = {red, green, blue}"
					+ "\n"
					+ "\nschema C = {nodes; attributes; arrows; equations;}"
					+ "\nschema C1 = void"
					+ "\nschema C2 = unit {string, int, color}"
					+ "\nschema C3 = (C + C)"
					+ "\nschema C4 = (C * C)"
					+ "\nschema C5 = (C union C)"
					+ "\nschema C6 = opposite C"
					+ "\nschema C7 = (C ^ C)"
					+ "\n"
					+ "\nmapping F = id C"
					+ "\nmapping F1 = (F then F)"
					+ "\nmapping F2 = {nodes; attributes; arrows;} : C -> C"
					+ "\nmapping F3 = inl C C"
					+ "\nmapping F4 = inr C C"
					+ "\nmapping F5 = (F3 + F4)"
					+ "\nmapping F6 = fst C C"
					+ "\nmapping F7 = snd C C"
					+ "\nmapping F8 = (F6 * F7)"
					+ "\nmapping F9 = void C"
					+ "\nmapping F10= unit {string, int, color} C"
					+ "\nmapping F13 = subschema C C5"
					+ "\nmapping F14 = opposite F"
					+ "\nmapping F11= eval C C"
					+ "\nmapping F12= curry id (C*C)"
					+ "\nmapping F15= iso1 C C"
					+ "\nmapping F16= iso2 C C"
					+ "\n"
					+ "\nquery q = delta F pi F sigma F"
					+ "\nquery p = (q then q)"
					+ "\n//see Schema Matching example for available strings"
					+ "\nQUERY Q1 = delta F"
					+ "\nQUERY Q2 = SIGMA F"
					+ "\nQUERY Q3 = pi F"
					+ "\nQUERY Q4 = match {} C C \"delta sigma forward\""
					+ "\nQUERY Q5 = (Q1 then Q2)"
					+ "\n"
					+ "\ninstance I   = { nodes; attributes; arrows; } : C"
					+ "\ninstance I1  = delta F I"
					+ "\ninstance I2  = pi F I"
					+ "\ninstance I3  = sigma F I"
					+ "\ninstance I4  = relationalize I"
					+ "\ninstance I5  = SIGMA F I"
					+ "\ninstance I8  = (I + I1)"
					+ "\ninstance I10 = (I ^ I)"
					+ "\ninstance I9  = (I10 * I)"
					+ "\ninstance I11 = unit C"
					+ "\ninstance I12 = void C"
					+ "\ninstance I13 = prop C"
					+ "\ninstance I6  = external C name"
					+ "\ninstance I7  = eval q I"
					+ "\ninstance I7x = EVAL Q1 I"
					+ "\n"
					+ "\ntransform t1 = id I"
					+ "\ntransform t2 = (t1 then t1)"
					+ "\ntransform t3 = {nodes;} : I -> I"
					+ "\ntransform t4 = I8.inl"
					+ "\ntransform t5 = I8.inr"
					+ "\ntransform t6 = I8.(t4+t5)"
					+ "\ntransform t7 = I9.fst"
					+ "\ntransform t9 = I9.snd"
					+ "\ntransform t10 = I9.(t7*t9)"
					+ "\ntransform t12 = I11.unit I"
					+ "\ntransform t13 = I12.void I"
					+ "\ntransform t15 = delta I1 I1 id I"
					+ "\ntransform t16 = sigma I3 I3 id I"
					+ "\ntransform t20 = SIGMA I5 I5 t1"
					+ "\ntransform t17 = pi I2 I2 id I"
					+ "\ntransform t18 = relationalize I4 I4 id I"
					+ "\ntransform t19 = I4.relationalize"
					+ "\ntransform t21 = external I6 I6 name"
					+ "\ntransform t22 = I9.eval"
					+ "\ntransform t23 = I10.curry t22"
					+ "\ntransform t24 = iso1 I I"
					+ "\ntransform t25 = iso2 I I"
					+ "\ntransform t26 = I13.true I11"
					+ "\ntransform t27 = I13.false I11"
					+ "\ntransform t28 = I13.chi t26"
					+ "\n"
					+ "\n////(co)monads also work for SIGMA and pi"
					+ "\ninstance I3X = delta F I3"
					+ "\ntransform I3Xa = I3X.return"
					+ "\ninstance I1X = sigma F I1"
					+ "\ntransform I1Xa = I1X.coreturn"
					+ "\n"
					+ "\ndrop I t1"


;
	
	//TODO typed exponentials
	//TODO isos for instances
					


}
