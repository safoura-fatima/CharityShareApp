package com.example.charityshareapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.charityshareapp.STORE.*;

public class PostListAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<Post> posts;

    public PostListAdapter(Context context, int resID, ArrayList<Post> posts) {
        super(context, resID, posts);
        this.context = context;
        this.posts = posts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            listItemView = inflater.inflate(R.layout.list_item_post, null);
        }
        else {
            listItemView = convertView;
        }

        Post p = posts.get(position);

        ((TextView)listItemView.findViewById(R.id.postTitle)).setText(p.title);
        ((TextView)listItemView.findViewById(R.id.posterName)).setText(p.poster_name);

        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(p.created_at);
            df.setTimeZone(TimeZone.getDefault());

            String fomatted_date = android.text.format.DateFormat.format("hh:mm a, dd-MMM-yyyy", date).toString();
            ((TextView)listItemView.findViewById(R.id.postDate)).setText(fomatted_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(p.poster_name.equals("admin"))
            ((TextView)listItemView.findViewById(R.id.posterName)).setTextColor(Color.RED);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.placeholder);

        Glide.
        with(context).
        setDefaultRequestOptions(requestOptions).
        load(STORE.BASE_URL_IMG + p.filename).
        into((ImageView)listItemView.findViewById(R.id.postImage));

        return listItemView;
    }
}
