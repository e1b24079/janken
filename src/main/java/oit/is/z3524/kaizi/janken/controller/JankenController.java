package oit.is.z3524.kaizi.janken.controller;

import java.security.Principal;

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
  // GET：手を受け取り，相手の手を待つ情報をmatchinfoに登録してwait.htmlを表示する
  @GetMapping("/fight")
  public String fight(@RequestParam Integer id, @RequestParam String hand, Principal prin, ModelMap model) {
    String loginUser = prin.getName();
    User me = userMapper.selectByName(loginUser);

    MatchInfo matchInfo = new MatchInfo();
    matchInfo.setUser1(me.getId());
    matchInfo.setUser2(id);
    matchInfo.setUser1Hand(hand);
    matchInfo.setIsActive(true);
    matchInfoMapper.insertMatchInfo(matchInfo);

    model.addAttribute("loginUser", loginUser);
    return "wait.html";
  }
}
