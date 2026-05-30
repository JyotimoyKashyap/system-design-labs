package com.example.posts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.posts.entity.Post;


@Repository
public interface PostRepository extends JpaRepository<Post, Long>{

}
