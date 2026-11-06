package oit.is.z3524.kaizi.janken.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MatchInfoMapper {

  // isActiveがTRUEの試合(相手の手を待っている試合)をすべて取得する
  @Select("SELECT id, user1, user2, user1Hand, isActive FROM matchinfo WHERE isActive = TRUE")
  ArrayList<MatchInfo> selectActiveMatchInfo();

  // 相手(user1)が自分(user2)を指名して手を待っている試合を取得する
  @Select("SELECT id, user1, user2, user1Hand, isActive FROM matchinfo WHERE isActive = TRUE AND user1 = #{user1} AND user2 = #{user2}")
  ArrayList<MatchInfo> selectActiveMatchInfoByUsers(@Param("user1") int user1, @Param("user2") int user2);

  // 試合が成立したmatchinfoのisActiveをFALSEにする
  @Update("UPDATE matchinfo SET isActive = FALSE WHERE id = #{id}")
  void updateMatchInfoInactive(int id);

  // 相手の手を待っている情報を登録する
  @Insert("INSERT INTO matchinfo (user1, user2, user1Hand, isActive) VALUES (#{user1}, #{user2}, #{user1Hand}, #{isActive})")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insertMatchInfo(MatchInfo matchInfo);
}
