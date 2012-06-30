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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.NoSuchConnectionException;
import org.springframework.social.connect.NotConnectedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Michael Lavelle
 */
class RooConnectionRepository implements ConnectionRepository {

	private final String userId;

	private final RooTemplate rooTemplate;

	private final ConnectionFactoryLocator connectionFactoryLocator;

	private final TextEncryptor textEncryptor;

	public RooConnectionRepository(String userId, RooTemplate rooTemplate,
			ConnectionFactoryLocator connectionFactoryLocator,
			TextEncryptor textEncryptor) {
		this.userId = userId;
		this.rooTemplate = rooTemplate;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}

	public MultiValueMap<String, Connection<?>> findAllConnections() {

		List<Connection<?>> resultList = new ArrayList<Connection<?>>();
		for (UserConnection userConnection : rooTemplate
				.getAllUserConnections(userId)) {
			resultList.add(connectionMapper.mapConnection(userConnection));
		}

		MultiValueMap<String, Connection<?>> connections = new LinkedMultiValueMap<String, Connection<?>>();
		Set<String> registeredProviderIds = connectionFactoryLocator
				.registeredProviderIds();
		for (String registeredProviderId : registeredProviderIds) {
			connections.put(registeredProviderId,
					Collections.<Connection<?>> emptyList());
		}
		for (Connection<?> connection : resultList) {
			String providerId = connection.getKey().getProviderId();
			if (connections.get(providerId).size() == 0) {
				connections.put(providerId, new LinkedList<Connection<?>>());
			}
			connections.add(providerId, connection);
		}
		return connections;
	}

	public List<Connection<?>> findConnections(String providerId) {
		List<Connection<?>> connections = new ArrayList<Connection<?>>();
		for (UserConnection userConnection : rooTemplate.getAllUserConnections(
				userId, providerId)) {
			connections.add(connectionMapper.mapConnection(userConnection));
		}

		return connections;
	}

	@SuppressWarnings("unchecked")
	public <A> List<Connection<A>> findConnections(Class<A> apiType) {
		List<?> connections = findConnections(getProviderId(apiType));
		return (List<Connection<A>>) connections;
	}

	public MultiValueMap<String, Connection<?>> findConnectionsToUsers(
			MultiValueMap<String, String> providerUsers) {
		if (providerUsers == null || providerUsers.isEmpty()) {
			throw new IllegalArgumentException(
					"Unable to execute find: no providerUsers provided");
		}
		List<Connection<?>> resultList = connectionMapper
				.mapConnections(rooTemplate.getAllUserConnections(userId,
						providerUsers));

		MultiValueMap<String, Connection<?>> connectionsForUsers = new LinkedMultiValueMap<String, Connection<?>>();
		for (Connection<?> connection : resultList) {
			String providerId = connection.getKey().getProviderId();
			List<String> userIds = providerUsers.get(providerId);
			List<Connection<?>> connections = connectionsForUsers
					.get(providerId);
			if (connections == null) {
				connections = new ArrayList<Connection<?>>(userIds.size());
				for (int i = 0; i < userIds.size(); i++) {
					connections.add(null);
				}
				connectionsForUsers.put(providerId, connections);
			}
			String providerUserId = connection.getKey().getProviderUserId();
			int connectionIndex = userIds.indexOf(providerUserId);
			connections.set(connectionIndex, connection);
		}
		return connectionsForUsers;
	}

	public Connection<?> getConnection(ConnectionKey connectionKey) {

		UserConnection userConnection = null;

		userConnection = rooTemplate.getUserConnection(userId,
				connectionKey.getProviderId(),
				connectionKey.getProviderUserId());
		if (userConnection == null) {
			throw new NoSuchConnectionException(connectionKey);
		}

		return connectionMapper.mapConnection(userConnection);

	}

	@SuppressWarnings("unchecked")
	public <A> Connection<A> getConnection(Class<A> apiType,
			String providerUserId) {
		String providerId = getProviderId(apiType);
		return (Connection<A>) getConnection(new ConnectionKey(providerId,
				providerUserId));
	}

