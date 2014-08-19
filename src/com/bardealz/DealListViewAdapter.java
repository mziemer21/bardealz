
package com.bardealz;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DealListViewAdapter extends ArrayAdapter<DealRowItem> {

	private Context context;

	public DealListViewAdapter(Context context, int resourceId, List<DealRowItem> items) {
		super(context, resourceId, items);
		this.context = context;
	}

	/* private view holder class */
	private class ViewHolder {
		TextView txtTitle, rating, time, name;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		DealRowItem rowItem = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_item_deal, null);
			holder = new ViewHolder();
			holder.rating = (TextView) convertView.findViewById(R.id.deal_list_rating);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.deal_list_title);
			holder.time = (TextView) convertView.findViewById(R.id.deal_list_time);
			holder.name = (TextView) convertView.findViewById(R.id.deal_list_name);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		holder.rating.setText(rowItem.getRating() + "%");
		holder.txtTitle.setText(rowItem.getTitle());
		holder.time.setText(rowItem.getTime());
		holder.name.setText(rowItem.getName());
		if(!rowItem.getShowName()){
			holder.name.setVisibility(View.GONE);
		}

		return convertView;
	}
}
