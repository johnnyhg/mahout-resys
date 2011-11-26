package com.mashup.resys.recommender;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.mashup.resys.neighborhood.BiaNearestNUserNeighborhood;

//比啊用户好友推荐
public class BiaUserFriendRecommender {
	
	private DataModel dataModel;
	
	public long[] recommend(int userID,int howmany) throws TasteException{
		
		long[] userIDs;
		UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(dataModel);
		userSimilarity = new PearsonCorrelationSimilarity(dataModel);
		UserNeighborhood biaUserNeighborhood = new BiaNearestNUserNeighborhood(howmany,userSimilarity,dataModel);
		userIDs = biaUserNeighborhood.getUserNeighborhood(userID);
		
		return userIDs;
	}
	
	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}
	
	
}
