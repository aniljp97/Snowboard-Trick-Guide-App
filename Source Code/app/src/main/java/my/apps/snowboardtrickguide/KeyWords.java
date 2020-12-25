package my.apps.snowboardtrickguide;

// keys words used for building trick names
public interface KeyWords {
    String cab_ = "Cab ";
    String frontside_ = "Frontside ";
    String switch_ = "Switch ";
    String fakie_ = "Fakie ";
    String ollie_ = "Ollie ";
    String nollie_ = "Nollie ";
    String backside_ = "Backside ";
    String straight_ = "Straight ";
    String shifty_ = "Shifty ";
    String air_ = "Air ";
    String grab_ = "Grab ";
    String tamedog_ = "Tamedog ";
    String wildcat_ = "Wildcat ";
    String supercat_ = "Supercat ";
    String double_ = "Double ";
    String triple_ = "Triple ";
    String fiftyfifty_ = "50-50 ";
    String tail_ = "Tail ";
    String nose_ = "Nose ";
    String press_ = "Press ";
    String tail_press_ = tail_+press_;
    String nose_press_ = nose_+press_;
    String out_ = "out ";
    String to_ = "to ";
    String tap_ = "Tap ";
    String noseslide_ = "Noseslide ";
    String bluntslide_ = "Bluntslide ";
    String boardslide_ = "Boardslide ";
    String noseblunt_ = "Noseblunt ";
    String tailslide_ = "Tailslide ";
    String lipslide_ = "Lipslide ";
    String sameway_ = "Sameway ";
    String pretzel_ = "Pretzel ";
    String hardway_ = "Hardway ";
    String spin_ = "spin ";
    String on_ = "On ";
    String off_ = "Off ";
    String disaster_ = "Disaster ";
    String rock_ = "Rock ";
    String stall_ = "Stall ";
    String board_ = "Board ";
    String bonk_ = "Bonk ";
    String roll_ = "Roll ";
    String bagel_ = "Bagel ";
    String tripod_ = "Tripod ";
    String layback_ = "Layback ";
    String handplant_ = "Handplant ";
    String miller_flip_ = "Miller Flip ";
    String block_ = "Block ";
    String poptart_ = "Poptart ";
    String pop_ = "Pop ";

    // slang version of trick name words
    String front_ = "Front ";
    String back_ = "Back ";
    String half_ = "Half ";
    String caballerial_ = "Caballerial ";
    String blunt_ = "Blunt ";
    String lip_ = "Lip ";

    // number strings
    String num450_ = "450 ";
    String num630_ = "630 ";
    String num180_ = "180 ";
    String num270_ = "270 ";
    String num360_ = "360 ";

    // types of features
    String box = "(Box)";
    String rail = "(Rail)";
    String pipe = "(Pipe)";
    String box_ = "Box ";
    String rail_ = "Rail ";
    String pipe_ = "Pipe ";
    String tree_ = "Tree ";

    // grab names used in the drop down list
    String nose = "Nose";
    String crail = "Crail";
    String mute = "Mute";
    String indy = "Indy";
    String seatbelt = "Seatbelt";
    String tail = "Tail";
    String stalefish = "Stalefish";
    String melon = "Melon";
    String other = "Other";

    // Placeholder text
    String grabPlaceholder_grab_ = "(grab name) "+grab_;
    String rotationPlaceholder_ = "(rotation) ";
    String rotation2Placeholder_ = "(rotation2) ";
    String featurePlaceholder_ = "(feature) ";

    // rules keys
    String aerial_switch_to_natural_rule = "aerialSwitchToNatural";
    String cab_to_switch_backside_rule = "cabToSwitchBackside";
    String switch_ollie_to_fakie_rule = "switchOllieToFakie";
    String remove_ollie_rule = "removeOllie";
    String tail_to_nose_rule = "tailToNose";
    String remove_tail_press_rule = "removeTailPress";
    String noseslide_to_bluntslide_rule = "noseslideToBluntslide";
    String noseslide_to_boardslide_rule = "noseslideToBoardslide";
    String noseslide_to_noseblunt_rule = "noseslideToNoseblunt";
    String noseslide_to_tailslide_rule = "noseslideToTailslide";
    String noseslide_to_lipslide_rule = "noseslideToLipslide";
    String switch_backside_to_hardway_cab_rule = "switchBacksideToHardwayCab";
    String switch_backside_to_hardway_cab_and_boardslide_to_lipslide_rule = "switchBacksideToHardwayCabAndBoardslideToLipslide";
    String frontside_to_backside_rule = "frontsideToBackside";
    String swap_switch_backside_with_cab_rule = "swapSwitchBacksideWithCab";
    String backside_to_frontside_rule = "backsideToFrontside";
    String jib_switch_to_natural_rule = "jibSwitchToNatural";
    String box_to_rail_rule = "boxToRail";
    String box_to_pipe_rule = "boxToPipe";
    String backside_to_frontside_or_cab_to_switch_backside_rule = "backsideToFrontsideOrCabToSwitchBackside";
    String swap_frontside_with_backside_rule = "swapFrontsideWithBackside";
    String ollie_to_nollie_rule = "ollieToNollie";

    String aerial = "Aerial";
    String butter = "Butter";
    String jib = "Jib";
}
