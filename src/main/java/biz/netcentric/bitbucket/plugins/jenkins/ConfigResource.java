/*
 * Copyright [2016] [Thomas Hartmann]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.netcentric.bitbucket.plugins.jenkins;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The plugin's configuration resource servlet. Get's and persists the plugin configuration.
 * 
 * @author Stephan.Kleine
 * @since 04/2013
 */
@Component
@Path("/")
public class ConfigResource {

    final static String PLUGIN_KEY_URL = ConfigResource.class.getName() + ".url";

    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public ConfigResource(@ComponentImport final UserManager userManager,
            @ComponentImport final PluginSettingsFactory pluginSettingsFactory,
            @ComponentImport final TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context final HttpServletRequest request) {
        final UserKey key = userManager.getRemoteUserKey();
        if (key == null || !userManager.isSystemAdmin(key)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
                Config config = new Config();
                config.setUrl((String) settings.get(PLUGIN_KEY_URL));
                return config;
            }
        })).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final Config config, @Context final HttpServletRequest request) {
        final UserKey key = userManager.getRemoteUserKey();
        if (key == null || !userManager.isSystemAdmin(key)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                pluginSettings.put(PLUGIN_KEY_URL, config.getUrl());
                return null;
            }
        });
        return Response.noContent().build();
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Config {
        @XmlElement
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(final String url) {
            this.url = url;
        }
    }
}
