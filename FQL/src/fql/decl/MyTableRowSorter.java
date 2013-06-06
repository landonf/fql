package fql.decl;

import java.util.Comparator;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class MyTableRowSorter extends TableRowSorter<TableModel> {

	public MyTableRowSorter(TableModel model) {
		super(model);
	}

	@Override
	protected boolean useToString(int c) {
		return false;
	}
	
	@Override 
	public Comparator getComparator(int c) {
		return new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof Integer && o2 instanceof Integer) {
		//			System.out.println("XXXX");
					return ((Integer)o1).compareTo((Integer)o2);
				}
			//	System.out.println("BBBB " + o1 + " and " + o2);
				return o1.toString().compareTo(o2.toString());
			}
			
		};
	}
}
