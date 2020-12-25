package my.apps.snowboardtrickguide.info;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.snowboardtrickguide.R;

import java.util.ArrayList;

public class LinkListAdapter extends ArrayAdapter<Link> {

    private final Activity context;
    private final ArrayList<Link> links;

    public LinkListAdapter(Activity context, ArrayList<Link> links) {
        super(context, R.layout.item_links_list, links);

        this.context = context;
        this.links = links;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d("LOG!", "" + position);
        LayoutInflater inflater = context.getLayoutInflater();
        View row = inflater.inflate(R.layout.item_links_list, null);

        TextView title_textview = row.findViewById(R.id.link_title);
        TextView subtitle_textview = row.findViewById(R.id.link_sub);

        title_textview.setText(links.get(position).getTitle());
        subtitle_textview.setText(links.get(position).getSubtitle());

        return row;
    }
}
