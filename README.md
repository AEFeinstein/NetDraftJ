# NetDraftJ
Online Draft Client &amp; Server for Magic: The Gathering, written in Java

## Building

This is an Eclipse project, so it's easiest to develop there.

The Windows executable can be built with `make clean all`. The dependencies are
1. make (part of msys2, mingw, etc.)
2. [Apache Ant](https://ant.apache.org/)
3. [Launch4j](http://launch4j.sourceforge.net/). 

## Features

NetDraftJ supports drafting cubes, which are supplied as a plaintext list of cards. See [`LegacyCube.txt`](https://github.com/AEFeinstein/NetDraftJ/blob/master/LegacyCube.txt) for an example.

NetDraftJ also supports drafting packs, which supplied as JSON. See [`ody_block.json`](https://github.com/AEFeinstein/NetDraftJ/blob/master/ody_block.json) for an example. Mythics replace rares at a 1/8 rate, if the set contains mythics. `'T'` is the rarity for timeshifted cards.

For reference, the values for `setCode` are:

| Name  | Code |
| ------------- | ------------- |
| Ikoria Commander | C20 |
| Ikoria: Lair of Behemoths | IKO |
| Secret Lair Drop | SLD |
| Unsanctioned | UND |
| Theros Beyond Death | THB |
| Game Night 2019 | GN2 |
| Mystery Booster | MB1 |
| Throne of Eldraine | ELD |
| Commander 2019 | C19 |
| Core Set 2020 | M20 |
| Modern Horizons | MH1 |
| Signature Spellbook: Gideon | SS2 |
| War of the Spark | WAR |
| War of the Spark Mythic Edition | WARMED |
| Ravnica Allegiance: Guild Kits | GK2 |
| Ravnica Allegiance | RNA |
| Ravnica Allegiance Mythic Edition | RNAMED |
| Ultimate Box Toppers | PUMA |
| Ultimate Masters | UMA |
| Game Night | GNT |
| Gift Pack | M19GP |
| Guilds of Ravnica Mythic Edition | GRNMED |
| Guilds of Ravnica: Guild Kits | GK1 |
| Guilds of Ravnica | GRN |
| Commander 2018 | C18 |
| Core Set 2019 | M19 |
| Commander Anthology 2018 | CM2 |
| Global Series: Jiang Yanggu and Mu Yanling | GS1 |
| Signature Spellbook: Jace | SS1 |
| Battlebond | BBD |
| Dominaria | DOM |
| Duel Decks: Elves vs. Inventors | DDU |
| Masters 25 | A25 |
| Rivals of Ixalan | RIX |
| Unstable | UST |
| Duel Decks: Merfolk vs. Goblins | DDT |
| Explorers of Ixalan | E02 |
| From the Vault: Transform | V17 |
| Iconic Masters | IMA |
| Ixalan | XLN |
| Commander 2017 | C17 |
| Hour of Devastation | HOU |
| Archenemy: Nicol Bolas | E01 |
| Commander Anthology | CMA |
| Tempest Remastered | TPR |
| Amonkhet | AKH |
| Masterpiece Series: Amonkhet Invocations | MPSAKH |
| Welcome Deck 2017 | W17 |
| Duel Decks: Mind vs. Might | DDS |
| Modern Masters 2017 Edition | MM3 |
| Aether Revolt | AER |
| Commander 2016 | C16 |
| Planechase Anthology | PCA |
| Duel Decks: Nissa vs. Ob Nixilis | DDR |
| Kaladesh | KLD |
| Masterpiece Series: Kaladesh Inventions | MPSKLD |
| Conspiracy: Take the Crown | CN2 |
| From the Vault: Lore | V16 |
| Eldritch Moon | EMN |
| Eternal Masters | EMA |
| Shadows over Innistrad | SOI |
| Welcome Deck 2016 | W16 |
| Duel Decks: Blessed vs. Cursed | DDQ |
| Oath of the Gatewatch | OGW |
| Commander 2015 | C15 |
| Battle for Zendikar | BFZ |
| Zendikar Expeditions | EXP |
| Duel Decks: Zendikar vs. Eldrazi | DDP |
| From the Vault: Angels | V15 |
| Magic Origins | ORI |
| Modern Masters 2015 Edition | MM2 |
| Dragons of Tarkir | DTK |
| Duel Decks: Elspeth vs. Kiora | DDO |
| Fate Reforged | FRF |
| Duel Decks Anthology, Divine vs. Demonic | DD3DVD |
| Duel Decks Anthology, Elves vs. Goblins | DD3EVG |
| Duel Decks Anthology, Garruk vs. Liliana | DD3GVL |
| Duel Decks Anthology, Jace vs. Chandra | DD3JVC |
| Ugin's Fate promos | UFP |
| Commander 2014 | C14 |
| Duel Decks: Speed vs. Cunning | DDN |
| Khans of Tarkir | KTK |
| From the Vault: Annihilation (2014) | V14 |
| Magic 2015 Core Set | M15 |
| Magic: The Gathering-Conspiracy | CNS |
| Vintage Masters | VMA |
| Journey into Nyx | JOU |
| Modern Event Deck 2014 | MD1 |
| Duel Decks: Jace vs. Vraska | DDM |
| Born of the Gods | BNG |
| Commander 2013 Edition | C13 |
| Duel Decks: Heroes vs. Monsters | HVM |
| Theros | THS |
| From the Vault: Twenty | V13 |
| Magic 2014 Core Set | M14 |
| Modern Masters | MMA |
| Dragon's Maze | DGM |
| Duel Decks: Sorin vs. Tibalt | SVT |
| Gatecrash | GTC |
| Commander's Arsenal | CRS |
| Return to Ravnica | RTR |
| Duel Decks: Izzet vs. Golgari | IVG |
| From the Vault: Realms | RLM |
| Magic 2013 | M13 |
| Planechase 2012 Edition | PC2 |
| Avacyn Restored | AVR |
| Duel Decks: Venser vs. Koth | VVK |
| Dark Ascension | DKA |
| Premium Deck Series: Graveborn | GRV |
| Innistrad | ISD |
| Duel Decks: Ajani vs. Nicol Bolas | AVB |
| From the Vault: Legends | LEG |
| Magic 2012 | M12 |
| Magic: The Gathering-Commander | CMD |
| New Phyrexia | NPH |
| Duel Decks: Knights vs. Dragons | KVD |
| Masters Edition IV | ME4 |
| Mirrodin Besieged | MBS |
| Premium Deck Series: Fire and Lightning | FAL |
| Duel Decks: Elspeth vs. Tezzeret | EVT |
| Scars of Mirrodin | SOM |
| From the Vault: Relics | RLC |
| Magic 2011 | M11 |
| Archenemy | ARC |
| Rise of the Eldrazi | ROE |
| Duel Decks: Phyrexia vs. the Coalition | PVC |
| Worldwake | WWK |
| Premium Deck Series: Slivers | SLI |
| Duel Decks: Garruk vs. Liliana | GVL |
| Masters Edition III | ME3 |
| Planechase | PCH |
| Zendikar | ZEN |
| From the Vault: Exiled | EXL |
| Magic 2010 | M10 |
| Alara Reborn | ARB |
| Duel Decks: Divine vs. Demonic | DVD |
| Conflux | CFX |
| Duel Decks: Jace vs. Chandra | JVC |
| Masters Edition II | ME2 |
| Shards of Alara | ALA |
| From the Vault: Dragons | DRG |
| Eventide | EVE |
| Shadowmoor | SHM |
| Morningtide | MOR |
| Duel Decks: Elves vs. Goblins | EVG |
| Masters Edition | MED |
| Lorwyn | LRW |
| Tenth Edition | 10E |
| Future Sight | FUT |
| Planar Chaos | PLC |
| Time Spiral | TSP |
| Time Spiral "Timeshifted" | TSB |
| Coldsnap | CS |
| Dissension | DIS |
| Guildpact | GP |
| Ravnica: City of Guilds | RAV |
| Ninth Edition | 9E |
| Saviors of Kamigawa | SOK |
| Betrayers of Kamigawa | BOK |
| Unhinged | UNH |
| Champions of Kamigawa | CHK |
| Fifth Dawn | FD |
| Darksteel | DS |
| Mirrodin | MR |
| Eighth Edition | 8E |
| Scourge | SC |
| Legions | LE |
| Onslaught | ON |
| Judgment | JU |
| Torment | TO |
| Odyssey | OD |
| Apocalypse | AP |
| Seventh Edition | 7E |
| Planeshift | PS |
| Beatdown Box Set | BD |
| Invasion | IN |
| Starter 2000 | S2 |
| Prophecy | PY |
| Nemesis | NE |
| Battle Royale Box Set | BR |
| Mercadian Masques | MM |
| Starter 1999 | ST |
| Portal Three Kingdoms | P3 |
| Urza's Destiny | UD |
| Classic Sixth Edition | 6E |
| Urza's Legacy | UL |
| Urza's Saga | US |
| Unglued | UG |
| Exodus | EX |
| Portal Second Age | P2 |
| Stronghold | SH |
| Tempest | TE |
| Portal | PT |
| Weatherlight | WL |
| Fifth Edition | 5E |
| Visions | VI |
| Mirage | MI |
| Alliances | AL |
| Homelands | HL |
| Chronicles | CH |
| Ice Age | IA |
| Fourth Edition | 4E |
| Fallen Empires | FE |
| The Dark | DK |
| Legends | LG |
| Antiquities | AQ |
| Revised Edition | R |
| Arabian Nights | AN |
| Unlimited Edition | U |
| Limited Edition Beta | B |
| Limited Edition Alpha | A |
| Vanguard | VNG |
| Promo set for Gatherer | MBP |
