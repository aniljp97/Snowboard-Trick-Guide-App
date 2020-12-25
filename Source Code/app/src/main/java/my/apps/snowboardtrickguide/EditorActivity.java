package my.apps.snowboardtrickguide;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snowboardtrickguide.R;

import my.apps.snowboardtrickguide.data.TricksDBHepler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static my.apps.snowboardtrickguide.TrickData.shortenTrickName;

public class EditorActivity extends AppCompatActivity {

    TrickData trickData;
    AutoCompleteTextView categoryAutoCTV;
    TextView shortNameTextview;
    Switch displayShortSwitch;
    EditText regularNameEditText;
    EditText dateEditText;
    Switch inListSwitch;
    EditText notesEditText;

    String curr_regular_name;
    String curr_short_name;

    private Toast toast = null;

    String[] categories = {"Jib", "Aerial", "Carve", "Butter", "Other"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trick_editor);

        trickData = (TrickData) getIntent().getSerializableExtra(getString(R.string.edit_trick_key));
        if(trickData != null) {
            initViews();
            populateTexts();
        } else {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            String formattedDate = df.format(c);
            trickData = new TrickData("",
                    "",
                    "",
                    formattedDate,
                    "",
                    getIntent().getIntExtra(getString(R.string.is_completed_key), 0),
                    0);
            initViews();
            dateEditText.setText(formattedDate);
            inListSwitch.setChecked(getIntent().getIntExtra(getString(R.string.is_completed_key), 0) == 1);
        }

