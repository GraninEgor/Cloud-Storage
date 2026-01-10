package org.example.cloudstorage.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.core.security.service.CustomUserService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserService customUserService;

    @Override
    protected void doFilterInternal(@NonNull  HttpServletRequest request,@NonNull  HttpServletResponse response,@NonNull  FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if(token!=null && jwtService.validateJwtToken(token)){
            setCustomUserDetailsToContextHolder(token);
        }

        filterChain.doFilter(request, response);
    }

    private void setCustomUserDetailsToContextHolder(String token){
        String username = jwtService.getUsernameFromToken(token);
        CustomUserDetails customUserDetails = customUserService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getTokenFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }

        return bearerToken.substring(7);
    }

}
