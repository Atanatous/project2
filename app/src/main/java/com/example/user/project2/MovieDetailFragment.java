package com.example.user.project2;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import static android.app.Activity.RESULT_OK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailFragment extends Fragment {

    private ListView mListView;
    private ArrayList<ListViewItem> mItemList = new ArrayList<>();
    private ListViewAdapter mAdapter;
    private String movieName;
    private String myScore;
    private String myComment;

    // Main function.
    // Make ListView and set listeners on it.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Bundle args = getArguments();
        String title = args.getString("title");

        mItemList.add(new ListViewItem(4.8, "재밌어요!"));
        mItemList.add(new ListViewItem(2.2, "별로에요"));
        mItemList.add(new ListViewItem(3.7, "볼만해요~"));

        mAdapter = new ListViewAdapter();
        mListView = (ListView) v.findViewById(R.id.detail_user_comments);
        mListView.setAdapter(mAdapter);

        final EditText score = (EditText) v.findViewById(R.id.detail_my_score);
        final EditText comment = (EditText) v.findViewById(R.id.detail_my_comment);

        Button submit = (Button) v.findViewById(R.id.detail_my_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myScore = score.getText().toString();
                myComment = comment.getText().toString();

                new JSONTaskSubmit().execute("http://13.125.74.215:8080/api/articles");//AsyncTask 시작시킴
            }
        });


        return v;
    }

    public class JSONTaskSubmit extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
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

                    JSONArray jsonArray = new JSONArray();

                    for (int i=0;i<mTempList.size();i++) {

                        String profileImageBase64="null";

                        //이미지를 Base64로 인코딩
                        if (mTempList.get(i).getIcon() == null)
                            profileImageBase64 = "null";
                        else
                        {
                            ByteArrayOutputStream outStream2 = new ByteArrayOutputStream();
                            Bitmap bitmap = ((BitmapDrawable) mTempList.get(i).getIcon()).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream2);
                            byte[] image = outStream2.toByteArray();
                            profileImageBase64 = Base64.encodeToString(image, 0);
                        }

                        //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.accumulate("name", mTempList.get(i).getName());
                        jsonObject.accumulate("number", mTempList.get(i).getPhoneNum());
                        jsonObject.accumulate("userPhoto", profileImageBase64);
                        jsonObject.accumulate("type", mTempList.get(i).getType());

                        jsonArray.put(jsonObject);
                    }

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonArray.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
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
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show(); //서버로 부터 받은 메시지를 출력해주는 부분
        }
    }



    class ListViewAdapter extends BaseAdapter implements Serializable{

        private static final long serialVersionUID = 1L;

        // 생성자
        public ListViewAdapter() {

        }

        // 지정한 위치에 있는 데이터와 관계된 아이템의 ID를 리턴.
        @Override
        public long getItemId(int position){
            return position;
        }

        // 지정한 위치에 있는 데이터 리턴.
        @Override
        public ListViewItem getItem(int position){
            return mItemList.get(position);
        }

        public ArrayList<ListViewItem> getItemList() {
            return mItemList;
        }

        // Adapter의 데이터 개수 리턴.
        @Override
        public int getCount(){
            return mItemList.size();
        }

        // position에 위치한 데이터를 화면에 출력하는 데 사용하는 View 리턴.
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            final int pos = position;
            final Context context = parent.getContext();

            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.fragment_movie_detail, parent, false);
            }

            TextView titleTextView = (TextView) convertView.findViewById(R.id.detail_my_score);
            TextView titleTextView2 = (TextView) convertView.findViewById(R.id.detail_my_comment);

            titleTextView.setText(mItemList.get(pos).getScore().toString());
            titleTextView2.setText(mItemList.get(pos).getName());

            return convertView;
        }
    }

    class ListViewItem implements Serializable{
        private static final long serialVersionUID = 1L;
        private Double mScore;
        private String mName;

        public ListViewItem(Double number, String name)
        {
            mScore = number;
            mName = name;
        }

        public Double getScore() {
            return mScore;
        }

        public void setScore(Double score) {
            mScore = score;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }
    }
}

