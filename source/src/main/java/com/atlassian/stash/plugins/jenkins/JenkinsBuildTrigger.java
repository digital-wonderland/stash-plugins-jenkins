package com.atlassian.stash.plugins.jenkins;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.stash.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;

public class JenkinsBuildTrigger implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator
{
    private static final Logger LOG = LoggerFactory.getLogger(JenkinsBuildTrigger.class);

    private static final String PROPERTY_URL = "url";

    private final ApplicationPropertiesService applicationProperties;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;

    public JenkinsBuildTrigger(final ApplicationPropertiesService applicationProperties, final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate) {
        this.applicationProperties = applicationProperties;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Connects to a configured URL to notify of all changes.
     */
    @Override
    public void postReceive(final RepositoryHookContext context, final Collection<RefChange> refChanges)
    {
        final String projectKey = context.getRepository().getProject().getKey();
        final String repositoryName = context.getRepository().getName();

        String url = context.getSettings().getString(PROPERTY_URL);
        if(StringUtils.isBlank(url)) {
            url = (String) transactionTemplate.execute(new TransactionCallback() {
                public Object doInTransaction() {
                    return pluginSettingsFactory.createGlobalSettings().get(ConfigResource.PLUGIN_KEY_URL);
                }
            });
        }

        String stashUrl = "http://" + applicationProperties.getBaseUrl().getHost() + "/" + projectKey.toLowerCase() + "/" + repositoryName + ".git";

        try {
            url = url + "/git/notifyCommit?url=" + URLEncoder.encode(stashUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }

        LOG.debug("Notifying Jenkins via URL: [{}]", url);

        if (url != null) {
            try {
                new URL(url).openConnection().getInputStream().close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository)
    {
        String url = settings.getString(PROPERTY_URL, "");
        if (StringUtils.isNotEmpty(url)) {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                errors.addFieldError(PROPERTY_URL, "Please supply a valid URL");
            }
        }
    }
}