    package com.amazon.hjs.app;

import com.amazon.hjs.dao.CustomerMapper;
import com.amazon.hjs.domain.Customer;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.MatchResult;

/**
 *
 */
public class TheHarness {
    public static void main(String[] args) throws IOException {
        SqlSession session = buildHardCodedSession();

        try {
            CustomerMapper mapper = initCustomerMapper(session);

            System.out.printf("by id: %s\n", mapper.selectCustomer(1));
            System.out.printf("by email: %s\n", mapper.selectCustomerByEmail("trentonl@amazon.com"));

            System.out.printf("enter a new customer: email  name  phone: ");
            String[] values = parseLine();

            mapper.insertCustomer(new Customer(values[0], values[1], values[2]));
        } finally {
            session.commit();
            session.close();
        }
    }

    public static CustomerMapper initCustomerMapper(SqlSession session) {
        // scala was balking at the .class
        // :TODO: can't go futher. stuck.
        return session.getMapper(CustomerMapper.class);
    }

    public static SqlSession buildHardCodedSession() {
        MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
        ds.setUrl("jdbc:mysql://localhost:4475/temp");
        ds.setUser("trenton");
        ds.setPassword("iaH21VPyIb2jpH");

        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, ds);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(CustomerMapper.class);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        SqlSession session = sqlSessionFactory.openSession();
        return session;
    }

    private static String[] parseLine() {
        Scanner scanner = new Scanner(System.in);
        scanner.findInLine("(\\S+)\\s+(\\S+)\\s+(\\S+)");
        MatchResult result = scanner.match();
        String[] values = new String[3];
        for (int i = 1; i <= result.groupCount(); i++) {
            values[i - 1] = result.group(i);
        }
        scanner.close();

//        System.out.println(Arrays.asList(values));

        return values;
    }
}
