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

    // Main function.
    // Make ListView and set listeners on it.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Bundle args = getArguments();
        String title = args.getString("title");
        String userId = args.getString("id");

        mItemList.add(new ListViewItem(4.8, "재밌어요!"));
        mItemList.add(new ListViewItem(2.2, "별로에요"));
        mItemList.add(new ListViewItem(3.7, "볼만해요~"));

        mAdapter = new ListViewAdapter();
        mListView = (ListView) v.findViewById(R.id.detail_user_comments);
        mListView.setAdapter(mAdapter);

        return v;
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