        handleTextChanges();
    }

    //////////////////// Methods for initializing the screen ////////////////////////
    private void initViews() {
        categoryAutoCTV = findViewById(R.id.category_trick_edittext);
        shortNameTextview = findViewById(R.id.short_name_textview);
        displayShortSwitch = findViewById(R.id.set_display_name_short_switch);
        regularNameEditText = findViewById(R.id.regular_trick_name_edittext);
        dateEditText = findViewById(R.id.date_trick_edittext);
        inListSwitch = findViewById(R.id.in_list_switch);
        notesEditText = findViewById(R.id.notes_trick_edittext);
        
        categoryAutoCTV.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories));

        curr_regular_name = trickData.getTrick_name();
        curr_short_name = trickData.getShorten_trick_name();

        findViewById(R.id.edit_save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if the all the views are filled and completed properly
                if(checkViewsCompleted()) {
                    Intent i = new Intent();
                    i.putExtra(getString(R.string.orginial_trick_key), trickData);
                    i.putExtra(getString(R.string.updated_trick_key), createTrickFromInput());
                    setResult(RESULT_OK, i);

                    if(trickData.getTrick_name().equals("")) {
                        finish();
                    } else {
                        if (isTextEdited())
                            saveTrickDialog();
                        else
                            finish();
                    }
                }
            }
        });
        findViewById(R.id.edit_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitAndDiscardDialog();
            }
        });
        displayShortSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    shortNameTextview.setText(curr_regular_name);
                    regularNameEditText.setText(curr_short_name);
                    regularNameEditText.setFocusable(false);
                    regularNameEditText.setClickable(true);
                    regularNameEditText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(EditorActivity.this, "\"Set displayed name to shortened version\" must be turned off to edit the trick name", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    shortNameTextview.setText(curr_short_name);
                    regularNameEditText.setText(curr_regular_name);
                    regularNameEditText.setFocusable(true);
                    regularNameEditText.setFocusableInTouchMode(true);
                    regularNameEditText.setEnabled(true);
                    regularNameEditText.setClickable(false);
                    regularNameEditText.setOnClickListener(null);
                }
            }
        });
        findViewById(R.id.editor_exit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitAndDiscardDialog();
            }
        });
        findViewById(R.id.delete_trick_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTrickDialog();
            }
        });
    }

    private void populateTexts() {
        categoryAutoCTV.setText(trickData.getTrick_category());
        shortNameTextview.setText(trickData.getShorten_trick_name());
        if(trickData.getDisplay_short() == 1) {
            shortNameTextview.setText(trickData.getTrick_name());
            regularNameEditText.setText(trickData.getShorten_trick_name());
            displayShortSwitch.setChecked(true);
        } else {
            shortNameTextview.setText(trickData.getShorten_trick_name());
            regularNameEditText.setText(trickData.getTrick_name());
            displayShortSwitch.setChecked(false);
        }
        dateEditText.setText(trickData.getDate_discovered());
        inListSwitch.setChecked(trickData.getCompleted() == 1);
        notesEditText.setText(trickData.getNotes());

        if(displayShortSwitch.isChecked()) {
            regularNameEditText.setFocusable(false);
            regularNameEditText.setClickable(true);
            regularNameEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (toast != null) toast.cancel();
                    toast = Toast.makeText(EditorActivity.this, "\"Set displayed name to shortened version\" must be turned off to edit the trick name", Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }

    private void handleTextChanges() {
        dateEditText.addTextChangedListener(new TextWatcher() {
            // i: start index of change
            // i1: number of characters deleted
            // i2: number of characters added
            private String current = "";
            private String mmddyyyy = "mmddyyyy";
            private Calendar cal = Calendar.getInstance();
            private int current_cursor;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                current_cursor = dateEditText.getSelectionStart();
            }

            @Override
            public void onTextChanged(CharSequence s, int j, int j1, int j2) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }

                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + mmddyyyy.substring(clean.length());
                    }else{
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int mon  = Integer.parseInt(clean.substring(0,2));
                        int day  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        mon = mon < 1 ? 1 : Math.min(mon, 12);
                        cal.set(Calendar.MONTH, mon-1);
                        year = (year<1900)?1900: Math.min(year, 2100);
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
                        clean = String.format("%02d%02d%02d",mon, day, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = Math.max(sel, 0);
                    current = clean;
                    dateEditText.setText(current);
                    dateEditText.setSelection(Math.min(sel, current.length()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                dateEditText.setBackgroundResource(android.R.color.transparent);
                if(current_cursor > 10)
                    current_cursor = 10;
                dateEditText.setSelection(current_cursor);
            }
        });

        regularNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!displayShortSwitch.isChecked()) {
                    curr_regular_name = charSequence.toString();
                    curr_short_name = shortenTrickName(charSequence.toString());
                    shortNameTextview.setText(curr_short_name);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                regularNameEditText.setBackgroundResource(android.R.color.transparent);
            }
        });

        categoryAutoCTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                categoryAutoCTV.setBackgroundResource(android.R.color.transparent);
            }
        });
    }

    ///////////////////// Methods for dialog boxes ////////////////////////
    private void quitAndDiscardDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Are you sure you want to discard the changes made?");
        dialog.setPositiveButton(getString(R.string.yes_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        dialog.setNegativeButton(getString(R.string.no_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        if(isTextEdited())
            dialog.create().show();
        else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void saveTrickDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Are you sure you want to apply these changes to this trick?");
        dialog.setNegativeButton(getString(R.string.no_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.setPositiveButton(getString(R.string.yes_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        dialog.create().show();
    }

    private void deleteTrickDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Are you sure you want to permanently delete this trick?");
        dialog.setNegativeButton(getString(R.string.no_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.setPositiveButton(getString(R.string.yes_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.putExtra(getString(R.string.orginial_trick_key), trickData);
                setResult(RESULT_FIRST_USER, intent);
                finish();
            }
        });
        dialog.create().show();
    }

    //////////////// Methods used to handle when the user is trying to leave the page //////////////
    private boolean isTextEdited() {

        if((displayShortSwitch.isChecked()
                && regularNameEditText.getText().toString().equals(trickData.getShorten_trick_name())
                && shortNameTextview.getText().toString().equals(trickData.getTrick_name()))
                || (!displayShortSwitch.isChecked()
                && regularNameEditText.getText().toString().equals(trickData.getTrick_name())
                && shortNameTextview.getText().toString().equals(trickData.getShorten_trick_name()))
        ) {
            if (categoryAutoCTV.getText().toString().equals(trickData.getTrick_category())
                    && ((displayShortSwitch.isChecked() && trickData.getDisplay_short() == 1) || (!displayShortSwitch.isChecked() && trickData.getDisplay_short() == 0))
                    && dateEditText.getText().toString().equals(trickData.getDate_discovered())
                    && notesEditText.getText().toString().equals(trickData.getNotes())
                    && ((inListSwitch.isChecked() && trickData.getCompleted() == 1) || (!inListSwitch.isChecked() && trickData.getCompleted() == 0))
            ) return false;
        }
        return true;
    }

    private boolean checkViewsCompleted() {
        // regular trick name isnt empty
        // category isnt empty
        // date is filled
        // isnt a duplicate
        boolean res = true;

        if(regularNameEditText.getText().toString().isEmpty() || shortNameTextview.getText().toString().isEmpty()) {
            regularNameEditText.setError(getString(R.string.trick_name_error));
            regularNameEditText.setBackgroundResource(R.drawable.edittext_error_background);
            res = false;
        }
        if(categoryAutoCTV.getText().toString().isEmpty()) {
            categoryAutoCTV.setError(getString(R.string.category_error));
            categoryAutoCTV.setBackgroundResource(R.drawable.edittext_error_background);
            res = false;
        }
        if(dateEditText.getText().toString().contains("y") || dateEditText.getText().toString().isEmpty()) {
            dateEditText.setError(getString(R.string.date_error));
            dateEditText.setBackgroundResource(R.drawable.edittext_error_background);
            res = false;
        }
        if(!trickData.getTrick_category().equalsIgnoreCase(categoryAutoCTV.getText().toString().trim()) || !trickData.getTrick_name().equalsIgnoreCase(curr_regular_name.trim())) {
            TricksDBHepler tricksDBHepler = new TricksDBHepler(this);
            Cursor data = new TricksDBHepler(this).getData();
            while(data.moveToNext()) {
                if(data.getString(1).equalsIgnoreCase(categoryAutoCTV.getText().toString().trim()) && data.getString(2).equalsIgnoreCase(curr_regular_name.trim())) {
                    regularNameEditText.setBackgroundResource(R.drawable.edittext_error_background);
                    regularNameEditText.setError(getString(R.string.duplicate_error));
                    categoryAutoCTV.setBackgroundResource(R.drawable.edittext_error_background);
                    categoryAutoCTV.setError(getString(R.string.duplicate_error));
                    res = false;
                    break;
                }
            }
        }

        return res;
    }

    private TrickData createTrickFromInput() {
        String category, name, short_name, date, notes;
        int is_completed, display_short;
        category = categoryAutoCTV.getText().toString().trim();
        name = curr_regular_name.trim();
        short_name = curr_short_name.trim();
        date = dateEditText.getText().toString();
        notes = notesEditText.getText().toString().trim();
        if(inListSwitch.isChecked())
            is_completed = 1;
        else
            is_completed = 0;
        if(displayShortSwitch.isChecked())
            display_short = 1;
        else
            display_short = 0;

        return new TrickData(category, name, short_name, date, notes, is_completed, display_short);
    }

    @Override
    public void onBackPressed() {
        quitAndDiscardDialog();
    }
}
