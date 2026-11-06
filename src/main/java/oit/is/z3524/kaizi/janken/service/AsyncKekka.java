package oit.is.z3524.kaizi.janken.service;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import oit.is.z3524.kaizi.janken.model.Match;
import oit.is.z3524.kaizi.janken.model.MatchMapper;

@Service
public class AsyncKekka {
  boolean dbUpdated = false;

  private final Logger logger = LoggerFactory.getLogger(AsyncKekka.class);

  @Autowired
  MatchMapper mMapper;

  /**
   * 成立した試合をmatchesテーブルに登録する
   *
   * @param match 登録する試合
   */
  @Transactional
  public void syncInsertMatch(Match match) {
    // 登録
    mMapper.insertMatch(match);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
  }

  public ArrayList<Match> syncShowActiveMatches() {
    return mMapper.selectActiveMatches();
  }

  /**
   * dbUpdatedがtrueのときのみブラウザにDBからisActiveがTRUEの試合結果を取得して送付する
   *
   * @param emitter
   */
  @Async
  public void asyncShowMatchResult(SseEmitter emitter) {
    dbUpdated = true;
    try {
      while (true) {// 無限ループ
        // DBが更新されていなければ0.5s休み
        if (false == dbUpdated) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }
        // DBが更新されていれば試合結果を取得してsendし，1s休み，dbUpdatedをfalseにする
        ArrayList<Match> activeMatches = this.syncShowActiveMatches();
        emitter.send(activeMatches);
        TimeUnit.MILLISECONDS.sleep(1000);
        dbUpdated = false;
        // 表示した試合結果のisActiveをFALSEにする
        for (Match match : activeMatches) {
          mMapper.updateMatchInactive(match.getId());
        }
      }
    } catch (Exception e) {
      // 例外の名前とメッセージだけ表示する
      logger.warn("Exception:" + e.getClass().getName() + ":" + e.getMessage());
    } finally {
      emitter.complete();
    }
    System.out.println("asyncShowMatchResult complete");
  }

}
