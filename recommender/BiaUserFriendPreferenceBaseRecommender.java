package com.mashup.resys.recommender;

import java.util.List;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveArrayIterator;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import com.mashup.resys.model.BiaDataModel;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.common.LongPair;

import org.apache.mahout.cf.taste.recommender.IDRescorer;


public class BiaUserFriendPreferenceBaseRecommender extends
		GenericUserBasedRecommender {

	private final static Logger log = Logger
			.getLogger(BiaUserFriendCollectionBaseRecommender.class);

	private UserNeighborhood userNeighborhood;

	public BiaUserFriendPreferenceBaseRecommender(DataModel dataModel,
			UserNeighborhood neighborhood, UserSimilarity similarity) {
		super(dataModel, neighborhood, similarity);
	}
	
	@Override
	//给userID这个用户推荐商品
	public List<RecommendedItem> recommend(long userID, int howMany,
			IDRescorer rescorer) throws TasteException {
		if (howMany < 1) {
			throw new IllegalArgumentException("howMany must be at least 1");
		}

		//获取当前用户的好友
		long[] theNeighborhood = getUserNeighborhood().getUserNeighborhood(
				userID);

		if (theNeighborhood.length == 0) {
			return Collections.emptyList();
		}

		FastIDSet allItemIDs = getAllOtherItems(theNeighborhood, userID);

		TopItems.Estimator<Long> estimator = new Estimator(userID,
				theNeighborhood);

		List<RecommendedItem> topItems = TopItems.getTopItems(howMany,
				allItemIDs.iterator(), rescorer, estimator);

		return topItems;
	}

	/* (non-Javadoc)
	 * @see org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender#doEstimatePreference(long, long[], long)
	 */
	@Override
	//计算当前用户与其所有好友在itemID号商品上的可推荐指数
	protected float doEstimatePreference(long theUserID,
			long[] theNeighborhood, long itemID) throws TasteException {
		if (theNeighborhood.length == 0) {
			return Float.NaN;
		}
		BiaDataModel dataModel = (BiaDataModel)getDataModel();
		UserSimilarity similarity = getSimilarity();
		
		double preference = 0.0;
		double totalSimilarity = 0.0;
		int count = 0;
		for (long userID : theNeighborhood) {
			if (userID != theUserID) {
				// See GenericItemBasedRecommender.doEstimatePreference() too
				Float pref = dataModel.getPreferenceValue(userID, itemID);
				if (pref != null) {
					double theSimilarity = similarity.userSimilarity(theUserID,
							userID);
					
					if (!Double.isNaN(theSimilarity)) {
						preference += theSimilarity * pref;
						totalSimilarity += theSimilarity;
						count++;
					}
				}
			}
		}

		// Throw out the estimate if it was based on no data points, of course, but also if based on
		// just one. This is a bit of a band-aid on the 'stock' item-based algorithm for the moment.
		// The reason is that in this case the estimate is, simply, the user's rating for one item
		// that happened to have a defined similarity. The similarity score doesn't matter, and that
		// seems like a bad situation.
		if (count <= 1) {
			return Float.NaN;
		}
		float estimate = (float) (preference / totalSimilarity);

		return estimate;
	}

	@Override
	//计算与当前用户品味最接近的用户
	public long[] mostSimilarUserIDs(long userID, int howMany)
			throws TasteException {

		UserSimilarity similarity = getSimilarity();

		TopItems.Estimator<Long> estimator = new MostSimilarEstimator(userID,
				similarity, null);

		long[] userFriend = getUserNeighborhood().getUserNeighborhood(userID);
		LongPrimitiveIterator userIDs = new LongPrimitiveArrayIterator(
				userFriend);

		return TopItems.getTopUsers(howMany, userIDs, null, estimator);

	}

	@Override
	//返回当前用户所有好友推荐的商品中去除当前好友推荐的部分
	protected FastIDSet getAllOtherItems(long[] theNeighborhood, long theUserID)
			throws TasteException {
		FastIDSet possibleItemIDs = new FastIDSet();

		for (long userID : theNeighborhood) {
			possibleItemIDs.addAll(((BiaDataModel) getDataModel())
					.getPreferenceIDsFromUser(userID));
		}

		possibleItemIDs.removeAll(((BiaDataModel) getDataModel())
				.getPreferenceIDsFromUser(theUserID));

		return possibleItemIDs;
	}

	//最相似评估器
	private static class MostSimilarEstimator implements
			TopItems.Estimator<Long> {

		private final long toUserID;
		private final UserSimilarity similarity;
		private final Rescorer<LongPair> rescorer;

		private MostSimilarEstimator(long toUserID, UserSimilarity similarity,
				Rescorer<LongPair> rescorer) {
			this.toUserID = toUserID;
			this.similarity = similarity;
			this.rescorer = rescorer;
		}

		@Override
		public double estimate(Long userID) throws TasteException {
			// Don't consider the user itself as a possible most similar user
			if (userID == toUserID) {
				return Double.NaN;
			}
			if (rescorer == null) {
				return similarity.userSimilarity(toUserID, userID);
			} else {
				LongPair pair = new LongPair(toUserID, userID);
				if (rescorer.isFiltered(pair)) {
					return Double.NaN;
				}
				double originalEstimate = similarity.userSimilarity(toUserID,
						userID);
				return rescorer.rescore(pair, originalEstimate);
			}
		}
	}

	//评估器
	private final class Estimator implements TopItems.Estimator<Long> {

		private final long theUserID;
		private final long[] theNeighborhood;

		Estimator(long theUserID, long[] theNeighborhood) {
			this.theUserID = theUserID;
			this.theNeighborhood = theNeighborhood;
		}

		@Override
		public double estimate(Long itemID) throws TasteException {
			return doEstimatePreference(theUserID, theNeighborhood, itemID);
		}
	}

	public UserNeighborhood getUserNeighborhood() {
		return userNeighborhood;
	}

	public void setUserNeighborhood(UserNeighborhood userNeighborhood) {
		this.userNeighborhood = userNeighborhood;
	}
}
