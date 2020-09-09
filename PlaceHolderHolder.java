package com.snowcoin.snowcoin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.johnkil.print.PrintView;
import com.unnamed.b.atv.model.TreeNode;

import java.util.Random;

/**
 * Created by Bogdan Melnychuk on 2/13/15.
 */
public class PlaceHolderHolder extends TreeNode.BaseNodeViewHolder<PlaceHolderHolder.PlaceItem> {


    public PlaceHolderHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, PlaceItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_place_node, null, false);


        TextView placeName = (TextView) view.findViewById(R.id.place_name);
        placeName.setText(value.name);

        Random r = new Random();
        boolean like = r.nextBoolean();

        return view;
    }

    @Override
    public void toggle(boolean active) {
    }


    public static class PlaceItem {
        public String name;

        public PlaceItem(String name) {
            this.name = name;
        }
        // rest will be hardcoded
    }

}