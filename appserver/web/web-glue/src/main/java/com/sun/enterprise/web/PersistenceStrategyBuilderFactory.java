/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.web;


import com.sun.enterprise.web.session.PersistenceType;
import org.apache.catalina.Context;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LoggerInfo;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
  * @author Rajiv Mordani
  */

public class PersistenceStrategyBuilderFactory {


    private static final Logger _logger = com.sun.enterprise.web.WebContainer.logger;

    @LogMessageInfo(
            message = "PersistenceStrategyBuilderFactory>>createPersistenceStrategyBuilder: " +
                    "resolvedPersistenceType = {0}, resolvedPersistenceFrequency = {1} " +
                    "resolvedPersistenceScope = {2}",
            level = "FINEST")
    public static final String CREATE_PERSISTENCE_STRATEGY_BUILDER_INFO = "AS-WEB-00293";

    @LogMessageInfo(
            message = "Could not find PersistentStrategyBuilder for persistenceType {0}",
            level = "FINEST")
    public static final String PERSISTENT_STRATEGY_BUILDER_NOT_FOUND = "AS-WEB-00294";

    @LogMessageInfo(
            message = "PersistenceStrategyBuilderFactory>>createPersistenceStrategyBuilder: " +
                    "CandidateBuilderClassName = {0}",
            level = "FINEST")
    public static final String CREATE_PERSISTENCE_STRATEGY_BUILDER_CLASS_NAME = "AS-WEB-00295";

    @LogMessageInfo(
            message = "resolvedPersistenceType = {0}",
            level = "FINEST")
    public static final String ACCESS_LOG_DIRECTORY_SET = "AS-WEB-00296";

    ServiceLocator services;


    /**
     * Constructor.
     */
    public PersistenceStrategyBuilderFactory(
            ServerConfigLookup serverConfigLookup, ServiceLocator services) {

        this.services = services;
    }


    /**
     * creates the correct implementation of PersistenceStrategyBuilder
     * if an invalid combination is input; an error is logged
     * and MemoryStrategyBuilder is returned
     */    
    public PersistenceStrategyBuilder createPersistenceStrategyBuilder(
            String persistenceType, String frequency, String scope,
            Context ctx) {
        String resolvedPersistenceFrequency = null;
        String resolvedPersistenceScope = null;

        if (persistenceType.equalsIgnoreCase(PersistenceType.MEMORY.getType()) ||
                persistenceType.equalsIgnoreCase(PersistenceType.FILE.getType()) ||
                persistenceType.equalsIgnoreCase(PersistenceType.COOKIE.getType())) {
            // Deliberately leaving frequency & scope null
        } else {
            resolvedPersistenceFrequency = frequency;
            resolvedPersistenceScope = scope;
        }

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, CREATE_PERSISTENCE_STRATEGY_BUILDER_INFO,
                    new Object[] {persistenceType, resolvedPersistenceFrequency, resolvedPersistenceScope});
        }

        PersistenceStrategyBuilder builder = services.getService(PersistenceStrategyBuilder.class, persistenceType);
        if (builder == null) {
            builder = new MemoryStrategyBuilder();
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, PERSISTENT_STRATEGY_BUILDER_NOT_FOUND, persistenceType);
            }
        } else {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, CREATE_PERSISTENCE_STRATEGY_BUILDER_CLASS_NAME, builder.getClass());
                }

              builder.setPersistenceFrequency(frequency);
              builder.setPersistenceScope(scope);
              builder.setPassedInPersistenceType(persistenceType);
          }
        return builder;
    } 

    /**
     * returns the application id for the module
     *
     * @param ctx the context
     */    

    public String getApplicationId(Context ctx) {
        if (ctx instanceof WebModule) {
            return ((WebModule)ctx).getID();
        } else {
            return ctx.getName();
        }
    }    

}
