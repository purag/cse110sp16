package com.cs110.lit.adventour;

/**
 * Created by achen on 5/6/16.
 */

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.cs110.lit.adventour.model.User;

import java.util.ArrayList;

public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final ArrayList<String> TourTitle;
    private final ArrayList<String> TourDescription;
    private final ArrayList<String> imageId;
    private final ArrayList<User> TourUsers;

    public CustomList(Activity context,ArrayList<String> TourTitle, ArrayList<String> TourDescription, ArrayList<String> imageId, ArrayList<User> TourUsers)
    {
        super(context, R.layout.list_single, TourTitle);
        this.context = context;
        this.TourTitle = TourTitle;
        this.TourDescription = TourDescription;
        this.imageId = imageId;
        this.TourUsers = TourUsers;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_single, null, true);

        // list single view items (view)
        TextView userName = (TextView) rowView.findViewById(R.id.list_user_name);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.list_txt_title);
        TextView txtDescription = (TextView) rowView.findViewById(R.id.list_txt_description);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_img);
        Glide.with(context).load(imageId.get(position)).into(imageView);

        // Set list item contents
        userName.setText(TourUsers.get(position).getUser_name());
        txtTitle.setText(TourTitle.get(position));
        Typeface customFont = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        txtTitle.setTypeface(customFont);

        txtDescription.setText(TourDescription.get(position));

        // imageView.setImageResource(imageId[position]);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inDither = false;
//        options.inJustDecodeBounds = false;
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        options.inSampleSize = 5;
//
//        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), imageId.get(position),options);
//
//        imageView.setImageBitmap(icon);

        return rowView;
    }
}