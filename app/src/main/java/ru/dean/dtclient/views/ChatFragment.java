package ru.dean.dtclient.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import ru.dean.dtclient.DTConnection;
import ru.dean.dtclient.R;
import ru.dean.dtclient.Storage;
import ru.dean.dtclient.adapters.ChatAdapter;
import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.dtstructs.DTTextMessage;
import ru.dean.dtclient.dtstructs.DTUser;
import ru.dean.dtclient.threads.HistoryThread;
import ru.dean.dtclient.utils.Utils;


public class ChatFragment extends Fragment implements DTConnection.DTConnectionListener, SwipeRefreshLayout.OnRefreshListener {

    private OnFragmentInteractionListener mListener;

    ListView lvChat;
    Button btnSend;
    Button btnSendFile;
    EditText et;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DTUser u;
    ChatAdapter adapter;
    Handler h;
    boolean historyThreadIsRunning = false;


    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Long UID = getArguments().getLong("UID");
        Storage.Inst().currentChatUserUID = UID;
        u = Storage.Inst().contactList.get(UID);

        DTConnection.Inst().addListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        lvChat = (ListView) v.findViewById(R.id.listViewChat);
        et = (EditText) v.findViewById(R.id.editTextMessage);
        btnSend = (Button) v.findViewById(R.id.buttonSend);
        btnSendFile = (Button) v.findViewById(R.id.buttonSendFile);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh);
        //mSwipeRefreshLayout.setColorScheme(R.blue, R.color.green, R.color.yellow, R.color.red);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        if (u != null) {
            btnSend.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String str = et.getText().toString();
                    et.setText("");
                    DTConnection.Inst().sendMessage(str, u.UID);
                    Storage.Inst().addUserToLast(u.UID);

                }
            });


            btnSendFile.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    SendFileDialogFragment sfdialog = new SendFileDialogFragment();
                    sfdialog.show(getFragmentManager(), "dlg1");

                }
            });

            //устанавливаем заголовок
            String text = "";
            if (Utils.isLikeUser(u)) {
                text = u.Family + " " + u.Name + " (" + u.UID + ")";
            }
             else {
                text = u.NickName + " " + " (" + u.UID + ")";
            }
            getActivity().setTitle(text);


            adapter = new ChatAdapter(Storage.Inst().messageList);
            lvChat.setAdapter(adapter);
            adapter.getFilter().filter(String.valueOf(u.UID));




            h = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    switch (msg.what) {
                        case 1:
                            ArrayList<DTTextMessage> histmessages = (ArrayList<DTTextMessage>) msg.obj;
                            if (histmessages != null) {
                                if (histmessages.size() > 0) {
                                    Storage.Inst().messageList.addAll(histmessages);
                                    adapter.getFilter().filter(String.valueOf(u.UID));

                                }
                            }
                            //выключаем индикацию загрузки в layoute
                            mSwipeRefreshLayout.setRefreshing(false);
                            historyThreadIsRunning = false;


                            break;
                    }
                }
            };

            //если не запущена загрузка истории для текущего чата и еще не было загружены ни одного сообщения для текущего чата
            if (!historyThreadIsRunning && Storage.Inst().contactList.get(u.UID).FirstMessageID == 0) {
                historyThreadIsRunning = true;
                HistoryThread hist = new HistoryThread(h, u.UID, Storage.Inst().contactList.get(u.UID).FirstMessageID, 3);
            }


        }

        return v;
    }



    @Override
    public void onResume() {
        super.onResume();
        et.requestFocus();
        InputMethodManager mgr =      (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
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
    public void onDestroy() {
        super.onDestroy();
        DTConnection.Inst().removeListener(this);
        Storage.Inst().currentChatUserUID = 0;
    }

    @Override
    public void emitSignal(String message, Object obj) {
        if (message == Consts.ADDTEXTMESSAGE) {
            adapter.getFilter().filter(String.valueOf(u.UID));
        }



    }

    @Override
    public void onRefresh() {
        //включаем индикацию загрузки в layoute
        if (!historyThreadIsRunning) {
            historyThreadIsRunning = true;
            HistoryThread hist = new HistoryThread(h, u.UID, Storage.Inst().contactList.get(u.UID).FirstMessageID, 30);
        }

        mSwipeRefreshLayout.setRefreshing(true);
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String message, Object obj);
    }




}
