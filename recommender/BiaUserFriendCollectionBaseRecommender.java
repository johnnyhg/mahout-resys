package com.mashup.resys.recommender;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.mashup.resys.model.BiaDataModel;

public class BiaUserFriendCollectionBaseRecommender extends GenericUserBasedRecommender {

	private final static Logger log = Logger.getLogger(BiaUserFriendCollectionBaseRecommender.class);

	public BiaUserFriendCollectionBaseRecommender(DataModel dataModel,
			UserNeighborhood neighborhood, UserSimilarity similarity) {
		super(dataModel, neighborhood, similarity);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender#doEstimatePreference(long, long[], long)
	 */
	@Override
	protected float doEstimatePreference(long theUserID,
			long[] theNeighborhood, long itemID) throws TasteException {
		// TODO Auto-generated method stub
		if (theNeighborhood.length == 0) {
			return Float.NaN;
		}
		
		UserSimilarity similarity = getSimilarity();
		
		float totalSimilarity = 0.0f;
		
		boolean foundAPref = false;
		
		for (long userID : theNeighborhood) {
			if ((userID != theUserID)&& (((BiaDataModel)getDataModel()).isFavorite((int) userID, (int) itemID) != false)) {
				foundAPref = true;
				totalSimilarity += similarity.userSimilarity(theUserID, userID);
			}
		}
		
		return foundAPref ? totalSimilarity : Float.NaN;
	}

	/* (non-Javadoc)
	 * @see org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender#getAllOtherItems(long[], long)
	 */
	@Override
	protected FastIDSet getAllOtherItems(long[] theNeighborhood, long theUserID)
			throws TasteException {
		
		FastIDSet possibleItemIDs = new FastIDSet();
		
		for (long userID : theNeighborhood) {
			possibleItemIDs.addAll(((BiaDataModel)getDataModel()).getCollectItemIDsFromUser(userID));
		}
		
		possibleItemIDs.removeAll(((BiaDataModel)getDataModel()).getCollectItemIDsFromUser(theUserID));
		
		return possibleItemIDs;
	}
}
