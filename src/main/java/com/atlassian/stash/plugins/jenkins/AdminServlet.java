package com.atlassian.stash.plugins.jenkins;

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
 * Created with IntelliJ IDEA.
 * User: stephan
 * Date: 16.4.2013
 * Time: 18:40
 * To change this template use File | Settings | File Templates.
 */
@Component
public class AdminServlet extends HttpServlet {

    private final UserManager userManager;
    private final LoginUriProvider loginUriProvider;
    private final TemplateRenderer renderer;

    @Autowired
    public AdminServlet(@ComponentImport final UserManager userManager, @ComponentImport final LoginUriProvider loginUriProvider, @ComponentImport final TemplateRenderer renderer) {
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

        response.setContentType("text/html;charset=utf-8");
        renderer.render("admin.vm", response.getWriter());
    }


    private void redirectToLogin(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(final HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

}
