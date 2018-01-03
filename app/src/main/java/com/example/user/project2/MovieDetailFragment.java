package com.example.user.project2;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailFragment extends Fragment {

    private ListView mListView;
    private ArrayList<ListViewItem> mItemList = new ArrayList<>();
    private ListViewAdapter mAdapter;
    private String movieTitle;
    private String myName;
    private Double myScore;
    private String myComment;
    private String movieDescription;
    private Double movieScore;
    private TextView descriptionView;
    private TextView scoreView;

    // Main function.
    // Make ListView and set listeners on it.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Bundle args = getArguments();

        String title = args.getString("title");
        movieTitle = title.replace(" ", "_");

        String userId = args.getString("id");
        myName = userId;

        TextView detail_movie_name = (TextView) v.findViewById(R.id.detail_movie_name);
        scoreView = (TextView) v.findViewById(R.id.detail_movie_score);
        descriptionView = (TextView) v.findViewById(R.id.detail_movie_description);
        detail_movie_name.setText(title);

        mListView = v.findViewById(R.id.detail_user_comments);
        mAdapter = new ListViewAdapter();

        ImageView detail_movie_photo = (ImageView) v.findViewById(R.id.detail_movie_photo);
        if (title.equals("The City Of Crime"))
            detail_movie_photo.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.crimecity));
        else if(title.equals("Dark Knight"))
            detail_movie_photo.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.darkknight));
        else if(title.equals("Good, Bad, Strange"))
            detail_movie_photo.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.nom3));
        else if(title.equals("Titanic"))
            detail_movie_photo.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.titanic));
        else if(title.equals("The Wolf of Wallstreet"))
            detail_movie_photo.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.wolf));

        new JSONTaskServer().execute("http://13.125.74.215:8080/api/articles/" + movieTitle);//AsyncTask 시작시킴

        final EditText score = (EditText) v.findViewById(R.id.detail_my_score);
        final EditText comment = (EditText) v.findViewById(R.id.detail_my_comment);

        Button submit = (Button) v.findViewById(R.id.detail_my_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myScore = Double.parseDouble(score.getText().toString());
                myComment = comment.getText().toString();

                new JSONTaskSubmit().execute("http://13.125.74.215:8080/api/articles");//AsyncTask 시작시킴

                score.setText("");
                comment.setText("");
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

                    //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("title", movieTitle);
                    jsonObject.accumulate("username", myName);
                    jsonObject.accumulate("message", myComment);
                    jsonObject.accumulate("score", myScore);

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
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
            mItemList.add(new ListViewItem(myName, myScore, myComment));
            mAdapter.notifyDataSetChanged();
        }
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
                JSONObject mJSONobject = new JSONObject(result);
                movieDescription = mJSONobject.getString("description");
                movieScore = mJSONobject.getDouble("score");
                movieScore = Double.parseDouble(String.format("%.1f",movieScore));
                descriptionView.setText(movieDescription);
                scoreView.setText(movieScore.toString());
                if (movieScore > 4.0) {
                    scoreView.setTextColor(getResources().getColor(R.color.Excellent));
                } else if (movieScore > 3.5) {
                    scoreView.setTextColor(getResources().getColor(R.color.Good));
                } else {
                    scoreView.setTextColor(getResources().getColor(R.color.Bad));
                }

                JSONArray commentArray = new JSONArray(mJSONobject.getString("comments"));

                mItemList.clear();

                for (int i=0; i<commentArray.length(); i++)
                {
                    JSONObject jsonObject = commentArray.getJSONObject(i); // i번째 Json데이터를 가져옴
                    String username = jsonObject.getString("username");
                    Double score = jsonObject.getDouble("score");

                    String message = jsonObject.getString("message");

                    mItemList.add(new ListViewItem(username, score, message));

                }

                mListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                convertView = inflater.inflate(R.layout.listview_comments, parent, false);
            }

            TextView id = (TextView) convertView.findViewById(R.id.comment_id);
            TextView score = (TextView) convertView.findViewById(R.id.comment_score);
            TextView comment = (TextView) convertView.findViewById(R.id.comment_comment);

            id.setText(mItemList.get(pos).getId().toString());
            score.setText(mItemList.get(pos).getScore().toString());
            comment.setText(mItemList.get(pos).getComment().toString());

            return convertView;
        }
    }

    class ListViewItem implements Serializable{
        private static final long serialVersionUID = 1L;
        private String mId;
        private Double mScore;
        private String mComment;

        public ListViewItem(String id, Double score, String comment)
        {
            mId = id;
            mScore = score;
            mComment = comment;
        }

        public String getId() {
            return mId;
        }

        public void setId(String id){
            mId = id;
        }

        public Double getScore() {
            return mScore;
        }

        public void setScore(Double score) {
            mScore = score;
        }

        public String getComment() {
            return mComment;
        }

        public void setmComment(String comment) {
            mComment = comment;
        }
    }
}

