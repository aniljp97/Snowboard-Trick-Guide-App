package my.apps.snowboardtrickguide.addtrick;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ablanco.zoomy.Zoomy;
import my.apps.snowboardtrickguide.KeyWords;
import com.example.snowboardtrickguide.R;
import my.apps.snowboardtrickguide.TrickData;
import my.apps.snowboardtrickguide.data.TricksDBHepler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TrickDiscoverActivity extends AppCompatActivity implements KeyWords {
    // root of trick tree
    Q root;
    Q curr_Q;

    // keep track of the past Q that have been visited on a search
    ArrayList<Q> previousQs = new ArrayList<>();

    // Holding Strings specific to screen the user is on
    String currTree = ""; // Message to put on top bar
    String currCategory = ""; // curr initial branch of tree
    String currTrick = ""; // curr trick once found

    // For the selecting of rotation amount
    NumberPicker spins_numberpicker;
    private final String[] spins_numberpicker_values = new String[]{
            "180", "360", "540", "720", "900", "1080", "1260", "1440", "1620", "1800", "1980", "2160"};
    private String spins_amount = "";
    private String spins_amount2 = "";

    // For the selecting of a certain grab
    Spinner grab_spinner;
    private final String[] grab_spinner_values = new String[]{
            nose, crail, mute, indy, seatbelt, tail, stalefish, melon, other};
    private String grab_name = "";

    // For the selecting of a certain feature
    Spinner feature_spinner;
    private final String[] feature_spinner_values = new String[]{
            box_, rail_, pipe_, tree_, other};
    private String feature_name = "";

    // Keeping track for specifically for butter tricks
    private boolean in_butter_tree = false;
    private String butter_trick_name = "";

    // For Toasts so that toast messages do not stack
    private Toast toast = null;

    // Shared Preferences and the keys for it
    SharedPreferences sharedPref;
    String dontshowAerialExplainKey = "dontshowAerial";
    String dontshowButterExplainKey = "dontshowButter";
    String dontshowJibExplainKey = "dontshowJib";
    String checked_val = "checked";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_category);
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        // Create tree and set the select a category page
        createTrickTree();
        setCategoryPage();
    }

    //////// Methods for setting up screen according to the question currently on ////////
    private String applySpinsAndGrabsToTrick(String trick) {
        trick = trick.replace(rotationPlaceholder_.trim(), spins_amount);
        trick = trick.replace(grabPlaceholder_grab_.trim(), grab_name + " " + grab_.trim());
        trick = trick.replace(rotation2Placeholder_.trim(), spins_amount2);
        trick = trick.replace(featurePlaceholder_.trim(), "("+feature_name.trim()+")");
        return trick;
    }

    private void setCategoryPage() {
        curr_Q = root;
        setContentView(R.layout.page_category);
        resetButterTrickVars();
        resetTrickVars();
        currTree = getString(R.string.top_bar_beginning_search_text);
        ((TextView) findViewById(R.id.top_bar_textview)).setText(currTree);
        findViewById(R.id.topleft_button).setOnLongClickListener(view -> {
            showAnswerDescriptionDialog(view);
            return true;
        });
        findViewById(R.id.topright_button).setOnLongClickListener(view -> {
            showAnswerDescriptionDialog(view);
            return true;
        });
        findViewById(R.id.bottomleft_button).setOnLongClickListener(view -> {
            showAnswerDescriptionDialog(view);
            return true;
        });
        findViewById(R.id.bottomright_button).setOnLongClickListener(view -> {
            showAnswerDescriptionDialog(view);
            return true;
        });
        findViewById(R.id.bottomright_button).setOnClickListener(this::showAnswerDescriptionDialog);
    }

    private void setButterContinuePage(Q q) {
        setContentView(R.layout.page_butter_continue);
        String trick_name = applySpinsAndGrabsToTrick(q.getTrickName());
        if (butter_trick_name.equals(""))
            butter_trick_name = butter_trick_name + trick_name;
        else
            butter_trick_name = butter_trick_name + " to " + trick_name;
        TextView butter_name_textview = findViewById(R.id.butter_name_discovered_textview);
        butter_name_textview.setText(butter_trick_name);
        butter_name_textview.setOnLongClickListener((View.OnLongClickListener) view -> {
            showFullTrickNameDialog(view);
            return true;
        });
        currTree = getString(R.string.top_bar_butter_found_text);
        ((Button) findViewById(R.id.save_button)).setVisibility(View.INVISIBLE);
    }

    private void setTrickNamePage(Q q) {
        // set to trick name page
        setContentView(R.layout.page_trick_name);
        String trick_name = q.getTrickName();
        trick_name = applySpinsAndGrabsToTrick(trick_name);
        if(in_butter_tree)
            trick_name = applySpinsAndGrabsToTrick(butter_trick_name);
        currTrick = trick_name;
        TextView trick_name_textview = findViewById(R.id.trick_name_discovered_textview);
        trick_name_textview.setText(trick_name);
        trick_name_textview.setOnLongClickListener((View.OnLongClickListener) view -> {
            showFullTrickNameDialog(view);
            return true;
        });
        if (currTree.contains(getString(R.string.aerial_button_text)))
            currTree = getString(R.string.top_bar_aerial_found_text);
        else if (currTree.contains(getString(R.string.jibb_button_text)))
            currTree = getString(R.string.top_bar_jib_found_text);
        ((Button) findViewById(R.id.save_button)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.top_bar_textview)).setText(currTree);

        Button shorten_button = findViewById(R.id.trick_to_short_vers_button);
        shorten_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String curr_trick_name = q.getTrickName();
                curr_trick_name = applySpinsAndGrabsToTrick(curr_trick_name);
                if(in_butter_tree)
                    curr_trick_name = applySpinsAndGrabsToTrick(butter_trick_name);
                String shorten_button_text = shorten_button.getText().toString();

                if(shorten_button_text.equals(getString(R.string.shorten_button_text))) {
                    trick_name_textview.setText(TrickData.shortenTrickName(curr_trick_name));
                    shorten_button.setText(getString(R.string.lengthen_button_text));
                } else {
                    trick_name_textview.setText(curr_trick_name);
                    shorten_button.setText(getString(R.string.shorten_button_text));
                }
            }
        });
    }

    private void setQtoLayout(Q q) {
        if (q.isEnd()) {
            if (in_butter_tree) {
                setButterContinuePage(q);
            } else {
                setTrickNamePage(q);
            }
        } else {

            String question = q.getQuestion();
            ArrayList<String> answers = q.getAnswers();
            String question_description = q.getQuestionDescription();

            switch (answers.size()) {
                case 1:
                    // it is a selection page for spins or grabs
                    if (answers.get(0).equals(getString(R.string.select_spins_text))) {
                        setContentView(R.layout.page_rotation);
                        initializeSpinsPicker();
                        ((TextView) findViewById(R.id.spins_title_textview)).setText(question);
                        ((Button) findViewById(R.id.spins_select_button)).setText(getString(R.string.select_button_text));
                        ((TextView) findViewById(R.id.spins_description_textview)).setText(question_description);
                    } else if (answers.get(0).equals(getString(R.string.select_grab_text))) {
                        setContentView(R.layout.page_grab);
                        initializeGrabSpinner();
                        initializeGrabImage();
                        ((TextView) findViewById(R.id.grab_title_textview)).setText(question);
                        ((Button) findViewById(R.id.grab_select_button)).setText(getString(R.string.select_button_text));
                        ((TextView) findViewById(R.id.grab_description_textview)).setText(question_description);
                    } else if (answers.get(0).equals(getString(R.string.select_feature_text))) {
                        setContentView(R.layout.page_feature);
                        initializeFeatureSpinner();
                        ((TextView) findViewById(R.id.feature_title_textview)).setText(question);
                        ((Button) findViewById(R.id.feature_select_button)).setText(getString(R.string.select_button_text));
                        ((TextView) findViewById(R.id.feature_description_textview)).setText(question_description);
                    }
                    break;
                case 2:
                    // it is a two option question
                    setContentView(R.layout.page_two_options);
                    ((TextView) findViewById(R.id.options2_title_textview)).setText(question);
                    ((Button) findViewById(R.id.options2_first_button)).setText(answers.get(0));
                    ((Button) findViewById(R.id.options2_second_button)).setText(answers.get(1));
                    ((TextView) findViewById(R.id.options2_description_textview)).setText(question_description);
                    break;
                case 3:
                    // it is a three option question
                    setContentView(R.layout.page_three_options);
                    ((TextView) findViewById(R.id.options3_title_textview)).setText(question);
                    ((Button) findViewById(R.id.options3_first_button)).setText(answers.get(0));
                    ((Button) findViewById(R.id.options3_second_button)).setText(answers.get(1));
                    ((Button) findViewById(R.id.options3_third_button)).setText(answers.get(2));
                    ((TextView) findViewById(R.id.options3_description_textview)).setText(question_description);
                    break;
                case 4:
                    // it is a four option question or Category question
                    if (!question.equals(getString(R.string.category_q_text))) {
                        setContentView(R.layout.page_four_options);
                        ((TextView) findViewById(R.id.options4_title_textview)).setText(question);
                        ((Button) findViewById(R.id.options4_first_button)).setText(answers.get(0));
                        ((Button) findViewById(R.id.options4_second_button)).setText(answers.get(1));
                        ((Button) findViewById(R.id.options4_third_button)).setText(answers.get(2));
                        ((Button) findViewById(R.id.options4_fourth_button)).setText(answers.get(3));
                        ((TextView) findViewById(R.id.options4_description_textview)).setText(question_description);
                    }
                    break;
            }

            if (previousQs.size() < 2)
                ((Button) findViewById(R.id.back_button)).setVisibility(View.INVISIBLE);
            else
                ((Button) findViewById(R.id.back_button)).setVisibility(View.VISIBLE);

            ((Button) findViewById(R.id.save_button)).setVisibility(View.INVISIBLE);
        }
        ((TextView) findViewById(R.id.top_bar_textview)).setText(currTree);
    }

    ////////////// Methods for initializing selection views ///////////////////
    private void initializeGrabSpinner() {
        grab_spinner = findViewById(R.id.grab_spinner);
        ArrayAdapter<String> grab_spinner_adapter = new ArrayAdapter<>(TrickDiscoverActivity.this,
                R.layout.item_spinner, grab_spinner_values);
        grab_spinner_adapter.setDropDownViewResource(R.layout.item_spinner);
        grab_spinner.setAdapter(grab_spinner_adapter);

        grab_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (grab_spinner.getSelectedItem().toString().equals(other))
                    ((EditText) findViewById(R.id.grab_other_edittext)).setVisibility(View.VISIBLE);
                else
                    ((EditText) findViewById(R.id.grab_other_edittext)).setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("LOG!", "nothing!!!");
            }
        });
    }

    private void initializeSpinsPicker() {
        spins_numberpicker = findViewById(R.id.spins_numberpicker);
        spins_numberpicker.setMaxValue(11);
        spins_numberpicker.setMinValue(0);
        spins_numberpicker.setDisplayedValues(spins_numberpicker_values);
    }

    private void initializeGrabImage() {
        ImageView grab_imageview = findViewById(R.id.grab_imageview);
        Zoomy.Builder builder = new Zoomy.Builder(this).target(grab_imageview);
        builder.register();
        int[] images_ids = {R.drawable.snowboardaddiction_grabs, R.drawable.crab_grab_goofy, R.drawable.crab_grab_regular};
        ImageButton cycle_image_button = findViewById(R.id.cycle_images_button);
        cycle_image_button.setOnClickListener(new View.OnClickListener() {
            int curr_index = 0;
            @Override
            public void onClick(View view) {
                Log.d("LOG!", "image tapped");
                if(curr_index < images_ids.length - 1)
                    curr_index++;
                else
                    curr_index = 0;
                grab_imageview.setImageResource(images_ids[curr_index]);
            }
        });
    }

    private void initializeFeatureSpinner() {
        feature_spinner = findViewById(R.id.feature_spinner);
        ArrayAdapter<String> feature_spinner_adapter = new ArrayAdapter<>(TrickDiscoverActivity.this,
                R.layout.item_spinner, feature_spinner_values);
        feature_spinner_adapter.setDropDownViewResource(R.layout.item_spinner);
        feature_spinner.setAdapter(feature_spinner_adapter);
        feature_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (feature_spinner.getSelectedItem().toString().equals(other))
                    ((EditText) findViewById(R.id.feature_other_edittext)).setVisibility(View.VISIBLE);
                else
                    ((EditText) findViewById(R.id.feature_other_edittext)).setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("LOG!", "nothing!!!");
            }
        });
    }

    ////////////// Methods for reseting certain global variables ///////////////////
    private void resetTrickVars() {
        spins_amount = "";
        spins_amount2 = "";
        grab_name = "";
        feature_name = "";
        previousQs = new ArrayList<>();
        currTree = getString(R.string.top_bar_beginning_search_text);
        currCategory = "";
        Q.resetRuleStack();
    }

    private void resetButterTrickVars() {
        in_butter_tree = false;
        butter_trick_name = "";
    }


    //////// Methods set to buttons in the screen layouts /////////
    public void moveToNextScreen(View view) {
        String answer = ((Button) view).getText().toString();
        if (curr_Q.getQuestion().equals(getString(R.string.category_q_text))) {
            if (answer.equals(getString(R.string.butter_button_text))) {
                in_butter_tree = true;
                currCategory = getString(R.string.butter_button_text);
            } else if(answer.equals(getString(R.string.aerial_button_text)))
                currCategory = getString(R.string.aerial_button_text);
            else if (answer.equals(getString(R.string.jibb_button_text)))
                currCategory = getString(R.string.jibb_button_text);
            showCategoryDescriptionAlertDialog(answer);
            currTree = getString(R.string.top_bar_search_in_tree_text) + " " + answer;
        }

        if (answer.equals(getString(R.string.select_button_text))) {
            if (view.getId() == R.id.spins_select_button) {
                if (spins_amount.equals("")) {
                    spins_amount = spins_numberpicker_values[spins_numberpicker.getValue()];
                } else {
                    spins_amount2 = spins_numberpicker_values[spins_numberpicker.getValue()];
                }
                answer = getString(R.string.select_spins_text);
            } else if (view.getId() == R.id.grab_select_button) {
                grab_name = grab_spinner.getSelectedItem().toString();
                if (grab_name.equals(other))
                    grab_name = ((EditText) findViewById(R.id.grab_other_edittext)).getText().toString();
                answer = getString(R.string.select_grab_text);
            } else if (view.getId() == R.id.feature_select_button) {
                feature_name = feature_spinner.getSelectedItem().toString();
                if (feature_name.equals(other))
                    feature_name = ((EditText) findViewById(R.id.feature_other_edittext)).getText().toString();
                answer = getString(R.string.select_feature_text);
            }
        }
        //Log.d("LOG!", answer);
        previousQs.add(curr_Q);
        curr_Q = curr_Q.getAnswerChild(answer);
        //Log.d("LOG!", curr_Q.getQuestion());
        setQtoLayout(curr_Q);
    }

    public void restartTrickSearch(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getString(R.string.are_you_sure_rest_text));
        dialog.setNegativeButton(getString(R.string.no_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.setPositiveButton(getString(R.string.yes_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setCategoryPage();
            }
        });
        dialog.create().show();
    }

    public void saveTrick(View view) {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String formattedDate = df.format(c);

        boolean isDuplicate = false;
        boolean isCompleted = false;
        TricksDBHepler tricksDBHepler = new TricksDBHepler(this);
        Cursor data = tricksDBHepler.getData();
        while(data.moveToNext()) {
            if(data.getString(1).equals(currCategory) && data.getString(2).equals(currTrick)) {
                isDuplicate = true;
                if(data.getInt(6) == 1)
                    isCompleted = true;
                break;
            }
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setNeutralButton(getString(R.string.cancel_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        if(isDuplicate) {
            if(isCompleted)
                dialog.setMessage(getString(R.string.duplicate_completed_list));
            else
                dialog.setMessage(getString(R.string.duplicate_todo_list));

            dialog.setPositiveButton(getString(R.string.replace_button_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.setMessage(getString(R.string.save_dialog_text));
                    dialog.setPositiveButton(getString(R.string.tricks_completed_button_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int display_short = 0;
                            if(((Button) findViewById(R.id.trick_to_short_vers_button)).getText().toString().equals(getString(R.string.lengthen_button_text)))
                                display_short = 1;
                            setResult(RESULT_OK, new Intent().putExtra(getString(R.string.add_replacement_key), new TrickData(
                                    currCategory,
                                    currTrick,
                                    TrickData.shortenTrickName(currTrick),
                                    formattedDate,
                                    "",
                                    1,
                                    display_short)));
                            finish();
                        }
                    });
                    dialog.setNegativeButton(getString(R.string.tricks_to_do_button_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int display_short = 0;
                            if(((Button) findViewById(R.id.trick_to_short_vers_button)).getText().toString().equals(getString(R.string.lengthen_button_text)))
                                display_short = 1;
                            setResult(RESULT_OK, new Intent().putExtra(getString(R.string.add_replacement_key), new TrickData(
                                    currCategory,
                                    currTrick,
                                    TrickData.shortenTrickName(currTrick),
                                    formattedDate,
                                    "",
                                    0,
                                    display_short)));
                            finish();
                        }
                    });
                    dialog.create().show();
                }
            });
        } else {
            dialog.setMessage(getString(R.string.save_dialog_text));
            dialog.setPositiveButton(getString(R.string.tricks_completed_button_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int display_short = 0;
                    if(((Button) findViewById(R.id.trick_to_short_vers_button)).getText().toString().equals(getString(R.string.lengthen_button_text)))
                        display_short = 1;
                    setResult(RESULT_FIRST_USER, new Intent().putExtra(getString(R.string.add_regular_key), new TrickData(
                            currCategory,
                            currTrick,
                            TrickData.shortenTrickName(currTrick),
                            formattedDate,
                            "",
                            1,
                            display_short)));
                    finish();
                }
            });
            dialog.setNegativeButton(getString(R.string.tricks_to_do_button_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int display_short = 0;
                    if(((Button) findViewById(R.id.trick_to_short_vers_button)).getText().toString().equals(getString(R.string.lengthen_button_text)))
                        display_short = 1;
                    setResult(RESULT_FIRST_USER, new Intent().putExtra(getString(R.string.add_regular_key), new TrickData(
                            currCategory,
                            currTrick,
                            TrickData.shortenTrickName(currTrick),
                            formattedDate,
                            "",
                            0,
                            display_short)));
                    finish();
                }
            });
        }

        dialog.create().show();
    }

    public void backToPreviousScreen(View view) {
        int butter_last_ind = butter_trick_name.lastIndexOf(" to ");
        if (curr_Q.isEnd() && in_butter_tree) {
            if (butter_last_ind >= 0)
                butter_trick_name = butter_trick_name.substring(0, butter_last_ind);
            else
                butter_trick_name = "";
        }
        curr_Q = previousQs.remove(previousQs.size() - 1);
        if (curr_Q.getAnswers().get(0).equals(getString(R.string.select_spins_text))) {
            if (!spins_amount2.equals("")) {
                spins_amount2 = "";
            } else {
                spins_amount = "";
            }
        }
        setQtoLayout(curr_Q);
    }

    public void butterContinue(View view) {
        resetTrickVars();
        currCategory = getString(R.string.butter_button_text);
        currTree = getString(R.string.top_bar_search_in_tree_text) + " " + getString(R.string.butter_button_text);
        curr_Q = root.getAnswerChild(getString(R.string.butter_button_text));
        setQtoLayout(curr_Q);
    }

    public void butterComplete(View v) {
        setTrickNamePage(curr_Q);
        ((Button) findViewById(R.id.back_button)).setVisibility(View.INVISIBLE);
    }

    public void quitTrickDiscoverActivity(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getString(R.string.are_you_sure_exit_text));
        dialog.setNegativeButton(getString(R.string.no_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.setPositiveButton(getString(R.string.yes_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        dialog.create().show();
    }

    @Override
    public void onBackPressed() {
        quitTrickDiscoverActivity(null);
    }

    ////////////////// Dialog pop ups //////////////////////
    public void showAnswerDescriptionDialog(View view) {
        String answer = ((Button) view).getText().toString();
        String description = curr_Q.getAnswerDescription(answer);

        if (description == null || description.equals("")) {
            if (toast != null) toast.cancel();
            toast = Toast.makeText(TrickDiscoverActivity.this, getString(R.string.no_description_dialog_text), Toast.LENGTH_SHORT);
            toast.show();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(description);
            dialog.setTitle(answer);
            dialog.setNegativeButton(getString(R.string.close_button_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.create().show();
        }
    }

    public void showFullTrickNameDialog(View view) {
        String trick = ((TextView) view).getText().toString();

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(trick);
        dialog.setNegativeButton(getString(R.string.close_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.create().show();
    }

    public void showCategoryDescriptionAlertDialog(String category) {
        String pref_key;
        String message;
        if (category.equals(getString(R.string.aerial_button_text))) {
            pref_key = dontshowAerialExplainKey;
            message = getString(R.string.aerial_explain_text);
        } else if (category.equals(getString(R.string.butter_button_text))) {
            pref_key = dontshowButterExplainKey;
            message = getString(R.string.butter_explain_text);
        } else if (category.equals(getString(R.string.jibb_button_text))) {
            pref_key = dontshowJibExplainKey;
            message = getString(R.string.jib_explain_text);
        } else {
            // TODO: Other option
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater dialogInflater = LayoutInflater.from(this);
        View descriptLayout = dialogInflater.inflate(R.layout.checkbox_dont_show_again, null);
        CheckBox dontShowAgain = descriptLayout.findViewById(R.id.skip_category_alert);
        dialog.setView(descriptLayout);
        dialog.setTitle(getString(R.string.important_button_text));
        dialog.setMessage(message);
        dialog.setNegativeButton(getString(R.string.ok_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dontShowAgain.isChecked()) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(pref_key, checked_val);
                    editor.apply();
                }
            }
        });

        if (sharedPref.getString(pref_key, "").equals("")) {
            dialog.show();
        }
    }

    //////// Methods that build the trick naming tree ////////
    private void createTrickTree() {
        root = new Q(getString(R.string.category_q_text),
                getString(R.string.aerial_button_text),
                getString(R.string.jibb_button_text),
                getString(R.string.butter_button_text));

        root.setAnswerDescriptions(getString(R.string.aerial_button_text),
                getString(R.string.aerial_description_text));
        root.setAnswerDescriptions(getString(R.string.jibb_button_text),
                getString(R.string.jib_description_text));
        root.setAnswerDescriptions(getString(R.string.butter_button_text),
                getString(R.string.butter_description_text));

        root = createAerialTree(root);
        root = createJibTree(root);
        root = createButterTree(root);
    }

    // TODO: add short trick name descriptions that will be populated as the trick notes
    private Q createAerialTree(Q root) {
        // level 1
        Q aerial =
                root.setChildToAnswer(getString(R.string.aerial_button_text),
                        new Q(getString(R.string.approach_stance_q_text),
                                getString(R.string.switch_button_text),
                                getString(R.string.natural_button_text),
                                getString(R.string.skip_button_text)));
        aerial.setQuestionDescription(getString(R.string.stance_approach_description_text));

        // Level 2
        Q aerial_switch =
                aerial.setChildToAnswer(getString(R.string.switch_button_text),
                        new Q(getString(R.string.any_spins_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        aerial_switch.setQuestionDescription(getString(R.string.spins_performed_description_text));
        aerial.setRuleToAnswer(getString(R.string.natural_button_text),
                aerial_switch,
                aerial_switch_to_natural_rule);
        aerial.setRuleToAnswer(getString(R.string.skip_button_text),
                aerial_switch,
                aerial_switch_to_natural_rule);

        // Level 3
        Q aerial_switch_yes =
                aerial_switch.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        aerial_switch_yes.setQuestionDescription(getString(R.string.rotation_direction_description_text));
        Q aerial_switch_no =
                aerial_switch.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.inversion_performed_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        aerial_switch_no.setQuestionDescription(getString(R.string.inversion_performed_description_text));

        // Level 4
        Q aerial_switch_yes_front = // many different selections
                aerial_switch_yes.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(getString(R.string.many_spins_q_text),
                                getString(R.string.select_spins_text)));
        aerial_switch_yes_front.setQuestionDescription(getString(R.string.select_spins_text));
        aerial_switch_yes.setRuleToAnswer(getString(R.string.backside_button_text),
                aerial_switch_yes_front,
                cab_to_switch_backside_rule);

        Q aerial_switch_no_yes =
                aerial_switch_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.inversion_dir_q_text),
                                getString(R.string.front_button_text),
                                getString(R.string.back_button_text)));
        aerial_switch_no_yes.setQuestionDescription(getString(R.string.inversion_direction_description_text));
        Q aerial_switch_no_no =
                aerial_switch_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.pop_used_q_text),
                                getString(R.string.ollie_button_text),
                                getString(R.string.nollie_button_text),
                                getString(R.string.straight_button_text)));
        aerial_switch_no_no.setQuestionDescription(getString(R.string.pop_description_text));

        // Level 5
        Q aerial_switch_yes_front_spins =
                aerial_switch_yes_front.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(getString(R.string.grab_performed_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        aerial_switch_yes_front_spins.setQuestionDescription(getString(R.string.grab_performed_description_text));

        Q aerial_switch_no_yes_front =
                aerial_switch_no_yes.setChildToAnswer(getString(R.string.front_button_text),
                        new Q(getString(R.string.num_flips_q_text),
                                "1",
                                "2",
                                "3"));
        Q aerial_switch_no_yes_back =
                aerial_switch_no_yes.setChildToAnswer(getString(R.string.back_button_text),
                        new Q(getString(R.string.num_flips_q_text),
                                "1",
                                "2",
                                "3"));

        Q aerial_switch_no_no_ollie =
                aerial_switch_no_no.setChildToAnswer(getString(R.string.ollie_button_text),
                        new Q(getString(R.string.grab_performed_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        aerial_switch_no_no_ollie.setQuestionDescription(getString(R.string.grab_performed_description_text));
        aerial_switch_no_no.setRuleToAnswer(getString(R.string.nollie_button_text),
                aerial_switch_no_no_ollie,
                switch_ollie_to_fakie_rule);
        aerial_switch_no_no.setRuleToAnswer(getString(R.string.straight_button_text),
                aerial_switch_no_no_ollie,
                remove_ollie_rule);

        // Level 6
        Q aerial_switch_no_no_ollie_yes =
                aerial_switch_no_no_ollie.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.select_grab_q_text),
                                getString(R.string.select_grab_text)));
        aerial_switch_no_no_ollie_yes.setQuestionDescription(getString(R.string.grab_description_text));
        Q aerial_switch_no_no_ollie_no =
                aerial_switch_no_no_ollie.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.shifty_performed_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        aerial_switch_no_no_ollie_no.setQuestionDescription(getString(R.string.shifty_description_text));

        Q aerial_switch_yes_front_spins_yes =
                aerial_switch_yes_front_spins.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.select_grab_q_text),
                                getString(R.string.select_grab_text)));
        aerial_switch_yes_front_spins_yes.setQuestionDescription(getString(R.string.grab_description_text));
        Q aerial_switch_yes_front_spins_no =
                aerial_switch_yes_front_spins.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.shifty_performed_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        aerial_switch_yes_front_spins_no.setQuestionDescription(getString(R.string.shifty_description_text));

        Q aerial_switch_no_yes_front_1 =
                aerial_switch_no_yes_front.setChildToAnswer("1",
                        new Q(switch_ + tamedog_));
        Q aerial_switch_no_yes_front_2 =
                aerial_switch_no_yes_front.setChildToAnswer("2",
                        new Q(switch_ + double_ + tamedog_));
        Q aerial_switch_no_yes_front_3 =
                aerial_switch_no_yes_front.setChildToAnswer("3",
                        new Q(switch_ + triple_ + tamedog_));

        Q aerial_switch_no_yes_back_1 =
                aerial_switch_no_yes_back.setChildToAnswer("1",
                        new Q(switch_ + wildcat_));
        Q aerial_switch_no_yes_back_2 =
                aerial_switch_no_yes_back.setChildToAnswer("2",
                        new Q(switch_ + supercat_));
        Q aerial_switch_no_yes_back_3 =
                aerial_switch_no_yes_back.setChildToAnswer("3",
                        new Q(switch_ + triple_ + wildcat_));

        // Level 7
        Q aerial_switch_no_no_ollie_no_yes =
                aerial_switch_no_no_ollie_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(switch_ + ollie_ + straight_ + air_ + shifty_));
        Q aerial_switch_no_no_ollie_no_no =
                aerial_switch_no_no_ollie_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + ollie_ + straight_ + air_));

        Q aerial_switch_no_no_ollie_yes_grab =
                aerial_switch_no_no_ollie_yes.setChildToAnswer(getString(R.string.select_grab_text),
                        new Q(switch_ + ollie_ + straight_ + air_ + grabPlaceholder_grab_));

        Q aerial_switch_yes_front_spins_yes_grab =
                aerial_switch_yes_front_spins_yes.setChildToAnswer(getString(R.string.select_grab_text),
                        new Q(cab_ + rotationPlaceholder_ + grabPlaceholder_grab_));

        Q aerial_switch_yes_front_spins_no_yes =
                aerial_switch_yes_front_spins_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(cab_ + rotationPlaceholder_ + shifty_));
        Q aerial_switch_yes_front_spins_no_no =
                aerial_switch_yes_front_spins_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(cab_ + rotationPlaceholder_));

        return root;
    }

    private Q createJibTree(Q root) {
        // Level 1
        Q jib =
                root.setChildToAnswer(getString(R.string.jibb_button_text),
                        new Q(getString(R.string.type_of_jib_q_text),
                                getString(R.string.slide_button_text),
                                getString(R.string.stall_button_text),
                                getString(R.string.tap_button_text)));
        jib.setQuestionDescription(getString(R.string.jib_type_description_text));

        // Level 2
        Q jib_slide =
                jib.setChildToAnswer(getString(R.string.slide_button_text),
                        new Q(getString(R.string.feature_q_text),
                                getString(R.string.select_feature_text)));
        jib_slide.setQuestionDescription(getString(R.string.feature_description_text));

        Q jib_stall =
                jib.setChildToAnswer(getString(R.string.stall_button_text),
                        new Q(getString(R.string.approach_stance_q_text),
                                getString(R.string.switch_button_text),
                                getString(R.string.natural_button_text),
                                getString(R.string.skip_button_text)));
        jib_stall.setQuestionDescription(getString(R.string.stance_approach_description_text));

        Q jib_tap =
                jib.setChildToAnswer(getString(R.string.tap_button_text),
                        new Q(getString(R.string.feature_tapped_q_text),
                                getString(R.string.select_feature_text)));
        jib_tap.setQuestionDescription(getString(R.string.feature_description_text));

        // Level 3
        Q jib_slide_feature =
                jib_slide.setChildToAnswer(getString(R.string.select_feature_text),
                        new Q(getString(R.string.approach_stance_q_text),
                                getString(R.string.switch_button_text),
                                getString(R.string.natural_button_text),
                                getString(R.string.skip_button_text)));
        jib_slide_feature.setQuestionDescription(getString(R.string.stance_approach_description_text));

        Q jib_stall_switch =
                jib_stall.setChildToAnswer(getString(R.string.switch_button_text),
                        new Q(getString(R.string.before_stall_feature_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));

        Q jib_stall_natural =
                jib_stall.setChildToAnswer(getString(R.string.natural_button_text),
                        new Q(getString(R.string.before_stall_feature_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        Q jib_stall_skip =
                jib_stall.setChildToAnswer(getString(R.string.skip_button_text),
                        jib_stall_natural);

        Q jib_tap_feature =
                jib_tap.setChildToAnswer(getString(R.string.select_feature_text),
                        new Q(getString(R.string.nose_tail_tap_out_q_text),
                                getString(R.string.nose_button_text),
                                getString(R.string.tail_button_text),
                                getString(R.string.skip_button_text)));
        jib_tap_feature.setQuestionDescription(getString(R.string.tap_board_description_text));

        // Level 4
        Q jib_slide_feature_switch =
                jib_slide_feature.setChildToAnswer(getString(R.string.switch_button_text),
                        new Q(getString(R.string.side_of_feature_approach_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text),
                                getString(R.string.straight_on_button_text)));
        jib_slide_feature_switch.setQuestionDescription(getString(R.string.side_feature_approached_description_text));
        Q jib_slide_feature_natural =
                jib_slide_feature.setRuleToAnswer(getString(R.string.natural_button_text),
                        jib_slide_feature_switch,
                        jib_switch_to_natural_rule);
        Q jib_slide_feature_skip =
                jib_slide_feature.setRuleToAnswer(getString(R.string.skip_button_text),
                        jib_slide_feature_switch,
                        jib_switch_to_natural_rule);

        Q jib_stall_switch_yes =
                jib_stall_switch.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        jib_stall_switch_yes.setQuestionDescription(getString(R.string.rotation_direction_description_text));
        Q jib_stall_switch_no =
                jib_stall_switch.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.board_contact_feature_stall_q_text),
                                getString(R.string.trailing_foot_button_text),
                                getString(R.string.inbetween_feet_button_text)));
        jib_stall_switch_no.setQuestionDescription(getString(R.string.stall_contact_description_text));

        Q jib_stall_natural_yes =
                jib_stall_natural.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        jib_stall_natural_yes.setQuestionDescription(getString(R.string.rotation_direction_description_text));
        Q jib_stall_natural_no =
                jib_stall_natural.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.board_contact_feature_stall_q_text),
                                getString(R.string.trailing_foot_button_text),
                                getString(R.string.inbetween_feet_button_text)));
        jib_stall_natural_no.setQuestionDescription(getString(R.string.stall_contact_description_text));

        Q jib_tap_feature_nose =
                jib_tap_feature.setChildToAnswer(getString(R.string.nose_button_text),
                        new Q(featurePlaceholder_ + nose_ + bonk_));
        Q jib_tap_feature_tail =
                jib_tap_feature.setChildToAnswer(getString(R.string.tail_button_text),
                        new Q(featurePlaceholder_ + tail_ + bonk_));
        Q jib_tap_feature_skip =
                jib_tap_feature.setChildToAnswer(getString(R.string.skip_button_text),
                        new Q(featurePlaceholder_ + bonk_));

        // Level 5
        Q jib_slide_feature_switch_frontside =
                jib_slide_feature_switch.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(getString(R.string.rotation_on_less_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_frontside.setQuestionDescription(getString(R.string.rotation_onto_feature_description_text));
        Q jib_slide_feature_switch_backside =
                jib_slide_feature_switch.setChildToAnswer(getString(R.string.backside_button_text),
                        new Q(getString(R.string.rotation_on_less_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_backside.setQuestionDescription(getString(R.string.rotation_onto_feature_description_text));
        Q jib_slide_feature_switch_straight =
                jib_slide_feature_switch.setChildToAnswer(getString(R.string.straight_on_button_text),
                        new Q(getString(R.string.rotation_on_less_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_straight.setQuestionDescription(getString(R.string.rotation_onto_feature_description_text));

        Q jib_stall_switch_yes_frontside =
                jib_stall_switch_yes.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(getString(R.string.after_stall_feature_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_stall_switch_yes_frontside.setQuestionDescription(getString(R.string.after_stall_rotation_description_text));
        Q jib_stall_switch_yes_backside =
                jib_stall_switch_yes.setRuleToAnswer(getString(R.string.backside_button_text),
                        jib_stall_switch_yes_frontside,
                        frontside_to_backside_rule);

        Q jib_stall_switch_no_trailing =
                jib_stall_switch_no.setChildToAnswer(getString(R.string.trailing_foot_button_text),
                        new Q(getString(R.string.press_back_foot_stall_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_stall_switch_no_trailing.setQuestionDescription(getString(R.string.press_stall_description_text));
        Q jib_stall_switch_no_inbetween =
                jib_stall_switch_no.setChildToAnswer(getString(R.string.inbetween_feet_button_text),
                        new Q(getString(R.string.after_stall_feature_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_stall_switch_no_inbetween.setQuestionDescription(getString(R.string.after_stall_rotation_description_text));

        Q jib_stall_natural_yes_frontside =
                jib_stall_natural_yes.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(getString(R.string.after_stall_feature_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_stall_natural_yes_frontside.setQuestionDescription(getString(R.string.after_stall_rotation_description_text));
        Q jib_stall_natural_yes_backside =
                jib_stall_natural_yes.setRuleToAnswer(getString(R.string.backside_button_text),
                        jib_stall_natural_yes_frontside,
                        frontside_to_backside_rule);

        Q jib_stall_natural_no_trailing =
                jib_stall_natural_no.setChildToAnswer(getString(R.string.trailing_foot_button_text),
                        new Q(getString(R.string.press_back_foot_stall_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_stall_natural_no_trailing.setQuestionDescription(getString(R.string.press_stall_description_text));
        Q jib_stall_natural_no_inbetween =
                jib_stall_natural_no.setChildToAnswer(getString(R.string.inbetween_feet_button_text),
                        new Q(getString(R.string.after_stall_feature_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_stall_natural_no_inbetween.setQuestionDescription(getString(R.string.after_stall_rotation_description_text));

        // Level 6
        Q jib_slide_feature_switch_frontside_yes =
                jib_slide_feature_switch_frontside.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.type_slide_on_feature_q_text),
                                getString(R.string.fifty_fifty_button_text),
                                getString(R.string.boardslide_button_text),
                                getString(R.string.lipslide_button_text)));
        jib_slide_feature_switch_frontside_yes.setQuestionDescription(getString(R.string.type_slide_description_text));
        Q jib_slide_feature_switch_frontside_no =
                jib_slide_feature_switch_frontside.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.select_rotation_amount_on_q_text),
                                getString(R.string.num_180_button_text),
                                getString(R.string.num_270_button_text)));
        jib_slide_feature_switch_frontside_no.setQuestionDescription(getString(R.string.select_rotation_180_270_description_text));

        Q jib_slide_feature_switch_backside_yes =
                jib_slide_feature_switch_backside.setRuleToAnswer(getString(R.string.yes_button_text),
                        jib_slide_feature_switch_frontside_yes,
                        frontside_to_backside_rule);
        Q jib_slide_feature_switch_backside_no =
                jib_slide_feature_switch_backside.setRuleToAnswer(getString(R.string.no_button_text),
                        jib_slide_feature_switch_frontside_no,
                        swap_switch_backside_with_cab_rule);

        Q jib_slide_feature_switch_straight_yes =
                jib_slide_feature_switch_straight.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.no_rotation_button_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        jib_slide_feature_switch_straight_yes.setQuestionDescription(getString(R.string.rotation_direction_description_text));
        Q jib_slide_feature_switch_straight_no =
                jib_slide_feature_switch_straight.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        jib_slide_feature_switch_straight_no.setQuestionDescription(getString(R.string.rotation_direction_description_text));

        Q jib_stall_switch_yes_frontside_yes =
                jib_stall_switch_yes_frontside.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(switch_ + frontside_ + disaster_ + to_ + fakie_));
        Q jib_stall_switch_yes_frontside_no =
                jib_stall_switch_yes_frontside.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + frontside_ + disaster_));

        Q jib_stall_switch_no_trailing_yes =
                jib_stall_switch_no_trailing.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.after_stall_feature_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_stall_switch_no_trailing_yes.setQuestionDescription(getString(R.string.after_stall_rotation_description_text));
        Q jib_stall_switch_no_trailing_no =
                jib_stall_switch_no_trailing.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.after_stall_feature_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_stall_switch_no_trailing_no.setQuestionDescription(getString(R.string.after_stall_rotation_description_text));

        Q jib_stall_switch_no_inbetween_yes =
                jib_stall_switch_no_inbetween.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(switch_ + board_ + stall_ + to_ + fakie_));
        Q jib_stall_switch_no_inbetween_no =
                jib_stall_switch_no_inbetween.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + board_ + stall_));

        Q jib_stall_natural_yes_frontside_yes =
                jib_stall_natural_yes_frontside.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(frontside_ + disaster_ + to_ + fakie_));
        Q jib_stall_natural_yes_frontside_no =
                jib_stall_natural_yes_frontside.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(frontside_ + disaster_));

        Q jib_stall_natural_no_trailing_yes =
                jib_stall_natural_no_trailing.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.after_stall_feature_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_stall_natural_no_trailing_yes.setQuestionDescription(getString(R.string.after_stall_rotation_description_text));
        Q jib_stall_natural_no_trailing_no =
                jib_stall_natural_no_trailing.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.after_stall_feature_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_stall_natural_no_trailing_no.setQuestionDescription(getString(R.string.after_stall_rotation_description_text));

        Q jib_stall_natural_no_inbetween_yes =
                jib_stall_natural_no_inbetween.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(switch_ + board_ + stall_));
        Q jib_stall_natural_no_inbetween_no =
                jib_stall_natural_no_inbetween.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + board_ + stall_ + to_ + fakie_));

        // Level 7
        Q jib_slide_feature_switch_frontside_yes_fifty =
                jib_slide_feature_switch_frontside_yes.setChildToAnswer(getString(R.string.fifty_fifty_button_text),
                        new Q(getString(R.string.press_performed_on_feature_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_frontside_yes_fifty.setQuestionDescription(getString(R.string.press_description_text));
        Q jib_slide_feature_switch_frontside_yes_boardslide =
                jib_slide_feature_switch_frontside_yes.setChildToAnswer(getString(R.string.boardslide_button_text),
                        new Q(getString(R.string.part_board_on_top_feature_q_text),
                                getString(R.string.leading_foot_button_text),
                                getString(R.string.trailing_foot_button_text),
                                getString(R.string.inbetween_feet_button_text)));
        jib_slide_feature_switch_frontside_yes_boardslide.setQuestionDescription(getString(R.string.part_board_feature_description_text));
        Q jib_slide_feature_switch_frontside_yes_lipslide =
                jib_slide_feature_switch_frontside_yes.setChildToAnswer(getString(R.string.lipslide_button_text),
                        new Q(getString(R.string.part_board_on_top_feature_q_text),
                                getString(R.string.leading_foot_button_text),
                                getString(R.string.trailing_foot_button_text),
                                getString(R.string.inbetween_feet_button_text)));
        jib_slide_feature_switch_frontside_yes_lipslide.setQuestionDescription(getString(R.string.part_board_feature_description_text));

        Q jib_slide_feature_switch_frontside_no_180 =
                jib_slide_feature_switch_frontside_no.setChildToAnswer(getString(R.string.num_180_button_text),
                        new Q(getString(R.string.board_pass_over_feature_q_text),
                                getString(R.string.front_button_text),
                                getString(R.string.back_button_text)));
        jib_slide_feature_switch_frontside_no_180.setQuestionDescription(getString(R.string.board_pass_over_description_text));
        Q jib_slide_feature_switch_frontside_no_270 =
                jib_slide_feature_switch_frontside_no.setChildToAnswer(getString(R.string.num_270_button_text),
                        new Q(getString(R.string.board_pass_over_feature_q_text),
                                getString(R.string.front_button_text),
                                getString(R.string.back_button_text)));
        jib_slide_feature_switch_frontside_no_270.setQuestionDescription(getString(R.string.board_pass_over_description_text));

        Q jib_slide_feature_switch_straight_yes_norotation =
                jib_slide_feature_switch_straight_yes.setChildToAnswer(getString(R.string.no_rotation_button_text),
                        new Q(getString(R.string.press_performed_on_feature_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_straight_yes_norotation.setQuestionDescription(getString(R.string.press_description_text));
        Q jib_slide_feature_switch_straight_yes_frontside =
                jib_slide_feature_switch_straight_yes.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(getString(R.string.rotation_off_90_less_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_straight_yes_frontside.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));
        Q jib_slide_feature_switch_straight_yes_backside =
                jib_slide_feature_switch_straight_yes.setRuleToAnswer(getString(R.string.backside_button_text),
                        jib_slide_feature_switch_straight_yes_frontside,
                        backside_to_frontside_rule);

        Q jib_slide_feature_switch_straight_no_frontside =
                jib_slide_feature_switch_straight_no.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(getString(R.string.more_180_continued_on_feature_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_straight_no_frontside.setQuestionDescription(getString(R.string.spin_continued_on_feature_description_text));
        Q jib_slide_feature_switch_straight_no_backside =
                jib_slide_feature_switch_straight_no.setRuleToAnswer(getString(R.string.backside_button_text),
                        jib_slide_feature_switch_straight_no_frontside,
                        cab_to_switch_backside_rule);

        Q jib_stall_switch_no_trailing_yes_yes =
                jib_stall_switch_no_trailing_yes.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(switch_ + rock_ + stall_ + to_ + fakie_));
        Q jib_stall_switch_no_trailing_yes_no =
                jib_stall_switch_no_trailing_yes.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + rock_ + stall_));

        Q jib_stall_switch_no_trailing_no_yes =
                jib_stall_switch_no_trailing_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(nose_ + stall_ + to_ + fakie_));
        Q jib_stall_switch_no_trailing_no_no =
                jib_stall_switch_no_trailing_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(nose_ + stall_));

        Q jib_stall_natural_no_trailing_yes_yes =
                jib_stall_natural_no_trailing_yes.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(rock_ + stall_));
        Q jib_stall_natural_no_trailing_yes_no =
                jib_stall_natural_no_trailing_yes.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(rock_ + stall_ + to_ + fakie_));

        Q jib_stall_natural_no_trailing_no_yes =
                jib_stall_natural_no_trailing_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(tail_ + stall_));
        Q jib_stall_natural_no_trailing_no_no =
                jib_stall_natural_no_trailing_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(tail_ + stall_ + to_ + fakie_));

        // Level 8
        Q jib_slide_feature_switch_frontside_yes_fifty_yes =
                jib_slide_feature_switch_frontside_yes_fifty.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.type_press_q_text),
                                getString(R.string.tail_press_button_text),
                                getString(R.string.nose_press_button_text)));
        jib_slide_feature_switch_frontside_yes_fifty_yes.setQuestionDescription(getString(R.string.press_description_text));

        Q jib_slide_feature_switch_frontside_yes_boardslide_leading =
                jib_slide_feature_switch_frontside_yes_boardslide.setChildToAnswer(getString(R.string.leading_foot_button_text),
                        new Q(getString(R.string.rotation_off_90_less_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_frontside_yes_boardslide_leading.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));
        Q jib_slide_feature_switch_frontside_yes_boardslide_trailing =
                jib_slide_feature_switch_frontside_yes_boardslide.setRuleToAnswer(getString(R.string.trailing_foot_button_text),
                        jib_slide_feature_switch_frontside_yes_boardslide_leading,
                        noseslide_to_bluntslide_rule);
        Q jib_slide_feature_switch_frontside_yes_boardslide_inbetween =
                jib_slide_feature_switch_frontside_yes_boardslide.setRuleToAnswer(getString(R.string.inbetween_feet_button_text),
                        jib_slide_feature_switch_frontside_yes_boardslide_leading,
                        noseslide_to_boardslide_rule);

        Q jib_slide_feature_switch_frontside_yes_lipslide_leading =
                jib_slide_feature_switch_frontside_yes_lipslide.setRuleToAnswer(getString(R.string.leading_foot_button_text),
                        jib_slide_feature_switch_frontside_yes_boardslide_leading,
                        noseslide_to_noseblunt_rule);
        Q jib_slide_feature_switch_frontside_yes_lipslide_trailing =
                jib_slide_feature_switch_frontside_yes_lipslide.setRuleToAnswer(getString(R.string.trailing_foot_button_text),
                        jib_slide_feature_switch_frontside_yes_boardslide_leading,
                        noseslide_to_tailslide_rule);
        Q jib_slide_feature_switch_frontside_yes_lipslide_inbetween =
                jib_slide_feature_switch_frontside_yes_lipslide.setRuleToAnswer(getString(R.string.inbetween_feet_button_text),
                        jib_slide_feature_switch_frontside_yes_boardslide_leading,
                        noseslide_to_lipslide_rule);

        Q jib_slide_feature_switch_frontside_no_180_front =
                jib_slide_feature_switch_frontside_no_180.setChildToAnswer(getString(R.string.front_button_text),
                        new Q(getString(R.string.rotation_off_more_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_frontside_no_180_front.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));

        Q jib_slide_feature_switch_frontside_no_180_back =
                jib_slide_feature_switch_frontside_no_180.setRuleToAnswer(getString(R.string.back_button_text),
                        jib_slide_feature_switch_frontside_no_180_front,
                        switch_backside_to_hardway_cab_rule);

        Q jib_slide_feature_switch_frontside_no_270_front =
                jib_slide_feature_switch_frontside_no_270.setChildToAnswer(getString(R.string.front_button_text),
                        new Q(getString(R.string.rotation_off_90_less_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_frontside_no_270_front.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));
        Q jib_slide_feature_switch_frontside_no_270_back =
                jib_slide_feature_switch_frontside_no_270.setRuleToAnswer(getString(R.string.back_button_text),
                        jib_slide_feature_switch_frontside_no_270_front,
                        switch_backside_to_hardway_cab_and_boardslide_to_lipslide_rule);

        Q jib_slide_feature_switch_straight_yes_norotation_yes =
                jib_slide_feature_switch_straight_yes_norotation.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.type_press_q_text),
                                getString(R.string.tail_press_button_text),
                                getString(R.string.nose_press_button_text)));
        jib_slide_feature_switch_straight_yes_norotation_yes.setQuestionDescription(getString(R.string.press_description_text));

        Q jib_slide_feature_switch_straight_yes_frontside_yes =
                jib_slide_feature_switch_straight_yes_frontside.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.position_landed_off_feature_q_text),
                                getString(R.string.switch_button_text),
                                getString(R.string.natural_button_text),
                                getString(R.string.skip_button_text)));
        jib_slide_feature_switch_straight_yes_frontside_yes.setQuestionDescription(getString(R.string.stance_landed_description_text));
        Q jib_slide_feature_switch_straight_yes_frontside_no =
                jib_slide_feature_switch_straight_yes_frontside.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.rotation_off_270_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_straight_yes_frontside_no.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));

        Q jib_slide_feature_switch_straight_no_frontside_yes =
                jib_slide_feature_switch_straight_no_frontside.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.total_spins_q_text),
                                getString(R.string.select_spins_text)));
        Q jib_slide_feature_switch_straight_no_frontside_no =
                jib_slide_feature_switch_straight_no_frontside.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.spins_before_feature_q_text),
                                getString(R.string.select_spins_text)));
        jib_slide_feature_switch_straight_no_frontside_no.setQuestionDescription(getString(R.string.rotation_onto_feature_description_text));

        // Level 9
        Q jib_slide_feature_switch_frontside_yes_fifty_yes_tail =
                jib_slide_feature_switch_frontside_yes_fifty_yes.setChildToAnswer(getString(R.string.tail_press_button_text),
                        new Q(getString(R.string.rotation_off_more_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_frontside_yes_fifty_yes_tail.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));
        Q jib_slide_feature_switch_frontside_yes_fifty_yes_nose =
                jib_slide_feature_switch_frontside_yes_fifty_yes.setRuleToAnswer(getString(R.string.nose_press_button_text),
                        jib_slide_feature_switch_frontside_yes_fifty_yes_tail,
                        tail_to_nose_rule);
        Q jib_slide_feature_switch_frontside_yes_fifty_no =
                jib_slide_feature_switch_frontside_yes_fifty.setRuleToAnswer(getString(R.string.no_button_text),
                        jib_slide_feature_switch_frontside_yes_fifty_yes_tail,
                        remove_tail_press_rule);

        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_yes =
                jib_slide_feature_switch_frontside_yes_boardslide_leading.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.position_landed_off_feature_q_text),
                                getString(R.string.switch_button_text),
                                getString(R.string.natural_button_text),
                                getString(R.string.skip_button_text)));
        jib_slide_feature_switch_frontside_yes_boardslide_leading_yes.setQuestionDescription(getString(R.string.stance_landed_description_text));
        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_no =
                jib_slide_feature_switch_frontside_yes_boardslide_leading.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.rotation_off_270_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_frontside_yes_boardslide_leading_no.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));

        Q jib_slide_feature_switch_frontside_no_180_front_yes =
                jib_slide_feature_switch_frontside_no_180_front.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.many_spins_q_text),
                                getString(R.string.select_spins_text)));
        jib_slide_feature_switch_frontside_no_180_front_yes.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));
        Q jib_slide_feature_switch_frontside_no_180_front_no =
                jib_slide_feature_switch_frontside_no_180_front.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.tap_performed_out_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_frontside_no_180_front_no.setQuestionDescription(getString(R.string.tap_off_description_text));

        Q jib_slide_feature_switch_frontside_no_270_front_yes =
                jib_slide_feature_switch_frontside_no_270_front.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.position_landed_off_feature_q_text),
                                getString(R.string.switch_button_text),
                                getString(R.string.natural_button_text),
                                getString(R.string.skip_button_text)));
        jib_slide_feature_switch_frontside_no_270_front_yes.setQuestionDescription(getString(R.string.stance_landed_description_text));
        Q jib_slide_feature_switch_frontside_no_270_front_no =
                jib_slide_feature_switch_frontside_no_270_front.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.rotation_off_270_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_frontside_no_270_front_no.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));

        Q jib_slide_feature_switch_straight_yes_norotation_yes_tail =
                jib_slide_feature_switch_straight_yes_norotation_yes.setChildToAnswer(getString(R.string.tail_press_button_text),
                        new Q(getString(R.string.rotation_off_more_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_straight_yes_norotation_yes_tail.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));
        Q jib_slide_feature_switch_straight_yes_norotation_yes_nose =
                jib_slide_feature_switch_straight_yes_norotation_yes.setRuleToAnswer(getString(R.string.nose_press_button_text),
                        jib_slide_feature_switch_straight_yes_norotation_yes_tail,
                        tail_to_nose_rule);
        Q jib_slide_feature_switch_straight_yes_norotation_no =
                jib_slide_feature_switch_straight_yes_norotation.setRuleToAnswer(getString(R.string.no_button_text),
                        jib_slide_feature_switch_straight_yes_norotation_yes_tail,
                        remove_tail_press_rule);

        Q jib_slide_feature_switch_straight_yes_frontside_yes_switch =
                jib_slide_feature_switch_straight_yes_frontside_yes.setChildToAnswer(getString(R.string.switch_button_text),
                        new Q(switch_ + backside_ + boardslide_ + to_ + fakie_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_straight_yes_frontside_yes_natural =
                jib_slide_feature_switch_straight_yes_frontside_yes.setChildToAnswer(getString(R.string.natural_button_text),
                        new Q(switch_ + backside_ + boardslide_ + featurePlaceholder_));
        Q jib_slide_feature_switch_straight_yes_frontside_yes_skip =
                jib_slide_feature_switch_straight_yes_frontside_yes.setChildToAnswer(getString(R.string.skip_button_text),
                        new Q(switch_ + backside_ + boardslide_ + featurePlaceholder_));

        Q jib_slide_feature_switch_straight_yes_frontside_no_yes =
                jib_slide_feature_switch_straight_yes_frontside_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_same_opposite_q_text),
                                getString(R.string.same_button_text),
                                getString(R.string.opposite_button_text)));
        jib_slide_feature_switch_straight_yes_frontside_no_yes.setQuestionDescription(getString(R.string.same_opposite_rotation_description_text));
        Q jib_slide_feature_switch_straight_yes_frontside_no_no =
                jib_slide_feature_switch_straight_yes_frontside_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.select_rotation_amount_off_q_text),
                                getString(R.string.num_450_button_text),
                                getString(R.string.num_630_button_text)));
        jib_slide_feature_switch_straight_yes_frontside_no_no.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));

        Q jib_slide_feature_switch_straight_no_frontside_yes_spins =
                jib_slide_feature_switch_straight_no_frontside_yes.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(cab_ + rotationPlaceholder_ + featurePlaceholder_ + spin_));

        Q jib_slide_feature_switch_straight_no_frontside_no_spins =
                jib_slide_feature_switch_straight_no_frontside_no.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(getString(R.string.rotation_off_more_180_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_straight_no_frontside_no_spins.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));

        // Level 10
        Q jib_slide_feature_switch_frontside_yes_fifty_yes_tail_yes =
                jib_slide_feature_switch_frontside_yes_fifty_yes_tail.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.many_spins_q_text),
                                getString(R.string.select_spins_text)));
        Q jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no =
                jib_slide_feature_switch_frontside_yes_fifty_yes_tail.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.tap_performed_out_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no.setQuestionDescription(getString(R.string.tap_off_description_text));

        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_yes_switch =
                jib_slide_feature_switch_frontside_yes_boardslide_leading_yes.setChildToAnswer(getString(R.string.switch_button_text),
                        new Q(switch_ + frontside_ + noseslide_ + to_ + fakie_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_yes_natural =
                jib_slide_feature_switch_frontside_yes_boardslide_leading_yes.setChildToAnswer(getString(R.string.natural_button_text),
                        new Q(switch_ + frontside_ + noseslide_ + featurePlaceholder_));
        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_yes_skip =
                jib_slide_feature_switch_frontside_yes_boardslide_leading_yes.setChildToAnswer(getString(R.string.skip_button_text),
                        new Q(switch_ + frontside_ + noseslide_ + featurePlaceholder_));

        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_no_yes =
                jib_slide_feature_switch_frontside_yes_boardslide_leading_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_same_opposite_q_text),
                                getString(R.string.same_button_text),
                                getString(R.string.opposite_button_text)));
        jib_slide_feature_switch_frontside_yes_boardslide_leading_no_yes.setQuestionDescription(getString(R.string.same_opposite_rotation_description_text));
        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_no_no =
                jib_slide_feature_switch_frontside_yes_boardslide_leading_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.select_rotation_amount_off_q_text),
                                getString(R.string.num_450_button_text),
                                getString(R.string.num_630_button_text)));
        jib_slide_feature_switch_frontside_yes_boardslide_leading_no_no.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));

        Q jib_slide_feature_switch_frontside_no_180_front_yes_spins =
                jib_slide_feature_switch_frontside_no_180_front_yes.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(switch_ + backside_ + num180_ + fiftyfifty_ + to_ + rotationPlaceholder_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_frontside_no_180_front_no_yes =
                jib_slide_feature_switch_frontside_no_180_front_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.nose_tail_tap_out_q_text),
                                getString(R.string.nose_button_text),
                                getString(R.string.tail_button_text)));
        jib_slide_feature_switch_frontside_no_180_front_no_yes.setQuestionDescription(getString(R.string.tap_off_description_text));
        Q jib_slide_feature_switch_frontside_no_180_front_no_no =
                jib_slide_feature_switch_frontside_no_180_front_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + backside_ + num180_ + fiftyfifty_ + featurePlaceholder_));

        Q jib_slide_feature_switch_frontside_no_270_front_yes_switch =
                jib_slide_feature_switch_frontside_no_270_front_yes.setChildToAnswer(getString(R.string.switch_button_text),
                        new Q(switch_ + backside_ + num270_ + boardslide_ + to_ + fakie_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_frontside_no_270_front_yes_natural =
                jib_slide_feature_switch_frontside_no_270_front_yes.setChildToAnswer(getString(R.string.natural_button_text),
                        new Q(switch_ + backside_ + num270_ + boardslide_ + featurePlaceholder_));
        Q jib_slide_feature_switch_frontside_no_270_front_yes_skip =
                jib_slide_feature_switch_frontside_no_270_front_yes.setChildToAnswer(getString(R.string.skip_button_text),
                        new Q(switch_ + backside_ + num270_ + boardslide_ + featurePlaceholder_));

        Q jib_slide_feature_switch_frontside_no_270_front_no_yes =
                jib_slide_feature_switch_frontside_no_270_front_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_same_opposite_q_text),
                                getString(R.string.same_button_text),
                                getString(R.string.opposite_button_text)));
        jib_slide_feature_switch_frontside_no_270_front_no_yes.setQuestionDescription(getString(R.string.same_opposite_rotation_description_text));
        Q jib_slide_feature_switch_frontside_no_270_front_no_no =
                jib_slide_feature_switch_frontside_no_270_front_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.select_rotation_amount_off_q_text),
                                getString(R.string.num_450_button_text),
                                getString(R.string.num_630_button_text)));
        jib_slide_feature_switch_frontside_no_270_front_no_no.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));

        Q jib_slide_feature_switch_straight_yes_norotation_yes_tail_yes =
                jib_slide_feature_switch_straight_yes_norotation_yes_tail.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.many_spins_q_text),
                                getString(R.string.select_spins_text)));
        Q jib_slide_feature_switch_straight_yes_norotation_yes_tail_no =
                jib_slide_feature_switch_straight_yes_norotation_yes_tail.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.tap_performed_out_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_straight_yes_norotation_yes_tail_no.setQuestionDescription(getString(R.string.tap_off_description_text));

        Q jib_slide_feature_switch_straight_yes_frontside_no_yes_same =
                jib_slide_feature_switch_straight_yes_frontside_no_yes.setChildToAnswer(getString(R.string.same_button_text),
                        new Q(switch_ + backside_ + boardslide_ + to_ + sameway_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_straight_yes_frontside_no_yes_opposite =
                jib_slide_feature_switch_straight_yes_frontside_no_yes.setChildToAnswer(getString(R.string.opposite_button_text),
                        new Q(switch_ + backside_ + boardslide_ + to_ + pretzel_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_straight_yes_frontside_no_no_450 =
                jib_slide_feature_switch_straight_yes_frontside_no_no.setChildToAnswer(getString(R.string.num_450_button_text),
                        new Q(switch_ + backside_ + boardslide_ + to_ + num450_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_straight_yes_frontside_no_no_630 =
                jib_slide_feature_switch_straight_yes_frontside_no_no.setChildToAnswer(getString(R.string.num_630_button_text),
                        new Q(switch_ + backside_ + boardslide_ + to_ + num630_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_straight_no_frontside_no_spins_yes =
                jib_slide_feature_switch_straight_no_frontside_no_spins.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.spins_after_feature_q_text),
                                getString(R.string.select_spins_text)));
        jib_slide_feature_switch_straight_no_frontside_no_spins_yes.setQuestionDescription(getString(R.string.rotation_off_feature_description_text));
        Q jib_slide_feature_switch_straight_no_frontside_no_spins_no =
                jib_slide_feature_switch_straight_no_frontside_no_spins.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.tap_performed_out_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        jib_slide_feature_switch_straight_no_frontside_no_spins_no.setQuestionDescription(getString(R.string.tap_off_description_text));

        // Level 11
        Q jib_slide_feature_switch_frontside_yes_fifty_yes_tail_yes_spins =
                jib_slide_feature_switch_frontside_yes_fifty_yes_tail_yes.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(switch_ + frontside_ + fiftyfifty_ + tail_press_ + to_ + rotationPlaceholder_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no_yes =
                jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.nose_tail_tap_out_q_text),
                                getString(R.string.nose_button_text),
                                getString(R.string.tail_button_text)));
        jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no_yes.setQuestionDescription(getString(R.string.tap_off_description_text));
        Q jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no_no =
                jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + frontside_ + fiftyfifty_ + tail_press_ + featurePlaceholder_));

        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_no_yes_same =
                jib_slide_feature_switch_frontside_yes_boardslide_leading_no_yes.setChildToAnswer(getString(R.string.same_button_text),
                        new Q(switch_ + frontside_ + noseslide_ + to_ + sameway_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_no_yes_opposite =
                jib_slide_feature_switch_frontside_yes_boardslide_leading_no_yes.setChildToAnswer(getString(R.string.opposite_button_text),
                        new Q(switch_ + frontside_ + noseslide_ + to_ + pretzel_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_no_no_450 =
                jib_slide_feature_switch_frontside_yes_boardslide_leading_no_no.setChildToAnswer(getString(R.string.num_450_button_text),
                        new Q(switch_ + frontside_ + noseslide_ + to_ + num450_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_frontside_yes_boardslide_leading_no_no_630 =
                jib_slide_feature_switch_frontside_yes_boardslide_leading_no_no.setChildToAnswer(getString(R.string.num_630_button_text),
                        new Q(switch_ + frontside_ + noseslide_ + to_ + num630_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_frontside_no_180_front_no_yes_nose =
                jib_slide_feature_switch_frontside_no_180_front_no_yes.setChildToAnswer(getString(R.string.nose_button_text),
                        new Q(switch_ + backside_ + num180_ + fiftyfifty_ + to_ + nose_ + tap_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_frontside_no_180_front_no_yes_tail =
                jib_slide_feature_switch_frontside_no_180_front_no_yes.setChildToAnswer(getString(R.string.tail_button_text),
                        new Q(switch_ + backside_ + num180_ + fiftyfifty_ + to_ + tail_ + tap_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_frontside_no_270_front_no_yes_same =
                jib_slide_feature_switch_frontside_no_270_front_no_yes.setChildToAnswer(getString(R.string.same_button_text),
                        new Q(switch_ + backside_ + num270_ + boardslide_ + to_ + sameway_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_frontside_no_270_front_no_yes_opposite =
                jib_slide_feature_switch_frontside_no_270_front_no_yes.setChildToAnswer(getString(R.string.opposite_button_text),
                        new Q(switch_ + backside_ + num270_ + boardslide_ + to_ + pretzel_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_frontside_no_270_front_no_no_450 =
                jib_slide_feature_switch_frontside_no_270_front_no_no.setChildToAnswer(getString(R.string.num_450_button_text),
                        new Q(switch_ + backside_ + num270_ + boardslide_ + to_ + num450_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_frontside_no_270_front_no_no_630 =
                jib_slide_feature_switch_frontside_no_270_front_no_no.setChildToAnswer(getString(R.string.num_630_button_text),
                        new Q(switch_ + backside_ + num270_ + boardslide_ + to_ + num630_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_straight_yes_norotation_yes_tail_yes_spins =
                jib_slide_feature_switch_straight_yes_norotation_yes_tail_yes.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(switch_ + fiftyfifty_ + tail_press_ + to_ + rotationPlaceholder_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_straight_yes_norotation_yes_tail_no_yes =
                jib_slide_feature_switch_straight_yes_norotation_yes_tail_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.nose_tail_tap_out_q_text),
                                getString(R.string.nose_button_text),
                                getString(R.string.tail_button_text)));
        jib_slide_feature_switch_straight_yes_norotation_yes_tail_no_yes.setQuestionDescription(getString(R.string.tap_off_description_text));
        Q jib_slide_feature_switch_straight_yes_norotation_yes_tail_no_no =
                jib_slide_feature_switch_straight_yes_norotation_yes_tail_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + fiftyfifty_ + tail_press_ + featurePlaceholder_));

        Q jib_slide_feature_switch_straight_no_frontside_no_spins_yes_spins =
                jib_slide_feature_switch_straight_no_frontside_no_spins_yes.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(cab_ + rotationPlaceholder_ + on_ + to_ + rotation2Placeholder_ + off_ + featurePlaceholder_));

        Q jib_slide_feature_switch_straight_no_frontside_no_spins_no_yes =
                jib_slide_feature_switch_straight_no_frontside_no_spins_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.nose_tail_tap_out_q_text),
                                getString(R.string.nose_button_text),
                                getString(R.string.tail_button_text)));
        jib_slide_feature_switch_straight_no_frontside_no_spins_no_yes.setQuestionDescription(getString(R.string.tap_off_description_text));
        Q jib_slide_feature_switch_straight_no_frontside_no_spins_no_no =
                jib_slide_feature_switch_straight_no_frontside_no_spins_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(cab_ + rotationPlaceholder_ + on_ + featurePlaceholder_));

        // Level 12
        Q jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no_yes_nose =
                jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no_yes.setChildToAnswer(getString(R.string.nose_button_text),
                        new Q(switch_ + frontside_ + fiftyfifty_ + tail_press_ + to_ + nose_ + tap_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no_yes_tail =
                jib_slide_feature_switch_frontside_yes_fifty_yes_tail_no_yes.setChildToAnswer(getString(R.string.tail_button_text),
                        new Q(switch_ + frontside_ + fiftyfifty_ + tail_press_ + to_ + tail_ + tap_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_straight_yes_norotation_yes_tail_no_yes_nose =
                jib_slide_feature_switch_straight_yes_norotation_yes_tail_no_yes.setChildToAnswer(getString(R.string.nose_button_text),
                        new Q(switch_ + fiftyfifty_ + tail_press_ + to_ + nose_ + tap_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_straight_yes_norotation_yes_tail_no_yes_tail =
                jib_slide_feature_switch_straight_yes_norotation_yes_tail_no_yes.setChildToAnswer(getString(R.string.tail_button_text),
                        new Q(switch_ + fiftyfifty_ + tail_press_ + to_ + tail_ + tap_ + out_ + featurePlaceholder_));

        Q jib_slide_feature_switch_straight_no_frontside_no_spins_no_yes_nose =
                jib_slide_feature_switch_straight_no_frontside_no_spins_no_yes.setChildToAnswer(getString(R.string.nose_button_text),
                        new Q(cab_ + rotationPlaceholder_ + on_ + to_ + nose_ + tap_ + out_ + featurePlaceholder_));
        Q jib_slide_feature_switch_straight_no_frontside_no_spins_no_yes_tail =
                jib_slide_feature_switch_straight_no_frontside_no_spins_no_yes.setChildToAnswer(getString(R.string.tail_button_text),
                        new Q(cab_ + rotationPlaceholder_ + on_ + to_ + tail_ + tap_ + out_ + featurePlaceholder_));

        return root;
    }

    private Q createButterTree(Q root) {
        // Level 1
        Q butter =
                root.setChildToAnswer(getString(R.string.butter_button_text),
                        new Q(getString(R.string.approach_stance_q_text),
                                getString(R.string.switch_button_text),
                                getString(R.string.natural_button_text),
                                getString(R.string.skip_button_text)));
        butter.setQuestionDescription(getString(R.string.stance_approach_description_text));

        // Level 2
        Q butter_switch =
                butter.setChildToAnswer(getString(R.string.switch_button_text),
                        new Q(getString(R.string.board_on_off_ground_q_text),
                                getString(R.string.on_the_ground_button_text),
                                getString(R.string.off_the_ground_button_text)));
        butter_switch.setQuestionDescription(getString(R.string.on_off_ground_description_text));
        Q butter_natural =
                butter.setChildToAnswer(getString(R.string.natural_button_text),
                        new Q(getString(R.string.board_on_off_ground_q_text),
                                getString(R.string.on_the_ground_button_text),
                                getString(R.string.off_the_ground_button_text)));
        butter_natural.setQuestionDescription(getString(R.string.on_off_ground_description_text));
        Q butter_skip =
                butter.setChildToAnswer(getString(R.string.skip_button_text),
                        butter_natural);

        // Level 3
        Q butter_switch_on =
                butter_switch.setChildToAnswer(getString(R.string.on_the_ground_button_text),
                        new Q(getString(R.string.side_board_pressed_q_text),
                                getString(R.string.front_button_text),
                                getString(R.string.back_button_text)));
        butter_switch_on.setQuestionDescription(getString(R.string.press_description_text));
        Q butter_switch_off =
                butter_switch.setChildToAnswer(getString(R.string.off_the_ground_button_text),
                        new Q(getString(R.string.hands_ground_some_point_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));

        Q butter_natural_on =
                butter_natural.setChildToAnswer(getString(R.string.on_the_ground_button_text),
                        new Q(getString(R.string.side_board_pressed_q_text),
                                getString(R.string.front_button_text),
                                getString(R.string.back_button_text)));
        butter_natural_on.setQuestionDescription(getString(R.string.press_description_text));
        Q butter_natural_off =
                butter_natural.setChildToAnswer(getString(R.string.off_the_ground_button_text),
                        new Q(getString(R.string.hands_ground_some_point_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));

        // Level 4
        Q butter_switch_on_front =
                butter_switch_on.setChildToAnswer(getString(R.string.front_button_text),
                        new Q(getString(R.string.any_rotation_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_switch_on_front.setQuestionDescription(getString(R.string.any_rotation_description_text));
        Q butter_switch_on_back =
                butter_switch_on.setChildToAnswer(getString(R.string.back_button_text),
                        new Q(getString(R.string.any_rotation_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_switch_on_back.setQuestionDescription(getString(R.string.any_rotation_description_text));

        Q butter_switch_off_yes =
                butter_switch_off.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_180_or_360_q_text),
                                getString(R.string.num_180_button_text),
                                getString(R.string.num_360_button_text)));
        butter_switch_off_yes.setQuestionDescription(getString(R.string.any_rotation_description_text));
        Q butter_switch_off_no =
                butter_switch_off.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.pop_used_q_text),
                                getString(R.string.ollie_button_text),
                                getString(R.string.nollie_button_text),
                                getString(R.string.straight_button_text)));
        butter_switch_off_no.setQuestionDescription(getString(R.string.pop_description_text));

        Q butter_natural_on_front =
                butter_natural_on.setChildToAnswer(getString(R.string.front_button_text),
                        new Q(getString(R.string.any_rotation_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_natural_on_front.setQuestionDescription(getString(R.string.any_rotation_description_text));
        Q butter_natural_on_back =
                butter_natural_on.setChildToAnswer(getString(R.string.back_button_text),
                        new Q(getString(R.string.any_rotation_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_natural_on_back.setQuestionDescription(getString(R.string.any_rotation_description_text));

        Q butter_natural_off_yes =
                butter_natural_off.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_180_or_360_q_text),
                                getString(R.string.num_180_button_text),
                                getString(R.string.num_360_button_text)));
        butter_natural_off_yes.setQuestionDescription(getString(R.string.any_rotation_description_text));
        Q butter_natural_off_no =
                butter_natural_off.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.pop_used_q_text),
                                getString(R.string.ollie_button_text),
                                getString(R.string.nollie_button_text),
                                getString(R.string.straight_button_text)));
        butter_natural_off_no.setQuestionDescription(getString(R.string.pop_description_text));

        // Level 5
        Q butter_switch_on_front_yes =
                butter_switch_on_front.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        butter_switch_on_front_yes.setQuestionDescription(getString(R.string.rotation_direction_description_text));
        Q butter_switch_on_front_no =
                butter_switch_on_front.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + nose_ + press_));

        Q butter_switch_on_back_yes =
                butter_switch_on_back.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        butter_switch_on_back_yes.setQuestionDescription(getString(R.string.rotation_direction_description_text));
        Q butter_switch_on_back_no =
                butter_switch_on_back.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.hands_on_ground_behind_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));

        Q butter_switch_off_yes_180 =
                butter_switch_off_yes.setChildToAnswer(getString(R.string.num_180_button_text),
                        new Q(switch_ + handplant_));
        Q butter_switch_off_yes_360 =
                butter_switch_off_yes.setChildToAnswer(getString(R.string.num_360_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        butter_switch_off_yes_360.setQuestionDescription(getString(R.string.rotation_direction_description_text));

        Q butter_switch_off_no_ollie =
                butter_switch_off_no.setChildToAnswer(getString(R.string.ollie_button_text),
                        new Q(getString(R.string.rotation_90_less_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_switch_off_no_ollie.setQuestionDescription(getString(R.string.any_rotation_description_text));

        Q butter_switch_off_no_nollie =
                butter_switch_off_no.setRuleToAnswer(getString(R.string.nollie_button_text),
                        butter_switch_off_no_ollie,
                        ollie_to_nollie_rule);
        Q butter_switch_off_no_straight =
                butter_switch_off_no.setRuleToAnswer(getString(R.string.straight_button_text),
                        butter_switch_off_no_ollie,
                        remove_ollie_rule);

        Q butter_natural_on_front_yes =
                butter_natural_on_front.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        butter_natural_on_front_yes.setQuestionDescription(getString(R.string.rotation_direction_description_text));
        Q butter_natural_on_front_no =
                butter_natural_on_front.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(nose_ + press_));

        Q butter_natural_on_back_yes =
                butter_natural_on_back.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        butter_natural_on_back_yes.setQuestionDescription(getString(R.string.rotation_direction_description_text));
        Q butter_natural_on_back_no =
                butter_natural_on_back.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.hands_on_ground_behind_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));

        Q butter_natural_off_yes_180 =
                butter_natural_off_yes.setChildToAnswer(getString(R.string.num_180_button_text),
                        new Q(handplant_));
        Q butter_natural_off_yes_360 =
                butter_natural_off_yes.setChildToAnswer(getString(R.string.num_360_button_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        butter_natural_off_yes_360.setQuestionDescription(getString(R.string.rotation_direction_description_text));

        Q butter_natural_off_no_ollie =
                butter_natural_off_no.setChildToAnswer(getString(R.string.ollie_button_text),
                        new Q(getString(R.string.rotation_90_less_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_natural_off_no_ollie.setQuestionDescription(getString(R.string.any_rotation_description_text));
        Q butter_natural_off_no_nollie =
                butter_natural_off_no.setRuleToAnswer(getString(R.string.nollie_button_text),
                        butter_natural_off_no_ollie,
                        ollie_to_nollie_rule);
        Q butter_natural_off_no_straight =
                butter_natural_off_no.setRuleToAnswer(getString(R.string.straight_button_text),
                        butter_natural_off_no_ollie,
                        remove_ollie_rule);

        // Level 6
        Q butter_switch_on_front_yes_frontside =
                butter_switch_on_front_yes.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(getString(R.string.pause_90_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_switch_on_front_yes_frontside.setQuestionDescription(getString(R.string.pause_at_90_description_text));
        Q butter_switch_on_front_yes_backside =
                butter_switch_on_front_yes.setRuleToAnswer(getString(R.string.backside_button_text),
                        butter_switch_on_front_yes_frontside,
                        backside_to_frontside_or_cab_to_switch_backside_rule);

        Q butter_switch_on_back_yes_frontside =
                butter_switch_on_back_yes.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(getString(R.string.pause_90_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_switch_on_back_yes_frontside.setQuestionDescription(getString(R.string.pause_at_90_description_text));
        Q butter_switch_on_back_yes_backside =
                butter_switch_on_back_yes.setRuleToAnswer(getString(R.string.backside_button_text),
                        butter_switch_on_back_yes_frontside,
                        backside_to_frontside_or_cab_to_switch_backside_rule);

        Q butter_switch_on_back_no_yes =
                butter_switch_on_back_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.hands_ground_back_leg_extended_q_text),
                                getString(R.string.extended_button_text),
                                getString(R.string.bent_button_text)));
        butter_switch_on_back_no_yes.setQuestionDescription(getString(R.string.back_leg_extended_description_text));
        Q butter_switch_on_back_no_no =
                butter_switch_on_back_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + tail_ + press_));

        Q butter_switch_off_yes_360_frontside =
                butter_switch_off_yes_360.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(switch_ + frontside_ + miller_flip_));
        Q butter_switch_off_yes_360_backside =
                butter_switch_off_yes_360.setChildToAnswer(getString(R.string.backside_button_text),
                        new Q(switch_ + backside_ + miller_flip_));

        Q butter_switch_off_no_ollie_yes =
                butter_switch_off_no_ollie.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.approached_up_incline_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_switch_off_no_ollie_yes.setQuestionDescription(getString(R.string.up_incline_description_text));
        Q butter_switch_off_no_ollie_no =
                butter_switch_off_no_ollie.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.approached_up_incline_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_switch_off_no_ollie_no.setQuestionDescription(getString(R.string.up_incline_description_text));

        Q butter_natural_on_front_yes_frontside =
                butter_natural_on_front_yes.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(getString(R.string.pause_90_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_natural_on_front_yes_frontside.setQuestionDescription(getString(R.string.pause_at_90_description_text));
        Q butter_natural_on_front_yes_backside =
                butter_natural_on_front_yes.setRuleToAnswer(getString(R.string.backside_button_text),
                        butter_natural_on_front_yes_frontside,
                        swap_frontside_with_backside_rule);

        Q butter_natural_on_back_yes_frontside =
                butter_natural_on_back_yes.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(getString(R.string.pause_90_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_natural_on_back_yes_frontside.setQuestionDescription(getString(R.string.pause_at_90_description_text));
        Q butter_natural_on_back_yes_backside =
                butter_natural_on_back_yes.setRuleToAnswer(getString(R.string.backside_button_text),
                        butter_natural_on_back_yes_frontside,
                        swap_frontside_with_backside_rule);

        Q butter_natural_on_back_no_yes =
                butter_natural_on_back_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.hands_ground_back_leg_extended_q_text),
                                getString(R.string.extended_button_text),
                                getString(R.string.bent_button_text)));
        butter_natural_on_back_no_yes.setQuestionDescription(getString(R.string.back_leg_extended_description_text));
        Q butter_natural_on_back_no_no =
                butter_natural_on_back_no.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(tail_ + press_));

        Q butter_natural_off_yes_360_frontside =
                butter_natural_off_yes_360.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(frontside_ + miller_flip_));
        Q butter_natural_off_yes_360_backside =
                butter_natural_off_yes_360.setChildToAnswer(getString(R.string.backside_button_text),
                        new Q(backside_ + miller_flip_));

        Q butter_natural_off_no_ollie_yes =
                butter_natural_off_no_ollie.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.approached_up_incline_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_natural_off_no_ollie_yes.setQuestionDescription(getString(R.string.up_incline_description_text));
        Q butter_natural_off_no_ollie_no =
                butter_natural_off_no_ollie.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.approached_up_incline_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_natural_off_no_ollie_no.setQuestionDescription(getString(R.string.up_incline_description_text));

        // Level 7
        Q butter_switch_on_front_yes_frontside_yes =
                butter_switch_on_front_yes_frontside.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.either_90_or_270_opposite_q_text),
                                getString(R.string.num_90_button_text),
                                getString(R.string.num_270_button_text)));
        Q butter_switch_on_front_yes_frontside_no =
                butter_switch_on_front_yes_frontside.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.many_spins_in_press_q_text),
                                getString(R.string.select_spins_text)));
        butter_switch_on_front_yes_frontside_no.setQuestionDescription(getString(R.string.any_rotation_description_text));

        Q butter_switch_on_back_yes_frontside_yes =
                butter_switch_on_back_yes_frontside.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.either_90_or_270_same_q_text),
                                getString(R.string.num_90_button_text),
                                getString(R.string.num_270_button_text)));
        Q butter_switch_on_back_yes_frontside_no =
                butter_switch_on_back_yes_frontside.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.many_spins_in_press_q_text),
                                getString(R.string.select_spins_text)));
        butter_switch_on_back_yes_frontside_no.setQuestionDescription(getString(R.string.any_rotation_description_text));

        Q butter_switch_on_back_no_yes_extended =
                butter_switch_on_back_no_yes.setChildToAnswer(getString(R.string.extended_button_text),
                        new Q(switch_ + tripod_));
        Q butter_switch_on_back_no_yes_bent =
                butter_switch_on_back_no_yes.setChildToAnswer(getString(R.string.bent_button_text),
                        new Q(switch_ + layback_));

        Q butter_switch_off_no_ollie_yes_yes =
                butter_switch_off_no_ollie_yes.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.nose_press_grabbing_tail_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_switch_off_no_ollie_yes_yes.setQuestionDescription(getString(R.string.board_block_description_text));
        Q butter_switch_off_no_ollie_yes_no =
                butter_switch_off_no_ollie_yes.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + ollie_));

        Q butter_switch_off_no_ollie_no_yes =
                butter_switch_off_no_ollie_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.tail_press_grabbing_nose_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_switch_off_no_ollie_no_yes.setQuestionDescription(getString(R.string.board_block_description_text));

        Q butter_natural_on_front_yes_frontside_yes =
                butter_natural_on_front_yes_frontside.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.either_90_or_270_opposite_q_text),
                                getString(R.string.num_90_button_text),
                                getString(R.string.num_270_button_text)));
        Q butter_natural_on_front_yes_frontside_no =
                butter_natural_on_front_yes_frontside.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.many_spins_in_press_q_text),
                                getString(R.string.select_spins_text)));
        butter_natural_on_front_yes_frontside_no.setQuestionDescription(getString(R.string.any_rotation_description_text));

        Q butter_natural_on_back_yes_frontside_yes =
                butter_natural_on_back_yes_frontside.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.either_90_or_270_same_q_text),
                                getString(R.string.num_90_button_text),
                                getString(R.string.num_270_button_text)));
        Q butter_natural_on_back_yes_frontside_no =
                butter_natural_on_back_yes_frontside.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.many_spins_in_press_q_text),
                                getString(R.string.select_spins_text)));
        butter_natural_on_back_yes_frontside_no.setQuestionDescription(getString(R.string.any_rotation_description_text));

        Q butter_natural_on_back_no_yes_extended =
                butter_natural_on_back_no_yes.setChildToAnswer(getString(R.string.extended_button_text),
                        new Q(tripod_));
        Q butter_natural_on_back_no_yes_bent =
                butter_natural_on_back_no_yes.setChildToAnswer(getString(R.string.bent_button_text),
                        new Q(layback_));

        Q butter_natural_off_no_ollie_yes_yes =
                butter_natural_off_no_ollie_yes.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.nose_press_grabbing_tail_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_natural_off_no_ollie_yes_yes.setQuestionDescription(getString(R.string.board_block_description_text));
        Q butter_natural_off_no_ollie_yes_no =
                butter_natural_off_no_ollie_yes.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(ollie_));

        Q butter_natural_off_no_ollie_no_yes =
                butter_natural_off_no_ollie_no.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(getString(R.string.tail_press_grabbing_nose_q_text),
                                getString(R.string.yes_button_text),
                                getString(R.string.no_button_text)));
        butter_natural_off_no_ollie_no_yes.setQuestionDescription(getString(R.string.board_block_description_text));

        // Level 8
        Q butter_switch_on_front_yes_frontside_yes_90 =
                butter_switch_on_front_yes_frontside_yes.setChildToAnswer(getString(R.string.num_90_button_text),
                        new Q(getString(R.string.last_90_rotation_direction_q_text),
                                getString(R.string.same_button_text),
                                getString(R.string.opposite_button_text)));
        butter_switch_on_front_yes_frontside_yes_90.setQuestionDescription(getString(R.string.last_90_after_pause_description_text));
        Q butter_switch_on_front_yes_frontside_yes_270 =
                butter_switch_on_front_yes_frontside_yes.setChildToAnswer(getString(R.string.num_270_button_text),
                        new Q(switch_ + backside_ + pretzel_));

        Q butter_switch_on_front_yes_frontside_no_spins =
                butter_switch_on_front_yes_frontside_no.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(cab_ + rotationPlaceholder_ + nose_ + roll_));

        Q butter_switch_on_back_yes_frontside_yes_90 =
                butter_switch_on_back_yes_frontside_yes.setChildToAnswer(getString(R.string.num_90_button_text),
                        new Q(getString(R.string.last_90_rotation_direction_q_text),
                                getString(R.string.same_button_text),
                                getString(R.string.opposite_button_text)));
        butter_switch_on_back_yes_frontside_yes_90.setQuestionDescription(getString(R.string.last_90_after_pause_description_text));
        Q butter_switch_on_back_yes_frontside_yes_270 =
                butter_switch_on_back_yes_frontside_yes.setChildToAnswer(getString(R.string.num_270_button_text),
                        new Q(switch_ + backside_ + bagel_));

        Q butter_switch_on_back_yes_frontside_no_spins =
                butter_switch_on_back_yes_frontside_no.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(cab_ + rotationPlaceholder_ + tail_ + roll_));

        Q butter_switch_off_no_ollie_yes_yes_yes =
                butter_switch_off_no_ollie_yes_yes.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(switch_ + nose_ + block_));
        Q butter_switch_off_no_ollie_yes_yes_no =
                butter_switch_off_no_ollie_yes_yes.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(switch_ + ollie_ + poptart_));

        Q butter_switch_off_no_ollie_no_yes_yes =
                butter_switch_off_no_ollie_no_yes.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(switch_ + num180_ + tail_ + block_));
        Q butter_switch_off_no_ollie_no_yes_no =
                butter_switch_off_no_ollie_no_yes.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.many_spins_q_text),
                                getString(R.string.select_spins_text)));
        butter_switch_off_no_ollie_no_yes_no.setQuestionDescription(getString(R.string.any_rotation_description_text));
        Q butter_switch_off_no_ollie_no_no =
                butter_switch_off_no_ollie_no.setChildToAnswer(getString(R.string.no_button_text),
                        butter_switch_off_no_ollie_no_yes_no);

        Q butter_natural_on_front_yes_frontside_yes_90 =
                butter_natural_on_front_yes_frontside_yes.setChildToAnswer(getString(R.string.num_90_button_text),
                        new Q(getString(R.string.last_90_rotation_direction_q_text),
                                getString(R.string.same_button_text),
                                getString(R.string.opposite_button_text)));
        butter_natural_on_front_yes_frontside_yes_90.setQuestionDescription(getString(R.string.last_90_after_pause_description_text));
        Q butter_natural_on_front_yes_frontside_yes_270 =
                butter_natural_on_front_yes_frontside_yes.setChildToAnswer(getString(R.string.num_270_button_text),
                        new Q(backside_ + pretzel_));

        Q butter_natural_on_front_yes_frontside_no_spins =
                butter_natural_on_front_yes_frontside_no.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(frontside_ + rotationPlaceholder_ + nose_ + roll_));

        Q butter_natural_on_back_yes_frontside_yes_90 =
                butter_natural_on_back_yes_frontside_yes.setChildToAnswer(getString(R.string.num_90_button_text),
                        new Q(getString(R.string.last_90_rotation_direction_q_text),
                                getString(R.string.same_button_text),
                                getString(R.string.opposite_button_text)));
        butter_natural_on_back_yes_frontside_yes_90.setQuestionDescription(getString(R.string.last_90_after_pause_description_text));
        Q butter_natural_on_back_yes_frontside_yes_270 =
                butter_natural_on_back_yes_frontside_yes.setChildToAnswer(getString(R.string.num_270_button_text),
                        new Q(backside_ + bagel_));

        Q butter_natural_on_back_yes_frontside_no_spins =
                butter_natural_on_back_yes_frontside_no.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(frontside_ + rotationPlaceholder_ + tail_ + roll_));

        Q butter_natural_off_no_ollie_yes_yes_yes =
                butter_natural_off_no_ollie_yes_yes.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(nose_ + block_));
        Q butter_natural_off_no_ollie_yes_yes_no =
                butter_natural_off_no_ollie_yes_yes.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(ollie_ + to_ + fakie_));

        Q butter_natural_off_no_ollie_no_yes_yes =
                butter_natural_off_no_ollie_no_yes.setChildToAnswer(getString(R.string.yes_button_text),
                        new Q(num180_ + tail_ + block_));
        Q butter_natural_off_no_ollie_no_yes_no =
                butter_natural_off_no_ollie_no_yes.setChildToAnswer(getString(R.string.no_button_text),
                        new Q(getString(R.string.many_spins_q_text),
                                getString(R.string.select_spins_text)));
        butter_natural_off_no_ollie_no_yes_no.setQuestionDescription(getString(R.string.any_rotation_description_text));
        Q butter_natural_off_no_ollie_no_no =
                butter_natural_off_no_ollie_no.setChildToAnswer(getString(R.string.no_button_text),
                        butter_natural_off_no_ollie_no_yes_no);

        // Level 9
        Q butter_switch_on_front_yes_frontside_yes_90_same =
                butter_switch_on_front_yes_frontside_yes_90.setChildToAnswer(getString(R.string.same_button_text),
                        new Q(switch_ + backside_ + nose_ + press_));
        Q butter_switch_on_front_yes_frontside_yes_90_opposite =
                butter_switch_on_front_yes_frontside_yes_90.setChildToAnswer(getString(R.string.opposite_button_text),
                        new Q(switch_ + backside_ + nose_ + press_ + to_ + fakie_));

        Q butter_switch_on_back_yes_frontside_yes_90_same =
                butter_switch_on_back_yes_frontside_yes_90.setChildToAnswer(getString(R.string.same_button_text),
                        new Q(switch_ + backside_ + tail_ + press_));
        Q butter_switch_on_back_yes_frontside_yes_90_opposite =
                butter_switch_on_back_yes_frontside_yes_90.setChildToAnswer(getString(R.string.opposite_button_text),
                        new Q(switch_ + backside_ + tail_ + press_ + to_ + fakie_));

        Q butter_switch_off_no_ollie_no_yes_no_spins =
                butter_switch_off_no_ollie_no_yes_no.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        butter_switch_off_no_ollie_no_yes_no_spins.setQuestionDescription(getString(R.string.rotation_direction_description_text));

        Q butter_natural_on_front_yes_frontside_yes_90_same =
                butter_natural_on_front_yes_frontside_yes_90.setChildToAnswer(getString(R.string.same_button_text),
                        new Q(backside_ + nose_ + press_ + to_ + fakie_));
        Q butter_natural_on_front_yes_frontside_yes_90_opposite =
                butter_natural_on_front_yes_frontside_yes_90.setChildToAnswer(getString(R.string.opposite_button_text),
                        new Q(backside_ + nose_ + press_));

        Q butter_natural_on_back_yes_frontside_yes_90_same =
                butter_natural_on_back_yes_frontside_yes_90.setChildToAnswer(getString(R.string.same_button_text),
                        new Q(backside_ + tail_ + press_ + to_ + fakie_));
        Q butter_natural_on_back_yes_frontside_yes_90_opposite =
                butter_natural_on_back_yes_frontside_yes_90.setChildToAnswer(getString(R.string.opposite_button_text),
                        new Q(backside_ + tail_ + press_));

        Q butter_natural_off_no_ollie_no_yes_no_spins =
                butter_natural_off_no_ollie_no_yes_no.setChildToAnswer(getString(R.string.select_spins_text),
                        new Q(getString(R.string.rotation_direction_q_text),
                                getString(R.string.frontside_button_text),
                                getString(R.string.backside_button_text)));
        butter_natural_off_no_ollie_no_yes_no_spins.setQuestionDescription(getString(R.string.rotation_direction_description_text));

        // Level 10
        Q butter_switch_off_no_ollie_no_yes_no_spins_frontside =
                butter_switch_off_no_ollie_no_yes_no_spins.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(switch_ + ollie_ + frontside_ + rotationPlaceholder_));
        Q butter_switch_off_no_ollie_no_yes_no_spins_backside =
                butter_switch_off_no_ollie_no_yes_no_spins.setChildToAnswer(getString(R.string.backside_button_text),
                        new Q(switch_ + ollie_ + backside_ + rotationPlaceholder_));

        Q butter_natural_off_no_ollie_no_yes_no_spins_frontside =
                butter_natural_off_no_ollie_no_yes_no_spins.setChildToAnswer(getString(R.string.frontside_button_text),
                        new Q(ollie_ + frontside_ + rotationPlaceholder_));
        Q butter_natural_off_no_ollie_no_yes_no_spins_backside =
                butter_natural_off_no_ollie_no_yes_no_spins.setChildToAnswer(getString(R.string.backside_button_text),
                        new Q(ollie_ + backside_ + rotationPlaceholder_));

        return root;
    }
}
