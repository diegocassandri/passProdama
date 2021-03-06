package com.prodama.config;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.prodama.security.UserDetail;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetail userDetailsService;

	@Value("${spring.ldap.base}")
	private String ldapBase;
	
	@Value("${spring.ldap.urls}")
	private String ldapUrl;
	
	@Value("${spring.ldap.username}")
	private String ldapUser;
	
	@Value("${spring.ldap.password}")
	private String ldapPass;
	
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		DefaultSpringSecurityContextSource ctx = new DefaultSpringSecurityContextSource(ldapUrl);
		ctx.setBase(ldapBase);
		ctx.afterPropertiesSet();
		ctx.isAnonymousReadOnly();
		ctx.setUserDn(ldapUser);
		ctx.setPassword(ldapPass);
		
		auth
		.ldapAuthentication()
		.userSearchFilter("(sAMAccountName={0})")
		.userDnPatterns("OU=Suporte,DC=prodama,DC=com,DC=br")	
		 .contextSource(ctx);
		
		auth.userDetailsService(userDetailsService)/*.passwordEncoder(passwordEncoder())*/;
		
		auth.inMemoryAuthentication()
		.withUser("prodama").password("asgardia").roles("ADMIN");
	}

	
	/*@Bean
	public DefaultSpringSecurityContextSource contextSource() {
		return  new DefaultSpringSecurityContextSource(Arrays.asList("ldap://192.168.7.48:389/"), "dc=PRODAMA,dc=COM,dc=BR");
	}*/
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/layout/**").antMatchers("/images/**").antMatchers("/stylesheets/**")
				.antMatchers("/resources/**").antMatchers("/js/**").antMatchers("/webjars/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic()
		.and()
		.authorizeRequests()
		.antMatchers("/conexoes/**").permitAll()
		.antMatchers(HttpMethod.POST, "/**").permitAll()
		.antMatchers(HttpMethod.PATCH, "/**").permitAll()
		.antMatchers(HttpMethod.DELETE, "/**").permitAll()
		.and()
		.csrf()
		.disable()
		.authorizeRequests()
		.anyRequest()
		.authenticated()
		.and()
		.formLogin()
		.loginPage("/login")
		.permitAll()
		.and()
		.logout()
	    .logoutRequestMatcher(new AntPathRequestMatcher("/logout")).and().exceptionHandling()
		.accessDeniedPage("/error/403").and().sessionManagement().invalidSessionUrl("/login");

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}


}
	
