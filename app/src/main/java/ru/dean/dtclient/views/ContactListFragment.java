package ru.dean.dtclient.views;

import android.app.Activity;
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
import ru.dean.dtclient.adapters.ContactListAdapter;
import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.views.ChatFragment;


public class ContactListFragment extends Fragment implements DTConnection.DTConnectionListener  {
    private OnFragmentInteractionListener mListener;

    private ListView listViewContacts;
    ContactListAdapter adapter;


    public ContactListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DTConnection.Inst().addListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_list, container, false);



        listViewContacts = (ListView) v.findViewById(R.id.listViewContacts);
        // создаем адаптер
        adapter = new ContactListAdapter(Storage.Inst().contactList);
        listViewContacts.setAdapter(adapter);
        //применяем фильтрацию и сортировку
        adapter.getFilter().filter("contacts");

        listViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

//                Intent intent = new Intent(getActivity(), ChatActivity.class);
                long UID = adapter.getItem(position).UID;
//                intent.putExtra("UID", UID);
//                startActivity(intent);

                //выбираем таб с последними разговорами
                getActivity().getActionBar().setSelectedNavigationItem(1);



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
        DTConnection.Inst().removeListener(this);
    }


    @Override
    public void emitSignal(String message, Object obj) {


        if (message == Consts.CHANGECLSTATE) {
            adapter.getFilter().filter("contacts");
        }
        else {
        }
    }




    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String message, Object obj);
    }

}
