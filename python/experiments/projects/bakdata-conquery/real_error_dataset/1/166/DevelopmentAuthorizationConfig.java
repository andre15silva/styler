package com.bakdata.conquery.models.auth.develop;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.auth.AuthorizationConfig;
import com.bakdata.conquery.models.auth.ProtoUser;
import com.bakdata.conquery.models.auth.permissions.AdminPermission;
import com.bakdata.conquery.models.auth.permissions.DatasetPermission;
import com.bakdata.conquery.models.auth.permissions.SuperPermission;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

@CPSType(base = AuthorizationConfig.class, id = "DEVELOPMENT")
@Getter
public class DevelopmentAuthorizationConfig implements AuthorizationConfig{
	
	@NotEmpty
	private List<ProtoUser> initialUsers = List.of(
		ProtoUser.builder()
			.name("SUPERUSER@SUPERUSER")
			.label("SUPERUSER")
			.permissions(Set.of("*"))
			.build()
		);

	@NotNull
	private List<String> overviewScope = List.of(
		DatasetPermission.DOMAIN,
		AdminPermission.DOMAIN,
		SuperPermission.DOMAIN);

}
