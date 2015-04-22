package com.renjj.fundvlaue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MyAdapter extends BaseAdapter {

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//View view = getItem(position);
		View view =null;
		if(convertView!=null){
			view = convertView;
		}else{
			//view = LayoutInflater.from(this).inflater(R.layout.fundvalue_listitem,null);
		}
		
		return null;
	}
	
	@Override
	public int getCount() {

		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

}
