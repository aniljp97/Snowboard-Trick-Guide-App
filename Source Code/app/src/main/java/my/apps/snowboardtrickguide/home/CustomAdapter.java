package my.apps.snowboardtrickguide.home;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import my.apps.snowboardtrickguide.KeyWords;

import com.example.snowboardtrickguide.R;
import my.apps.snowboardtrickguide.TrickData;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> implements KeyWords {

    private RecyclerViewClickListener buttonLister;
    private List<TrickData> localDataSet;

    private boolean isSelectionMode = false;

    private static RecyclerViewClickListener itemListener;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView trickIconImageView;
        private final TextView categoryTextView;
        private final TextView trickNameTextView;
        private final TextView trickDateTextView;
        private final LinearLayout container;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            trickIconImageView = (ImageView) view.findViewById(R.id.trick_icon_imageview);
            categoryTextView = (TextView) view.findViewById(R.id.category_textview);
            trickNameTextView = (TextView) view.findViewById(R.id.trick_name_textview);
            trickDateTextView = (TextView) view.findViewById(R.id.trick_date_textview);
            container = view.findViewById(R.id.trick_item_LL);
        }

        public ImageView getTrickIconImageView() {
            return trickIconImageView;
        }

        public TextView getCategoryTextView() {
            return categoryTextView;
        }

        public TextView getTrickNameTextView() {
            return trickNameTextView;
        }

        public TextView getTrickDateTextView() {
            return trickDateTextView;
        }

        public LinearLayout getContainer() {
            return container;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public CustomAdapter(List<TrickData> dataSet, RecyclerViewClickListener buttonListener) {
        localDataSet = dataSet;
        this.buttonLister = buttonListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_trick_list, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        final TrickData trickData = localDataSet.get(position);
        viewHolder.container.setBackgroundResource(trickData.isSelected() ? R.drawable.item_list_selected_background : R.drawable.item_list_background);

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if(trickData.getTrick_category().equals(aerial))
            viewHolder.getTrickIconImageView().setImageResource(R.drawable.ic__01_snowboarding);
        else if(trickData.getTrick_category().equals(jib))
            viewHolder.getTrickIconImageView().setImageResource(R.drawable.ic__02_rail);
        else if (trickData.getTrick_category().equals(butter))
            viewHolder.getTrickIconImageView().setImageResource(R.drawable.ic__03_butter);
        else
            viewHolder.getTrickIconImageView().setImageResource(R.drawable.ic_other_snowboard);


        if(trickData.getDisplay_short() == 1)
            viewHolder.getTrickNameTextView().setText(trickData.getShorten_trick_name());
        else
            viewHolder.getTrickNameTextView().setText(trickData.getTrick_name());
        viewHolder.getCategoryTextView().setText(trickData.getTrick_category());
        viewHolder.getTrickDateTextView().setText(trickData.getDate_discovered());

        viewHolder.getContainer().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSelectionMode) {
                    trickData.setIsSelected(!trickData.isSelected());
                    viewHolder.container.setBackgroundResource(trickData.isSelected() ? R.drawable.item_list_selected_background : R.drawable.item_list_background);
                    if(getSelectedCount() == 0)
                        isSelectionMode = false;
                }
                buttonLister.recyclerViewListClicked(position);
            }
        });

        viewHolder.getContainer().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!isSelectionMode) {
                    isSelectionMode = true;
                }
                trickData.setIsSelected(!trickData.isSelected());
                viewHolder.container.setBackgroundResource(trickData.isSelected() ? R.drawable.item_list_selected_background : R.drawable.item_list_background);

                buttonLister.recyclerViewListLongClicked(position);
                if(getSelectedCount() == 0)
                    isSelectionMode = false;
                return true;
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public TrickData getItem(int position) { return localDataSet.get(position); }

    public int getSelectedCount() {
        int count = 0;
        for(TrickData d : localDataSet) {
            if(d.isSelected())
                count++;
        }
        return count;
    }

    public ArrayList<TrickData> getSelectedItems() {
        ArrayList<TrickData> selectedTricks = new ArrayList<>();
        for(TrickData trick : localDataSet) {
            if(trick.isSelected()) {
                selectedTricks.add(trick);
            }
        }
        return selectedTricks;
    }

    public void deselectAllItems() {
        for(TrickData d : localDataSet) {
            d.setIsSelected(false);
        }
        isSelectionMode = false;
    }

    public void updateData(ArrayList<TrickData> new_data) {
        localDataSet.clear();
        localDataSet.addAll(new_data);
    }

    public void addItem(int position, TrickData trickData) {
        localDataSet.add(position, trickData);
    }

    public void removeItem(int position) {
        localDataSet.remove(position);
    }
}

