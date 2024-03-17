package com.msm.heartnal.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@Configuration
@PropertySource("classpath:application.yml") // 이 클래스에서 참조할 properties 파일의 위치
@RequiredArgsConstructor
@EnableTransactionManagement // Spring 에서 제공하는 Annotation 기반 트랜잭션 활성화, 사용하지는 않는 중
public class DBConfig {

	private final ApplicationContext applicationContext;

	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	public HikariConfig hikariConfig() {
		// Connection Pool Library 중 하나인 히카리 CP
		return new HikariConfig();
	}

	@Bean
	public DataSource dataSource() {
		return new HikariDataSource(hikariConfig());
	}

	/**
	 *	Mybatis 와 Spring 의 연동모듈로 사용됨
	 *	Mybatis XML Mapper, 설정 파일 위치 등을 지정하며,
	 *	SqlSessionFactoryBean 자체가 아닌, getObject 메서드가 리턴하는 SqlSessionFactory 를 생성함
	 */
	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource());
		factoryBean.setMapperLocations(applicationContext.getResources("classpath:/mapper/**/*Mapper.xml"));

		factoryBean.setTypeAliasesPackage("com.business.product");
		factoryBean.setConfiguration(mybatisConfig());
		return factoryBean.getObject();
	}

	/**
	 * 	SqlSessionTemplate
	 * 	SqlSession 객체 생성
	 * 	SqlSessionTemplate 는 SqlSession 을 구현하고, 코드에서 SqlSession 을 대체하는 역할을 함
	 * 	여러개의 DAO 나 Mapper 에서 공유 가능
	 * 	필요한 시점에서 세션을 닫고, 커밋 또는 롤백하는 것을 포함한 세션의 생명주기를 관리
	 */
	@Bean
	public SqlSessionTemplate sqlSession() throws Exception {
		return new SqlSessionTemplate(sqlSessionFactory());
	}

	/**
	 * 	application.yml 에서 mybatis.configuration 으로 시작하는 모든 설정을 Bean 등록
	 */
	@Bean
	@ConfigurationProperties(prefix = "mybatis.configuration")
	public org.apache.ibatis.session.Configuration mybatisConfig() {
		return new org.apache.ibatis.session.Configuration();
	}

	/**
	 * 	트랜잭션 처리
	 */
	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}
}
