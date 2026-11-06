package oit.is.z3524.kaizi.janken.controller;

import java.security.Principal;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import oit.is.z3524.kaizi.janken.model.Entry;
import oit.is.z3524.kaizi.janken.model.UserMapper;
import oit.is.z3524.kaizi.janken.model.MatchMapper;
import oit.is.z3524.kaizi.janken.model.User;
import oit.is.z3524.kaizi.janken.model.Match;
import oit.is.z3524.kaizi.janken.model.MatchInfo;
import oit.is.z3524.kaizi.janken.model.MatchInfoMapper;

@Controller
public class JankenController {

  @Autowired
  private Entry entry; // DIによりSpringが生成した1つのEntryが注入される
  @Autowired
  private UserMapper userMapper;
  @Autowired
  private MatchMapper matchMapper;
  @Autowired
  private MatchInfoMapper matchInfoMapper;

  // ログインユーザをエントリーに追加し，画面表示用の値をModelに入れる
  private void setEntry(Principal prin, ModelMap model) {
    String loginUser = prin.getName();
    this.entry.addUser(loginUser);
    model.addAttribute("loginUser", loginUser);
    model.addAttribute("entry", this.entry);
    model.addAttribute("users", userMapper.selectAllUsers());
    model.addAttribute("matches", matchMapper.selectAllMatches());
    model.addAttribute("activeMatches", matchInfoMapper.selectActiveMatchInfo());
  }

  // GET：じゃんけん画面を表示する
  @GetMapping("/janken")
  public String janken(Principal prin, ModelMap model) {
    setEntry(prin, model);
    return "janken.html";
  }

  // POST：入力された名前を受け取って表示する
  @PostMapping("/janken")
  public String jankenName(@RequestParam String name, Principal prin, ModelMap model) {
    setEntry(prin, model);
    model.addAttribute("name", name);
    return "janken.html";
  }

  // GET：手を受け取ってじゃんけんする
  @GetMapping("/janken/game")
  public String jankenGame(@RequestParam String hand, Principal prin, ModelMap model) {
    setEntry(prin, model);

    String[] hands = { "Gu", "Choki", "Pa" };
    String cpuHand = hands[(int) (Math.random() * 3)];

    String result;
    if (hand.equals(cpuHand)) {
      result = "あいこ";
    } else if ((hand.equals("Gu") && cpuHand.equals("Choki"))
        || (hand.equals("Choki") && cpuHand.equals("Pa"))
        || (hand.equals("Pa") && cpuHand.equals("Gu"))) {
      result = "あなたの勝ち！";
    } else {
      result = "あなたの負け…";
    }

    model.addAttribute("myHand", hand);
    model.addAttribute("cpuHand", cpuHand);
    model.addAttribute("result", result);
    return "janken.html";
  }

  @GetMapping("/match")
  public String match(@RequestParam Integer id, Principal prin, ModelMap model) {
    model.addAttribute("loginUser", prin.getName());
    model.addAttribute("opponent", userMapper.selectById(id));
    return "match.html";
  }
  // GET：手を受け取り，相手が先に手を出していれば試合を成立させ，
  // まだであれば相手の手を待つ情報をmatchinfoに登録してwait.htmlを表示する
  @GetMapping("/fight")
  public String fight(@RequestParam Integer id, @RequestParam String hand, Principal prin, ModelMap model) {
    String loginUser = prin.getName();
    User me = userMapper.selectByName(loginUser);

    // 相手(id)が自分を指名して手を待っているmatchinfoを探す
    ArrayList<MatchInfo> waiting = matchInfoMapper.selectActiveMatchInfoByUsers(id, me.getId());
    if (!waiting.isEmpty()) {
      // 試合成立：先に手を出した相手をuser1，後から手を出した自分をuser2としてmatchesに登録する
      MatchInfo info = waiting.get(0);
      Match match = new Match();
      match.setUser1(info.getUser1());
      match.setUser2(me.getId());
      match.setUser1Hand(info.getUser1Hand());
      match.setUser2Hand(hand);
      match.setIsActive(true);
      matchMapper.insertMatch(match);
      // 成立したmatchinfoはFALSEにする
      matchInfoMapper.updateMatchInfoInactive(info.getId());
    } else {
      // 相手がまだ手を出していないので，相手の手を待つ情報を登録する
      MatchInfo matchInfo = new MatchInfo();
      matchInfo.setUser1(me.getId());
      matchInfo.setUser2(id);
      matchInfo.setUser1Hand(hand);
      matchInfo.setIsActive(true);
      matchInfoMapper.insertMatchInfo(matchInfo);
    }

    model.addAttribute("loginUser", loginUser);
    return "wait.html";
  }
}
