package com.gelakinetic.NetDraftJ.Server;

import java.util.HashMap;
import java.util.List;

// This class defines what limited pack data is used in a draft. It's loaded from a JSON file
public class LimitedDraftPacks {

    class LimitedPack {
        // The set code for this pack
        String setCode;
        // The distribution of commons, uncommons, and rares for this pack
        HashMap<Character, Integer> distribution;
        // The highest number to use for normal packs
        int maxCardNumber = -1;
    }

    // A list of limited packs to use for this draft
    List<LimitedPack> limitedPacks;
}
