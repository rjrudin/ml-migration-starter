package com.example.entity.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Properties;

/**
 * Problems
 * - Have to use a tool like Eclipse to generate classes
 * - Generated classes can have dozens of errors in them that require manual fixing
 * - Many-to-one relationships are fetch-eager by default
 * - Infinite recursion problems with JSON due to bidirectional relationships, requires manual fixing
 */
public class Main {

	public static void main(String[] args) throws Exception {
		DriverManagerDataSource ds = new DriverManagerDataSource(
			"jdbc:mysql://localhost:3306/sakila?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
			"root", "password");
		LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
		fb.setDataSource(ds);
		fb.setPersistenceUnitName("jpaData");
		fb.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		Properties props = new Properties();
		props.setProperty("hibernate.show_sql", "true");
		props.setProperty("hibernate.format_sql", "true");
		props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		fb.setJpaProperties(props);
		fb.setPackagesToScan("com.example.entity.model");
		fb.afterPropertiesSet();
		EntityManagerFactory entityManagerFactory = fb.getObject();

		EntityManager em = entityManagerFactory.createEntityManager();
//		for (Object o : em.createQuery("select a from Actor a").setMaxResults(3).getResultList()) {
//			Actor a = (Actor)o;
//			System.out.println(a.getFilmActors());
//		}

		ObjectMapper objectMapper = new ObjectMapper();

		for (Object o : em.createQuery("select c from Country c LEFT JOIN FETCH c.cities").setMaxResults(3).getResultList()) {
			Country a = (Country) o;
			em.detach(a);
			String json = objectMapper.writeValueAsString(a);
			System.out.println(json);
		}
	}
}
