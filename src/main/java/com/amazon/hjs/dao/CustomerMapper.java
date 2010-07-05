package com.amazon.hjs.dao;

import com.amazon.hjs.domain.Customer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 *
 */
public interface CustomerMapper {
    @Select("select * from customer where id = #{id}")
    Customer selectCustomer(int id);

    @Select("select * from customer where email = #{email}")
    Customer selectCustomerByEmail(String email);

    @Insert("insert into customer (email, name, phone) values (#{email}, #{name}, #{phone})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCustomer(Customer customer);
}

/*

CREATE TABLE `customer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(128) NOT NULL,
  `name` varchar(128) DEFAULT NULL,
  `phone` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 |

insert customer values (null, "trentonl@gmail.com", "Trenton Lipscomb", "503-828-1821");
insert customer values (null, "trentonl@amazon.com", "Mr Lipscomb", "206-123-1234");

*/