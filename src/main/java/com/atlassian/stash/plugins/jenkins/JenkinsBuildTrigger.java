package com.atlassian.stash.plugins.jenkins;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.stash.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.atlassian.stash.ssh.api.SshCloneUrlResolver;
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

    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;
    private final SshCloneUrlResolver sshCloneUrlResolver;

    public JenkinsBuildTrigger(final PluginSettingsFactory pluginSettingsFactory,
                               final TransactionTemplate transactionTemplate,
                               final SshCloneUrlResolver sshCloneUrlResolver) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        this.sshCloneUrlResolver = sshCloneUrlResolver;
    }

    /**
     * Notifies Jenkins of a new commit assuming Jenkins is configured to connect to Stash via SSH.
     */
    @Override
    public void postReceive(final RepositoryHookContext context, final Collection<RefChange> refChanges)
    {
        String url = context.getSettings().getString(PROPERTY_URL);
        if(StringUtils.isBlank(url)) {
            url = (String) transactionTemplate.execute(new TransactionCallback() {
                public Object doInTransaction() {
                    return pluginSettingsFactory.createGlobalSettings().get(ConfigResource.PLUGIN_KEY_URL);
                }
            });
        }

        try {
            url = String.format("%s/git/notifyCommit?url=%s", url, URLEncoder.encode(sshCloneUrlResolver.getCloneUrl(context.getRepository()), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }

        LOG.debug("Notifying Jenkins via URL: [{}]", url);

        if (url != null) {
            try {
                new URL(url).openConnection().getInputStream().close();
            }
            catch (Exception e) {
                LOG.error("Unable to connect to Jenkins at [" + url + "]", e);
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