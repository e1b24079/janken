package oit.is.z3524.kaizi.janken.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MatchMapper {

  @Select("SELECT id, user1, user2, user1Hand, user2Hand, isActive FROM matches")
  ArrayList<Match> selectAllMatches();

  @Insert("INSERT INTO matches (user1, user2, user1Hand, user2Hand, isActive) VALUES (#{user1}, #{user2}, #{user1Hand}, #{user2Hand}, #{isActive})")
  void insertMatch(Match match);

  // isActiveがTRUEの試合結果をすべて取得する
  @Select("SELECT id, user1, user2, user1Hand, user2Hand, isActive FROM matches WHERE isActive = TRUE")
  ArrayList<Match> selectActiveMatches();

  // 表示が終わった試合結果のisActiveをFALSEにする
  @Update("UPDATE matches SET isActive = FALSE WHERE id = #{id}")
  void updateMatchInactive(int id);
}
