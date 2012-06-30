package org.springframework.social.connect.roo;

import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.extension.connect.jdbc.AbstractUsersConnectionRepositoryTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = { "/META-INF/spring/applicationContext.xml" })

public class RooUsersConnectionRepositoryTest extends
		AbstractUsersConnectionRepositoryTest<RooUsersConnectionRepository> {

	private EmbeddedDatabase database;

	private boolean testMySqlCompatiblity = true;

	@Autowired
	private ResourceDatabasePopulator resourceDatabasePopulator;

	@Autowired
	private RooTemplate dataAccessor;

	@Autowired
	private LocalContainerEntityManagerFactoryBean entityManagerFactory;

	@Override
	protected Boolean checkIfProviderConnectionsExist(String arg0) {
		return null;
	}

	@Override
	protected RooUsersConnectionRepository createUsersConnectionRepository() {
		EmbeddedDatabaseFactory factory = new EmbeddedDatabaseFactory();
		if (testMySqlCompatiblity) {
			factory.setDatabaseConfigurer(new DataSourceTestConfig.MySqlCompatibleH2DatabaseConfigurer());
		} else {
			factory.setDatabaseType(EmbeddedDatabaseType.H2);
		}

		factory.setDatabasePopulator(resourceDatabasePopulator);
		database = factory.getDatabase();
		entityManagerFactory.setDataSource(database);
		return new RooUsersConnectionRepository(dataAccessor,
				connectionFactoryRegistry, Encryptors.noOpText());
	}
	
	@After
	public void tearDown() {
		if (database != null) {
			database.shutdown();
		}
	}

	@Override
	protected void insertConnection(String userId, String providerId,
			String providerUserId, int rank, String displayName,
			String profileUrl, String imageUrl, String accessToken,
			String secret, String refreshToken, Long expireTime) {
		dataAccessor.createUserConnection(userId, providerId, providerUserId,
				rank, displayName, profileUrl, imageUrl, accessToken, secret,
				refreshToken, expireTime);

	}

	@Override
	protected void setConnectionSignUpOnUsersConnectionRepository(
			RooUsersConnectionRepository usersConnectionRepository,
			ConnectionSignUp connectionSignUp) {
		usersConnectionRepository.setConnectionSignUp(connectionSignUp);
	}

}