	@SuppressWarnings("unchecked")
	public <A> Connection<A> getPrimaryConnection(Class<A> apiType) {
		String providerId = getProviderId(apiType);
		Connection<A> connection = (Connection<A>) findPrimaryConnection(providerId);
		if (connection == null) {
			throw new NotConnectedException(providerId);
		}
		return connection;
	}

	@SuppressWarnings("unchecked")
	public <A> Connection<A> findPrimaryConnection(Class<A> apiType) {
		String providerId = getProviderId(apiType);
		return (Connection<A>) findPrimaryConnection(providerId);
	}

	@Transactional
	public void addConnection(Connection<?> connection) {
		ConnectionData data = connection.createData();
		int rank = rooTemplate.getRank(userId, data.getProviderId());
		rooTemplate.createUserConnection(userId, data.getProviderId(),
				data.getProviderUserId(), rank, data.getDisplayName(),
				data.getProfileUrl(), data.getImageUrl(),
				encrypt(data.getAccessToken()), encrypt(data.getSecret()),
				encrypt(data.getRefreshToken()), data.getExpireTime());

	}

	@Transactional
	public void updateConnection(Connection<?> connection) {
		ConnectionData data = connection.createData();
		UserConnection userConnection = rooTemplate.getUserConnection(userId,
				data.getProviderId(), data.getProviderUserId());
		userConnection.setDisplayName(data.getDisplayName());
		userConnection.setProfileUrl(data.getProfileUrl());
		userConnection.setImageUrl(data.getImageUrl());
		userConnection.setAccessToken(encrypt(data.getAccessToken()));
		userConnection.setSecret(encrypt(data.getSecret()));
		userConnection.setRefreshToken(encrypt(data.getRefreshToken()));
		userConnection.setExpireTime(data.getExpireTime());
		rooTemplate.saveUserConnection(userConnection);

	}

	public void removeConnections(String providerId) {
		rooTemplate.removeUserConnections(userId, providerId);
	}

	public void removeConnection(ConnectionKey connectionKey) {
		rooTemplate.removeUserConnection(userId, connectionKey.getProviderId(),
				connectionKey.getProviderUserId());
	}

	private Connection<?> findPrimaryConnection(String providerId) {

		List<UserConnection> userConnections = rooTemplate
				.getPrimaryUserConnections(userId, providerId);
		if (userConnections.size() > 0) {
			return connectionMapper.mapConnection(userConnections.get(0));
		} else {
			return null;
		}
	}

	private final ServiceProviderConnectionMapper connectionMapper = new ServiceProviderConnectionMapper();

	private final class ServiceProviderConnectionMapper {

		public List<Connection<?>> mapConnections(
				List<UserConnection> userConnections) {
			List<Connection<?>> connections = new ArrayList<Connection<?>>();
			for (UserConnection userConnection : userConnections) {
				connections.add(mapConnection(userConnection));
			}
			return connections;
		}

		public Connection<?> mapConnection(UserConnection userConnection) {
			ConnectionData connectionData = mapConnectionData(userConnection);
			ConnectionFactory<?> connectionFactory = connectionFactoryLocator
					.getConnectionFactory(connectionData.getProviderId());
			return connectionFactory.createConnection(connectionData);
		}

		private ConnectionData mapConnectionData(UserConnection userConnection) {
			return new ConnectionData(userConnection.getProviderId(),
					userConnection.getProviderUserId(),
					userConnection.getDisplayName(),
					userConnection.getProfileUrl(),
					userConnection.getImageUrl(),
					decrypt(userConnection.getAccessToken()),
					decrypt(userConnection.getSecret()),
					decrypt(userConnection.getRefreshToken()),
					userConnection.getExpireTime());
		}

		private String decrypt(String encryptedText) {
			return encryptedText != null ? textEncryptor.decrypt(encryptedText)
					: encryptedText;
		}
	}

	private <A> String getProviderId(Class<A> apiType) {
		return connectionFactoryLocator.getConnectionFactory(apiType)
				.getProviderId();
	}

	private String encrypt(String text) {
		return text != null ? textEncryptor.encrypt(text) : text;
	}

}