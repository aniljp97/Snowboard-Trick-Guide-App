package my.apps.snowboardtrickguide.addtrick;

import android.util.Log;

import my.apps.snowboardtrickguide.KeyWords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// My class to hold the contents of the trick guide tree similar to a node.
// A Q() represents a question with any number of answers and each answer points to another Q object
public class Q {
    private String question;
    private List<String> answers;
    private List<Q> answer_questions = new ArrayList<>();
    private String question_description = "Select the answer that corresponds to the trick performed";
    private List<String> answer_descriptions = new ArrayList<>();

    private List<String> answer_rules = new ArrayList<>();
    private static List<String> rule_stack = new ArrayList<>();

    public Q(String trick_name) {
        this.question = trick_name;
    }

    public Q(String question, String...answers) {
        this.question = question;
        this.answers = Arrays.asList(answers);

        for(int i = 0; i < answers.length; i++) {
            answer_questions.add(null);
            answer_rules.add(null);
            answer_descriptions.add(null);
        }
    }

    public static void resetRuleStack() {
        rule_stack = new ArrayList<>();
    }

    public void setQuestionDescription(String question_description) {
        this.question_description = question_description;
    }

    public Q setAnswerDescriptions(String answer, String answer_description) {
        int ans_index = answers.indexOf(answer);
        if(ans_index != -1) {
            answer_descriptions.set(ans_index, answer_description);
        }
        return this;
    }

    public Q setChildToAnswer(String answer, Q child) {
        int ans_index = answers.indexOf(answer);
        if(ans_index != -1) {
            answer_questions.set(ans_index, child);
        }

        return child;
    }

    public Q setRuleToAnswer(String answer, Q redirect, String rule_key) {
        int ans_index = answers.indexOf(answer);
        if(ans_index != -1) {
            answer_rules.set(ans_index, rule_key);
            answer_questions.set(ans_index, redirect);
        }
        return redirect;
    }

    public String traverse(String...answers) {
        Q curr_Q = this;
        for(String ans : answers) {
            curr_Q = curr_Q.getAnswerChild(ans);
        }
        String res = curr_Q.getTrickName();
        resetRuleStack();
        return res;
    }

    public String getQuestionDescription() {
        return question_description;
    }

    public String getAnswerDescription(String ans) {
        if(this.isEnd())
            return "Is an end";

        if(!answers.contains(ans))
            return null;
        return answer_descriptions.get(answers.indexOf(ans));
    }

    public Q getAnswerChild(String ans) {
        if(this.isEnd())
            return this;

        if(!answers.contains(ans)) {
            Log.e("LOG!", "\""+ans+"\""+ " is not a answer available for this question: "+this.getQuestion());
        }

        String poss_rule = answer_rules.get(answers.indexOf(ans));
        if (poss_rule != null)
            rule_stack.add(poss_rule);

        return answer_questions.get(answers.indexOf(ans));
    }

    public ArrayList<String> getAnswers() {
        return new ArrayList<>(answers);
    }

    public String getQuestion() {
        return question;
    }

    public String getTrickName() {
        if(isEnd()) {
            String trick = getQuestion();
            for (int j = rule_stack.size() - 1; j >= 0; j--) {
                trick = new Rules().apply(rule_stack.get(j), trick);
            }
            return trick.trim();
        }
        else
            return "This is a Question, not a trick name";
    }

    public boolean isEnd() {
        return answers == null;
    }


    public String testAllPossibilities() {
        String res = "";

        ArrayList<Q> Q_to_visit = new ArrayList<>(Arrays.asList(this));
        ArrayList<String> answers_to_visit = new ArrayList<>();
        ArrayList<Integer> answer_levels = new ArrayList<>(Arrays.asList(0));

        ArrayList<String> ans_trail = new ArrayList<>();
        Q curr_Q;

        while(!Q_to_visit.isEmpty()) {
            // take the first Q from Q_to_visit
            curr_Q = Q_to_visit.remove(0);
            int curr_level = answer_levels.remove(0);

            if(curr_Q.isEnd()) {
                resetRuleStack();
                res = res.concat(this.traverse(ans_trail.toArray(new String[0]))) + ":\n\t";
                Log.d("LOG!", this.traverse(ans_trail.toArray(new String[0])));
                ans_trail.remove(ans_trail.size() - 1);
            } else {
                ArrayList<String> answers = curr_Q.getAnswers();
                for (int j = answers.size() - 1; j >= 0; j--) {
                    Q_to_visit.add(0, curr_Q.getAnswerChild(answers.get(j)));
                    answers_to_visit.add(0, answers.get(j));

                    answer_levels.add(0, curr_level+1);
                }
            }

            if(answer_levels.size() == 0)
                return res;
            for(int next_level = answer_levels.get(0) - 1; next_level < ans_trail.size();) {
                ans_trail.remove(ans_trail.size() -1);
            }
            ans_trail.add(answers_to_visit.remove(0));
        }

        return res;
    }
}

// Rules to add to a trick name once found to get the correct trick name
class Rules implements KeyWords {

