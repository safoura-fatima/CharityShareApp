package com.example.charityshareapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.charityshareapp.STORE.User;
import java.util.ArrayList;

public class UserListAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<User> users;

    public UserListAdapter(Context context, int resID, ArrayList<User> users) {
        super(context, resID, users);
        this.context = context;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            listItemView = inflater.inflate(R.layout.list_item_user, null);
        }
        else {
            listItemView = convertView;
        }

        User u = users.get(position);

        ((TextView)listItemView.findViewById(R.id.userName)).setText(u.name);
        ((TextView)listItemView.findViewById(R.id.userEmail)).setText(u.email);
        ((TextView)listItemView.findViewById(R.id.userMobile)).setText(u.mobile);
        ((TextView)listItemView.findViewById(R.id.userType)).setText(u.type);

        if(u.account_active.equals("0")) {
            ((TextView)listItemView.findViewById(R.id.userType)).setText(u.type + " (banned)");
            ((TextView)listItemView.findViewById(R.id.userType)).setTextColor(Color.RED);
        }

        return listItemView;
    }
}
