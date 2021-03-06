package za.co.entelect.tools.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Arrays;

/**
* Generated by Entelect Tools(Spring JPA Plugin) on Thu Dec 14 14:47:54 CAT 2017
* User ronald22
*/
public abstract class AbstractService<T extends Serializable, ID extends Serializable, Repository extends JpaRepository<T, ID>> {

    protected Repository repository;

    public AbstractService(Repository repository) {
        this.repository = repository;
    }

    public Repository getRepository() {
        return this.repository;
    }

    public <S extends T> S save(S entity) {
        return repository.save(entity);
    }

    public <S extends T> Iterable<S> save(S... entities) {
        return repository.save(Arrays.asList(entities));
    }

    public <S extends T> Iterable<S> save(Iterable<S> entities) {
        return repository.save(entities);
    }

    public T findOne(ID id) {
        return repository.findOne(id);
    }

    public boolean exists(ID id) {
        return repository.exists(id);
    }

    public Iterable<T> findAll() {
        return repository.findAll();
    }

    public Iterable<T> findAll(Iterable<ID> ids) {
        return repository.findAll(ids);
    }

    public long count() {
        return repository.count();
    }

    public void deleteById(ID id) {
        repository.delete(id);
    }

    public void delete(T... entities) {
        repository.delete(Arrays.asList(entities));
    }

    public void delete(T entity) {
        repository.delete(entity);
    }

    public void delete(Iterable<? extends T> entities) {
        repository.delete(entities);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public Iterable<T> findAll(Sort sort) {
        return repository.findAll(sort);
    }

    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
