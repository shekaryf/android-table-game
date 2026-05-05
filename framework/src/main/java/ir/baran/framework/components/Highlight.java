package ir.baran.framework.components;

public class Highlight {
	public Highlight(int from, int to, int color) {
		_From = from;
		_To = to;
		_Color = color;
	}

	public int _From;
	public int _To;
	public int _Color;
	public boolean _Bold = false;
}
