package ru.dean.dtclient.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ru.dean.dtclient.R;
import ru.dean.dtclient.Storage;
import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.dtstructs.DTUser;
import ru.dean.dtclient.utils.Utils;

public class ContactListAdapter extends BaseAdapter implements Filterable {
    ArrayList<DTUser> filteredmData = new ArrayList<DTUser>();
    private HashMap<Long, DTUser> mData;
    private String[] mKeys;

    public ContactListAdapter(HashMap<Long, DTUser> map) {
        mData = map;

    }


    @Override
    public int getCount() {
        return filteredmData.size();
    }

    @Override
    public DTUser getItem(int position) {
        return filteredmData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        final View view;

        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
        } else {
            view = convertView;
        }

        DTUser u = getItem(pos);

        // TODO replace findViewById by ViewHolder

        String text = "";
        if (Utils.isLikeUser(u)) {
            text = u.Family + " " + u.Name + " ";
        } else   {
            text = u.NickName;
        }

        ((TextView) view.findViewById(R.id.tvLogin)).setText(text);

        ImageView iconStatus = (ImageView) view.findViewById(R.id.iconStatus);

        if (u.Active)
            iconStatus.setImageResource(R.drawable.online);
        else
            iconStatus.setImageResource(R.drawable.offline);

        return view;
    }




    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                filteredmData = (ArrayList<DTUser>) results.values;

                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<DTUser> FilteredArrList=new ArrayList<DTUser>();

                constraint = constraint.toString().toLowerCase();
                for(Map.Entry<Long, DTUser> entry : mData.entrySet()) {
                    DTUser u = entry.getValue();

                    if (Utils.isLikeUser(u)  && constraint == "contacts") {
                        FilteredArrList.add(u);
                    }
                     if (!Utils.isLikeUser(u) && constraint == "conference")  {
                        FilteredArrList.add(u);
                    }


                }

                results.count = FilteredArrList.size();
                results.values = FilteredArrList;

                return results;
            }
        };
        return filter;
    }



}