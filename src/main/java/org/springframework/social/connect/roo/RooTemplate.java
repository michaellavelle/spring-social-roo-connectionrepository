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

import java.util.List;
import java.util.Set;

import org.springframework.util.MultiValueMap;

/**
 * @author Michael Lavelle
 */
public interface RooTemplate {

	public UserConnection saveUserConnection(UserConnection userConnection);

	public UserConnection createUserConnection(String userId,
			String providerId, String providerUserId, int rank,
			String displayName, String profileUrl, String imageUrl,
			String accessToken, String secret, String refreshToken,
			Long expireTime);

	public void removeUserConnection(String userId, String providerId,
			String providerUserId);

	public void removeUserConnections(String userId, String providerId);

	public List<UserConnection> getUserConnections(String providerId,
			String providerUserId);

	public UserConnection getUserConnection(String userId, String providerId,
			String providerUserId);

	public List<UserConnection> getAllUserConnections(String userId,
			String providerId);

	public List<UserConnection> getAllUserConnections(String userId);

	public List<UserConnection> getAllUserConnections(String userId,
			MultiValueMap<String, String> providerUsers);

	public int getRank(String userId, String providerId);

	public List<UserConnection> getPrimaryUserConnections(String userId,
			String providerId);

	public Set<String> findUsersConnectedTo(String providerId,
			Set<String> providerUserIds);

}
