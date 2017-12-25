package dsoap.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import dsoap.tools.ConfigurationSettings;

public class TestFilter implements Filter {

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String path = ((HttpServletRequest) request).getRequestURL().toString();
        String query = ((HttpServletRequest) request).getQueryString();
        String method = ((HttpServletRequest) request).getMethod();
        if (path.contains(".jsp") || path.contains(".action") || path.contains("/service")) {
            System.out.println("\n\n#####################################################################################\n" + path + "   " + query + "   " + method);
        }
        ConfigurationSettings.MACHINE = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        try {
            filterChain.doFilter(request, response);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (ServletException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

}
