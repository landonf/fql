package fql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

public class Denotation {
	
	Signature sig;
	int levels;
	
	Set<Path> nodes;
	Map<Path, Path> blackEdges;
	Map<Path, Path> orangeEdges;
	Map<Path, Path> blueEdges;
	
	Set<Pair<Path, Path>> eqs;
	
	public Denotation(Signature sig, int levels) throws FQLException {
		this.sig = sig;
		this.levels = levels;
		
		nodes = new HashSet<>();
		for (Node n : sig.nodes) {
			List<String> l = new LinkedList<>();
			l.add(n.string);
			nodes.add(new Path(sig, l));
		}
		
		blackEdges = new HashMap<>();
		orangeEdges = new HashMap<>();
		blueEdges = new HashMap<>();
		
		for (int i = 0; i < levels; i++) {
			inc();
		}
	}
	
	private void inc() {
		// TODO Auto-generated method stub
		
	}

	public JPanel view() {
		return null;
	}

}
