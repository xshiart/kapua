/*******************************************************************************
 * Copyright (c) 2016, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.authorization.role.shiro;

import org.eclipse.kapua.commons.model.query.AbstractKapuaQuery;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.authorization.role.RolePermission;
import org.eclipse.kapua.service.authorization.role.RolePermissionQuery;

/**
 * {@link RolePermission} query implementation.
 *
 * @since 1.0.0
 */
public class RolePermissionQueryImpl extends AbstractKapuaQuery<RolePermission> implements RolePermissionQuery {

    /**
     * Constructor
     */
    public RolePermissionQueryImpl() {
        super();
    }

    /**
     * Constructor
     *
     * @param scopeId
     */
    public RolePermissionQueryImpl(KapuaId scopeId) {
        this();
        setScopeId(scopeId);
    }
}
