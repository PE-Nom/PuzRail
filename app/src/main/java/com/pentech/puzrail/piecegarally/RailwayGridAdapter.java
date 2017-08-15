package com.pentech.puzrail.piecegarally;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pentech.puzrail.R;
import com.pentech.puzrail.database.Line;

import java.util.ArrayList;

/**
 * Created by takashi on 2017/08/15.
 */

public class RailwayGridAdapter extends BaseAdapter {
    private static String TAG = "RailwayGridAdapter";
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Line> lines;

    public RailwayGridAdapter(Context context, ArrayList<Line> lines) {
        super();
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        this.lines = new ArrayList<Line>(Arrays.asList(lines));
        this.lines = lines;
    }

    @Override
    public int getCount() {
        // 全要素数を返す
        return this.lines.size();
    }

    @Override
    public Object getItem(int position) {
        return this.lines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.lines.get(position).getDrawableResourceId();
    }

    private class ViewHolder {
        ImageView piece_border_image;
        ImageView railway_line_image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // main.xml の <GridView .../> に railway_grid_item.xml を inflate して convertView とする
            convertView = inflater.inflate(R.layout.railway_grid_item, parent, false);
            // ViewHolder を生成
            holder = new ViewHolder();
            holder.piece_border_image = (ImageView)convertView.findViewById(R.id.piece_border_grid_image_view);
            holder.railway_line_image = (ImageView) convertView.findViewById(R.id.railway_line_grid_image_view);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Line railway = this.lines.get(position);
        Drawable lineDrawable = ResourcesCompat.getDrawable(this.context.getResources(), railway.getDrawableResourceId(), null);
        holder.railway_line_image.setImageDrawable(lineDrawable);

        return convertView;
    }
}
