package my.apps.snowboardtrickguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.snowboardtrickguide.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    boolean backPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        BottomNavigationView navigationView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_complete, R.id.navigation_to_do, R.id.navigation_info, R.id.navigation_settings
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        backPressedOnce = false;
        // TODO: allow left to right swiping in between fragments
    }

    /////////// Methods for when user is trying to exit the app ////////////////
    @Override
    public void onBackPressed() {
        if(backPressedOnce) {
            super.onBackPressed();
            return;
        } else {
            backPressedOnce = true;
            Toast.makeText(this, getString(R.string.back_button_pressed_text), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backPressedOnce =false;
                }
            }, 2000);
        }
    }
}

