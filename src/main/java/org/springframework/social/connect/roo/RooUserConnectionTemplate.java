/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.connect.roo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.DuplicateConnectionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

/**
 * RooTemplate implementation to enable use of Roo to persist UserConnections.
 * This is a prototype implementation which is functionally complete but can be
 * improved in terms of efficiency. In particular, the methods
 * getRank(),findUsersConnectedTo() and getAll(String userId,
 * MultiValueMap<String, String> providerUsers) are naive implementations which
 * use existing finder methods to load lists of objects and apply operations to
 * each of the loaded objects. These methods can be improved by writing more
 * specialised finder/remove methods on the entities themselves to remove need
 * to load lists of objects but delegate work to the DAO layer.
 * 
 * @author Michael Lavelle
 */
@Service
public class RooUserConnectionTemplate implements RooTemplate {

	@Override
	public Set<String> findUsersConnectedTo(String providerId,
			Set<String> providerUserIds) {
		return new HashSet<String>(UserConnection
				.findUserIdsByProviderIdAndProviderUserIds(providerId,
						providerUserIds).getResultList());
	}

	@Override
	public List<UserConnection> getPrimaryUserConnections(String userId,
			String providerId) {

		return UserConnection.findUserConnectionsByUserIdAndProviderIdAndRank(
				userId, providerId, 1).getResultList();
	}

	@Override
	@Transactional
	public int getRank(String userId, String providerId) {

		Integer maxRank = UserConnection.findMaxRankByUserIdAndProviderId(
				userId, providerId).getSingleResult();
		return maxRank == null ? 1 : (maxRank.intValue() + 1);
	}

	@Override
	public List<UserConnection> getAllUserConnections(String userId,
			MultiValueMap<String, String> providerUsers) {
		List<UserConnection> remoteUsers = new ArrayList<UserConnection>();
		for (Map.Entry<String, List<String>> providerUsersEntry : providerUsers
				.entrySet()) {
			String providerId = providerUsersEntry.getKey();
			for (String providerUserId : providerUsersEntry.getValue()) {
				UserConnection userConnection = getUserConnection(userId,
						providerId, providerUserId);
				if (userConnection != null) {
					remoteUsers.add(userConnection);
				}
			}
		}
		return remoteUsers;
	}

	@Override
	public List<UserConnection> getAllUserConnections(String userId) {

		return UserConnection.findUserConnectionsByUserId(userId)
				.getResultList();
	}

	@Override
	public List<UserConnection> getAllUserConnections(String userId,
			String providerId) {
		return UserConnection.findUserConnectionsByUserIdAndProviderId(userId,
				providerId).getResultList();
	}

	@Override
	public UserConnection getUserConnection(String userId, String providerId,
			String providerUserId) {
		try {
			UserConnection userConnection = UserConnection
					.findUserConnectionByUserIdAndProviderIdAndProviderUserId(
							userId, providerId, providerUserId);
			return userConnection;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<UserConnection> getUserConnections(String providerId,
			String providerUserId) {
		return UserConnection.findUserConnectionsByProviderIdAndProviderUserId(
				providerId, providerUserId).getResultList();
	}

	@Override
	@Transactional
	public void removeUserConnections(String userId, String providerId) {
		for (UserConnection userConnection : UserConnection
				.findUserConnectionsByUserIdAndProviderId(userId, providerId)
				.getResultList()) {
			userConnection.remove();
		}
	}

	@Override
	@Transactional
	public void removeUserConnection(String userId, String providerId,
			String providerUserId) {
		UserConnection userConnection = null;
		try {
			userConnection = UserConnection
					.findUserConnectionByUserIdAndProviderIdAndProviderUserId(
							userId, providerId, providerUserId);
			if (userConnection != null)
				userConnection.remove();
		} catch (EmptyResultDataAccessException e) {
		}
	}

	@Override
	@Transactional
	public UserConnection createUserConnection(String userId,
			String providerId, String providerUserId, int rank,
			String displayName, String profileUrl, String imageUrl,
			String accessToken, String secret, String refreshToken,
			Long expireTime) {

		try {
			UserConnection existingConnection = getUserConnection(userId,
					providerId, providerUserId);
			if (existingConnection != null)
				throw new DuplicateConnectionException(new ConnectionKey(
						providerId, providerUserId));
		} catch (EmptyResultDataAccessException e) {
		}

		UserConnection userConnection = new UserConnection();
		userConnection.setUserId(userId);
		userConnection.setProviderId(providerId);
		userConnection.setProviderUserId(providerUserId);
		userConnection.setRank(rank);
		userConnection.setDisplayName(displayName);
		userConnection.setProfileUrl(profileUrl);
		userConnection.setImageUrl(imageUrl);
		userConnection.setAccessToken(accessToken);
		userConnection.setSecret(secret);
		userConnection.setRefreshToken(refreshToken);
		userConnection.setExpireTime(expireTime);
		userConnection.persist();
		return userConnection;
	}

	@Override
	@Transactional
	public UserConnection saveUserConnection(UserConnection userConnection) {
		userConnection.persist();
		return userConnection;
	}

}
