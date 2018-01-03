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
public class ContactsFragment extends Fragment {

    private ListView mListView;
    private ArrayList<ListViewItem> mTempList = new ArrayList<>();
    private ArrayList<ListViewItem> mItemList = new ArrayList<>();
    private ListViewAdapter mAdapter = new ListViewAdapter();

    private CallbackManager callbackManager;

    // Main function.
    // Make ListView and set listeners on it.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_contacts, container, false);

        callbackManager = CallbackManager.Factory.create();

        mListView = (ListView) v.findViewById(R.id.listview_contacts);
        mListView.setAdapter(mAdapter);

        LoginButton btn_putFacebookContacts = (LoginButton) v.findViewById(R.id.btn_putFacebookContacts);
        btn_putFacebookContacts.setReadPermissions("user_friends");

        // If using in a fragment
        btn_putFacebookContacts.setFragment(this);

        // Callback registration
        btn_putFacebookContacts.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code

                mTempList.clear();

                GraphRequest request = new GraphRequest( loginResult.getAccessToken(),"/me/taggable_friends",null,HttpMethod.GET,new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        //Log.d("response", response.toString());
                        JSONObject object = response.getJSONObject();
                        try {
                            JSONArray mJSONArray = object.getJSONArray("data");

                            for (int i=0; i < mJSONArray.length(); i++)
                            {
                                mTempList.add(new ListViewItem(null, mJSONArray.getJSONObject(i).getString("name"), "", "FACEBOOK"));
                                //Log.d("mTempList", mTempList.get(i).getName());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new JSONTaskContacts().execute("http://13.125.74.215:8080/api/contacts");//AsyncTask 시작시킴
                        GraphRequest nextReq = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);

                        if(nextReq != null)
                        {
                            nextReq.setCallback(this);
                            nextReq.executeAsync();
                        }
                    }
                });

                Bundle paramaters = new Bundle();
                paramaters.putString("fields","name,id");
                request.setParameters(paramaters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                // App code

                Toast.makeText(getActivity(), "login cancled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code

                Toast.makeText(getActivity(), "login error", Toast.LENGTH_SHORT).show();
            }
        });

        Button btn_putPhoneContacts = (Button) v.findViewById(R.id.btn_putPhoneContacts);
        btn_putPhoneContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                putContactListbyPhone();
            }
        });

        Button btn_getServerContacts = (Button) v.findViewById(R.id.btn_getServerContacts);
        btn_getServerContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getServerContacts();
            }
        });

        Button btn_deleteServerContacts = (Button) v.findViewById(R.id.btn_deleteServerContacts);
        btn_deleteServerContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteServerContacts();
                Toast.makeText(getActivity(), "All data deleted", Toast.LENGTH_SHORT).show();
            }
        });

        EditText editTextFilter = (EditText)v.findViewById(R.id.editTextFilter) ;
        editTextFilter.addTextChangedListener(new TextWatcher() {
                                                  @Override public void afterTextChanged(Editable edit) {
                                                      String filterText = edit.toString() ;
                                                      ((ListViewAdapter)mListView.getAdapter()).getFilter().filter(filterText) ;
                                                  }

                                                  @Override public void beforeTextChanged(CharSequence s, int start, int count, int after)
                                                  {

                                                  }
                                                  @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

                                                  }
                                              }
        ) ;


        /*

        //Set ShortClick Listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Pass data 'adapter' and 'clicked item' to Detail_Fragment
                // To pass, use bundle packing.
                ListViewContacts item = (ListViewContacts) parent.getItemAtPosition(position);
                ArrayList<ListViewContacts> itemList = new ArrayList<>();
                ArrayList<ListViewAdapter> adapterList = new ArrayList<>();
                itemList.add(item);
                adapterList.add(mAdapter);

                /*ContactDetail_Fragment detail_fragment = new ContactDetail_Fragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("itemList", itemList);
                bundle.putSerializable("adapterList", adapterList);
                detail_fragment.setArguments(bundle);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack("List");
                transaction.replace(R.id.fragment_container, detail_fragment);
                transaction.commit();
            }
        });
        */

        return v;
    }

    // Load and add all contact data from phone
    public void putContactListbyPhone(){
        ContentResolver cr = getActivity().getContentResolver();
        Cursor mCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        mCursor.moveToFirst();

        mTempList.clear();

        while (mCursor.moveToNext()){

            int nameidx = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneidx = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int ididx = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);

            String name = mCursor.getString(nameidx);
            String phoneNum = mCursor.getString(phoneidx);

            InputStream photo = openPhoto(mCursor.getLong(ididx));

            Bitmap bitmap;
            bitmap = BitmapFactory.decodeStream(photo);
            Drawable profileImage = new BitmapDrawable(bitmap);

            if (photo != null){
                mTempList.add(new ListViewItem(profileImage, name, phoneNum, "PHONE"));
            } else {
                mTempList.add(new ListViewItem(null, name, phoneNum, "PHONE"));
            }
        }

        new JSONTaskContacts().execute("http://13.125.74.215:8080/api/contacts");//AsyncTask 시작시킴
    }

    /*// Load and add all contact data from facebook account
    public void putContactListbyFacebook(){
        new JSONTaskContacts().execute("http://13.125.74.215:8080/api/contacts");//AsyncTask 시작시킴
    }*/

    // Load all contact data from facebook account
    public void getServerContacts(){
        new JSONTaskServer().execute("http://13.125.74.215:8080/api/contacts");//AsyncTask 시작시킴
    }

    public void deleteServerContacts(){
        new TaskServerDelete().execute("http://13.125.74.215:8080/api/contacts");//AsyncTask 시작시킴
    }

    public InputStream openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = getActivity().getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO }, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK){
            return;
        }

        /*if (requestCode == REQ_CODE_SELECT_IMAGE) {
            try {
                Bitmap mImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                Drawable mImageDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(mImage, 300, 300, true));

                mItem.setIcon(mImageDrawable);
                mAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    public class JSONTaskContacts extends AsyncTask<String, String, String> {

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
                JSONArray mJSONArray = new JSONArray(result);

                mItemList.clear();

                for (int i=0; i<mJSONArray.length(); i++)
                {
                    JSONObject jsonObject = mJSONArray.getJSONObject(i); //i번째 Json데이터를 가져옴
                    String type = jsonObject.getString("type");
                    String name = jsonObject.getString("name");
                    String number = jsonObject.getString("number");
                    String userPhoto = jsonObject.getString("userPhoto");

                    if (userPhoto.equals("null"))
                        mItemList.add(new ListViewItem(null, name, number, type));
                    else {
                        //데이터 base64 형식으로 Decode
                        byte[] bytePlainOrg = Base64.decode(userPhoto, 0);

                        //byte[] 데이터  stream 데이터로 변환 후 bitmapFactory로 이미지 생성
                        ByteArrayInputStream inStream = new ByteArrayInputStream(bytePlainOrg);
                        Bitmap bm = BitmapFactory.decodeStream(inStream);

                        mItemList.add(new ListViewItem(new BitmapDrawable(bm), name, number, type));
                    }
                }

                mListView.invalidateViews();
                mListView.refreshDrawableState();
                mAdapter.notifyDataSetChanged();

                Toast.makeText(getActivity(), "Download completed", Toast.LENGTH_LONG).show(); //서버로 부터 받은 값을 출력해주는 부분
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class TaskServerDelete extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("DELETE");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();

                    //버퍼를 생성하고 넣음

                    JSONArray jsonArray = new JSONArray();

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
        }
    }

    class ListViewAdapter extends BaseAdapter implements Serializable, Filterable {

        private static final long serialVersionUID = 1L;
        private ArrayList<ListViewItem> filteredItemList;

        Filter listFilter ;

        // 생성자
        public ListViewAdapter() {
            filteredItemList = mItemList;
        }

        // 지정한 위치에 있는 데이터와 관계된 아이템의 ID를 리턴.
        @Override
        public long getItemId(int position){
            return position;
        }

        // 지정한 위치에 있는 데이터 리턴.
        @Override
        public ListViewItem getItem(int position){
            return filteredItemList.get(position);
        }

        public ArrayList<ListViewItem> getItemList() {
            return filteredItemList;
        }

        // Adapter의 데이터 개수 리턴.
        @Override
        public int getCount(){
            return filteredItemList.size();
        }

        // position에 위치한 데이터를 화면에 출력하는 데 사용하는 View 리턴.
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            final int pos = position;
            final Context context = parent.getContext();

            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_contacts, parent, false);
            }

            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1);
            TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1);
            TextView titleTextView2 = (TextView) convertView.findViewById(R.id.textView2);
            ImageView iconImageView2 = (ImageView) convertView.findViewById(R.id.imageView2);

            if (filteredItemList.get(pos).getIcon() == null)
                iconImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.noimage));
            else
                iconImageView.setImageDrawable(filteredItemList.get(pos).getIcon());

            titleTextView.setText(filteredItemList.get(pos).getName());
            titleTextView2.setText(filteredItemList.get(pos).getPhoneNum());

            if (filteredItemList.get(pos).getType().equals("PHONE"))
                iconImageView2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.smartphone));
            else if (filteredItemList.get(pos).getType().equals("FACEBOOK"))
                iconImageView2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.facebook));

            return convertView;
        }

        @Override
        public Filter getFilter() {
            if (listFilter == null) { listFilter = new ListFilter() ; } return listFilter ;

        }

        private class ListFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults() ;

                if (constraint == null || constraint.length() == 0) {
                    results.values = mItemList ;
                    results.count = mItemList.size() ;
                } else {
                    ArrayList<ListViewItem> itemList = new ArrayList<ListViewItem>() ;

                    for (ListViewItem item : mItemList) {
                        if (item.getName().toUpperCase().contains(constraint.toString().toUpperCase()) ||
                                item.getPhoneNum().toUpperCase().contains(constraint.toString().toUpperCase()))
                        {
                            itemList.add(item) ;
                        }
                    }

                    results.values = itemList ;
                    results.count = itemList.size() ;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                // update listview by filtered data list.
                filteredItemList = (ArrayList<ListViewItem>) results.values ;

                // notify
                if (results.count > 0) {
                    notifyDataSetChanged() ;
                } else {
                    notifyDataSetInvalidated() ;
                }
            }
        }
    }

    class ListViewItem implements Serializable{
        private static final long serialVersionUID = 1L;
        private Drawable mIcon;
        private String mName;
        private String mPhoneNum;
        private String mType;
        /*private String mMail;*/

        public ListViewItem()
        {

        }

        public ListViewItem(Drawable icon, String name, String number, String type)
        {
            mIcon = icon;
            mName = name;
            mPhoneNum = number;
            mType = type;
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

        public String getPhoneNum() {
            return mPhoneNum;
        }

        public void setPhoneNum(String phoneNum) {
            mPhoneNum = phoneNum;
        }

        public String getType() {
            return mType;
        }

        public void setType(String type) {
            mType = type;
        }
    }


}
