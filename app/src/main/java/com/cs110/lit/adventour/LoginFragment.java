package com.cs110.lit.adventour;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.LoginFragmentListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private LoginFragmentListener mListener;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param activity the activity rendering this login fragment.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(LoginFragmentListener activity) {
        LoginFragment fragment = new LoginFragment();
        fragment.setMListener(activity);
        return fragment;
    }

    public void setMListener(LoginFragmentListener mListener) {
        this.mListener = mListener;
    }

    public LoginFragmentListener getMListener() {
        return mListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.login_fragment, container, false);

        Button signInBtn = (Button) v.findViewById(R.id.email_sign_in_button);
        Button registerBtn = (Button) v.findViewById(R.id.email_create_account_button);
        final EditText emailInput = (EditText) v.findViewById(R.id.email);
        final EditText passInput = (EditText) v.findViewById(R.id.password);

//        Typeface t = Typeface.createFromAsset(getAssets(), "fonts/BerninoSansCondensedEB.ttf");
//        signInBtn.setTypeface(t);
//        registerBtn.setTypeface(t);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSignInClicked(emailInput, passInput);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onRegisterClicked();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginFragmentListener) {
            mListener = (LoginFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface LoginFragmentListener {
        // TODO: Update argument type and name
        void onRegisterClicked();
        void onSignInClicked(EditText mEmailView, EditText mPasswordView);
    }
}
