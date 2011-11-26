package com.mashup.resys.neighborhood;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.SamplingLongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.mashup.resys.model.BiaDataModel;


// 用于推荐好友，需寻找除好友之外的最近邻居
public class BiaNearestNUserNeighborhood extends NearestNUserNeighborhood {
	
	public BiaNearestNUserNeighborhood(int n, UserSimilarity userSimilarity,
			DataModel dataModel) {
		super(n, userSimilarity, dataModel);
		// TODO Auto-generated constructor stub
	}
	
	public BiaNearestNUserNeighborhood(int n, double minSimilarity,
			UserSimilarity userSimilarity, DataModel dataModel,
			double samplingRate) {
		super(n, minSimilarity, userSimilarity, dataModel, samplingRate);
		// TODO Auto-generated constructor stub
	}

	public BiaNearestNUserNeighborhood(int n, double minSimilarity,
			UserSimilarity userSimilarity, DataModel dataModel) {
		super(n, minSimilarity, userSimilarity, dataModel);
		// TODO Auto-generated constructor stub
	}

	@Override
	public long[] getUserNeighborhood(long userID) throws TasteException {
		// TODO Auto-generated method stub
		UserSimilarity userSimilarityImpl = getUserSimilarity();

		TopItems.Estimator<Long> estimator = new Estimator(userSimilarityImpl,
				userID, minSimilarity);

		LongPrimitiveIterator userIDs = SamplingLongPrimitiveIterator
				.maybeWrapIterator(((BiaDataModel)getDataModel()).getUserIDsExceptFriends((int)userID), getSamplingRate());

		return TopItems.getTopUsers(n, userIDs, null, estimator);
	}

	private static class Estimator implements TopItems.Estimator<Long> {
		private final UserSimilarity userSimilarityImpl;
		private final long theUserID;
		private final double minSim;

		private Estimator(UserSimilarity userSimilarityImpl, long theUserID,
				double minSim) {
			this.userSimilarityImpl = userSimilarityImpl;
			this.theUserID = theUserID;
			this.minSim = minSim;
		}

		@Override
		public double estimate(Long userID) throws TasteException {
			if (userID == theUserID) {
				return Double.NaN;
			}
			double sim = userSimilarityImpl.userSimilarity(theUserID, userID);
			return sim >= minSim ? sim : Double.NaN;
		}
	}
}
