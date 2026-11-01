package com.vms.ws;

import com.vms.security.AuthPrincipal;
import com.vms.security.JwtService;
import com.vms.security.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtService jwtService;
    private final String frontendUrl;

    public WebSocketConfig(JwtService jwtService, @Value("${app.frontend-url}") String frontendUrl) {
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(frontendUrl);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new JwtChannelInterceptor(jwtService));
    }

    private static final class JwtChannelInterceptor implements ChannelInterceptor {
        private static final Pattern ORG_TOPIC = Pattern.compile("^/topic/org/(\\d+)/(active|notifications)$");
        private final JwtService jwtService;

        private JwtChannelInterceptor(JwtService jwtService) {
            this.jwtService = jwtService;
        }

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor == null) {
                return message;
            }
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authorization = accessor.getFirstNativeHeader("Authorization");
                if (authorization == null || !authorization.startsWith("Bearer ")) {
                    throw new IllegalArgumentException("WebSocket authentication required");
                }
                AuthPrincipal principal = jwtService.extractPrincipal(authorization.substring(7));
                accessor.setUser(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + principal.role().name()))));
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                if (!(accessor.getUser() instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken token)
                        || !(token.getPrincipal() instanceof AuthPrincipal principal)
                        || !isAllowed(accessor.getDestination(), principal)) {
                    throw new IllegalArgumentException("WebSocket subscription denied");
                }
            }
            return message;
        }

        private boolean isAllowed(String destination, AuthPrincipal principal) {
            if (destination == null) {
                return false;
            }
            Matcher matcher = ORG_TOPIC.matcher(destination);
            if (!matcher.matches() || !Long.toString(principal.orgId()).equals(matcher.group(1))) {
                return false;
            }
            return "active".equals(matcher.group(2)) || principal.role() == Role.ADMIN;
        }
    }
}
