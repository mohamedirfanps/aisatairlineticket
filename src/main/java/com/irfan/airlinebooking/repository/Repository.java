package com.irfan.airlinebooking.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD contract (demonstrates <b>generics</b>).
 * Concrete JDBC implementations live in this package.
 */
public interface Repository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean deleteById(ID id);
}
