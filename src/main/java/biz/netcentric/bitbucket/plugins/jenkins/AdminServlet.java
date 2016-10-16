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

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Renders the admin ui widget if the current user is permitted to configure the plugin.
 *
 * @author Stephan.Kleine
 * @since 04/2013
 */
@Component
public class AdminServlet extends HttpServlet {

    private static final String CONTENT_TYPE = "text/html;charset=utf-8";

    private static final String ADMIN_VM_TEMPLATE = "admin.vm";

    private final UserManager userManager;

    private final LoginUriProvider loginUriProvider;

    private final TemplateRenderer renderer;

    @Autowired
    public AdminServlet(@ComponentImport final UserManager userManager, @ComponentImport final LoginUriProvider loginUriProvider,
            @ComponentImport final TemplateRenderer renderer) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        final UserKey key = userManager.getRemoteUserKey();
        if (key == null || !userManager.isSystemAdmin(key)) {
            redirectToLogin(request, response);
            return;
        }

        response.setContentType(CONTENT_TYPE);
        renderer.render(ADMIN_VM_TEMPLATE, response.getWriter());
    }

    private void redirectToLogin(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(final HttpServletRequest request) {
        final StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

}
