package com.example.drawawaytest.Cards;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.drawawaytest.Cards.Cards;
import com.example.drawawaytest.R;

import java.util.List;

public class ArrayAdapter extends android.widget.ArrayAdapter<Cards> {

    Context context;

    public ArrayAdapter(Context context, int resourceId, List<Cards> items){
        super(context, resourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Cards card_item = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);

        TextView category = (TextView) convertView.findViewById(R.id.category);

        TextView description = (TextView) convertView.findViewById(R.id.description);

        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        category.setText(card_item.getCategory());

        description.setText(card_item.getDescription());

        name.setText(card_item.getName());

        switch (card_item.getProfilBildUrl()){
            case "default":
                image.setImageResource(R.mipmap.default_drawaway);
                break;
            default:
                Glide.clear(image);
                Glide.with(convertView.getContext()).load(card_item.getProfilBildUrl()).into(image);
                break;
        }

        return convertView;
    }
}
