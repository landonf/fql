package fql;

import javax.swing.JSplitPane;

@SuppressWarnings("serial")
/**
 * @author ryan
 * A split panel, but doesn't work well.
 */
public class FQLSplit extends JSplitPane {

	double bias;
	
	public FQLSplit(double bias, int split) {
		super(split);
		this.bias = bias;
		setDividerSize(2);
	}
	
	@Override
	public void setVisible(boolean b) {
		setVisible(b);
		setDividerLocation(bias);
		invalidate();
	}
	
	
}
