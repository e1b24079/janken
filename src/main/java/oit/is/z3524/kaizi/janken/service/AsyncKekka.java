package oit.is.z3524.kaizi.janken.service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
  MatchMapper matchMapper;

  /**
   * 成立した試合をmatchesテーブルに登録する
   *
   * @param match 登録する試合
   */
  @Transactional
  public void syncInsertMatch(Match match) {
    matchMapper.insertMatch(match);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
  }

  /**
   * dbUpdatedがtrueのときのみ，DBからログインユーザのisActiveがTRUEの試合結果を取得してブラウザに送付する
   *
   * @param emitter
   * @param userId ログインユーザのID
   */
  @Async
  public void asyncShowMatchResult(SseEmitter emitter, int userId) {
    // ブラウザとの接続が終了(タイムアウト等)したらループを抜けるためのフラグ
    AtomicBoolean finished = new AtomicBoolean(false);
    emitter.onCompletion(() -> finished.set(true));

    dbUpdated = true;
    try {
      // 接続直後にコメントを送ってレスポンスを確定させる(ブラウザ側には表示されない)
      emitter.send(SseEmitter.event().comment("connected"));
      int count = 0;
      while (!finished.get()) {
        // 10sごとにコメントを送り，ブラウザが閉じられていれば例外でループを抜ける
        count++;
        if (count % 20 == 0) {
          emitter.send(SseEmitter.event().comment("keep-alive"));
        }
        // DBが更新されていなければ0.5s休み
        if (false == dbUpdated) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }
        // 自分が含まれるisActiveがTRUEの試合結果を取得する．まだなければ0.5s休み
        Match match = matchMapper.selectActiveMatchByUser(userId);
        if (match == null) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }
        // 試合結果をsendし，1s休み，dbUpdatedをfalseにする
        emitter.send(match);
        TimeUnit.MILLISECONDS.sleep(1000);
        dbUpdated = false;
        // 表示した試合結果のisActiveをFALSEにする
        matchMapper.updateMatchInactive(match.getId());
        break;
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
