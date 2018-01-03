package com.example.user.project2;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private EditText loginId;
    private EditText loginPW;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_login, container, false);

        loginId = (EditText) v.findViewById(R.id.loginId);
        loginPW = (EditText) v.findViewById(R.id.loginPW);



        Button loginBtn = (Button) v.findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = loginId.getText().toString();
                String password = loginPW.getText().toString();

                JSONTaskContacts connection = new JSONTaskContacts(id, password);
                connection.execute("http://13.125.74.215:8080/login");
            }
        });

        Button signUpBtn = (Button) v.findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignFragment signFragment = new SignFragment();

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack("Login");
                transaction.replace(R.id.fragment_container, signFragment);
                transaction.commit();
            }
        });


        return v;
    }

    public class JSONTaskContacts extends AsyncTask<String, String, String> {
        private String mUsername;
        private String mPassword;

        public JSONTaskContacts(String id, String password) {
            this.mUsername = id;
            this.mPassword = password;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("username", mUsername);
                    jsonObject.accumulate("password", mPassword);

                    Log.d(TAG, "doInBackground: " + jsonObject);

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject reply = new JSONObject(result);
                int responseCode = reply.getInt("result");

                if (responseCode == 1) {
                    MovieListFragment movieListFragment = new MovieListFragment();

                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, movieListFragment);
                    transaction.commit();

                } else {
                    loginPW.setText("");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("올바르지 않은 ID / Password 입니다.");
                    builder.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //서버로 부터 받은 값을 출력해주는 부분
        }
    }
}