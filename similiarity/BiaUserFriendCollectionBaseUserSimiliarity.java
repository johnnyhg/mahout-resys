package com.mashup.resys.similiarity;

import java.util.Collection;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.mashup.service.ICollectionService;
import org.apache.log4j.*;

//计算两个用户收藏的共同商品的程度来计算用户相似度
public class BiaUserFriendCollectionBaseUserSimiliarity implements UserSimilarity{

	private ICollectionService collectionService;
	private final static Logger log = Logger.getLogger(BiaUserFriendCollectionBaseUserSimiliarity.class);

	@Override
	public double userSimilarity(long theUserID, long userID) throws TasteException {
		// TODO Auto-generated method stub
		
		List<Integer> theUserCollectionList = collectionService.findItemIdsByUserId((int)theUserID);
		
		List<Integer> userCollectionList = collectionService.findItemIdsByUserId((int)userID);
		
		int num = 0;
		
		for (int itemID : userCollectionList) {
			
			if (theUserCollectionList.contains(itemID)) {
				num++;
			}
			
		}
				
		return num == 0 ? Double.NaN : (double)num/theUserCollectionList.size();
	}
	
	@Override
	public void setPreferenceInferrer(PreferenceInferrer arg0) {
		// TODO Auto-generated method stub
		// do nothing
	}

	@Override
	public void refresh(Collection<Refreshable> arg0) {
		// TODO Auto-generated method stub
		// do nothing
	}

	public void setCollectionService(ICollectionService collectionService) {
		this.collectionService = collectionService;
	}
}