    public String apply(String key, String str) {
        if(key.equals(aerial_switch_to_natural_rule))
            return aerialSwitchToNatural(str);
        if(key.equals(cab_to_switch_backside_rule))
            return cabToSwitchBackside(str);
        if(key.equals(switch_ollie_to_fakie_rule))
            return switchOllieToFakie(str);
        if(key.equals(remove_ollie_rule))
            return removeOllie(str);
        if(key.equals(tail_to_nose_rule))
            return tailToNose(str);
        if(key.equals(remove_tail_press_rule))
            return removeTailPress(str);
        if(key.equals(noseslide_to_bluntslide_rule))
            return noseslideToBluntslide(str);
        if(key.equals(noseslide_to_boardslide_rule))
            return noseslideToBoardslide(str);
        if(key.equals(noseslide_to_noseblunt_rule))
            return noseslideToNoseblunt(str);
        if(key.equals(noseslide_to_tailslide_rule))
            return noseslideToTailslide(str);
        if(key.equals(noseslide_to_lipslide_rule))
            return noseslideToLipslide(str);
        if(key.equals(switch_backside_to_hardway_cab_rule))
            return switchBacksideToHardwayCab(str);
        if(key.equals(switch_backside_to_hardway_cab_and_boardslide_to_lipslide_rule))
            return switchBacksideToHardwayCabAndBoardslideToLipslide(str);
        if(key.equals(frontside_to_backside_rule))
            return frontsideToBackside(str);
        if(key.equals(swap_switch_backside_with_cab_rule))
            return swapSwitchBacksideWithCab(str);
        if(key.equals(backside_to_frontside_rule))
            return backsideToFrontside(str);
        if(key.equals(jib_switch_to_natural_rule))
            return jibSwitchToNatural(str);
        if(key.equals(box_to_rail_rule))
            return boxToRail(str);
        if(key.equals(box_to_pipe_rule))
            return boxToPipe(str);
        if(key.equals(backside_to_frontside_or_cab_to_switch_backside_rule))
            return backsideToFrontsideOrCabToSwitchBackside(str);
        if(key.equals(swap_frontside_with_backside_rule))
            return swapFrontsideWithBackside(str);
        if(key.equals(ollie_to_nollie_rule))
            return ollieToNollie(str);
        return str;
    }

    private String aerialSwitchToNatural(String trick) {
        if(trick.contains(cab_))
            trick = trick.replaceFirst(cab_, frontside_);
        else if(trick.contains(fakie_))
            trick = trick.replaceFirst(fakie_, nollie_);
        else
            trick = trick.replaceFirst(switch_,"");
        return trick;
    }

    private String jibSwitchToNatural(String trick) {
        if(trick.contains(switch_))
            trick = trick.replaceFirst(switch_, "");
        else if(trick.contains(cab_))
            trick = trick.replaceFirst(cab_, frontside_);
        return trick;
    }

    private String cabToSwitchBackside(String trick) {
        if(trick.contains(cab_))
            trick = trick.replaceFirst(cab_, switch_ + backside_);

        return trick;
    }

    private String switchOllieToFakie(String trick) {
        trick = trick.replaceFirst(switch_ + ollie_, fakie_);
        return trick;
    }

    private String removeOllie(String trick) {
        if(trick.endsWith(ollie_))
            trick = trick.replace(ollie_, pop_);
        else if(trick.equals(ollie_+to_+fakie_))
            trick = trick.replace(ollie_, air_);
        else
            trick = trick.replaceFirst(ollie_, "");
        return trick;
    }

    private String tailToNose(String trick) {
        trick = trick.replaceFirst(tail_, nose_);
        return trick;
    }

    private String removeTailPress(String trick) {
        trick = trick.replaceFirst(tail_press_, "");
        return trick;
    }

    private String noseslideToBluntslide(String trick) {
        trick = trick.replaceFirst(noseslide_, bluntslide_);
        return trick;
    }

    private String noseslideToBoardslide(String trick) {
        trick = trick.replaceFirst(noseslide_, boardslide_);
        return trick;
    }

    private String noseslideToNoseblunt(String trick) {
        trick = trick.replaceFirst(noseslide_, noseblunt_);
        return trick;
    }

    private String noseslideToTailslide(String trick) {
        trick = trick.replaceFirst(noseslide_, tailslide_);
        return trick;
    }

    private String noseslideToLipslide(String trick) {
        trick = trick.replaceFirst(noseslide_, lipslide_);
        return trick;
    }

    private String switchBacksideToHardwayCab(String trick) {
        trick = trick.replaceFirst(switch_+backside_, hardway_+cab_);
        return trick;
    }

    private String switchBacksideToHardwayCabAndBoardslideToLipslide(String trick) {
        trick = switchBacksideToHardwayCab(trick);
        trick = trick.replaceFirst(boardslide_,lipslide_);
        return trick;
    }

    private String frontsideToBackside(String trick) {
        trick = trick.replaceFirst(frontside_,backside_);
        return trick;
    }

    private String swapSwitchBacksideWithCab(String trick) {
        if(trick.contains(switch_+backside_))
            trick = trick.replaceFirst(switch_+backside_, cab_);
        else if (trick.contains(cab_))
            trick = trick.replaceFirst(cab_, switch_+backside_);
        return trick;
    }

    private String backsideToFrontside(String trick) {
        trick = trick.replaceFirst(backside_, frontside_);
        return trick;
    }

    private String boxToRail(String trick) {
        trick = trick.replace(box, rail);
        return trick;
    }

    private String boxToPipe(String trick) {
        trick = trick.replace(box, pipe);
        return trick;
    }

    private String backsideToFrontsideOrCabToSwitchBackside(String trick) {
        if(trick.contains(backside_))
            trick = trick.replace(backside_, frontside_);
        else if(trick.contains(cab_))
            trick = trick.replace(cab_, switch_+backside_);
        return trick;
    }

    private String swapFrontsideWithBackside(String trick) {
        if(trick.contains(backside_))
            trick = trick.replace(backside_, frontside_);
        else if(trick.contains(frontside_))
            trick = trick.replace(frontside_, backside_);
        return trick;
    }

    private String ollieToNollie(String trick) {
        trick = trick.replace(ollie_, nollie_);
        return trick;
    }
}

