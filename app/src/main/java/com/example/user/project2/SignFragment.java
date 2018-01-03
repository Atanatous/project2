package com.example.user.project2;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
 * Created by user on 2018-01-03.
 */

public class SignFragment extends Fragment {
    private Button commitBtn;
    private Button idCheckBtn;
    private EditText inputID;
    private EditText inputPW;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_signup, container, false);

        inputID = (EditText) v.findViewById(R.id.inputID);
        inputPW = (EditText) v.findViewById(R.id.inputPW);

        commitBtn = (Button) v.findViewById(R.id.commit);
        idCheckBtn = (Button) v.findViewById(R.id.checkID);
        commitBtn.setEnabled(false);

        commitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = inputID.getText().toString();
                String pw = inputPW.getText().toString();

                 if (pw.length() < 4) {
                     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                     builder.setMessage("비밀 번호는 4자리 이상이어야 합니다.");
                     builder.show();
                 } else {
                     JSONTaskContacts jsonTaskContacts = new JSONTaskContacts(id, pw);
                     jsonTaskContacts.execute("http://13.125.74.215:8080/sign_up");
                 }
            }
        });

        idCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = inputID.getText().toString();
                String pw = inputPW.getText().toString();
                if (id.equals("")){
                    Toast.makeText(getActivity(), "아이디를 입력하세요.", Toast.LENGTH_LONG).show();
                }
                else {
                    JSONTaskContacts jsonTaskContacts = new JSONTaskContacts(id, pw);
                    jsonTaskContacts.execute("http://13.125.74.215:8080/sign_up_check");
                }
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
                String responseType = reply.getString("type");
                int responseCode = reply.getInt("result");

                if (responseType.equals("commit")) {
                    if (responseCode == 1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        String ID = inputID.getText().toString();
                        builder.setMessage(ID + "님 회원가입을 축하드립니다.");
                        builder.show();

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        fragmentManager.popBackStack();
                        transaction.commit();
                    } else {
                        Toast.makeText(getActivity(), "Something Wrong", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (responseCode == 1) {
                        commitBtn.setEnabled(true);
                        inputID.setEnabled(false);
                        idCheckBtn.setEnabled(false);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("중복 ID 입니다.");
                        builder.show();
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //서버로 부터 받은 값을 출력해주는 부분
        }
    }
}
