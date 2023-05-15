package com.pcandroiddev.noteworthybackend.repository;

import com.pcandroiddev.noteworthybackend.model.note.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface NoteRepository extends JpaRepository<Note, Integer> {

    @Query("from Note n where n.user.id = :userId")
    List<Note> findAllByUserId(@Param("userId") Integer userId);

    @Query("from Note n where n.user.id = :userId order by case n.priority when 'LOW' then 1 when 'MEDIUM' then 2 when 'HIGH' then 3 end")
    List<Note> findAllByUserIdOrderByPriorityAsc(@Param("userId") Integer userId);

    @Query("from Note n order by case n.priority when 'LOW' then 1 when 'MEDIUM' then 2 when 'HIGH' then 3 end")
    List<Note> findAllOrderByPriorityAsc();

    @Query("from Note n where n.user.id = :userId order by case n.priority when 'HIGH' then 1 when 'MEDIUM' then 2 when 'LOW' then 3 end")
    List<Note> findAllByUserIdOrderByPriorityDesc(@Param("userId") Integer userId);

    @Query("from Note n order by case n.priority when 'HIGH' then 1 when 'MEDIUM' then 2 when 'LOW' then 3 end")
    List<Note> findAllOrderByPriorityDesc();

    @Query("SELECT n FROM Note n JOIN n.user u WHERE u.id = :userId AND (n.title LIKE %:searchText% OR n.description LIKE %:searchText%)")
    List<Note> searchByTitleOrDescriptionAndUserId(@Param("searchText") String searchText, @Param("userId") Integer userId);

    @Query("SELECT n FROM Note n WHERE n.title LIKE %:searchText% OR n.description LIKE %:searchText%")
    List<Note> searchByTitleOrDescription(@Param("searchText") String searchText);


}
