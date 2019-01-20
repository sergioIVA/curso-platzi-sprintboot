package com.platzi.profesoresplatzi.security;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.filter.GenericFilterBean;

import com.platzi.profesoresplatzi.configuration.DataBaseConfiguration;
import com.platzi.profesoresplatzi.model.Rol;

import io.jsonwebtoken.Jwts;



@Controller
public class JwtFilter extends GenericFilterBean {
	
			
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain filterChain)
            throws IOException, ServletException {
    	
    	
    	// Obtenemos el token que viene en el encabezado de la peticion
        String token = ((HttpServletRequest) request).getHeader("Authorization");
        String[] list_roles = null;
        
        if (token != null) {
            String username = Jwts.parser()
                    .setSigningKey(JwtUtil.KEYSECRET) //la clave secreta esta declara en JwtUtil
                    .parseClaimsJws(token) //este metodo es el que valida
                    .getBody()
                    .getSubject();        //extraigo el nombre de usuario del token
        
	        /*De manera "sucia" vamos a obtener los roles del usuario*/
	        DataBaseConfiguration db = new DataBaseConfiguration();       
	        JdbcTemplate jdbcTemplate = new JdbcTemplate(db.dataSource());
	
	        Collection roles = jdbcTemplate.query(
	                "select user_role_id,username,role from user_roles where username='" + username + "'", new RowMapper() {
						@Override
						public Object mapRow(ResultSet rs, int arg1) throws SQLException {
							// TODO Auto-generated method stub
							Rol rol = new Rol();
	                        rol.setId(rs.getLong("user_role_id"));
	                        rol.setUsername(rs.getString("username"));
	                        rol.setRol(rs.getString("role"));
	                        return rol;
						}
	                });
	
	        list_roles = new String[roles.size()];
	        int i = 0;
	        for (Object rol : roles) {
	        	list_roles[i] = rol.toString();
	        	i++;
	        }
        }
        Authentication authentication = JwtUtil.getAuthentication((HttpServletRequest)request, list_roles);
        				
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        filterChain.doFilter(request, response);
                 
    }
}