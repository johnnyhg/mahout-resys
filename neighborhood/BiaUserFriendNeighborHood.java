package com.mashup.resys.neighborhood;

import java.util.Collections;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.mashup.domain.Friend;
import com.mashup.service.IFriendService;

import edu.emory.mathcs.backport.java.util.Arrays;

// 好友邻居，从好友中算出最近邻居
public class BiaUserFriendNeighborHood extends AbstractUserNeighborhood {

	private IFriendService friendService;
	
	public BiaUserFriendNeighborHood(UserSimilarity userSimilarity,
			DataModel dataModel) {
		super(userSimilarity, dataModel, 1.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mahout.cf.taste.neighborhood.UserNeighborhood#getUserNeighborhood(long)
	 */
	@Override
	public long[] getUserNeighborhood(long userID) throws TasteException,
			NullPointerException {
		// TODO Auto-generated method stub
		List<Friend> friendList = Collections.EMPTY_LIST;
		try {
			friendList = friendService.findByUserId((int)userID);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (friendList != null && friendList.size() != 0) {
			long[] theUserFriend = new long[friendList.size()];

			for (int i = 0; i < friendList.size(); i++) {
				theUserFriend[i] = friendList.get(i).getUserByFriendId().getUserId();
			}
			Arrays.sort(theUserFriend);

			return theUserFriend;

		} else {
			return null;
		}
	}

	/**
	 * @param friendService
	 *            the friendService to set
	 */
	public void setFriendService(IFriendService friendService) {
		this.friendService = friendService;
	}

}
