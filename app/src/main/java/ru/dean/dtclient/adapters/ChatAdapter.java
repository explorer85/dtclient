package ru.dean.dtclient.adapters;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.dean.dtclient.R;
import ru.dean.dtclient.Storage;
import ru.dean.dtclient.dtstructs.DTTextMessage;
import ru.dean.dtclient.dtstructs.DTUser;

/**
 * Created by explorer on 07.10.2014.
 */
public class ChatAdapter extends BaseAdapter implements Filterable {
    ArrayList<DTTextMessage> filteredmData = new ArrayList<DTTextMessage>();
    ArrayList<DTTextMessage> mData;

    public ChatAdapter(ArrayList<DTTextMessage> messageList) {
        mData = messageList;

    }

    @Override
    public int getCount() {
        return filteredmData.size();
    }

    @Override
    public DTTextMessage getItem(int position) {
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
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        } else {
            view = convertView;
        }

        DTTextMessage m = getItem(pos);


        DTUser author;
        String author_name = "";
        if (!m.author.equals(m.sender)) { //если author != sender значит сообщение из конференции
            if (!m.sender.equals(Storage.Inst().user.UID)) {  //если sender не текущий пользователь
                if (Storage.Inst().contactList.containsKey(m.author)) {
                    author = Storage.Inst().contactList.get(m.author);
                    author_name = author.Family + " " + author.Name + " ";
                }
            }
        }

        // TODO replace findViewById by ViewHolder
        TextView tv =  ((TextView) view.findViewById(R.id.comment));
        LinearLayout lin =  ((LinearLayout) view.findViewById(R.id.wrapper));
        tv.setText((new SimpleDateFormat("[HH:mm:ss]")).format(m.datetime) +  " " + author_name  + m.Body);

        //отрисовка даты для каждого элемента перед которым сообщение с отличной датой
        TextView tv_date =  ((TextView) view.findViewById(R.id.tv_date));
        tv_date.setText("");
        tv_date.setVisibility(View.GONE);

        if (pos!=0) {
            Date d1 = m.datetime;
            Date d2 = getItem(pos - 1).datetime;
            if (d1.getDate() != d2.getDate()) {
                tv_date.setText(new SimpleDateFormat("dd:MM:yy").format(d1));
                tv_date.setVisibility(View.VISIBLE);
            }
        } else {
            tv_date.setText(new SimpleDateFormat("dd:MM:yy").format(m.datetime));
            tv_date.setVisibility(View.VISIBLE);
        }



        if (m.state == 0)
            tv.setTextColor(Color.RED);
        if (m.state == 1)
            tv.setTextColor(Color.BLACK);



        tv.setBackgroundResource(m.sender.equals(Storage.Inst().user.UID) ? R.drawable.bubble_yellow : R.drawable.bubble_green);
        lin.setGravity(m.sender.equals(Storage.Inst().user.UID) ? Gravity.LEFT : Gravity.RIGHT);


        return view;
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                filteredmData = (ArrayList<DTTextMessage>) results.values;

                // сортируем список сообщений по дате
                Collections.sort(filteredmData, new Comparator<DTTextMessage>() {
                    public int compare(DTTextMessage o1, DTTextMessage o2) {
                        int i = o1.datetime.compareTo(o2.datetime);
                        return i;
                    }
                });

                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<DTTextMessage> FilteredArrList=new ArrayList<DTTextMessage>();

                if (constraint == null || constraint.length() == 0) {
                    results.count = mData.size();
                    results.values = mData;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mData.size(); i++) {
                        DTTextMessage m = mData.get(i);

                        //если в конкретном элементе оригинальных значений присутствует искомый набор символов, то добавляем этот эл-т в FilteredArrList
                        if (m.sender.equals(Storage.Inst().user.UID)) {
                            if (m.receiver.equals(Long.parseLong(constraint.toString()))) {
                                FilteredArrList.add(m);
                            }
                        }
                        else {
                            if (m.sender == Long.parseLong(constraint.toString())) {
                                FilteredArrList.add(m);
                            }
                        }
                    }
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}
