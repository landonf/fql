package fql.examples;

public class EmptyExample extends Example {
	
	//TODO add drop to FQL syntax highlighting

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
					+ "\nenum color = {red, green, blue}\n"
					+ "\nschema C = {nodes; attributes; arrows; equations;}"
					+ "\nschema C1 = void"
					+ "\nschema C2 = unit"
					+ "\nschema C3 = (C + C)"
					+ "\nschema C4 = (C * C)"
					+ "\n//schema C4 = (C ^ C)"
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
					+ "\nmapping F10= unit C"
					+ "\n//mapping F11= eval C C"
					+ "\n//mapping F12= curry id (C*C)"
					+ "\n"
					+ "\nquery q = delta F pi F sigma F"
					+ "\nquery p = (q then q)"
					+ "\n"
					+ "\ninstance I  = { nodes; attributes; arrows; } : C"
					+ "\ninstance I1  = delta F I"
					+ "\ninstance I2  = pi F I"
					+ "\ninstance I3  = sigma F I"
					+ "\ninstance I4  = relationalize I"
					+ "\ninstance I5  = SIGMA F I"
					+ "\ninstance I8  = (I + I)"
					+ "\ninstance I9 = (I * I)"
					+ "\n//instance I10 = (I ^ I)"
					+ "\ninstance I11 = unit C"
					+ "\ninstance I12 = void C"
					+ "\n//instance I3 = prop C"
					+ "\ninstance I6  = external C name"
					+ "\ninstance I7  = eval q I"
					+ "\n"
					+ "\ntransform t1 = id I"
					+ "\ntransform t2 = (t1 then t1)"
					+ "\ntransform t3 = {nodes;} : I -> I"
					+ "\ntransform t4 = I8.inl"
					+ "\ntransform t5 = I8.inr"
					+ "\ntransform t6 = I8.(t4+t5)"
					+ "\ntransform t7= I9.fst"
					+ "\ntransform t9= I9.snd"
					+ "\ntransform t10 = I9.(t7*t9)"
					+ "\ntransform t12 = I11.unit I"
					+ "\ntransform t13 = I12.void I"
					+ "\n//transform t14 = I8.(t4^t5)"
					+ "\n//transform curry"
					+ "\n//transform eval"
					+ "\n//transforms t15 = delta I1 I1 (id I1)"
					+ "\n//transforms t16 = sigma I3 I3 (id I3)"
					+ "\n//transforms t17 = pi I2 I2 (id I2)"
					+ "\n//transforms t18 = relationalize I1 I1 (id I1)"
					
					+ "\n\ndrop I t1\n\n";
					


}
