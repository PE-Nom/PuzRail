package com.pentech.puzrail;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pentech.puzrail.database.Company;
import com.pentech.puzrail.database.DBAdapter;
import com.pentech.puzrail.ui.GaugeView;

import java.util.ArrayList;

/**
 * Created by takashi on 2017/03/26.
 */

public class CompanyListAdapter extends BaseAdapter {

    private String TAG="CompanyAdapter";
    private MainActivity context;
    private ArrayList<Company> companies = new ArrayList<Company>();
    private DBAdapter dbAdapter;

    public CompanyListAdapter(Context context, ArrayList<Company> companies, DBAdapter dbAdapter){
        this.context = (MainActivity)context;
        this.companies = companies;
        this.dbAdapter = dbAdapter;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
//        Log.d(TAG,String.format("getCount = %d",this.companies.size()));
        return this.companies.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
//        Log.d(TAG,String.format("getItem position = %d",position));
        return this.companies.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
//        Log.d(TAG,String.format("getItemId position = %d",position));
        return this.companies.get(position).getId();
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = this.context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.company_list_item,null);
            holder = new ViewHolder();
            holder.companyName = (TextView)convertView.findViewById(R.id.companyName);
            holder.companyKana = (TextView)convertView.findViewById(R.id.companyKana);
            holder.companyTotalScore = (TextView)convertView.findViewById(R.id.companyScore);

            holder.silhouetteProgressDenominator = (TextView)convertView.findViewById(R.id.silhouetteProgDenominator);
            holder.silhouetteProgressValue = (TextView)convertView.findViewById(R.id.silhouetteProgValue);
            holder.silhouetteScore = (TextView) convertView.findViewById(R.id.silhouetteScore);

            holder.locationProgressDenominator = (TextView)convertView.findViewById(R.id.locationProgDenominator);
            holder.locationProgressValue = (TextView)convertView.findViewById(R.id.locationProgValue);
            holder.locationScore = (TextView) convertView.findViewById(R.id.locationScore);

            holder.stationsProgressDenominator = (TextView)convertView.findViewById(R.id.stationsProgDenominator);
            holder.stationsProgressValue = (TextView)convertView.findViewById(R.id.stationsProgValue);
            holder.stationsScore = (TextView) convertView.findViewById(R.id.stationsSocre);

            holder.silhouetteProgress = (GaugeView)convertView.findViewById(R.id.silhouetteProgress);
            holder.locationProgress = (GaugeView)convertView.findViewById(R.id.locationProgress);
            holder.stationsProgress = (GaugeView)convertView.findViewById(R.id.stationsProgress);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

            Company company = this.companies.get(position);
            holder.companyName.setText(company.getName()+" ");
            holder.companyName.setTextColor(Color.parseColor("#142d81"));
            holder.companyKana.setText("("+company.getKana()+")");
            holder.companyKana.setTextColor(Color.parseColor("#142d81"));

            int id = company.getId();
            int totalLines = this.dbAdapter.countTotalLines(id);
            int answeredLines = this.dbAdapter.countSilhouetteAnsweredLines(id);
            int locatedLines = this.dbAdapter.countLocationAnsweredLines(id);
            int totalStations = this.dbAdapter.countTotalStationsInCompany(id);
            int openedStations = this.dbAdapter.countAnsweredStationsInCompany(id);

            holder.companyTotalScore.setText(String.format("%d",company.getCompanyTotalScore()));
            // 路線シルエットの進捗
            holder.silhouetteProgressValue.setText(String.format("%d",answeredLines));
            holder.silhouetteProgressDenominator.setText(String.format("/%d",totalLines));
            int silhouetteProgress = 100*answeredLines/totalLines;
    //        Log.d(TAG,String.format("namedLines = %d, totalLines = %d, silhouetteProgress = %d",namedLines,totalLines,silhouetteProgress));
            holder.silhouetteProgress.setData(silhouetteProgress,"%", ContextCompat.getColor(this.context, R.color.color_90),90,true);
            holder.silhouetteScore.setText(String .format("%d",company.getSilhouetteTotalScore()));

            // 地図合わせの進捗
            holder.locationProgressValue.setText(String.format("%d",locatedLines));
            holder.locationProgressDenominator.setText(String.format("/%d",totalLines));
            int trackLayingProgress = 100*locatedLines/totalLines;
    //        Log.d(TAG,String.format("locatedLines = %d, totalLines = %d, tracklayingProgress = %d",locatedLines,totalLines,locationProgress));
            holder.locationProgress.setData(trackLayingProgress,"%",ContextCompat.getColor(this.context, R.color.color_60),90,true);
            holder.locationScore.setText(String.format("%d",company.getLocationTotalScore()));

            // 駅開設の進捗
            holder.stationsProgressValue.setText(String.format("%d",openedStations));
            holder.stationsProgressDenominator.setText(String.format("/%d",totalStations));
            int stationOpenProgress = 100*openedStations/totalStations;
    //        Log.d(TAG,String.format("opendStations = %d, totalStations = %d, stationProgress = %d",openedStations,totalStations,stationsProgress));
            holder.stationsProgress.setData(stationOpenProgress,"%",ContextCompat.getColor(this.context, R.color.color_30),90,true);
            holder.stationsScore.setText(String.format("%d",company.getStationsTotalScore()));

        return convertView;
    }

    private class ViewHolder {
        TextView companyName;
        TextView companyKana;
        TextView companyTotalScore;

        TextView silhouetteProgressDenominator;
        TextView silhouetteProgressValue;
        GaugeView silhouetteProgress;
        TextView silhouetteScore;

        TextView locationProgressDenominator;
        TextView locationProgressValue;
        GaugeView locationProgress;
        TextView locationScore;

        TextView stationsProgressDenominator;
        TextView stationsProgressValue;
        GaugeView stationsProgress;
        TextView stationsScore;
    }
}
