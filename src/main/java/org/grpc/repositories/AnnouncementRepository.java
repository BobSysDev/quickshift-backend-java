package org.grpc.repositories;

import org.grpc.entities.Announcement;
import org.grpc.entities.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AnnouncementRepository extends CrudRepository<Announcement, Long> {
  public Announcement findById(long id);
  boolean existsById(Long aLong);
  public List<Announcement> findAll();
  public List<Announcement> findAllByDateTimeOfPostingAfter(LocalDateTime dateTime);
  long deleteByAuthor(Employee author);

  @Query("select a from Announcement a order by a.dateTimeOfPosting")
  List<Announcement> findByOrderByDateTimeOfPostingAsc();

  @Query("select a from Announcement a order by a.dateTimeOfPosting DESC")
  List<Announcement> findByOrderByDateTimeOfPostingDesc();
}