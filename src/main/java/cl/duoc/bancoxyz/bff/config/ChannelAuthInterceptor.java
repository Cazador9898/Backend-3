package cl.duoc.bancoxyz.bff.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ChannelAuthInterceptor implements HandlerInterceptor {

    @Value("${app.bff.web-token}")
    private String webToken;

    @Value("${app.bff.mobile-token}")
    private String mobileToken;

    @Value("${app.bff.atm-token}")
    private String atmToken;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String suppliedToken = request.getHeader("X-CHANNEL-TOKEN");
        String expectedToken = expectedToken(path);

        if (expectedToken == null) {
            return true;
        }

        if (!expectedToken.equals(suppliedToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Token de canal inválido o ausente\"}");
            return false;
        }

        if (path.startsWith("/api/bff/atm") &&
                (request.getHeader("X-ATM-ID") == null || request.getHeader("X-ATM-ID").isBlank())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"El canal ATM requiere la cabecera X-ATM-ID\"}");
            return false;
        }

        return true;
    }

    private String expectedToken(String path) {
        if (path.startsWith("/api/bff/web")) {
            return webToken;
        }
        if (path.startsWith("/api/bff/mobile")) {
            return mobileToken;
        }
        if (path.startsWith("/api/bff/atm")) {
            return atmToken;
        }
        return null;
    }
}
