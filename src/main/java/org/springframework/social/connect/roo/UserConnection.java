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

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findUserConnectionsByUserId",
		"findUserConnectionsByUserIdAndProviderId",
		"findUserConnectionsByUserIdAndProviderIdAndRank",
		"findUserConnectionsByProviderIdAndProviderUserId",
		"findUserConnectionByUserIdAndProviderIdAndProviderUserId",
		"findMaxRankByUserIdAndProviderId",
		"findUserIdsByProviderIdAndProviderUserIds" })
/**
 * @author Michael Lavelle
 */
public class UserConnection {

	private String accessToken;
	private String displayName;
	private Long expireTime;
	private String imageUrl;
	private String profileUrl;
	private String providerId;
	private String providerUserId;
	private int rank;
	private String refreshToken;
	private String secret;
	private String userId;
}
