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
import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment {

    private ListView mListView;
    private ArrayList<ListViewItem> mItemList = new ArrayList<>();
    private ListViewAdapter mAdapter;
    private String userId;
    private String[] movieTitle =
            {"The_City_of_Crime", "Dark_Knight",
            "Good,_Bad_,Strange", "Titanic",
            "The_Wolf_of_Wallstreet"};

    // Main function.
    // Make ListView and set listeners on it.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_movie_list, container, false);
        Bundle bundle = getArguments();
        userId = bundle.getString("id");

        TextView username = (TextView) v.findViewById(R.id.userId);
        TextView commentNum = (TextView) v.findViewById(R.id.commentNum);
        username.setText(userId);
        commentNum.setText("5");

        new JSONTaskServer().execute("http://13.125.74.215:8080/api/articles/");

//        mItemList.add(new ListViewItem(ContextCompat.getDrawable(getActivity(), R.drawable.crimecity), "The City of Crime", 3.5, "범죄들의 천국"));
//        mItemList.add(new ListViewItem(ContextCompat.getDrawable(getActivity(), R.drawable.darkknight), "Dark Knight", 4.2, "어두운 밤"));
//        mItemList.add(new ListViewItem(ContextCompat.getDrawable(getActivity(), R.drawable.nom3), "Good, Bad, Strange", 3.8, "모든 놈들은 항상 이상했다."));
//        mItemList.add(new ListViewItem(ContextCompat.getDrawable(getActivity(), R.drawable.titanic), "Titanic", 4.4, "백허그밖에 기억나지 않아요"));
//        mItemList.add(new ListViewItem(ContextCompat.getDrawable(getActivity(), R.drawable.wolf), "The Wolf of Wallstreet", 3.7, "더 늑대 구슬 벽 거리 거닐다."));

        mAdapter = new ListViewAdapter();
        mListView = (ListView) v.findViewById(R.id.listview_movies);
        mListView.setAdapter(mAdapter);

        //Set ShortClick Listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Pass data 'adapter' and 'clicked item' to Detail_Fragment
                // To pass, use bundle packing.
               ListViewItem item = (ListViewItem) parent.getItemAtPosition(position);
//                ArrayList<ListViewItem> itemList = new ArrayList<>();
//                ArrayList<ListViewAdapter> adapterList = new ArrayList<>();
//                itemList.add(item);
//                adapterList.add(mAdapter);

                MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
                Bundle bundle = new Bundle();
//                bundle.putSerializable("itemList", itemList);
//                bundle.putSerializable("adapterList", adapterList);
                bundle.putString("title", item.getName());
                bundle.putString("id", userId);
                movieDetailFragment.setArguments(bundle);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack("List");
                transaction.replace(R.id.fragment_container, movieDetailFragment);
                transaction.commit();
            }
        });


        return v;
    }


    public class JSONTaskServer extends AsyncTask<String, String, String> {

        protected String doInBackground(String... urls) {
            try {

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    //URL url = new URL("http://192.168.25.16:3000/users");
                    URL url = new URL(urls[0]);//url을 가져온다.
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();//연결 수행

                    //입력 스트림 생성
                    InputStream stream = con.getInputStream();

                    //속도를 향상시키고 부하를 줄이기 위한 버퍼를 선언한다.
                    reader = new BufferedReader(new InputStreamReader(stream));

                    //실제 데이터를 받는곳
                    StringBuffer buffer = new StringBuffer();

                    //line별 스트링을 받기 위한 temp 변수
                    String line = "";

                    //아래라인은 실제 reader에서 데이터를 가져오는 부분이다. 즉 node.js서버로부터 데이터를 가져온다.
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    //다 가져오면 String 형변환을 수행한다. 이유는 protected String doInBackground(String... urls) 니까
                    return buffer.toString();

                    //아래는 예외처리 부분이다.
                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //종료가 되면 disconnect메소드를 호출한다.
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        //버퍼를 닫아준다.
                        if(reader != null){
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }//finally 부분
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        //doInBackground메소드가 끝나면 여기로 와서 리스트뷰의 값을 바꿔준다.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONArray jsonArray = new JSONArray(result);
                Drawable wolf = ContextCompat.getDrawable(getActivity(), R.drawable.wolf);
                Drawable crimecity = ContextCompat.getDrawable(getActivity(), R.drawable.crimecity);
                Drawable titanic = ContextCompat.getDrawable(getActivity(), R.drawable.titanic);
                Drawable nom3 = ContextCompat.getDrawable(getActivity(), R.drawable.nom3);
                Drawable darknight = ContextCompat.getDrawable(getActivity(), R.drawable.darkknight);

                for (int i=0; i<jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i); //i번째 Json데이터를 가져옴
                    String title = jsonObject.getString("title");
                    Double score = jsonObject.getDouble("score");
                    String description = jsonObject.getString("description");
                    ListViewItem listViewItem;
                    title = title.replace("_", " ");
                    switch (title) {
                        case "The City of Crime":
                            listViewItem = new ListViewItem(crimecity, title, score, description);
                            mItemList.add(listViewItem);
                            break;
                        case "Dark Knight":
                            listViewItem = new ListViewItem(darknight, title, score, description);
                            mItemList.add(listViewItem);
                            break;
                        case "Good, Bad, Strange":
                            listViewItem = new ListViewItem(nom3, title, score, description);
                            mItemList.add(listViewItem);
                            break;
                        case "Titanic":
                            listViewItem = new ListViewItem(titanic, title, score, description);
                            mItemList.add(listViewItem);
                            break;
                        case "The Wolf of Wallstreet":
                            listViewItem = new ListViewItem(wolf, title, score, description);
                            mItemList.add(listViewItem);
                            break;
                    }
                    mListView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class ListViewAdapter extends BaseAdapter implements Serializable {

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
                convertView = inflater.inflate(R.layout.listview_movies, parent, false);
            }

            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.list_movie_photo);
            TextView title = (TextView) convertView.findViewById(R.id.list_movie_name);
            TextView description = (TextView) convertView.findViewById(R.id.list_movie_description);
            TextView score = (TextView) convertView.findViewById(R.id.list_movie_score);

            iconImageView.setImageDrawable(mItemList.get(pos).getIcon());
            title.setText(mItemList.get(pos).getName());
            description.setText(mItemList.get(pos).getDescription());
            score.setText(mItemList.get(pos).getScore().toString());

            return convertView;
        }
    }

    class ListViewItem implements Serializable{
        private static final long serialVersionUID = 1L;
        private Drawable mIcon;
        private String mName;
        private Double mScore;
        private String mDescription;

        public ListViewItem(Drawable icon, String name, Double number, String description)
        {
            mIcon = icon;
            mName = name;
            mScore = number;
            mDescription = description;
        }

        public Drawable getIcon() {
            return mIcon;
        }

        public void setIcon(Drawable icon) {
            mIcon = icon;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public Double getScore() {
            return mScore;
        }

        public void setScore(Double score) {
            mScore = score;
        }

        public String getDescription() { return mDescription; }

        public void setDescription(String description) { mDescription = description; }
    }

}

