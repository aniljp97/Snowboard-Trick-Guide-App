package my.apps.snowboardtrickguide.info;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.snowboardtrickguide.R;

import java.util.ArrayList;

public class LinksActivity extends AppCompatActivity {

    private ArrayList<Link> links = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_links);
        getSupportActionBar().setTitle(getString(R.string.helpful_links_button_text));

        links.add(new Link("Park Features Handbook", "robertsski.com", "http://www.robertsski.com/webpgss/terrain-park-feature-handbook.htm"));
        links.add(new Link("Guide to Snowboard Grabs", "snowboardaddiction.com", "https://snowboardaddiction.com/blogs/tramp-board-training/112716804-the-complicated-world-of-snowboard-grabs"));
        links.add(new Link("How To Do Snowboard Grabs and Examples", "snowboarder.com", "https://www.snowboarder.com/grab-directory/"));
        links.add(new Link("Trick Terminology (Frontside/Backside, Stance, Rotation, Jibbing", "snowboardaddiction.com", "https://snowboardaddiction.com/blogs/jib-board-training/snowboard-terminology"));
        links.add(new Link("Snowboarding Terms and Slang", "the-house.com", "https://www.the-house.com/portal/snowboarding-terms/"));
        links.add(new Link("10 Foundational, Beginner Snowboard Rail Tricks (Video)", "snowboardprocamp.com", "https://www.youtube.com/watch?v=IJ-Te-dWRws&ab_channel=SnowboardProCamp"));
        links.add(new Link("10 Foundational, Beginner Snowboard Butter Tricks (Video)", "snowboardprocamp.com", "https://www.youtube.com/watch?v=G9qlTInKbNE&t=624s&ab_channel=SnowboardProCamp"));
        // TODO: add more links

        LinkListAdapter adapter = new LinkListAdapter(this,links);
        ListView listView = findViewById(R.id.links_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intentURL = new Intent(Intent.ACTION_VIEW, Uri.parse(links.get(i).getLink()));
                startActivity(intentURL);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

class Link {
    String title;
    String subtitle;
    String link;

    Link(String title, String subtitle, String link) {
        this.link = link;
        this.subtitle = subtitle;
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTitle() {
        return title;
    }
}
