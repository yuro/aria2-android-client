package tk.igeek.aria2.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tk.igeek.aria2.BitTorrent;
import tk.igeek.aria2.Status;
import tk.igeek.aria2.android.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TrackersFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_trackers, container, false);
		
		Bundle extras = getActivity().getIntent().getExtras();
		Status status = null;
		if (extras != null) {
			status = extras.getParcelable("downloadItemInfo");
		}
		
		if (status != null) {
			ListView listview = (ListView) rootView.findViewById(R.id.download_item_info_tracker_list_view);
			
			BitTorrent bitTorrent = status.getBittorrent();
			if(bitTorrent.isHaveSetData() == true)
			{
				final ArrayList<String> list = bitTorrent.getAnnounceList();
				final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(),android.R.layout.simple_list_item_1, list);
				listview.setAdapter(adapter);
				listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, final View view,
							int position, long id) {
						
					}

				});
			}
			
			
		}
		
		return rootView;
	}
	
	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}
}
