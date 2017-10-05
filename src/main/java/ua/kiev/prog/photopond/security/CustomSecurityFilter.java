package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.kiev.prog.photopond.exception.AccessDeniedException;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class CustomSecurityFilter implements Filter {
    private static Logger log = LogManager.getLogger(CustomSecurityFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            log.trace("filter chain start");
            filterChain.doFilter(servletRequest, servletResponse);
            log.trace("filter chain finish");
        } catch (AccessDeniedException e) {
            log.trace("filter chain caught exception");
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendRedirect("/testingAccessDenied");
        }
    }

    @Override
    public void destroy() {

    }
}
