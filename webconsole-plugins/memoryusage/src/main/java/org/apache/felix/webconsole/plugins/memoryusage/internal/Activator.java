/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.webconsole.plugins.memoryusage.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class Activator implements BundleActivator
{

    private MemoryUsageSupport support;

    public void start(BundleContext bundleContext)
    {

        support = new MemoryUsageSupport(bundleContext);

        // install thread handler shell command
        try
        {
            register(bundleContext, new String[]
                { "org.apache.felix.shell.Command" }, new MemoryUsageCommand(support), null);
        }
        catch (Throwable t)
        {
            // shell service might not be available, don't care
        }

        // install Web Console plugin
        try
        {
            MemoryUsagePanel tdp = new MemoryUsagePanel(support);
            tdp.activate(bundleContext);

            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            properties.put("felix.webconsole.label", tdp.getLabel());

            register(bundleContext, new String[]
                { "javax.servlet.Servlet", "org.apache.felix.webconsole.ConfigurationPrinter" }, tdp, properties);
        }
        catch (Throwable t)
        {
            // web console might not be available, don't care
        }

        // register for configuration
        try
        {
            MemoryUsageConfigurator tdp = new MemoryUsageConfigurator(support);
            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            properties.put(Constants.SERVICE_PID, MemoryUsageConfigurator.NAME);
            register(bundleContext, new String[]
                { "org.osgi.service.cm.ManagedService" }, tdp, properties);
        }
        catch (Throwable t)
        {
            // Configuration Admin and Metatype Service API might not be available, don't care
        }
    }

    public void stop(BundleContext bundleContext)
    {
        if (support != null)
        {
            support.dispose();
            support = null;
        }
    }

    private void register(BundleContext context, String[] serviceNames, Object service,
        Dictionary<String, Object> properties)
    {

        // ensure properties
        if (properties == null)
        {
            properties = new Hashtable<String, Object>();
        }

        // default settings
        properties.put(Constants.SERVICE_DESCRIPTION, "Memory Usage (" + serviceNames[0] + ")");
        properties.put(Constants.SERVICE_VENDOR, "Apache Software Foundation");

        context.registerService(serviceNames, service, properties);
    }

}
