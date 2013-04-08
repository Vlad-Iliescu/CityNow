package ro.citynow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class UserListAdapter extends BaseAdapter {
    private ArrayList<User> userList = new ArrayList<User>();

    public void addUser(User user) {
        userList.add(user);
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    @Override
    public long getItemId(int i) {
        User user = userList.get(i);
        return user.getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            view = inflater.inflate(R.layout.user_item, viewGroup, false);
        }

        User user = userList.get(i);

        ImageView logo = (ImageView) view.findViewById(R.id.logoView);
        logo.setImageBitmap(user.getPoza());

        TextView username = (TextView) view.findViewById(R.id.usernameView);
        username.setText(user.getDenumire());

        TextView adress = (TextView) view.findViewById(R.id.adressView);
        adress.setText(user.getFullAdress());

        return view;
    }

    public void clearList() {
        this.userList.clear();
    }
}
