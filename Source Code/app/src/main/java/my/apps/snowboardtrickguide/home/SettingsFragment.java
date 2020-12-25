package my.apps.snowboardtrickguide.home;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.snowboardtrickguide.R;

public class SettingsFragment extends Fragment {
    private View view_root;

    private SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view_root = inflater.inflate(R.layout.fragment_settings, container, false);

        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        initSortCheckBoxes();
        initEmailFeedback();

        return view_root;
    }

    //////////////// Methods for initializing the screen ////////////////////////
    private void initSortCheckBoxes() {
        SharedPreferences.Editor prefEditor = preferences.edit();

        LinearLayout name_LL = view_root.findViewById(R.id.name_checkbox_LL);
        LinearLayout date_LL = view_root.findViewById(R.id.date_checkbox_LL);
        LinearLayout category_LL = view_root.findViewById(R.id.category_checkbox_LL);
        LinearLayout ascending_LL = view_root.findViewById(R.id.ascending_checkbox_LL);
        LinearLayout descending_LL = view_root.findViewById(R.id.descending_checkbox_LL);

        CheckBox name_cb = view_root.findViewById(R.id.name_checkbox);
        CheckBox date_cb = view_root.findViewById(R.id.date_checkbox);
        CheckBox category_cb = view_root.findViewById(R.id.category_checkbox);
        CheckBox ascending_cb = view_root.findViewById(R.id.ascending_checkbox);
        CheckBox descending_cb = view_root.findViewById(R.id.descending_checkbox);

        name_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_cb.setChecked(true);
                date_cb.setChecked(false);
                category_cb.setChecked(false);

                prefEditor.putString(getString(R.string.sort_list_by_key), getString(R.string.sort_list_by_name_value));
                prefEditor.apply();
            }
        });
        date_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_cb.setChecked(false);
                date_cb.setChecked(true);
                category_cb.setChecked(false);

                prefEditor.putString(getString(R.string.sort_list_by_key), getString(R.string.sort_list_by_date_value));
                prefEditor.apply();
            }
        });
        category_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_cb.setChecked(false);
                date_cb.setChecked(false);
                category_cb.setChecked(true);

                prefEditor.putString(getString(R.string.sort_list_by_key), getString(R.string.sort_list_by_category_value));
                prefEditor.apply();
            }
        });

        ascending_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ascending_cb.setChecked(true);
                descending_cb.setChecked(false);

                prefEditor.putString(getString(R.string.sort_list_order_key), getString(R.string.sort_list_ascending_value));
                prefEditor.apply();
            }
        });
        descending_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ascending_cb.setChecked(false);
                descending_cb.setChecked(true);

                prefEditor.putString(getString(R.string.sort_list_order_key), getString(R.string.sort_list_descending_value));
                prefEditor.apply();
            }
        });

        String sort_by_checked = preferences.getString(getString(R.string.sort_list_by_key), getString(R.string.sort_list_by_date_value));
        String sort_order_checked = preferences.getString(getString(R.string.sort_list_order_key), getString(R.string.sort_list_descending_value));

        if(sort_by_checked.equals(getString(R.string.sort_list_by_name_value)))
            name_cb.setChecked(true);
        else if(sort_by_checked.equals(getString(R.string.sort_list_by_category_value)))
            category_cb.setChecked(true);
        else if(sort_by_checked.equals(getString(R.string.sort_list_by_date_value)))
            date_cb.setChecked(true);

        if(sort_order_checked.equals(getString(R.string.sort_list_ascending_value)))
            ascending_cb.setChecked(true);
        else if(sort_order_checked.equals(getString(R.string.sort_list_descending_value)))
            descending_cb.setChecked(true);
    }

    private void initEmailFeedback() {
        Button email_button = view_root.findViewById(R.id.email_button);
        email_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mailto = "mailto:curbK10@gmail.com" +
                        "?cc=" +
                        "&subject=" + Uri.encode("User FeedBack: <insert what about>") +
                        "&body=" + Uri.encode("");
                Intent emailIntent = new Intent((Intent.ACTION_SENDTO));
                emailIntent.setData(Uri.parse(mailto));

                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), "Error openning email app", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // TODO: add link to rate app in app store
}
