package my.apps.snowboardtrickguide;

import org.apache.commons.lang3.text.WordUtils;

import java.io.Serializable;

// Class used to hold all of the data for each trick in a list
public class TrickData implements Serializable, KeyWords {
    private String trick_name;
    private String shorten_trick_name;
    private String trick_category;
    private String date_discovered;
    private int completed;
    private int display_short;
    private boolean selected = false;
    private String notes;

    public TrickData(String trick_category, String trick_name, String shorten_trick_name, String date_discovered, String notes, int completed, int display_short) {
        this.trick_name = trick_name;
        this.shorten_trick_name = shorten_trick_name;
        this.trick_category = trick_category;
        this.date_discovered = date_discovered;
        this.notes = notes;
        this.completed = completed;
        this.display_short = display_short;
    }

    public void setIsSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() { return selected; };

    public String getTrick_name() {
        return trick_name.trim();
    }

    public String getShorten_trick_name() {
        return shorten_trick_name.trim();
    }

    public String getTrick_category() {
        return trick_category.trim();
    }

    public String getDate_discovered() {
        return date_discovered.trim();
    }

    public String getNotes() {
        return notes.trim();
    }

    public int getCompleted() { return completed; }

    public int getDisplay_short() {
        return display_short;
    }

    public static String shortenTrickName(String trick_name) {
        String new_name = WordUtils.capitalizeFully(trick_name) + " ";
        new_name = new_name.replace(frontside_, front_);
        new_name = new_name.replace(backside_, back_);
        new_name = new_name.replace(cab_+num180_, half_+cab_);
        new_name = new_name.replace(grab_, "");
        new_name = new_name.replace(to_, "");
        new_name = new_name.replace("To ", "");
        new_name = new_name.replace(boardslide_, board_);
        new_name = new_name.replace(bluntslide_, blunt_);
        new_name = new_name.replace(lipslide_, lip_);
        new_name = new_name.replace(sameway_, bagel_);

        for(String word : new_name.split(" ")) { // replacing all the numbers with their first digit
            try {
                Integer.parseInt(word);
                new_name = new_name.replace(word, word.substring(0, word.length()/2));
            } catch (NumberFormatException ignored) {}
        }

        return new_name.trim();
    }
}
