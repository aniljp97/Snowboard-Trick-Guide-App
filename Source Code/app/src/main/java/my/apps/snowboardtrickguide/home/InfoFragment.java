package my.apps.snowboardtrickguide.home;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.snowboardtrickguide.R;
import my.apps.snowboardtrickguide.data.TricksDBHepler;
import my.apps.snowboardtrickguide.info.LinksActivity;
import my.apps.snowboardtrickguide.info.TerminologyActivity;
import my.apps.snowboardtrickguide.info.UnderstandingActivity;

public class InfoFragment extends Fragment {
    private View view_root;

    TricksDBHepler tricksDBHepler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view_root = inflater.inflate(R.layout.fragment_info, container, false);

        tricksDBHepler = new TricksDBHepler(getActivity());

        initializeViews();

        return view_root;
        // TODO: add more stats and links
    }

    private void initializeViews() {
        TextView discovered_textview = view_root.findViewById(R.id.num_trick_discovered_textview);
        TextView completed_textview = view_root.findViewById(R.id.num_of_completed_textview);
        TextView todo_textview = view_root.findViewById(R.id.num_of_todo_textview);

        Button terminology_button = view_root.findViewById(R.id.terminology_button);
        Button links_button = view_root.findViewById(R.id.links_button);
        Button understanding_button = view_root.findViewById(R.id.understanding_button);

        int discovered, completed, todo;
        discovered = completed = todo = 0;
        Cursor data = tricksDBHepler.getData();
        while(data.moveToNext()) {
            discovered++;
            if(data.getInt(6) > 0)
                completed++;
            else
                todo++;
        }
        discovered_textview.setText(String.valueOf(discovered));
        completed_textview.setText(String.valueOf(completed));
        todo_textview.setText(String.valueOf(todo));

        terminology_button.setOnClickListener(view -> goToTerminologyActivity());
        links_button.setOnClickListener(view -> goToLinksActivity());
        understanding_button.setOnClickListener(view -> goToUnderstanding());
    }

    //////////////// Methods for Button clicks ////////////////////////
    public void goToTerminologyActivity() {
        startActivity(new Intent(getActivity(), TerminologyActivity.class));
    }

    public void goToLinksActivity() {
        startActivity(new Intent(getActivity(), LinksActivity.class));
    }

    public void goToUnderstanding() {
        startActivity(new Intent(getActivity(), UnderstandingActivity.class));
    }
}
