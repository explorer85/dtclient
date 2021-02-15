package ru.dean.dtclient.views;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import ru.dean.dtclient.DTConnection;
import ru.dean.dtclient.MyActivity;
import ru.dean.dtclient.R;
import ru.dean.dtclient.Storage;
import ru.dean.dtclient.adapters.LastDialogsAdapter;
import ru.dean.dtclient.dtstructs.Consts;


public class LastDialogsFragment extends Fragment implements DTConnection.DTConnectionListener {
    private OnFragmentInteractionListener mListener;

    private ListView listViewLastDialogs;
    LastDialogsAdapter adapter;

    public LastDialogsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DTConnection.Inst().addListener(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        DTConnection.Inst().removeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_last_dialogs, container, false);



        listViewLastDialogs = (ListView) v.findViewById(R.id.listViewLastDialogs);
        // создаем адаптер
        adapter = new LastDialogsAdapter(Storage.Inst().contactList);
        listViewLastDialogs.setAdapter(adapter);
        //применяем фильтрацию и сортировку
        adapter.getFilter().filter("");
        listViewLastDialogs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                long UID = adapter.getItem(position).UID;

                ChatFragment fragchat = new ChatFragment();
                Bundle args = new Bundle();
                args.putLong("UID", UID);
                fragchat.setArguments(args);

                getFragmentManager().beginTransaction()
                        .addToBackStack("LAST")
                        .replace(R.id.container, fragchat, "CHAT")
                        .commit();

            }
        });

        //меняем надпись с состоянием подключения на экшн баре
        ((MyActivity)getActivity()).changeConnectionLabel();
        return v;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void emitSignal(String message, Object obj) {
        //применяем фильтрацию и сортировку
        if (message == Consts.ADDTEXTMESSAGE) {
            adapter.getFilter().filter("");
        }
        //меняем состояние подключения
        if (message == Consts.CHANGECLSTATE) {
            adapter.getFilter().filter("");
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String message, Object obj);
    }

}
