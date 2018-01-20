package com.gelakinetic.NetDraftJ.Database;

public class MtgCard {

	public int mMultiverseId;
	public String mCardName;
	public String mMagicCardsInfoSetCode;
	public String mCardNumber;
	public String mCardType;
	public String mSetCode;

	public MtgCard(String name) {
		mMultiverseId = 0;
		mCardName = name;
		mMagicCardsInfoSetCode = null;
		mCardNumber = null;
		mCardType = null;
		mSetCode = null;
	}

	public MtgCard(int multiverseId) {
		mMultiverseId = multiverseId;
		mCardName = null;
		mMagicCardsInfoSetCode = null;
		mCardNumber = null;
		mCardType = null;
		mSetCode = null;
	}
}
