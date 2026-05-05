package ir.baran.framework.components;

import java.util.Vector;

public class ListFormItem {
	public int _Id;
	public String _Text;
	public Vector<Highlight> _Highlights = new Vector<Highlight>();
	public String _bitmapBase64 = "";
	public String _bitmapPath = "";
	public int _bitmapResource = 0;
	public Object _tag;

}
