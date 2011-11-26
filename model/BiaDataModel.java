package com.mashup.resys.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveArrayIterator;

import com.mashup.domain.Collection;
import com.mashup.domain.Friend;
import com.mashup.domain.Product;
import com.mashup.domain.ProductPreference;
import com.mashup.domain.User;
import com.mashup.service.ICollectionService;
import com.mashup.service.IProductPreferenceService;
import com.mashup.service.IFriendService;
import com.mashup.service.IUserService;

import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;

public class BiaDataModel extends MySQLJDBCDataModel {
	public static final String DEFAULT_PREFERENCE_TABLE = "product_preference";
	public static final String DEFAULT_USER_ID_COLUMN = "userID";
	public static final String DEFAULT_ITEM_ID_COLUMN = "productId";
	public static final String DEFAULT_PREFERENCE_COLUMN = "preference";

	private ICollectionService collectionService;
	private IProductPreferenceService productPreferenceService;
	private IFriendService friendService;
	private IUserService userService;
	
	public BiaDataModel(DataSource dataSource) {
		super(dataSource, DEFAULT_PREFERENCE_TABLE, DEFAULT_USER_ID_COLUMN,
				DEFAULT_ITEM_ID_COLUMN, DEFAULT_PREFERENCE_COLUMN);

	}

	public Boolean isFavorite(int userID, int itemID) throws TasteException {

		List<Collection> collectionList = collectionService.findByUserId(userID);
		
		int i = 0;

		if (collectionList != null && collectionList.size() > 0) {

			for (Collection collection : collectionList) {
				if (collection.getProduct().getProductId() == itemID) {
					i++;
				}
			}
		}

		if (i > 0) {
			return true;
		} else {
			return false;
		}

	}
	
	public FastIDSet getCollectItemIDsFromUser(long userID)
			throws TasteException {

		FastIDSet itemIDs = new FastIDSet();

		String query = "SELECT productId FROM collection where "
				+ DEFAULT_USER_ID_COLUMN + "=?";
		Connection conn = null;
		List<Product> topItems = new ArrayList<Product>();

		try {
			conn = (Connection) getDataSource().getConnection();
			PreparedStatement collectionStam = conn.prepareStatement(query);

			collectionStam.setInt(1, (int) userID);

			ResultSet resultSet = collectionStam.executeQuery();

			while (resultSet.next()) {
				itemIDs.add(resultSet.getInt(1));
			}

			collectionStam.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return itemIDs;
	}

	public FastIDSet getPreferenceIDsFromUser(long userID) throws TasteException {

		FastIDSet itemIDs = new FastIDSet();

		String query = "SELECT productId FROM product_preference where "
				+ DEFAULT_USER_ID_COLUMN + "=?";
		Connection conn = null;
		
		try {
			conn = (Connection) getDataSource().getConnection();
			PreparedStatement collectionStam = conn.prepareStatement(query);

			collectionStam.setInt(1, (int) userID);

			ResultSet resultSet = collectionStam.executeQuery();

			while (resultSet.next()) {
				itemIDs.add(resultSet.getInt(1));
			}
			resultSet.close();
			collectionStam.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return itemIDs;
	}
	
	public long[] getUserFriends(int userID) {
		
		List<Friend> userFriends = friendService.findByUserId(userID);
		
		long[] userFriendArray = new long[userFriends.size()];
		
		for (int i = 0; i < userFriendArray.length; i++) {
			
			userFriendArray[i] = userFriends.get(i).getUserByFriendId().getUserId();

		}
		
		return userFriendArray;
	}

	public LongPrimitiveIterator getUserIDsExceptFriends(int userID)
			throws TasteException {

		List<User> users = userService.findAll();

		List<Friend> theUserFriend = friendService.findByUserId(userID);

		for (int i = 0; i < theUserFriend.size(); i++) {
			users.remove(theUserFriend.get(i).getUserByFriendId());
		}

		long[] allUsersID = new long[users.size()];

		for (int i = 0; i < allUsersID.length; i++) {
			allUsersID[i] = users.get(i).getUserId();
		}

		LongPrimitiveIterator allUsers = new LongPrimitiveArrayIterator(
				allUsersID);

		return allUsers;
	}

	/**
	 * @param collectionService
	 *            the collectionService to set
	 */
	public void setCollectionService(ICollectionService collectionService) {
		this.collectionService = collectionService;
	}

	public void setFriendService(IFriendService friendService) {
		this.friendService = friendService;
	}

	public void setUserService(IUserService userService) {
		this.userService = userService;
	}

	public void setProductPreferenceService(
			IProductPreferenceService productPreferenceService) {
		this.productPreferenceService = productPreferenceService;
	}
	
}
