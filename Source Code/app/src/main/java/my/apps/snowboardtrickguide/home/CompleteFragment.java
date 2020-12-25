package my.apps.snowboardtrickguide.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration;
import my.apps.snowboardtrickguide.EditorActivity;
import com.example.snowboardtrickguide.R;
import my.apps.snowboardtrickguide.TrickData;
import my.apps.snowboardtrickguide.addtrick.TrickDiscoverActivity;
import my.apps.snowboardtrickguide.data.TricksDBHepler;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

import static android.app.Activity.*;

public class CompleteFragment extends Fragment implements RecyclerViewClickListener {
    private View view_root;
    private CustomAdapter trick_adapter;

    final private int addTrickRequestCode = 1;
    final private int editTrickRequestCode = 2;
    final private int addTrickManuallyRequestCode = 3;

    RecyclerView trick_recyclerview;
    TricksDBHepler tricksDBHepler;
    LinearLayoutManager linearLayoutManager;

    ActionBar action_bar;
    String action_bar_title;

    String curr_search = "";

    Menu menu;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view_root = inflater.inflate(R.layout.fragment_completed_list, container, false);
        setHasOptionsMenu(true);

        trick_recyclerview = (RecyclerView) view_root.findViewById(R.id.completed_recyclerview);
        linearLayoutManager = new LinearLayoutManager(view_root.getContext());
        tricksDBHepler = new TricksDBHepler(getActivity());

        action_bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        action_bar_title = action_bar.getTitle().toString();

        populateRecycleView();
        setTrickRecyclerView();

