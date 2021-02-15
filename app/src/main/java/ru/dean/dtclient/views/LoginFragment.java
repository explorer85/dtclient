package ru.dean.dtclient.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import ru.dean.dtclient.DTConnection;
import ru.dean.dtclient.R;
import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.dtstructs.DTTextMessage;


public class LoginFragment extends Fragment implements DTConnection.DTConnectionListener {
    private OnFragmentInteractionListener mListener;
    private EditText mLogin;
    private EditText mPassword;
    private Button mButtonLogin;
    private TextView mError;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getActionBar().hide();
        getActivity().setTitle("Авторизация");
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        mLogin = (EditText)v.findViewById(R.id.login);
        mPassword = (EditText) v.findViewById(R.id.password);
        mButtonLogin = (Button) v.findViewById(R.id.buttonLogin);
        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLogin();
            }
        });
        mError = (TextView) v.findViewById(R.id.textViewError);


        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        DTConnection.Inst().addListener(this);

        //авторизуемся автоматически если это указано в настройках
        SharedPreferences sPref = getActivity().getSharedPreferences("dtclientsettings", 0);
        if (sPref.getBoolean("autologin", false)) {
            mLogin.setText(sPref.getString("login", ""));
            mPassword.setText(sPref.getString("password", ""));
            attemptLogin();
        }


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        //показываем клавиатуру
        mLogin.requestFocus();
        InputMethodManager mgr =      (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(mLogin, InputMethodManager.SHOW_IMPLICIT);
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



    public void attemptLogin() {



        // Reset errors.
        mLogin.setError(null);

        String login = mLogin.getText().toString();
        String password = mPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid login
        if (TextUtils.isEmpty(login)) {
            mLogin.setError("Введите логин");
            focusView = mLogin;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {


            mButtonLogin.setEnabled(false);
            mLogin.setEnabled(false);
            mPassword.setEnabled(false);
            mError.setText("");
            DTConnection.Inst().login(login, password);

        }

    }

    @Override
    public void emitSignal(String message, Object obj) {

        if (message == "01") {
            mError.setText("Нет соединения с сервером");
        }
        if (message == "02") {
            mError.setText("Неизвестный абонент");
        }
        if (message == "03") {
            mError.setText("Неверный пароль");
        }
        if (message == "04") {
            mError.setText("Абонент уже подключен");
        }

        if (message == Consts.USERCONNECTED) {

           //убираем клавиатуру
           InputMethodManager mgr =      (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
           mgr.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

            mListener.onFragmentInteraction("1", new Object());

        }

        mButtonLogin.setEnabled(true);
        mLogin.setEnabled(true);
        mPassword.setEnabled(true);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String message, Object obj);
    }
}

