/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.user.internal;

import org.eclipse.kapua.commons.jpa.AbstractNamedEntityCacheFactory;

/**
 * Cache factory for the {@link UserServiceImpl}
 */
public class UserCacheFactory extends AbstractNamedEntityCacheFactory {

    private UserCacheFactory() {
        super("UserId", "UserName");
    }

    protected static UserCacheFactory getInstance() {
        return new UserCacheFactory();
    }

}