        ExtendedFloatingActionButton fab = view_root.findViewById(R.id.find_trick_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToDiscoverTrickActivity();
            }
        });
        trick_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int pastVisibleItems = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                if (pastVisibleItems == 0) {
                    fab.extend();
                } else {
                    fab.shrink();
                }
            }
        });
        trick_recyclerview.addItemDecoration(new EndOffsetItemDecoration(200));

        return view_root;
    }

    ///////////////// Methods handling returning from an Activity /////////////////
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == addTrickRequestCode) { // from going to Discover new trick
            if(resultCode == RESULT_FIRST_USER) { // add new trick
                TrickData new_trick_data = (TrickData) data.getSerializableExtra(getString(R.string.add_regular_key));
                addTrick(new_trick_data);
            }
            if(resultCode == RESULT_OK) { // replace existing trick with new trick
                TrickData new_trick_data = (TrickData) data.getSerializableExtra(getString(R.string.add_replacement_key));
                deleteTrick(new_trick_data);
                addTrick(new_trick_data);
            }
        }

        if(requestCode == editTrickRequestCode) { // from clicking on a trick in list
            if(resultCode == RESULT_OK) { // update trick
                TrickData orginal_trick_data = (TrickData) data.getSerializableExtra(getString(R.string.orginial_trick_key));
                TrickData new_trick_data = (TrickData) data.getSerializableExtra(getString(R.string.updated_trick_key));
                updateTrick(orginal_trick_data,
                        new_trick_data.getTrick_category(),
                        new_trick_data.getTrick_name(),
                        new_trick_data.getShorten_trick_name(),
                        new_trick_data.getDate_discovered(),
                        new_trick_data.getNotes(),
                        new_trick_data.getCompleted(),
                        new_trick_data.getDisplay_short());
            }
            if(resultCode == RESULT_FIRST_USER) { // delete trick
                TrickData trick = (TrickData) data.getSerializableExtra(getString(R.string.orginial_trick_key));
                deleteTrick(trick);
            }
        }

        if(requestCode == addTrickManuallyRequestCode) { // from clicking to add trick manually
            if(resultCode == RESULT_OK) { // add new trick
                TrickData new_trick_data = (TrickData) data.getSerializableExtra(getString(R.string.updated_trick_key));
                addTrick(new_trick_data);
            }
        }

        populateRecycleView();
        setTrickRecyclerView();
        setActionBarReadMode();
    }

    ////////////////// Methods for options menu ////////////////////
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        this.menu = menu;
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.bar_action, menu);
        setActionBarReadMode();

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search_inlist).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                curr_search = s;
                populateRecycleView();
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSearchListMode();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.onActionViewCollapsed();
                setActionBarReadMode();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                trick_adapter.deselectAllItems();
                populateRecycleView();
                setActionBarReadMode();
                break;
            case R.id.action_delete:
                deleteAreYouSureDialog();
                break;
            case R.id.action_move_to_todolist:
                moveToToDoAreYouSureDialog();
                break;
            case R.id.action_add_trick_manual:
                goToAddTrickManualActivity();
                break;
        }

        return true;
    }

    ///////////////// Methods for dialog boxes /////////////////////
    private void deleteAreYouSureDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage("Are you sure you want to permanently delete the "+trick_adapter.getSelectedCount()+ " selected tricks?");
        dialog.setPositiveButton(getString(R.string.yes_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(TrickData trick : trick_adapter.getSelectedItems())
                    deleteTrick(trick);
                populateRecycleView();
                setActionBarReadMode();
            }
        });
        dialog.setNegativeButton(getString(R.string.no_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getContext(), "Delete canceled", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.create().show();
    }

    private void moveToToDoAreYouSureDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage("Are you sure you want to move the " + trick_adapter.getSelectedCount() + " selected tricks to your Tricks To Do List?");
        dialog.setPositiveButton(getString(R.string.yes_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (TrickData trick : trick_adapter.getSelectedItems())
                    updateTrick(trick, null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            -1);
                populateRecycleView();
                setActionBarReadMode();
            }
        });
        dialog.setNegativeButton(getString(R.string.no_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.create().show();
    }

    /////////////////// Methods to make changes to the the TricksDBHelper database /////////////////
    private void addTrick(TrickData trick) {
        boolean insertTrick = tricksDBHepler.addData(trick);

        if(insertTrick) {
            Toast.makeText(getActivity(),"Trick successfully added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(),"Failed to add trick", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTrick(TrickData trick) {
        int rows_deleted = tricksDBHepler.deleteData(trick);

        if(rows_deleted > 0) {
            Toast.makeText(getActivity(),"Successfully deleted " + rows_deleted + " tricks", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(),"Failed to delete any rows", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTrick(TrickData trick, String category, String name, String short_name, String date, String notes, int is_completed, int display_short) {
        int rows_updated = tricksDBHepler.updateData(trick,
                category,
                name,
                short_name,
                date,
                notes,
                is_completed,
                display_short);

        if(rows_updated > 0) {
            Toast.makeText(getActivity(),"Successfully updated " + rows_updated + " tricks", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(),"Failed to update any tricks", Toast.LENGTH_SHORT).show();
        }
    }

    //////////////// Methods dealing with setting up the RecyclerView and screen ////////////////////
    private void setActionBarEditMode() {
        action_bar.setTitle(String.valueOf(trick_adapter.getSelectedCount()));
        action_bar.setHomeAsUpIndicator(R.drawable.ic_clear24px);
        action_bar.setDisplayHomeAsUpEnabled(true);
        menu.findItem(R.id.action_delete).setVisible(true);
        menu.findItem(R.id.action_move_to_completelist).setVisible(false);
        menu.findItem(R.id.action_move_to_todolist).setVisible(true);
        menu.findItem(R.id.action_add_trick_manual).setVisible(false);

        menu.findItem(R.id.action_search_inlist).setVisible(false);
    }

    private void setActionBarReadMode() {
        action_bar.setTitle(action_bar_title);
        action_bar.setDisplayHomeAsUpEnabled(false);
        menu.findItem(R.id.action_add_trick_manual).setVisible(true);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_move_to_completelist).setVisible(false);
        menu.findItem(R.id.action_move_to_todolist).setVisible(false);

        menu.findItem(R.id.action_search_inlist).setVisible(true);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search_inlist).getActionView();
        if(!searchView.isIconified())
            menu.findItem(R.id.action_add_trick_manual).setVisible(false);
    }

    private void setSearchListMode() {
        menu.findItem(R.id.action_add_trick_manual).setVisible(false);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_move_to_completelist).setVisible(false);
        menu.findItem(R.id.action_move_to_todolist).setVisible(false);

        menu.findItem(R.id.action_search_inlist).setVisible(true);
    }

    private void populateRecycleView() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(MODE_PRIVATE);

        ArrayList<TrickData> trickDataArrayList;
        int direction = -1;
        if(sharedPreferences.getString(getString(R.string.sort_list_order_key), getString(R.string.sort_list_descending_value)).equals(getString(R.string.sort_list_ascending_value)))
            direction = 1;

        String sort_by = sharedPreferences.getString(getString(R.string.sort_list_by_key), getString(R.string.sort_list_by_date_value));
        if(sort_by.equals(getString(R.string.sort_list_by_name_value)))
            trickDataArrayList = sortByName(direction);
        else if(sort_by.equals(getString(R.string.sort_list_by_category_value)))
            trickDataArrayList = sortByCategory(direction);
        else
            trickDataArrayList = sortByDate(direction);

        ArrayList<TrickData> searchFilteredTrickDataArrayList = filterForSearch(trickDataArrayList, curr_search);

        trick_adapter = new CustomAdapter(searchFilteredTrickDataArrayList, this);
        trick_recyclerview.setAdapter(trick_adapter);
        trick_recyclerview.setLayoutManager(linearLayoutManager);
    }

    private void setTrickRecyclerView() {
        RecyclerView trick_recyclerview = (RecyclerView) view_root.findViewById(R.id.completed_recyclerview);

        if(trick_adapter.getItemCount() == 0) {
            view_root.findViewById(R.id.empty_list_view).setVisibility(View.VISIBLE);
            trick_recyclerview.setVisibility(View.GONE);
        } else {
            view_root.findViewById(R.id.empty_list_view).setVisibility(View.GONE);
            trick_recyclerview.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void recyclerViewListClicked(int position) {
        if(trick_adapter.getSelectedCount() > 0) {
            setActionBarEditMode();
        } else {
            if(action_bar.getTitle().toString().equals(action_bar_title)) {
                Intent intent = new Intent(getActivity(), EditorActivity.class);
                intent.putExtra(getString(R.string.edit_trick_key), trick_adapter.getItem(position));
                startActivityForResult(intent, editTrickRequestCode);
            } else {
                setActionBarReadMode();
            }
        }
    }

    @Override
    public void recyclerViewListLongClicked(int position) {
        if(trick_adapter.getSelectedCount() > 0) {
            setActionBarEditMode();
        } else {
            setActionBarReadMode();
        }
    }

    ///////////////// Methods to start an activity //////////////////
    private void goToDiscoverTrickActivity() {
        Intent intent = new Intent(getActivity(), TrickDiscoverActivity.class);
        startActivityForResult(intent, addTrickRequestCode);
    }

    private void goToAddTrickManualActivity() {
        Intent intent = new Intent(getActivity(), EditorActivity.class);
        intent.putExtra(getString(R.string.is_completed_key), 1);
        startActivityForResult(intent, addTrickManuallyRequestCode);
    }

    /////////////// Methods to get sorted ArrayList of data from TrickDBHelper /////////////////
    private ArrayList<TrickData> sortByDate(int direction) {
        Cursor data = tricksDBHepler.getData();
        ArrayList<TrickData> trickDataArrayList = new ArrayList<>();
        while(data.moveToNext()) {
            if(data.getInt(6) == 1) {
                String curr_date = data.getString(4);
                curr_date = curr_date.substring(6) + curr_date.substring(0, 5);
                curr_date = curr_date.replace("/", "");
                int num_curr_date = Integer.parseInt(curr_date);
                int i;
                for (i = 0; i < trickDataArrayList.size(); i++) {
                    String item_date = trickDataArrayList.get(i).getDate_discovered();
                    item_date = item_date.substring(6) + item_date.substring(0, 5);
                    item_date = item_date.replace("/", "");
                    int num_item_date = Integer.parseInt(item_date);
                    if (direction <= 0 && num_curr_date >= num_item_date)
                        break;
                    if(direction > 0 && num_curr_date <= num_item_date)
                        break;
                }
                trickDataArrayList.add(i,
                        new TrickData(
                                data.getString(1),
                                data.getString(2),
                                data.getString(3),
                                data.getString(4),
                                data.getString(5),
                                data.getInt(6),
                                data.getInt(7)));
            }
        }
        return trickDataArrayList;
    }

    private ArrayList<TrickData> sortByName(int direction) {
        Cursor data = tricksDBHepler.getData();
        ArrayList<TrickData> trickDataArrayList = new ArrayList<>();
        while(data.moveToNext()) {
            if(data.getInt(6) == 1) {
                String curr_name = data.getString(2);
                int i;
                for (i = 0; i < trickDataArrayList.size(); i++) {
                    String item_name = trickDataArrayList.get(i).getTrick_name();
                    if (direction <=0 && curr_name.compareToIgnoreCase(item_name) <= 0)
                        break;
                    if(direction > 0 && curr_name.compareToIgnoreCase(item_name) >= 0 )
                        break;
                }
                trickDataArrayList.add(i,
                        new TrickData(
                                data.getString(1),
                                data.getString(2),
                                data.getString(3),
                                data.getString(4),
                                data.getString(5),
                                data.getInt(6),
                                data.getInt(7)));
            }
        }
        return trickDataArrayList;
    }

    private ArrayList<TrickData> sortByCategory(int direction) {
        Cursor data = tricksDBHepler.getData();
        ArrayList<TrickData> trickDataArrayList = new ArrayList<>();
        while(data.moveToNext()) {
            if(data.getInt(6) == 1) {
                String curr_category = data.getString(1);
                int i;
                for (i = 0; i < trickDataArrayList.size(); i++) {
                    String item_category = trickDataArrayList.get(i).getTrick_category();
                    if (direction <=0 && curr_category.compareToIgnoreCase(item_category) <= 0)
                        break;
                    if(direction > 0 && curr_category.compareToIgnoreCase(item_category) >= 0 )
                        break;
                }
                trickDataArrayList.add(i,
                        new TrickData(
                                data.getString(1),
                                data.getString(2),
                                data.getString(3),
                                data.getString(4),
                                data.getString(5),
                                data.getInt(6),
                                data.getInt(7)));
            }
        }
        return trickDataArrayList;
    }

    private ArrayList<TrickData> filterForSearch(ArrayList<TrickData> list, String search) {
        ArrayList<TrickData> filter_list = new ArrayList<>();

        for(TrickData item : list) {
            String item_prop = item.getTrick_name() + " " + item.getShorten_trick_name() + " " + item.getTrick_category();
            if(item_prop.toUpperCase().contains(search.toUpperCase())) {
                filter_list.add(item);
            }
        }

        return filter_list;
    }
}

