package tv.accedo.colourmemory;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by archie on 4/3/16.
 */
public class HighScoreAdapter extends BaseTableAdapter {

    private List<HighScore> userScores;
    private AppCompatActivity mContext;
    private String[] headers;
    private Realm realmDb;

    public HighScoreAdapter(AppCompatActivity c) {
        userScores = new RealmList<>();
        mContext = c;
        headers = new String[] { "Rank", "Name", "Score" };
        // Init DB
        final RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).build();
        realmDb = Realm.getInstance(realmConfig);
    }

    public boolean populateScore() {
        RealmResults<HighScore> result = realmDb.where(HighScore.class).findAll();
        result.sort("score", Sort.DESCENDING);
        userScores = realmDb.copyFromRealm(result);
        if(userScores.size() == 0)
            return false;
        return true;
    }


    @Override
    public int getRowCount() {
        return userScores.size();
    }

    @Override
    public int getColumnCount() {
        return headers.length-1;
    }

    @Override
    public View getView(int row, int column, View convertView, ViewGroup parent) {
        View view = null;

        switch (getItemViewType(row, column)) {
            case 0:
                view = getHeader(column, convertView, parent);
                break;
            case 1:
                view = getBody(row, column, convertView, parent);
                break;
            default:
                throw new RuntimeException("wtf1?");
        }
        return view;
    }

    private View getHeader(int column, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mContext.getLayoutInflater().inflate(R.layout.item_table_header, parent, false);
        }
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(headers[column+1]);
        return convertView;
    }

    private View getBody(int row, int column, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mContext.getLayoutInflater().inflate(R.layout.item_table, parent, false);
        }
        convertView.setBackgroundResource(row % 2 == 0 ? R.drawable.bg_table_color1 : R.drawable.bg_table_color2);
        switch(column+1) {
            case 0:
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(userScores.get(row).getRank());
                break;
            case 1:
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(userScores.get(row).getName());
                break;
            case 2:
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(Long.toString(userScores.get(row).getScore()));
                break;
            default:
                throw new RuntimeException("wtf2?");
        }

        return convertView;
    }

    @Override
    public int getWidth(int column) {
        return 1;
    }

    @Override
    public int getHeight(int row) {
        Display display = mContext.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float h = ((float)size.y/100f)*7f;
        return Math.round(h);
    }

    @Override
    public int getItemViewType(int row, int column) {

        if(row == -1)
            return 0;

        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
