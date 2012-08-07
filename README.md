spring-social-roo-connectionrepository
======================================

A UsersConnectionRepository/ConnectionRepository implementation using Roo managed entities for persistence
as an alternative to the JDBC versions in spring-social-core.  You can see this implementation in action in the 
SocialSignin Roo Showcase -> https://github.com/socialsignin/socialsignin-roo-showcase

To use this implementation in your application:

*1 Add our snapshot repository and dependency to your project, eg. in pom.xml:

```

<repository>
    <id>opensourceagility-snapshots</id>
  <url>http://repo.opensourceagility.com/snapshots</url>
</repository>
...
 <dependency>
        <groupId>org.springframework.social</groupId>
        <artifactId>spring-social-roo-connectionrepository</artifactId>
	    <version>1.0.2-SNAPSHOT</version>
</dependency>

```

*2 Component scan for the RooTemplate implementation, and the UserConnection roo-managed entity:

```
    <context:component-scan base-package="org.springframework.social.connect.roo">
        <context:exclude-filter expression=".*_Roo_.*" type="regex"/>
    </context:component-scan>
```

*3 Replace JdbcUsersConnectionRepository/JdbcConnectionRepository bean configurations with 
RooUsersConnectionRepository/RooConnectionRepository implementations.  

The construction of these beans should only need to change in respect to the first argument of the
RooUsersConnectionRepository constructor, which must be the component-scanned RooTemplate (registered under the bean name
"rooUserConnectionTemplate") instead of a DataSource bean.

Your datasource bean will be autowired into the RooTemplate implementation instead by the component-scan.

Your configuration should now resemble the following:

```

@Autowired 
private RooTemplate rooTemplate:
...


    @Bean
    @Scope(value="singleton", proxyMode=ScopedProxyMode.INTERFACES) 
	public UsersConnectionRepository usersConnectionRepository() {
		return new RooUsersConnectionRepository(rooTemplate, connectionFactoryLocator(), Encryptors.noOpText());
	}

	@Bean
	@Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)	
	public ConnectionRepository connectionRepository() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			throw new IllegalStateException("Unable to get a ConnectionRepository: no user signed in");
		}
		return usersConnectionRepository().createConnectionRepository(authentication.getName());
}

```

or in xml...

```

    <bean id="usersConnectionRepository"
    	class="org.springframework.social.connect.roo.RooUsersConnectionRepository">
		<constructor-arg ref="rooUserConnectionTemplate" />
		<constructor-arg ref="connectionFactoryLocator" />
		<constructor-arg ref="textEncryptor" />
    </bean>

	<bean
		class="org.springframework.social.connect.roo.RooConnectionRepository"
		id="connectionRepository" factory-method="createConnectionRepository"
		factory-bean="usersConnectionRepository" scope="request">
		<constructor-arg value="#{request.userPrincipal.name}" />
		<aop:scoped-proxy proxy-target-class="true" />
	</bean>
```

*4  Add <class>org.springframework.social.connect.roo.UserConnection</class> to your persistence.xml, eg:

```
        <persistence-unit name="persistenceUnit" transaction-type="RESOURCE_LOCAL">
            <provider>org.hibernate.ejb.HibernatePersistence</provider>
            <class>org.springframework.social.connect.roo.UserConnection</class>
        <properties>
            ....

```

With this configuration, there is no need to create the user connection table, as Roo will take care of
ORM for the UserConnection entity, which you can now use amongst any other persistent classes in your application.

This implementation contains an associated Test class for the repositories which subclasses AbstractUsersConnectionRepositoryTest
from https://github.com/michaellavelle/spring-social-core-extension.   This applies the same suite of tests to the
repositories as for the JDBC version from spring-core



