package com.atlassian.stash.plugins.jenkins;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.bitbucket.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryCloneLinksRequest;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.setting.RepositorySettingsValidator;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;
import com.atlassian.bitbucket.util.NamedLink;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

/**
 * Asynchronous post receive hook that react's whenever a persistant operation has been applied to the repository. It uses the configurd
 * jenkins URL, created a notification URL fpor this repo and calls the URL. This triggers the jenkins build for the repository the jenkins
 * build trigger is configured.
 * 
 * @author Stephan.Kleine
 * @since 04/2013
 */
@Component
public class JenkinsBuildTrigger implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {
    private static final Logger LOG = LoggerFactory.getLogger(JenkinsBuildTrigger.class);

    private static final String PROPERTY_URL = "url";

    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;
    private final RepositoryService repositoryService;

    @Autowired
    public JenkinsBuildTrigger(@ComponentImport final PluginSettingsFactory pluginSettingsFactory,
            @ComponentImport final TransactionTemplate transactionTemplate,
            @ComponentImport final RepositoryService repositoryService) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        this.repositoryService = repositoryService;
    }

    /**
     * Notifies Jenkins of a new commit assuming Jenkins is configured to connect to Stash via SSH.
     */
    public void postReceive(final RepositoryHookContext context, final Collection<RefChange> refChanges) {
        String url = context.getSettings().getString(PROPERTY_URL);
        if (StringUtils.isBlank(url)) {
            url = (String) transactionTemplate.execute(new TransactionCallback() {
                public Object doInTransaction() {
                    return pluginSettingsFactory.createGlobalSettings().get(ConfigResource.PLUGIN_KEY_URL);
                }
            });
        }

        try {
            final RepositoryCloneLinksRequest linksRequest = new RepositoryCloneLinksRequest.Builder()
                    .protocol("ssh")
                    .repository(context.getRepository())
                    .build();
            final Set<NamedLink> links = repositoryService.getCloneLinks(linksRequest);
            if (links.isEmpty()) {
                LOG.error("Unable to calculate clone link for repository [{}]", context.getRepository());
            } else {
                url = String.format("%s/git/notifyCommit?url=%s", url, URLEncoder.encode(links.iterator().next().getHref(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }

        LOG.debug("Notifying Jenkins via URL: [{}]", url);

        if (url != null) {
            try {
                new URL(url).openConnection().getInputStream().close();
            } catch (Exception e) {
                LOG.error("Unable to connect to Jenkins at [" + url + "]", e);
            }
        }
    }

    public void validate(final Settings settings, final SettingsValidationErrors errors, final Repository repository) {
        final String url = settings.getString(PROPERTY_URL, "");
        if (StringUtils.isNotEmpty(url)) {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                errors.addFieldError(PROPERTY_URL, "Please supply a valid URL");
            }
        }
    }
}