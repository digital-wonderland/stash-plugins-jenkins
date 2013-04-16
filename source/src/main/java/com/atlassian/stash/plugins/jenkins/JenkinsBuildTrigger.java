package com.atlassian.stash.plugins.jenkins;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public class JenkinsBuildTrigger implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator
{
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

        url = url + "/git/notifyCommit?url=http://" +
                applicationProperties.getBaseUrl().getHost() + "/" + projectKey + "/" + repositoryName + ".git";

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