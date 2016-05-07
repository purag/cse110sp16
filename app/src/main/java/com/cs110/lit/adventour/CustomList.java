package com.cs110.lit.adventour;

/**
 * Created by achen on 5/6/16.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final String[] TourTitle;
    private final String[] TourDescription;
    private final Integer[] imageId;
    public CustomList(Activity context,
                      String[] TourTitle, String[] TourDescription, Integer[] imageId) {
        super(context, R.layout.list_single, TourTitle);
        this.context = context;
        this.TourTitle = TourTitle;
        this.TourDescription = TourDescription;
        this.imageId = imageId;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.list_txt_title);
        TextView txtDescription = (TextView) rowView.findViewById(R.id.list_txt_description);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_img);

        txtTitle.setText(TourTitle[position]);
        Typeface customFont = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        txtTitle.setTypeface(customFont);
        
        txtDescription.setText(TourDescription[position]);

        // imageView.setImageResource(imageId[position]);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 10;

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                imageId[position],options);

        imageView.setImageBitmap(icon);

        return rowView;
    }
}