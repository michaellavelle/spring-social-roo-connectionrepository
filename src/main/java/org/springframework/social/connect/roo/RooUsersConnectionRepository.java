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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UsersConnectionRepository;

/**
 * {@link UsersConnectionRepository} that uses the Roo to persist connection
 * data to a relational database.
 * 
 * @author Michael Lavelle
 */
public class RooUsersConnectionRepository implements UsersConnectionRepository {

	private final RooTemplate rooTemplate;

	private final ConnectionFactoryLocator connectionFactoryLocator;

	private final TextEncryptor textEncryptor;

	private ConnectionSignUp connectionSignUp;

	public RooUsersConnectionRepository(RooTemplate rooTemplate,
			ConnectionFactoryLocator connectionFactoryLocator,
			TextEncryptor textEncryptor) {
		this.rooTemplate = rooTemplate;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}

	/**
	 * The command to execute to create a new local user profile in the event no
	 * user id could be mapped to a connection. Allows for implicitly creating a
	 * user profile from connection data during a provider sign-in attempt.
	 * Defaults to null, indicating explicit sign-up will be required to
	 * complete the provider sign-in attempt.
	 * 
	 * @see #findUserIdsWithConnection(Connection)
	 */
	public void setConnectionSignUp(ConnectionSignUp connectionSignUp) {
		this.connectionSignUp = connectionSignUp;
	}

	public List<String> findUserIdsWithConnection(Connection<?> connection) {

		ConnectionKey key = connection.getKey();
		List<UserConnection> localUserConnections = rooTemplate
				.getUserConnections(connection.getKey().getProviderId(),
						connection.getKey().getProviderUserId());
		if (localUserConnections.size() == 0 && connectionSignUp != null) {
			String newUserId = connectionSignUp.execute(connection);
			if (newUserId != null) {
				createConnectionRepository(newUserId).addConnection(connection);
				return Arrays.asList(newUserId);
			}
		}
		List<String> localUserIds = new ArrayList<String>();
		for (UserConnection localUserConnection : localUserConnections) {
			localUserIds.add(localUserConnection.getUserId());
		}
		return localUserIds;
	}

	public Set<String> findUserIdsConnectedTo(String providerId,
			Set<String> providerUserIds) {

		return rooTemplate.findUsersConnectedTo(providerId, providerUserIds);

	}

	public ConnectionRepository createConnectionRepository(String userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId cannot be null");
		}
		return new RooConnectionRepository(userId, rooTemplate,
				connectionFactoryLocator, textEncryptor);
	}

}