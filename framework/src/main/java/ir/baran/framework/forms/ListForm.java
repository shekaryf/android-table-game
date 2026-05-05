package ir.baran.framework.forms;

import ir.baran.framework.components.ListFormAdapter;
import ir.baran.framework.components.ListFormItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public abstract class ListForm extends Form implements OnItemClickListener {

	protected ListView listView;
	public ListFormItem _SelectedListFormItem;

	@Override
	public void initContent(LinearLayout llContent) {
		this.listView = new ListView(this);
		LinearLayout.LayoutParams lpList = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		listView.setLayoutParams(lpList);
		listView.setScrollingCacheEnabled(false);
		listView.setAdapter(initAdapter());
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setVerticalScrollBarEnabled(true);
		this.listView.setOnItemClickListener(this);
		llContent.addView(listView);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		ListFormAdapter adapter = (ListFormAdapter) listView.getAdapter();
		this._SelectedListFormItem = (ListFormItem) adapter.getItem(position);
		onItemSelected(_SelectedListFormItem, position);
	}

	protected abstract void onItemSelected(ListFormItem selectedItem,
			int position);

	protected abstract ListFormAdapter initAdapter();
}
