package ir.baran.framework.forms;

import ir.baran.framework.components.ListFormAdapter;
import ir.baran.framework.components.ListFormItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

public abstract class GridForm extends Form implements OnItemClickListener {

	protected GridView _GridView;
	public ListFormItem _SelectedGridItem;
	protected int _NumColumns = 3;

	@Override
	public void initContent(LinearLayout llContent) {
		this._GridView = new GridView(this);
		LinearLayout.LayoutParams lpList = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		_GridView.setLayoutParams(lpList);
		_GridView.setNumColumns(this._NumColumns);
		_GridView.setScrollingCacheEnabled(false);
		_GridView.setAdapter(initAdapter());
		_GridView.setVerticalScrollBarEnabled(true);
		this._GridView.setOnItemClickListener(this);
		llContent.addView(_GridView);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		ListFormAdapter adapter = (ListFormAdapter) _GridView.getAdapter();
		this._SelectedGridItem = (ListFormItem) adapter.getItem(position);
		onItemSelected(_SelectedGridItem, position);
	}

	protected abstract void onItemSelected(ListFormItem selectedItem,
			int position);

	protected abstract ListFormAdapter initAdapter();
}
